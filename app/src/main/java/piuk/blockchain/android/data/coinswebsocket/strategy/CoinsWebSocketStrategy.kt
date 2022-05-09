package piuk.blockchain.android.data.coinswebsocket.strategy

import com.blockchain.core.chains.bitcoincash.BchDataManager
import com.blockchain.core.chains.erc20.Erc20DataManager
import com.blockchain.core.chains.erc20.isErc20
import com.blockchain.featureflag.FeatureFlag
import com.blockchain.network.websocket.ConnectionEvent
import com.blockchain.network.websocket.WebSocket
import com.blockchain.utils.appendSpaced
import com.blockchain.websocket.CoinsWebSocketInterface
import com.blockchain.websocket.MessagesSocketHandler
import com.google.gson.Gson
import info.blockchain.balance.AssetCatalogue
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.wallet.exceptions.DecryptionException
import info.blockchain.wallet.payload.data.allAddresses
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import java.math.BigDecimal
import java.util.Locale
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.web3j.utils.Convert
import piuk.blockchain.android.R
import piuk.blockchain.android.data.coinswebsocket.models.BtcBchResponse
import piuk.blockchain.android.data.coinswebsocket.models.Coin
import piuk.blockchain.android.data.coinswebsocket.models.Entity
import piuk.blockchain.android.data.coinswebsocket.models.EthResponse
import piuk.blockchain.android.data.coinswebsocket.models.EthTransaction
import piuk.blockchain.android.data.coinswebsocket.models.Input
import piuk.blockchain.android.data.coinswebsocket.models.Output
import piuk.blockchain.android.data.coinswebsocket.models.Parameters
import piuk.blockchain.android.data.coinswebsocket.models.SocketRequest
import piuk.blockchain.android.data.coinswebsocket.models.SocketResponse
import piuk.blockchain.android.data.coinswebsocket.models.TokenTransfer
import piuk.blockchain.android.data.coinswebsocket.models.TransactionState
import piuk.blockchain.android.util.AppUtil
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.androidcore.data.access.PinRepository
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.events.ActionEvent
import piuk.blockchain.androidcore.data.events.TransactionsUpdatedEvent
import piuk.blockchain.androidcore.data.events.WalletAndTransactionsUpdatedEvent
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.data.rxjava.RxBus
import piuk.blockchain.androidcore.utils.PersistentPrefs
import piuk.blockchain.androidcore.utils.extensions.emptySubscribe
import timber.log.Timber

data class WebSocketReceiveEvent(
    val address: String,
    val hash: String
)

private data class CoinWebSocketInput(
    val guid: String,
    val ethAddress: String?,
    val receiveBtcAddresses: List<String>,
    val receiveBchAddresses: List<String>,
    val erc20Tokens: List<AssetInfo>,
    val xPubsBtc: List<String>,
    val xPubsBch: List<String>
)

class CoinsWebSocketStrategy(
    private val coinsWebSocket: WebSocket<String, String>,
    private val ethDataManager: EthDataManager,
    private val erc20DataManager: Erc20DataManager,
    private val bchDataManager: BchDataManager,
    private val stringUtils: StringUtils,
    private val gson: Gson,
    private val json: Json,
    private val replaceGsonKtxFF: FeatureFlag,
    private val rxBus: RxBus,
    private val prefs: PersistentPrefs,
    private val pinRepository: PinRepository,
    private val appUtil: AppUtil,
    private val payloadDataManager: PayloadDataManager,
    private val assetCatalogue: AssetCatalogue
) : CoinsWebSocketInterface {

    private var coinWebSocketInput: CoinWebSocketInput? = null
    private val compositeDisposable = CompositeDisposable()
    private var messagesSocketHandler: MessagesSocketHandler? = null

    fun setMessagesHandler(messagesSocketHandler: MessagesSocketHandler) {
        this.messagesSocketHandler = messagesSocketHandler
    }

    fun open(): Completable {
        return Completable.fromCallable {
            initInput()
            subscribeToEvents()
            coinsWebSocket.open()
        }
    }

    private fun sendMessage(message: SocketRequest) {
        replaceGsonKtxFF.enabled.onErrorReturn { false }.subscribe { replaceGsonKtx ->
            coinsWebSocket.send(
                if (replaceGsonKtx) json.encodeToString(message)
                else gson.toJson(message)
            )
        }
    }

    private fun subscribeToEvents() {
        compositeDisposable += coinsWebSocket.connectionEvents
            .subscribe { evt ->
                if (evt is ConnectionEvent.Connected) {
                    ping()
                    subscribe()
                }
            }

        compositeDisposable += coinsWebSocket.responses.distinctUntilChanged()
            .subscribe { response ->
                replaceGsonKtxFF.enabled.onErrorReturn { false }.subscribe { replaceGsonKtx ->
                    val socketResponse: SocketResponse =
                        if (replaceGsonKtx) json.decodeFromString(response)
                        else gson.fromJson(response, SocketResponse::class.java)

                    if (socketResponse.op == "on_change")
                        checkForWalletChange(socketResponse.checksum)
                    when (socketResponse.coin) {
                        Coin.ETH -> handleEthTransaction(response)
                        Coin.BTC -> handleBtcTransaction(response)
                        Coin.BCH -> handleBchTransaction(response)
                        else -> {
                        }
                    }
                }
            }
    }

    private fun checkForWalletChange(checksum: String?) {
        if (checksum == null) return
        val localChecksum = payloadDataManager.payloadChecksum
        val isSameChecksum = checksum == localChecksum

        if (!isSameChecksum && payloadDataManager.tempPassword != null) {
            compositeDisposable += downloadChangedPayload()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .emptySubscribe()
        }
    }

    private fun downloadChangedPayload(): Completable =
        payloadDataManager.initializeAndDecrypt(
            payloadDataManager.sharedKey,
            payloadDataManager.guid,
            payloadDataManager.tempPassword!!
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete { this.updateBtcBalancesAndTransactions() }
            .doOnError { throwable ->
                Timber.e(throwable)
                if (throwable is DecryptionException) {
                    appUtil.unpairWallet()
                    appUtil.restartApp()
                }
            }

    private fun handleTransactionInputsAndOutputs(
        inputs: List<Input>,
        outputs: List<Output>,
        hash: String?,
        containsAddress: (address: String) -> Boolean?
    ): Pair<String?, BigDecimal> {
        var value = 0.toBigDecimal()
        var totalValue = 0.toBigDecimal()
        var inAddr: String? = null

        inputs.forEach { input ->
            input.prevOut?.let { output ->
                if (output.value != null) {
                    value = output.value
                }
                if (output.xpub != null) {
                    totalValue -= value
                } else if (output.addr != null) {
                    if (containsAddress(output.addr) == true) {
                        totalValue -= value
                    } else if (inAddr == null) {
                        inAddr = output.addr
                    }
                }
            }
        }

        outputs.forEach { output ->
            output.value?.let {
                value = output.value
            }
            if (output.addr != null && hash != null) {
                rxBus.emitEvent(
                    WebSocketReceiveEvent::class.java,
                    WebSocketReceiveEvent(
                        output.addr,
                        hash
                    )
                )
            }
            if (output.xpub != null) {
                totalValue += value
            } else if (output.addr != null && containsAddress(output.addr) == true) {
                totalValue += value
            }
        }
        return inAddr to totalValue
    }

    private fun handleBtcTransaction(response: String) {
        replaceGsonKtxFF.enabled.onErrorReturn { false }.subscribe { replaceGsonKtx ->

            val btcResponse: BtcBchResponse =
                if (replaceGsonKtx) json.decodeFromString(response)
                else gson.fromJson(response, BtcBchResponse::class.java)

            val transaction = btcResponse.transaction ?: return@subscribe

            handleTransactionInputsAndOutputs(
                transaction.inputs,
                transaction.outputs,
                transaction.hash
            ) { x ->
                payloadDataManager.wallet?.containsImportedAddress(x)
            }

            updateBtcBalancesAndTransactions()
        }
    }

    private fun handleBchTransaction(response: String) {
        replaceGsonKtxFF.enabled.onErrorReturn { false }.subscribe { replaceGsonKtx ->
            val bchResponse: BtcBchResponse =
                if (replaceGsonKtx) json.decodeFromString(response)
                else gson.fromJson(response, BtcBchResponse::class.java)

            val transaction = bchResponse.transaction ?: return@subscribe

            val (inAddr, totalValue) =
                handleTransactionInputsAndOutputs(
                    transaction.inputs,
                    transaction.outputs,
                    transaction.hash
                ) { x ->
                    bchDataManager.getImportedAddressStringList().contains(x)
                }

            updateBchBalancesAndTransactions()

            val title = stringUtils.getString(R.string.app_name)

            if (totalValue > BigDecimal.ZERO) {
                val amount = CryptoValue.fromMinor(CryptoCurrency.BCH, totalValue)
                val marquee =
                    stringUtils.getString(R.string.received_bitcoin_cash) + amount.toStringWithSymbol()

                var text = marquee
                text += " ${stringUtils.getString(R.string.common_from).toLowerCase(Locale.US)} $inAddr"
                messagesSocketHandler?.triggerNotification(
                    title, marquee, text
                )
            }
        }
    }

    private fun updateBtcBalancesAndTransactions() {
        compositeDisposable += payloadDataManager.updateAllBalances()
            .andThen(payloadDataManager.updateAllTransactions())
            .subscribe {
                rxBus.emitEvent(ActionEvent::class.java, WalletAndTransactionsUpdatedEvent())
            }
    }

    private fun updateBchBalancesAndTransactions() {
        compositeDisposable += bchDataManager.updateAllBalances()
            .andThen(bchDataManager.getWalletTransactions(50, 0))
            .subscribe {
                rxBus.emitEvent(ActionEvent::class.java, WalletAndTransactionsUpdatedEvent())
            }
    }

    private fun handleEthTransaction(response: String) {
        replaceGsonKtxFF.enabled.onErrorReturn { false }.subscribe { replaceGsonKtx ->
            val ethResponse: EthResponse =
                if (replaceGsonKtx) json.decodeFromString(response)
                else gson.fromJson(response, EthResponse::class.java)

            val title = stringUtils.getString(R.string.app_name)

            if (ethResponse.transaction != null && ethResponse.getTokenType() == CryptoCurrency.ETHER) {
                val transaction: EthTransaction = ethResponse.transaction
                val ethAddress = ethAddress()
                if (transaction.state == TransactionState.CONFIRMED && transaction.to.equals(ethAddress, true)
                ) {
                    val marqueeBuilder = StringBuilder()
                        .append(stringUtils.getString(R.string.received_ethereum).format(CryptoCurrency.ETHER.name))
                        .appendSpaced(Convert.fromWei(BigDecimal(transaction.value), Convert.Unit.ETHER))
                        .appendSpaced(CryptoCurrency.ETHER.displayTicker)

                    val textBuilder = StringBuilder()
                        .append(marqueeBuilder)
                        .appendSpaced(stringUtils.getString(R.string.common_from).toLowerCase(Locale.US))
                        .appendSpaced(transaction.from)

                    messagesSocketHandler?.triggerNotification(title, marqueeBuilder.toString(), textBuilder.toString())
                }
                updateEthTransactions()
            }

            if (ethResponse.entity == Entity.TokenAccount &&
                ethResponse.tokenTransfer != null &&
                ethResponse.tokenTransfer.to.equals(ethAddress(), true)
            ) {
                val tokenTransaction = ethResponse.tokenTransfer
                val asset = ethResponse.getTokenType()
                if (asset.isErc20()) {
                    triggerErc20NotificationAndUpdate(asset, tokenTransaction, title)
                }
            }
        }
    }

    private fun triggerErc20NotificationAndUpdate(
        asset: AssetInfo,
        tokenTransaction: TokenTransfer,
        title: String
    ) {
        val amountString = CryptoValue.fromMinor(asset, tokenTransaction.value).toStringWithSymbol()
        val formatMarquee = stringUtils.getString(R.string.received_erc20_marquee)
        val marquee = formatMarquee.format(
            asset.name,
            amountString
        )
        val formatText = stringUtils.getString(R.string.received_erc20_text)
        val text = formatText.format(
            asset.name,
            amountString,
            tokenTransaction.from
        )

        erc20DataManager.flushCaches(asset)
        messagesSocketHandler?.run {
            triggerNotification(title, marquee, text)
            sendBroadcast(TransactionsUpdatedEvent())
        }
    }

    private fun updateEthTransactions() {
        compositeDisposable += ethDataManager.fetchEthAddress()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = { messagesSocketHandler?.sendBroadcast(TransactionsUpdatedEvent()) },
                onError = { throwable -> Timber.e(throwable, "downloadEthTransactions failed") }
            )
    }

    override fun subscribeToXpubBtc(xpub: String) {
        val updatedList = (coinWebSocketInput?.xPubsBtc?.toMutableList() ?: mutableListOf()) + xpub
        coinWebSocketInput = coinWebSocketInput?.copy(xPubsBtc = updatedList)

        subscribeXpub(Coin.BTC, xpub)
    }

    override fun subscribeToExtraBtcAddress(address: String) {
        coinWebSocketInput?.let { input ->
            val updatedList = input.receiveBtcAddresses.toMutableList() + address
            coinWebSocketInput = input.copy(receiveBtcAddresses = updatedList)

            sendMessage(
                SocketRequest.SubscribeRequest(
                    Entity.Account,
                    Coin.BTC,
                    Parameters.SimpleAddress(address = address)
                )
            )
        }
    }

    fun close(): Completable {
        return Completable.fromCallable {
            unsubscribeFromAddresses()
            coinsWebSocket.close()
            compositeDisposable.clear()
        }.onErrorComplete()
    }

    private fun unsubscribeFromAddresses() {
        coinWebSocketInput?.let { input ->
            input.receiveBtcAddresses.forEach { address ->
                unsubscribeAddress(Coin.BTC, address)
            }

            input.receiveBchAddresses.forEach { address ->
                unsubscribeAddress(Coin.BCH, address)
            }

            input.xPubsBtc.forEach { xPub ->
                unsubscribeXpub(Coin.BTC, xPub)
            }

            input.xPubsBch.forEach { xPub ->
                unsubscribeXpub(Coin.BCH, xPub)
            }

            input.ethAddress?.let { address ->
                unsubscribeAddress(Coin.ETH, address)
            }

            input.erc20Tokens.forEach { asset ->
                unsubscribeErc20(ethAddress(), asset)
            }

            sendMessage(
                SocketRequest.UnSubscribeRequest(
                    Entity.Wallet,
                    Coin.None,
                    Parameters.Guid(input.guid)
                )
            )
        }
    }

    private fun unsubscribeAddress(coin: Coin, address: String) {
        sendMessage(
            SocketRequest.UnSubscribeRequest(
                Entity.Account,
                coin,
                Parameters.SimpleAddress(address)
            )
        )
    }

    private fun unsubscribeErc20(ethAddress: String?, asset: AssetInfo) {
        ethAddress?.let {
            asset.l2identifier?.let { contractAddr ->
                sendMessage(
                    SocketRequest.UnSubscribeRequest(
                        Entity.TokenAccount,
                        Coin.ETH,
                        Parameters.TokenedAddress(
                            address = ethAddress,
                            tokenAddress = contractAddr
                        )
                    )
                )
            }
        }
    }

    private fun unsubscribeXpub(coin: Coin, xpub: String) {
        sendMessage(
            SocketRequest.UnSubscribeRequest(
                Entity.Xpub,
                coin,
                Parameters.SimpleAddress(address = xpub)
            )
        )
    }

    private fun initInput() {
        coinWebSocketInput = CoinWebSocketInput(
            guid = guid(),
            ethAddress = ethAddress(),
            receiveBtcAddresses = btcReceiveAddresses(),
            receiveBchAddresses = bchReceiveAddresses(),
            erc20Tokens = assetCatalogue.supportedL2Assets(CryptoCurrency.ETHER),
            xPubsBtc = xPubsBtc(),
            xPubsBch = xPubsBch()
        )
    }

    private fun guid(): String = prefs.walletGuid

    private fun xPubsBch(): List<String> {
        return if (payloadDataManager.wallet?.isUpgradedToV3 == true) {
            bchDataManager.getActiveXpubs().allAddresses()
        } else {
            emptyList()
        }
    }

    private fun xPubsBtc(): List<String> =
        if (payloadDataManager.wallet?.isUpgradedToV3 == true) {
            payloadDataManager.wallet
                ?.walletBody
                ?.accounts
                ?.map { a -> a.xpubs.allAddresses() }
                ?.flatten() ?: emptyList()
        } else {
            emptyList()
        }

    private fun btcReceiveAddresses(): List<String> =
        payloadDataManager.wallet?.let {
            mutableListOf<String>().apply {
                val importedList = it.importedAddressList
                importedList.forEach { element ->
                    val address = element.address
                    if (address.isNullOrEmpty().not()) {
                        add(address!!)
                    }
                }
            }
        } ?: emptyList()

    private fun bchReceiveAddresses(): List<String> =
        payloadDataManager.wallet?.let {
            mutableListOf<String>().apply {
                val importedList = bchDataManager.getImportedAddressStringList()
                importedList.forEach { address ->
                    if (address.isNotEmpty()) {
                        add(address)
                    }
                }
            }
        } ?: emptyList()

    private fun ethAddress(): String =
        ethDataManager.accountAddress

    private fun subscribe() {
        coinWebSocketInput?.let { input ->
            sendMessage(
                SocketRequest.SubscribeRequest(
                    Entity.Wallet,
                    Coin.None,
                    Parameters.Guid(input.guid)
                )
            )

            input.receiveBtcAddresses.forEach { address ->
                subscribeAddress(Coin.BTC, address)
            }

            input.receiveBchAddresses.forEach { address ->
                subscribeAddress(Coin.BCH, address)
            }

            input.xPubsBtc.forEach { xPub ->
                subscribeXpub(Coin.BTC, xPub)
            }

            input.xPubsBch.forEach { xPub ->
                subscribeXpub(Coin.BCH, xPub)
            }

            input.ethAddress?.let { address ->
                subscribeAddress(Coin.ETH, address)
            }

            input.erc20Tokens.forEach {
                subscribeErc20(input.ethAddress, it)
            }
        }
    }

    private fun subscribeAddress(coin: Coin, address: String) {
        sendMessage(
            SocketRequest.SubscribeRequest(
                Entity.Account,
                coin,
                Parameters.SimpleAddress(address = address)
            )
        )
    }

    private fun subscribeXpub(coin: Coin, xpub: String) {
        sendMessage(
            SocketRequest.SubscribeRequest(
                Entity.Xpub,
                coin,
                Parameters.SimpleAddress(address = xpub)
            )
        )
    }

    private fun subscribeErc20(ethAddress: String?, asset: AssetInfo) {
        ethAddress?.let {
            asset.l2identifier?.let { contractAddress ->
                sendMessage(
                    SocketRequest.SubscribeRequest(
                        Entity.TokenAccount,
                        Coin.ETH,
                        Parameters.TokenedAddress(
                            address = ethAddress,
                            tokenAddress = contractAddress
                        )
                    )
                )
            }
        }
    }

    private fun ping() {
        sendMessage(SocketRequest.PingRequest)
    }

    private fun EthResponse.getTokenType(): AssetInfo {
        require(entity == Entity.Account || entity == Entity.TokenAccount)
        return when {
            entity == Entity.Account && !isErc20Token() -> CryptoCurrency.ETHER
            entity == Entity.TokenAccount -> getErc20ParamType()
            else -> {
                throw IllegalStateException("This should never trigger!")
            }
        }
    }

    private fun EthResponse.isErc20Token(): Boolean =
        assetCatalogue.supportedL2Assets(CryptoCurrency.ETHER)
            .filter { it.l2identifier != null }
            .firstOrNull {
                transaction?.to.equals(it.l2identifier, true)
            } != null

    private fun EthResponse.getErc20ParamType(): AssetInfo =
        assetCatalogue.supportedL2Assets(CryptoCurrency.ETHER)
            .filter { it.l2identifier != null }
            .firstOrNull {
                param?.tokenAddress.equals(it.l2identifier, true)
            } ?: throw IllegalStateException("Unknown asset")
}
