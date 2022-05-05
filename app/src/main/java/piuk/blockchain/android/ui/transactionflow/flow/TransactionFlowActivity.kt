package piuk.blockchain.android.ui.transactionflow.flow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import com.blockchain.coincore.AssetAction
import com.blockchain.coincore.BlockchainAccount
import com.blockchain.coincore.NullCryptoAccount
import com.blockchain.coincore.SingleAccount
import com.blockchain.coincore.TransactionTarget
import com.blockchain.coincore.impl.CustodialTradingAccount
import com.blockchain.commonarch.presentation.base.SlidingModalBottomDialog
import com.blockchain.commonarch.presentation.base.addAnimationTransaction
import com.blockchain.commonarch.presentation.mvi.MviActivity
import com.blockchain.componentlib.alert.SnackbarType
import com.blockchain.componentlib.databinding.ToolbarGeneralBinding
import com.blockchain.componentlib.navigation.NavigationBarButton
import com.blockchain.componentlib.viewextensions.gone
import com.blockchain.componentlib.viewextensions.hideKeyboard
import com.blockchain.componentlib.viewextensions.visible
import com.blockchain.logging.RemoteLogger
import com.blockchain.preferences.DashboardPrefs
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.koin.android.ext.android.inject
import org.koin.core.scope.Scope
import org.koin.java.KoinJavaComponent
import piuk.blockchain.android.R
import piuk.blockchain.android.campaign.CampaignType
import piuk.blockchain.android.databinding.ActivityTransactionFlowBinding
import piuk.blockchain.android.ui.customviews.BlockchainSnackbar
import piuk.blockchain.android.ui.dashboard.sheets.KycUpgradeNowSheet
import piuk.blockchain.android.ui.kyc.navhost.KycNavHostActivity
import piuk.blockchain.android.ui.transactionflow.analytics.TxFlowAnalytics
import piuk.blockchain.android.ui.transactionflow.engine.TransactionIntent
import piuk.blockchain.android.ui.transactionflow.engine.TransactionModel
import piuk.blockchain.android.ui.transactionflow.engine.TransactionState
import piuk.blockchain.android.ui.transactionflow.engine.TransactionStep
import piuk.blockchain.android.ui.transactionflow.flow.customisations.BackNavigationState
import piuk.blockchain.android.ui.transactionflow.flow.customisations.TransactionFlowCustomisations
import piuk.blockchain.android.ui.transactionflow.transactionFlowActivityScope
import piuk.blockchain.android.util.getAccount
import piuk.blockchain.android.util.getTarget
import piuk.blockchain.android.util.putAccount
import piuk.blockchain.android.util.putTarget
import timber.log.Timber

class TransactionFlowActivity :
    MviActivity<TransactionModel, TransactionIntent, TransactionState, ActivityTransactionFlowBinding>(),
    SlidingModalBottomDialog.Host,
    KycUpgradeNowSheet.Host {

    private val scopeId: String by lazy {
        "${TX_SCOPE_ID}_${this@TransactionFlowActivity.hashCode()}"
    }

    val scope: Scope by lazy {
        openScope()
        KoinJavaComponent.getKoin().getScope(scopeId)
    }

    override val model: TransactionModel by scope.inject()

    override val alwaysDisableScreenshots: Boolean
        get() = false

    override val toolbarBinding: ToolbarGeneralBinding
        get() = binding.toolbar

    private val analyticsHooks: TxFlowAnalytics by inject()
    private val customiser: TransactionFlowCustomisations by inject()
    private val remoteLogger: RemoteLogger by inject()
    private val dashboardPrefs: DashboardPrefs by inject()

    private val sourceAccount: SingleAccount by lazy {
        intent.extras?.getAccount(SOURCE) as? SingleAccount ?: kotlin.run {
            remoteLogger.logException(IllegalStateException(), "No source account specified for action $action")
            NullCryptoAccount()
        }
    }

    private val transactionTarget: TransactionTarget by lazy {
        intent.extras?.getTarget(TARGET) ?: kotlin.run {
            remoteLogger.logException(IllegalStateException(), "No target account specified for action $action")
            NullCryptoAccount()
        }
    }

    private val action: AssetAction by lazy {
        intent.extras?.getSerializable(ACTION) as? AssetAction ?: throw IllegalStateException("No action specified")
    }

    private val startKycForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        startModel()
    }

    private val compositeDisposable = CompositeDisposable()
    private var currentStep: TransactionStep = TransactionStep.ZERO
    private lateinit var state: TransactionState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        updateToolbar(
            menuItems = listOf(
                NavigationBarButton.Icon(R.drawable.ic_close) { finish() }
            ),
            backAction = { onBackPressed() }
        )
        binding.txProgress.visible()
        startModel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tx_flow, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun initBinding(): ActivityTransactionFlowBinding =
        ActivityTransactionFlowBinding.inflate(layoutInflater)

    private fun startModel() {
        val intentMapper = TransactionFlowIntentMapper(
            sourceAccount = sourceAccount,
            target = transactionTarget,
            action = action
        )

        compositeDisposable += sourceAccount.requireSecondPassword()
            .map { intentMapper.map(it) }
            .subscribeBy(
                onSuccess = { transactionIntent ->
                    model.process(transactionIntent)
                },
                onError = {
                    Timber.e("Unable to configure transaction flow, aborting. e == $it")
                    BlockchainSnackbar.make(
                        binding.root, getString(R.string.common_error), type = SnackbarType.Error
                    ).show()
                    finish()
                }
            )
    }

    override fun render(newState: TransactionState) {
        handleStateChange(newState)
        state = newState
    }

    private fun handleStateChange(state: TransactionState) {
        if (currentStep == state.currentStep) {
            return
        }

        when (state.currentStep) {
            TransactionStep.ZERO -> {
                // do nothing
            }
            TransactionStep.CLOSED -> dismissFlow()
            else -> analyticsHooks.onStepChanged(state)
        }

        state.currentStep.takeIf { it != TransactionStep.ZERO }?.let { step ->
            showFlowStep(step)
            customiser.getScreenTitle(state).takeIf { it.isNotEmpty() }?.let {
                updateToolbarTitle(it)
            } ?: updateToolbar()

            currentStep = step
        }

        if (!state.canGoBack) {
            updateToolbarBackAction(null)
        } else {
            updateToolbarBackAction { onBackPressed() }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateOnBackPressed { finish() }
                true
            }
            R.id.action_close -> {
                dismissFlow()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onBackPressed() {
        navigateOnBackPressed { finish() }
    }

    private fun navigateOnBackPressed(finalAction: () -> Unit) {
        if (::state.isInitialized && state.canGoBack) {
            when (customiser.getBackNavigationAction(state)) {
                BackNavigationState.ClearTransactionTarget -> {
                    model.process(TransactionIntent.ClearSelectedTarget)
                    model.process(TransactionIntent.ReturnToPreviousStep)
                }
                BackNavigationState.ResetPendingTransaction -> {
                    hideKeyboard()
                    model.process(TransactionIntent.InvalidateTransaction)
                }
                BackNavigationState.ResetPendingTransactionKeepingTarget -> {
                    hideKeyboard()
                    binding.txProgress.visible()
                    model.process(TransactionIntent.InvalidateTransactionKeepingTarget)
                }
                BackNavigationState.NavigateToPreviousScreen -> model.process(TransactionIntent.ReturnToPreviousStep)
            }
        } else {
            finalAction()
        }
    }

    private fun showFlowStep(step: TransactionStep) {
        when (step) {
            TransactionStep.ZERO,
            TransactionStep.CLOSED -> null
            TransactionStep.NOT_ELIGIBLE -> KycUpgradeNowSheet.newInstance()
            TransactionStep.ENTER_PASSWORD -> EnterSecondPasswordFragment.newInstance()
            TransactionStep.SELECT_SOURCE -> SelectSourceAccountFragment.newInstance()
            TransactionStep.ENTER_ADDRESS -> EnterTargetAddressFragment.newInstance()
            TransactionStep.ENTER_AMOUNT -> {
                checkRemainingSendAttemptsWithoutBackup()
                EnterAmountFragment.newInstance()
            }
            TransactionStep.SELECT_TARGET_ACCOUNT -> SelectTargetAccountFragment.newInstance()
            TransactionStep.CONFIRM_DETAIL -> ConfirmTransactionFragment.newInstance()
            TransactionStep.IN_PROGRESS -> TransactionProgressFragment.newInstance()
        }?.let {
            binding.txProgress.gone()

            val transaction = supportFragmentManager.beginTransaction()
                .addAnimationTransaction()
                .replace(R.id.tx_flow_content, it, it.toString())

            if (!supportFragmentManager.fragments.contains(it)) {
                transaction.addToBackStack(it.toString())
            }

            transaction.commit()
        }
    }

    private fun checkRemainingSendAttemptsWithoutBackup() {
        if (state.action == AssetAction.Send &&
            state.sendingAccount is CustodialTradingAccount &&
            dashboardPrefs.remainingSendsWithoutBackup > 0
        ) {
            dashboardPrefs.remainingSendsWithoutBackup = dashboardPrefs.remainingSendsWithoutBackup - 1
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissFlow()
    }

    private fun dismissFlow() {
        compositeDisposable.clear()
        model.destroy()
        if (scope.isNotClosed()) {
            scope.close()
        }
        if (this.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            finish()
        }
    }

    private fun openScope() =
        try {
            KoinJavaComponent.getKoin().getOrCreateScope(
                scopeId,
                transactionFlowActivityScope
            )
        } catch (e: Throwable) {
            Timber.wtf("Error opening scope for id $scopeId - $e")
        }

    override fun startKycClicked() {
        val campaign = when (action) {
            AssetAction.Swap -> CampaignType.Swap
            AssetAction.Buy -> CampaignType.SimpleBuy
            AssetAction.InterestDeposit -> CampaignType.Interest
            AssetAction.InterestWithdraw -> CampaignType.Interest
            else -> CampaignType.None
        }
        startKycForResult.launch(KycNavHostActivity.newIntent(this, campaign))
    }

    override fun onSheetClosed() {
        // do nothing
    }

    companion object {
        private const val SOURCE = "SOURCE_ACCOUNT"
        private const val TARGET = "TARGET_ACCOUNT"
        private const val ACTION = "ASSET_ACTION"
        private const val TX_SCOPE_ID = "TRANSACTION_ACTIVITY_SCOPE_ID"

        fun newIntent(
            context: Context,
            sourceAccount: BlockchainAccount = NullCryptoAccount(),
            target: TransactionTarget = NullCryptoAccount(),
            action: AssetAction
        ): Intent {
            val bundle = Bundle().apply {
                putAccount(SOURCE, sourceAccount)
                putTarget(TARGET, target)
                putSerializable(ACTION, action)
            }

            return Intent(context, TransactionFlowActivity::class.java).apply {
                putExtras(bundle)
            }
        }
    }
}
