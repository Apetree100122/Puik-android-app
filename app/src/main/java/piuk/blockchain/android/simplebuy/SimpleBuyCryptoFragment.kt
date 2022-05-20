package piuk.blockchain.android.simplebuy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.blockchain.analytics.events.LaunchOrigin
import com.blockchain.coincore.AssetAction
import com.blockchain.commonarch.presentation.base.SlidingModalBottomDialog
import com.blockchain.commonarch.presentation.mvi.MviFragment
import com.blockchain.componentlib.viewextensions.gone
import com.blockchain.componentlib.viewextensions.visible
import com.blockchain.core.limits.TxLimit
import com.blockchain.domain.eligibility.model.TransactionsLimit
import com.blockchain.extensions.exhaustive
import com.blockchain.featureflag.FeatureFlag
import com.blockchain.koin.entitySwitchSilverEligibilityFeatureFlag
import com.blockchain.koin.scopedInject
import com.blockchain.nabu.datamanagers.OrderState
import com.blockchain.nabu.datamanagers.PaymentMethod
import com.blockchain.nabu.datamanagers.PaymentMethod.UndefinedCard.CardFundSource
import com.blockchain.nabu.datamanagers.UndefinedPaymentMethod
import com.blockchain.nabu.datamanagers.custodialwalletimpl.PaymentMethodType
import com.blockchain.nabu.models.data.RecurringBuyFrequency
import com.blockchain.preferences.CurrencyPrefs
import com.blockchain.utils.capitalizeFirstChar
import com.blockchain.utils.isLastDayOfTheMonth
import com.blockchain.utils.to12HourFormat
import com.bumptech.glide.Glide
import info.blockchain.balance.AssetCatalogue
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.FiatCurrency
import info.blockchain.balance.FiatValue
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.math.BigDecimal
import java.time.ZonedDateTime
import org.koin.android.ext.android.inject
import piuk.blockchain.android.R
import piuk.blockchain.android.campaign.CampaignType
import piuk.blockchain.android.cards.CardDetailsActivity
import piuk.blockchain.android.cards.CardDetailsActivity.Companion.ADD_CARD_REQUEST_CODE
import piuk.blockchain.android.cards.icon
import piuk.blockchain.android.databinding.FragmentSimpleBuyBuyCryptoBinding
import piuk.blockchain.android.simplebuy.paymentmethods.PaymentMethodChooserBottomSheet
import piuk.blockchain.android.ui.customviews.inputview.FiatCryptoViewConfiguration
import piuk.blockchain.android.ui.customviews.inputview.PrefixedOrSuffixedEditText
import piuk.blockchain.android.ui.dashboard.asDeltaPercent
import piuk.blockchain.android.ui.dashboard.sheets.KycUpgradeNowSheet
import piuk.blockchain.android.ui.dashboard.sheets.WireTransferAccountDetailsBottomSheet
import piuk.blockchain.android.ui.dashboard.showLoading
import piuk.blockchain.android.ui.kyc.navhost.KycNavHostActivity
import piuk.blockchain.android.ui.linkbank.BankAuthActivity
import piuk.blockchain.android.ui.linkbank.BankAuthSource
import piuk.blockchain.android.ui.recurringbuy.RecurringBuyAnalytics
import piuk.blockchain.android.ui.recurringbuy.RecurringBuyAnalytics.Companion.PAYMENT_METHOD_UNAVAILABLE
import piuk.blockchain.android.ui.recurringbuy.RecurringBuyAnalytics.Companion.SELECT_PAYMENT
import piuk.blockchain.android.ui.recurringbuy.RecurringBuySelectionBottomSheet
import piuk.blockchain.android.ui.resources.AssetResources
import piuk.blockchain.android.ui.settings.SettingsAnalytics
import piuk.blockchain.android.ui.transactionflow.analytics.InfoBottomSheetDismissed
import piuk.blockchain.android.ui.transactionflow.analytics.InfoBottomSheetKycUpsellActionClicked
import piuk.blockchain.android.ui.transactionflow.engine.TransactionErrorState
import piuk.blockchain.android.ui.transactionflow.flow.TransactionFlowInfoBottomSheet
import piuk.blockchain.android.ui.transactionflow.flow.TransactionFlowInfoHost
import piuk.blockchain.android.ui.transactionflow.flow.customisations.InfoActionType
import piuk.blockchain.android.ui.transactionflow.flow.customisations.InfoBottomSheetType
import piuk.blockchain.android.ui.transactionflow.flow.customisations.TransactionFlowBottomSheetInfo
import piuk.blockchain.android.ui.transactionflow.flow.customisations.TransactionFlowInfoBottomSheetCustomiser
import piuk.blockchain.android.util.getResolvedColor
import piuk.blockchain.android.util.getResolvedDrawable
import piuk.blockchain.android.util.setAssetIconColoursWithTint

class SimpleBuyCryptoFragment :
    MviFragment<SimpleBuyModel, SimpleBuyIntent, SimpleBuyState, FragmentSimpleBuyBuyCryptoBinding>(),
    RecurringBuySelectionBottomSheet.Host,
    SimpleBuyScreen,
    TransactionFlowInfoHost,
    PaymentMethodChooserBottomSheet.Host,
    KycUpgradeNowSheet.Host {

    override val model: SimpleBuyModel by scopedInject()
    private val assetResources: AssetResources by inject()
    private val assetCatalogue: AssetCatalogue by inject()
    private val currencyPrefs: CurrencyPrefs by inject()
    private val entitySwitchSilverEligibilityFF: FeatureFlag by inject(entitySwitchSilverEligibilityFeatureFlag)
    private val bottomSheetInfoCustomiser: TransactionFlowInfoBottomSheetCustomiser by inject()
    private var infoActionCallback: () -> Unit = {}

    private val fiatCurrency: FiatCurrency
        get() = currencyPrefs.tradingCurrency

    private var lastState: SimpleBuyState? = null
    private val compositeDisposable = CompositeDisposable()

    private val asset: AssetInfo
        get() = arguments?.getString(ARG_CRYPTO_ASSET)?.let {
            assetCatalogue.assetInfoFromNetworkTicker(it)
        } ?: throw IllegalArgumentException("No cryptoCurrency specified")

    private val preselectedMethodId: String?
        get() = arguments?.getString(ARG_PAYMENT_METHOD_ID)

    private val preselectedAmount: FiatValue?
        get() = arguments?.getString(ARG_AMOUNT)?.let { amount ->
            FiatValue.fromMajor(fiatCurrency, BigDecimal(amount))
        }

    private val errorContainer by lazy {
        binding.errorLayout.errorContainer
    }

    override fun navigator(): SimpleBuyNavigator =
        (activity as? SimpleBuyNavigator)
            ?: throw IllegalStateException("Parent must implement SimpleBuyNavigator")

    override fun onBackPressed(): Boolean = true

    override fun initBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSimpleBuyBuyCryptoBinding =
        FragmentSimpleBuyBuyCryptoBinding.inflate(inflater, container, false)

    override fun onResume() {
        super.onResume()
        model.process(SimpleBuyIntent.UpdateExchangeRate(fiatCurrency, asset))
        model.process(SimpleBuyIntent.FlowCurrentScreen(FlowScreen.ENTER_AMOUNT))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        activity.updateToolbar(
            toolbarTitle = getString(R.string.tx_title_buy, asset.displayTicker),
            backAction = { activity.onBackPressed() }
        )
        model.process(SimpleBuyIntent.InitialiseSelectedCryptoAndFiat(asset, fiatCurrency))
        model.process(
            SimpleBuyIntent.FetchSuggestedPaymentMethod(
                fiatCurrency,
                preselectedMethodId
            )
        )
        model.process(SimpleBuyIntent.FetchSupportedFiatCurrencies)
        compositeDisposable += entitySwitchSilverEligibilityFF.enabled
            .subscribe { enabled ->
                if (enabled) model.process(SimpleBuyIntent.FetchEligibility)
            }
        analytics.logEvent(SimpleBuyAnalytics.BUY_FORM_SHOWN)

        preselectedAmount

        compositeDisposable += binding.inputAmount.amount
            .doOnSubscribe {
                preselectedAmount?.let { amount ->
                    model.process(SimpleBuyIntent.AmountUpdated(amount))
                }
            }
            .subscribe {
                when (it) {
                    is FiatValue -> model.process(SimpleBuyIntent.AmountUpdated(it))
                    else -> throw IllegalStateException("CryptoValue is not supported as input yet")
                }
            }

        binding.btnContinue.setOnClickListener { startBuy() }

        compositeDisposable += binding.inputAmount.onImeAction.subscribe {
            if (it == PrefixedOrSuffixedEditText.ImeOptions.NEXT)
                startBuy()
        }
    }

    override fun showAvailableToAddPaymentMethods() =
        showPaymentMethodsBottomSheet(
            paymentOptions = lastState?.paymentOptions ?: PaymentOptions(),
            state = PaymentMethodsChooserState.AVAILABLE_TO_ADD
        )

    private fun showPaymentMethodsBottomSheet(paymentOptions: PaymentOptions, state: PaymentMethodsChooserState) {
        showBottomSheet(
            when (state) {
                PaymentMethodsChooserState.AVAILABLE_TO_PAY ->
                    PaymentMethodChooserBottomSheet.newInstance(
                        paymentMethods = paymentOptions.availablePaymentMethods
                            .filter { method ->
                                method.canBeUsedForPaying() ||
                                    method is PaymentMethod.UndefinedBankAccount
                            },
                        mode = PaymentMethodChooserBottomSheet.DisplayMode.PAYMENT_METHODS,
                        canAddNewPayment = paymentOptions.availablePaymentMethods.any { method -> method.canBeAdded() }
                    )
                PaymentMethodsChooserState.AVAILABLE_TO_ADD ->
                    PaymentMethodChooserBottomSheet.newInstance(
                        paymentMethods = paymentOptions.availablePaymentMethods
                            .filter { method ->
                                method.canBeAdded()
                            },
                        mode = PaymentMethodChooserBottomSheet.DisplayMode.PAYMENT_METHOD_TYPES,
                        canAddNewPayment = true,
                        canUseCreditCards = canUseCreditCards()
                    )
            }
        )
    }

    private fun startBuy() {
        lastState?.takeIf { canContinue(it) }?.let { state ->
            model.process(SimpleBuyIntent.BuyButtonClicked)
            model.process(SimpleBuyIntent.CancelOrderIfAnyAndCreatePendingOne)
            analytics.logEvent(
                buyConfirmClicked(
                    state.amount.toBigInteger().toString(),
                    state.fiatCurrency.networkTicker,
                    state.selectedPaymentMethod?.paymentMethodType?.toAnalyticsString().orEmpty()
                )
            )

            val paymentMethodDetails = state.selectedPaymentMethodDetails
            check(paymentMethodDetails != null)

            analytics.logEvent(
                BuyAmountEntered(
                    frequency = state.recurringBuyFrequency.name,
                    inputAmount = state.amount,
                    maxCardLimit = if (paymentMethodDetails is PaymentMethod.Card) {
                        paymentMethodDetails.limits.max
                        state.amount
                    } else null,
                    outputCurrency = state.selectedCryptoAsset?.networkTicker ?: return,
                    paymentMethod = state.selectedPaymentMethod?.paymentMethodType
                        ?: return
                )
            )
        }
    }

    private fun showDialogRecurringBuyUnavailable(paymentMethodDefined: Boolean) {
        showAlert(
            AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                .setTitle(R.string.recurring_buy_unavailable_title)
                .setMessage(R.string.recurring_buy_unavailable_message)
                .setCancelable(false)
                .setPositiveButton(R.string.recurring_buy_cta_alert) { dialog, _ ->
                    val interval = RecurringBuyFrequency.ONE_TIME
                    model.process(SimpleBuyIntent.RecurringBuyIntervalUpdated(interval))
                    binding.recurringBuyCta.text = interval.toHumanReadableRecurringBuy(requireContext())
                    dialog.dismiss()
                }
                .create()
        )

        if (paymentMethodDefined) {
            analytics.logEvent(RecurringBuyAnalytics.RecurringBuyUnavailableShown(SELECT_PAYMENT))
        } else {
            analytics.logEvent(
                RecurringBuyAnalytics.RecurringBuyUnavailableShown(PAYMENT_METHOD_UNAVAILABLE)
            )
        }
    }

    override fun render(newState: SimpleBuyState) {
        lastState = newState
        if (newState.buyErrorState != null) {
            showErrorState(newState.buyErrorState)
            return
        }

        binding.recurringBuyCta.text = newState.recurringBuyFrequency.toHumanReadableRecurringBuy(requireContext())

        newState.selectedCryptoAsset?.let {
            binding.inputAmount.configuration = FiatCryptoViewConfiguration(
                inputCurrency = newState.fiatCurrency,
                outputCurrency = newState.fiatCurrency,
                exchangeCurrency = it,
                canSwap = false,
                predefinedAmount = newState.order.amount ?: FiatValue.zero(newState.fiatCurrency)
            )
            binding.buyIcon.setAssetIconColoursWithTint(it)
        }
        newState.selectedCryptoAsset?.let {
            assetResources.loadAssetIcon(binding.cryptoIcon, it)
            binding.cryptoText.text = it.name
        }

        newState.exchangePriceWithDelta?.let {
            binding.cryptoExchangeRate.text = it.price.toStringWithSymbol()
            binding.priceDelta.asDeltaPercent(it.delta)
        }

        (newState.limits.max as? TxLimit.Limited)?.amount?.takeIf {
            it.currency == fiatCurrency
        }?.let {
            binding.inputAmount.maxLimit = it
        }

        val transactionsLimit = newState.transactionsLimit
        if (transactionsLimit is TransactionsLimit.Limited) {
            binding.inputAmount.showInfo(
                getString(R.string.tx_enter_amount_orders_limit_info, transactionsLimit.maxTransactionsLeft)
            ) {
                val info = bottomSheetInfoCustomiser.info(
                    InfoBottomSheetType.TRANSACTIONS_LIMIT, newState,
                    newState.fiatCurrency.type
                )
                if (info != null) {
                    showBottomSheet(TransactionFlowInfoBottomSheet.newInstance(info))
                    infoActionCallback = handlePossibleInfoAction(info, newState.transactionsLimit)
                }
            }
        } else {
            binding.inputAmount.hideInfo()
        }

        if (newState.paymentOptions.availablePaymentMethods.isEmpty()) {
            paymentMethodLoading()
            disableRecurringBuyCta(false)
        } else {
            newState.selectedPaymentMethodDetails?.let { paymentMethod ->
                renderDefinedPaymentMethod(newState, paymentMethod)
            }
        }

        binding.btnContinue.isEnabled = canContinue(newState)

        updateInputStateUI(newState)
        showCtaOrError(newState)

        if (newState.confirmationActionRequested &&
            newState.kycVerificationState != null &&
            newState.orderState == OrderState.PENDING_CONFIRMATION
        ) {
            handlePostOrderCreationAction(newState)
        }

        newState.newPaymentMethodToBeAdded?.let {
            handleNewPaymentMethodAdding(newState)
        }

        newState.linkBankTransfer?.let {
            model.process(SimpleBuyIntent.ResetLinkBankTransfer)
            startActivityForResult(
                BankAuthActivity.newInstance(
                    it, BankAuthSource.SIMPLE_BUY, requireContext()
                ),
                BankAuthActivity.LINK_BANK_REQUEST_CODE
            )
        }
    }

    private fun showCtaOrError(newState: SimpleBuyState) {
        when {
            newState.errorStateShouldBeIndicated() -> showError(newState)
            else -> showCta()
        }
    }

    private fun showError(state: SimpleBuyState) {
        binding.btnContinue.gone()
        binding.errorLayout.errorMessage.text = state.errorState.message(state)
        errorContainer.visible()

        val infoType = when (state.errorState) {
            TransactionErrorState.INSUFFICIENT_FUNDS -> InfoBottomSheetType.INSUFFICIENT_FUNDS
            TransactionErrorState.BELOW_MIN_PAYMENT_METHOD_LIMIT,
            TransactionErrorState.BELOW_MIN_LIMIT -> InfoBottomSheetType.BELOW_MIN_LIMIT
            // we need to keep those for working with the feature flag off, otherwise we would be based only on the
            // suggested upgrade
            TransactionErrorState.OVER_GOLD_TIER_LIMIT,
            TransactionErrorState.OVER_SILVER_TIER_LIMIT -> InfoBottomSheetType.OVER_MAX_LIMIT
            TransactionErrorState.ABOVE_MAX_PAYMENT_METHOD_LIMIT ->
                InfoBottomSheetType.ABOVE_MAX_PAYMENT_METHOD_LIMIT
            else -> null
        }

        val bottomSheetInfo = infoType?.let { type ->
            bottomSheetInfoCustomiser.info(type, state, state.fiatCurrency.type)
        }
        bottomSheetInfo?.let { info ->
            errorContainer.setOnClickListener {
                showBottomSheet(TransactionFlowInfoBottomSheet.newInstance(info))
                infoActionCallback =
                    handlePossibleInfoAction(info, state.transactionsLimit ?: TransactionsLimit.Unlimited)
            }
        } ?: errorContainer.setOnClickListener {}
    }

    private fun handlePossibleInfoAction(
        info: TransactionFlowBottomSheetInfo,
        transactionsLimit: TransactionsLimit
    ): () -> Unit {
        val type = info.action?.actionType ?: return {}
        return when (type) {
            InfoActionType.KYC_UPGRADE -> return {
                analytics.logEvent(InfoBottomSheetKycUpsellActionClicked(AssetAction.Buy))
                showBottomSheet(
                    KycUpgradeNowSheet.newInstance(transactionsLimit)
                )
            }
            InfoActionType.BUY -> {
                {}
            }
        }
    }

    private fun showCta() {
        binding.btnContinue.visible()
        errorContainer.gone()
    }

    private fun handleNewPaymentMethodAdding(state: SimpleBuyState) {
        require(state.newPaymentMethodToBeAdded is UndefinedPaymentMethod)
        addPaymentMethod(state.newPaymentMethodToBeAdded.type, state.fiatCurrency)
        model.process(SimpleBuyIntent.AddNewPaymentMethodHandled)
        model.process(SimpleBuyIntent.SelectedPaymentMethodUpdate(state.newPaymentMethodToBeAdded))
    }

    private fun updateInputStateUI(newState: SimpleBuyState) {
        binding.inputAmount.onAmountValidationUpdated(
            isValid = !newState.errorStateShouldBeIndicated()
        )
    }

    private fun handlePostOrderCreationAction(newState: SimpleBuyState) {
        when {
            newState.selectedPaymentMethod?.isActive() == true -> {
                navigator().goToCheckOutScreen()
            }
            newState.selectedPaymentMethod?.paymentMethodType == PaymentMethodType.GOOGLE_PAY &&
                newState.kycVerificationState == KycState.VERIFIED_AND_ELIGIBLE -> {
                // We need to ensure that only verified and eligible users can use Google Pay
                navigator().goToCheckOutScreen()
            }
            newState.selectedPaymentMethod?.isEligible == true -> {
                addPaymentMethod(newState.selectedPaymentMethod.paymentMethodType, newState.fiatCurrency)
            }
            else -> {
                require(newState.kycVerificationState != null)
                require(newState.kycVerificationState != KycState.VERIFIED_AND_ELIGIBLE)
                when (newState.kycVerificationState) {
                    // Kyc state unknown because error, or gold docs unsubmitted
                    KycState.PENDING -> {
                        startKyc()
                    }
                    // Awaiting results state
                    KycState.IN_REVIEW,
                    KycState.UNDECIDED -> {
                        navigator().goToKycVerificationScreen()
                    }
                    // Got results, kyc verification screen will show error
                    KycState.VERIFIED_BUT_NOT_ELIGIBLE,
                    KycState.FAILED -> {
                        navigator().goToKycVerificationScreen()
                    }
                    KycState.VERIFIED_AND_ELIGIBLE -> throw IllegalStateException(
                        "Payment method should be active or eligible"
                    )
                }.exhaustive
            }
        }
    }

    override fun startKycClicked() {
        startKyc()
    }

    /**
     * Once User selects the option to Link a bank then his/her Kyc status is checked.
     * If is VERIFIED_AND_ELIGIBLE then we try to link a bank and if the fetched partner is supported
     * then the LinkBankActivity is launched.
     * In case that user is not VERIFIED_AND_ELIGIBLE then we just select the payment method and when
     * user presses Continue the KYC flow is launched
     */

    private fun startKyc() {
        model.process(SimpleBuyIntent.NavigationHandled)
        model.process(SimpleBuyIntent.KycStarted)
        analytics.logEvent(SimpleBuyAnalytics.START_GOLD_FLOW)
        KycNavHostActivity.startForResult(this, CampaignType.SimpleBuy, SimpleBuyActivity.KYC_STARTED)
    }

    private fun canContinue(state: SimpleBuyState): Boolean =
        if (state.amount.isZero) false else {
            state.errorState == TransactionErrorState.NONE &&
                state.selectedPaymentMethod != null &&
                !state.isLoading
        }

    private fun renderDefinedPaymentMethod(state: SimpleBuyState, selectedPaymentMethod: PaymentMethod) {
        renderRecurringBuy(state)

        with(binding) {
            paymentMethod.visible()
            paymentMethodSeparator.visible()
            paymentMethodTitle.visible()
            paymentMethodLimit.visible()
            paymentMethodDetailsRoot.visible()
            paymentMethodDetailsRoot.setOnClickListener {
                showPaymentMethodsBottomSheet(
                    state = state.paymentOptions.availablePaymentMethods.toPaymentMethodChooserState(),
                    paymentOptions = state.paymentOptions
                )
            }
        }

        when (selectedPaymentMethod) {
            is PaymentMethod.Card -> renderCardPayment(selectedPaymentMethod)
            is PaymentMethod.Funds -> renderFundsPayment(selectedPaymentMethod)
            is PaymentMethod.Bank -> renderBankPayment(selectedPaymentMethod)
            is PaymentMethod.UndefinedCard -> renderUndefinedCardPayment(selectedPaymentMethod)
            is PaymentMethod.UndefinedBankTransfer -> renderUndefinedBankTransfer(selectedPaymentMethod)
            is PaymentMethod.GooglePay -> renderGooglePayPayment(selectedPaymentMethod)
            else -> {
                // Nothing to do here.
            }
        }
    }

    private fun List<PaymentMethod>.toPaymentMethodChooserState(): PaymentMethodsChooserState {
        with(filter { it.canBeUsedForPaying() }) {
            return if (
                this.isEmpty() ||
                // Show AVAILABLE_TO_ADD if GooglePay is the only method that can be used for paying
                (this.size == 1 && this.singleOrNull { it is PaymentMethod.GooglePay } != null)
            ) {
                PaymentMethodsChooserState.AVAILABLE_TO_ADD
            } else {
                PaymentMethodsChooserState.AVAILABLE_TO_PAY
            }
        }
    }

    private fun renderRecurringBuy(state: SimpleBuyState) {

        val paymentMethodIsEligibleForSelectedFreq =
            state.isSelectedPaymentMethodEligibleForSelectedFrequency() ||
                state.recurringBuyFrequency == RecurringBuyFrequency.ONE_TIME

        if (!paymentMethodIsEligibleForSelectedFreq) {
            model.process(SimpleBuyIntent.RecurringBuyIntervalUpdated(RecurringBuyFrequency.ONE_TIME))
        }

        if (state.isSelectedPaymentMethodRecurringBuyEligible()) {
            enableRecurringBuyCta()
        } else {
            disableRecurringBuyCta(state.selectedPaymentMethodDetails?.canBeUsedForPaying() ?: false)
        }
    }

    private fun enableRecurringBuyCta() {
        binding.recurringBuyCta.apply {
            background = requireContext().getResolvedDrawable(R.drawable.bkgd_button_white_selector)
            setTextColor(requireContext().getResolvedColor(R.color.button_white_text_states))
            setOnClickListener {
                showBottomSheet(RecurringBuySelectionBottomSheet.newInstance())
            }
        }
    }

    private fun disableRecurringBuyCta(paymentMethodDefined: Boolean) {
        binding.recurringBuyCta.apply {
            background = requireContext().getResolvedDrawable(R.drawable.bkgd_grey_000_rounded)
            setTextColor(requireContext().getResolvedColor(R.color.grey_800))
            setOnClickListener {
                showDialogRecurringBuyUnavailable(paymentMethodDefined)
            }
        }
    }

    private fun renderFundsPayment(paymentMethod: PaymentMethod.Funds) {
        with(binding) {
            paymentMethodBankInfo.gone()
            assetResources.loadAssetIcon(paymentMethodIcon, paymentMethod.fiatCurrency)
            paymentMethodTitle.text = paymentMethod.fiatCurrency.name

            paymentMethodLimit.text = paymentMethod.limits.max.toStringWithSymbol()
        }
    }

    private fun renderBankPayment(paymentMethod: PaymentMethod.Bank) {
        with(binding) {
            paymentMethodIcon.setImageResource(R.drawable.ic_bank_transfer)
            if (paymentMethod.iconUrl.isNotEmpty()) {
                Glide.with(requireContext()).load(paymentMethod.iconUrl).into(paymentMethodIcon)
            }

            paymentMethodTitle.text = paymentMethod.bankName
            paymentMethodBankInfo.text =
                requireContext().getString(
                    R.string.payment_method_type_account_info, paymentMethod.uiAccountType,
                    paymentMethod.accountEnding
                )
            paymentMethodBankInfo.visible()
            paymentMethodLimit.text =
                getString(R.string.payment_method_limit, paymentMethod.limits.max.toStringWithSymbol())
        }
    }

    private fun renderUndefinedCardPayment(selectedPaymentMethod: PaymentMethod.UndefinedCard) {
        with(binding) {
            paymentMethodBankInfo.gone()
            paymentMethodIcon.setImageResource(R.drawable.ic_payment_card)
            paymentMethodTitle.text = if (canUseCreditCards())
                getString(R.string.credit_or_debit_card) else getString(R.string.add_debit_card)
            paymentMethodLimit.text =
                getString(R.string.payment_method_limit, selectedPaymentMethod.limits.max.toStringWithSymbol())
        }
    }

    private fun renderUndefinedBankTransfer(selectedPaymentMethod: PaymentMethod.UndefinedBankTransfer) {
        with(binding) {
            paymentMethodBankInfo.gone()
            paymentMethodIcon.setImageResource(R.drawable.ic_bank_transfer)
            paymentMethodTitle.text = getString(R.string.easy_bank_transfer)
            paymentMethodLimit.text =
                getString(R.string.payment_method_limit, selectedPaymentMethod.limits.max.toStringWithSymbol())
        }
    }

    private fun renderCardPayment(selectedPaymentMethod: PaymentMethod.Card) {
        with(binding) {
            paymentMethodBankInfo.gone()
            paymentMethodIcon.setImageResource(selectedPaymentMethod.cardType.icon())
            paymentMethodTitle.text = selectedPaymentMethod.detailedLabel()
            paymentMethodLimit.text =
                getString(R.string.payment_method_limit, selectedPaymentMethod.limits.max.toStringWithSymbol())
        }
    }

    private fun paymentMethodLoading() {
        with(binding) {
            paymentMethodTitle.showLoading()
            paymentMethodBankInfo.showLoading()
            paymentMethodLimit.showLoading()
            paymentMethodIcon.resetLoader()
            binding.paymentMethodDetailsRoot.setOnClickListener { }
        }
    }

    private fun showErrorState(errorState: ErrorState) {
        when (errorState) {
            ErrorState.DailyLimitExceeded ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.sb_checkout_daily_limit_title),
                    description = getString(R.string.sb_checkout_daily_limit_blurb)
                )
            ErrorState.WeeklyLimitExceeded ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.sb_checkout_weekly_limit_title),
                    description = getString(R.string.sb_checkout_weekly_limit_blurb)
                )
            ErrorState.YearlyLimitExceeded ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.sb_checkout_yearly_limit_title),
                    description = getString(R.string.sb_checkout_yearly_limit_blurb)
                )
            ErrorState.ExistingPendingOrder ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.sb_checkout_pending_order_title),
                    description = getString(R.string.sb_checkout_pending_order_blurb)
                )
            ErrorState.InsufficientCardFunds ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardInsufficientFunds),
                    description = getString(R.string.msg_cardInsufficientFunds)
                )
            ErrorState.CardBankDeclined ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardBankDecline),
                    description = getString(R.string.msg_cardBankDecline)
                )
            ErrorState.CardDuplicated ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardDuplicate),
                    description = getString(R.string.msg_cardDuplicate)
                )
            ErrorState.CardBlockchainDeclined ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardBlockchainDecline),
                    description = getString(R.string.msg_cardBlockchainDecline)
                )
            ErrorState.CardAcquirerDeclined ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardAcquirerDecline),
                    description = getString(R.string.msg_cardAcquirerDecline)
                )
            ErrorState.CardPaymentNotSupported ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardPaymentNotSupported),
                    description = getString(R.string.msg_cardPaymentNotSupported)
                )
            ErrorState.CardCreateFailed ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardCreateFailed),
                    description = getString(R.string.msg_cardCreateFailed)
                )
            ErrorState.CardPaymentFailed ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardPaymentFailed),
                    description = getString(R.string.msg_cardPaymentFailed)
                )
            ErrorState.CardCreateAbandoned ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardCreateAbandoned),
                    description = getString(R.string.msg_cardCreateAbandoned)
                )
            ErrorState.CardCreateExpired ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardCreateExpired),
                    description = getString(R.string.msg_cardCreateExpired)
                )
            ErrorState.CardCreateBankDeclined ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardCreateBankDeclined),
                    description = getString(R.string.msg_cardCreateBankDeclined)
                )
            ErrorState.CardCreateDebitOnly ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardCreateDebitOnly),
                    description = getString(R.string.msg_cardCreateDebitOnly),
                    button = getString(R.string.sb_checkout_card_debit_only_cta)
                )
            ErrorState.CardPaymentDebitOnly ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardPaymentDebitOnly),
                    description = getString(R.string.msg_cardPaymentDebitOnly)
                )
            ErrorState.CardNoToken ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.title_cardCreateNoToken),
                    description = getString(R.string.msg_cardCreateNoToken)
                )
            is ErrorState.UnhandledHttpError ->
                navigator().showErrorInBottomSheet(
                    title = getString(
                        R.string.common_http_error_with_message,
                        errorState.nabuApiException.getErrorDescription()
                    ),
                    description = errorState.nabuApiException.getErrorDescription()
                )
            ErrorState.InternetConnectionError ->
                navigator().showErrorInBottomSheet(
                    title = getString(R.string.executing_connection_error),
                    description = getString(R.string.something_went_wrong_try_again)
                )
            ErrorState.ApproveBankInvalid,
            ErrorState.ApprovedBankAccountInvalid,
            ErrorState.ApprovedBankDeclined,
            ErrorState.ApprovedBankExpired,
            ErrorState.ApprovedBankFailed,
            ErrorState.ApprovedBankFailedInternal,
            ErrorState.ApprovedBankInsufficientFunds,
            ErrorState.ApprovedBankLimitedExceed,
            ErrorState.ApprovedBankRejected,
            is ErrorState.PaymentFailedError,
            ErrorState.ProviderIsNotSupported,
            is ErrorState.ApprovedBankUndefinedError,
            ErrorState.BankLinkingTimeout,
            ErrorState.Card3DsFailed,
            ErrorState.UnknownCardProvider,
            ErrorState.LinkedBankNotSupported -> throw IllegalStateException(
                "Error $errorState should not presented here"
            )
        }.exhaustive
    }

    private fun renderGooglePayPayment(selectedPaymentMethod: PaymentMethod.GooglePay) {
        with(binding) {
            paymentMethodBankInfo.gone()
            paymentMethodIcon.setImageResource(R.drawable.google_pay_mark)
            paymentMethodTitle.text = selectedPaymentMethod.detailedLabel()
            paymentMethodLimit.gone()
        }
        disableRecurringBuyCta(false)
    }

    override fun onPause() {
        super.onPause()
        model.process(SimpleBuyIntent.NavigationHandled)
    }

    override fun onIntervalSelected(interval: RecurringBuyFrequency) {
        model.process(SimpleBuyIntent.RecurringBuyIntervalUpdated(interval))
        binding.recurringBuyCta.text = interval.toHumanReadableRecurringBuy(requireContext())
    }

    override fun onActionInfoTriggered() {
        infoActionCallback()
    }

    override fun onSheetClosed() {
        model.process(SimpleBuyIntent.ClearError)
    }

    override fun onSheetClosed(sheet: SlidingModalBottomDialog<*>) {
        super<TransactionFlowInfoHost>.onSheetClosed(sheet)
        if (sheet is TransactionFlowInfoBottomSheet) {
            analytics.logEvent(InfoBottomSheetDismissed(AssetAction.Buy))
        }
    }

    override fun onPaymentMethodChanged(paymentMethod: PaymentMethod) {
        model.process(SimpleBuyIntent.PaymentMethodChangeRequested(paymentMethod))
        if (paymentMethod.canBeUsedForPaying())
            analytics.logEvent(
                BuyPaymentMethodSelected(
                    paymentMethod.toNabuAnalyticsString()
                )
            )

        when (paymentMethod) {
            is PaymentMethod.UndefinedCard -> {
                analytics.logEvent(SettingsAnalytics.LinkCardClicked(LaunchOrigin.BUY))
            }

            is PaymentMethod.UndefinedBankAccount -> {
                analytics.logEvent(BankTransferClicked(fiatCurrency = fiatCurrency))
            }

            else -> {
            }
        }
    }

    private fun addPaymentMethod(type: PaymentMethodType, fiatCurrency: FiatCurrency) {
        when (type) {
            PaymentMethodType.PAYMENT_CARD -> {
                val intent = Intent(activity, CardDetailsActivity::class.java)
                startActivityForResult(intent, ADD_CARD_REQUEST_CODE)
            }
            PaymentMethodType.FUNDS -> {
                showBottomSheet(
                    WireTransferAccountDetailsBottomSheet.newInstance(
                        fiatCurrency
                    )
                )
            }
            PaymentMethodType.BANK_TRANSFER -> {
                model.process(SimpleBuyIntent.LinkBankTransferRequested)
            }
            else -> {
            }
        }
        analytics.logEvent(PaymentMethodSelected(type.toAnalyticsString()))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_CARD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val preselectedId =
                (data?.extras?.getSerializable(CardDetailsActivity.CARD_KEY) as? PaymentMethod.Card)?.id
            updatePaymentMethods(preselectedId)
        }
        if (requestCode == BankAuthActivity.LINK_BANK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val preselectedId = data?.extras?.getString(BankAuthActivity.LINKED_BANK_ID_KEY)
            updatePaymentMethods(preselectedId)
        }
        if (requestCode == SimpleBuyActivity.KYC_STARTED) {
            if (resultCode == SimpleBuyActivity.RESULT_KYC_SIMPLE_BUY_COMPLETE) {
                model.process(SimpleBuyIntent.KycCompleted)
                navigator().goToKycVerificationScreen()
            } else if (resultCode == KycNavHostActivity.RESULT_KYC_FOR_SDD_COMPLETE) {
                model.process(
                    SimpleBuyIntent.UpdatePaymentMethodsAndAddTheFirstEligible(
                        fiatCurrency
                    )
                )
            }
        }
    }

    private fun updatePaymentMethods(preselectedId: String?) {
        model.process(
            SimpleBuyIntent.FetchSuggestedPaymentMethod(
                fiatCurrency,
                preselectedId
            )
        )
    }

    private fun canUseCreditCards(): Boolean {
        val paymentMethods = lastState?.paymentOptions?.availablePaymentMethods

        paymentMethods?.filterIsInstance<PaymentMethod.UndefinedCard>()?.forEach { card ->
            card.cardFundSources?.let { it ->
                val sources = it.toHashSet()
                // Credit cards are supported by default, unless they are explicitly missing from CardFundSources
                return sources.isEmpty() ||
                    (sources.size == 1 && sources.contains(CardFundSource.UNKNOWN)) ||
                    sources.contains(CardFundSource.CREDIT)
            }
        }
        return true
    }

    companion object {
        private const val ARG_CRYPTO_ASSET = "CRYPTO"
        private const val ARG_PAYMENT_METHOD_ID = "PAYMENT_METHOD_ID"
        private const val ARG_AMOUNT = "AMOUNT"

        fun newInstance(
            asset: AssetInfo,
            preselectedMethodId: String? = null,
            preselectedAmount: String? = null
        ): SimpleBuyCryptoFragment {
            return SimpleBuyCryptoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CRYPTO_ASSET, asset.networkTicker)
                    preselectedMethodId?.let { putString(ARG_PAYMENT_METHOD_ID, preselectedMethodId) }
                    preselectedAmount?.let { putString(ARG_AMOUNT, preselectedAmount) }
                }
            }
        }
    }

    private enum class PaymentMethodsChooserState {
        AVAILABLE_TO_PAY, AVAILABLE_TO_ADD
    }

    private fun TransactionErrorState.message(state: SimpleBuyState): String =
        when (this) {
            TransactionErrorState.BELOW_MIN_LIMIT ->
                resources.getString(R.string.minimum_buy, state.limits.minAmount.toStringWithSymbol())
            TransactionErrorState.INSUFFICIENT_FUNDS -> resources.getString(
                R.string.not_enough_funds, state.fiatCurrency.displayTicker
            )
            TransactionErrorState.OVER_SILVER_TIER_LIMIT,
            TransactionErrorState.OVER_GOLD_TIER_LIMIT -> resources.getString(
                R.string.over_your_limit
            )
            TransactionErrorState.BELOW_MIN_PAYMENT_METHOD_LIMIT -> resources.getString(
                R.string.minimum_buy, state.limits.minAmount.toStringWithSymbol()
            )
            TransactionErrorState.ABOVE_MAX_PAYMENT_METHOD_LIMIT -> resources.getString(
                R.string.maximum_with_value, state.limits.maxAmount.toStringWithSymbol()
            )
            else -> resources.getString(R.string.empty)
        }

    private fun SimpleBuyState.errorStateShouldBeIndicated() =
        errorState != TransactionErrorState.NONE && amount.isPositive
}

fun RecurringBuyFrequency.toHumanReadableRecurringBuy(context: Context): String {
    return when (this) {
        RecurringBuyFrequency.ONE_TIME -> context.getString(R.string.recurring_buy_one_time_short)
        RecurringBuyFrequency.DAILY -> context.getString(R.string.recurring_buy_daily_1)
        RecurringBuyFrequency.WEEKLY -> context.getString(R.string.recurring_buy_weekly_1)
        RecurringBuyFrequency.BI_WEEKLY -> context.getString(R.string.recurring_buy_bi_weekly_1)
        RecurringBuyFrequency.MONTHLY -> context.getString(R.string.recurring_buy_monthly_1)
        else -> context.getString(R.string.common_unknown)
    }
}

fun RecurringBuyFrequency.toHumanReadableRecurringDate(context: Context, dateTime: ZonedDateTime): String {
    return when (this) {
        RecurringBuyFrequency.DAILY -> {
            context.getString(
                R.string.recurring_buy_frequency_subtitle_each_day,
                dateTime.to12HourFormat()
            )
        }
        RecurringBuyFrequency.BI_WEEKLY, RecurringBuyFrequency.WEEKLY -> {
            context.getString(
                R.string.recurring_buy_frequency_subtitle,
                dateTime.dayOfWeek.toString().capitalizeFirstChar()
            )
        }
        RecurringBuyFrequency.MONTHLY -> {
            if (dateTime.isLastDayOfTheMonth()) {
                context.getString(R.string.recurring_buy_frequency_subtitle_monthly_last_day)
            } else {
                context.getString(
                    R.string.recurring_buy_frequency_subtitle_monthly,
                    dateTime.dayOfMonth.toString()
                )
            }
        }
        RecurringBuyFrequency.ONE_TIME,
        RecurringBuyFrequency.UNKNOWN -> ""
    }
}
