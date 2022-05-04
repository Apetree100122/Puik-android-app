package piuk.blockchain.android.ui.settings.v2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.blockchain.analytics.events.AnalyticsEvents
import com.blockchain.api.services.ContactPreference
import com.blockchain.blockchaincard.domain.models.BlockchainCard
import com.blockchain.blockchaincard.domain.models.BlockchainCardProduct
import com.blockchain.blockchaincard.ui.BlockchainCardFragment
import com.blockchain.commonarch.presentation.base.BlockchainActivity
import com.blockchain.commonarch.presentation.base.FlowFragment
import com.blockchain.commonarch.presentation.base.SlidingModalBottomDialog
import com.blockchain.commonarch.presentation.base.addAnimationTransaction
import com.blockchain.componentlib.databinding.ToolbarGeneralBinding
import com.blockchain.componentlib.navigation.NavigationBarButton
import com.blockchain.core.payments.LinkedPaymentMethod
import com.blockchain.featureflag.FeatureFlag
import com.blockchain.koin.notificationPreferencesFeatureFlag
import com.blockchain.koin.scopedInject
import com.blockchain.nabu.BasicProfileInfo
import com.blockchain.nabu.Tier
import com.blockchain.nabu.datamanagers.custodialwalletimpl.PaymentMethodType
import com.blockchain.walletconnect.ui.dapps.DappsListFragment
import info.blockchain.balance.FiatCurrency
import piuk.blockchain.android.R
import piuk.blockchain.android.databinding.ActivitySettingsBinding
import piuk.blockchain.android.support.SupportCentreActivity
import piuk.blockchain.android.ui.addresses.AddressesActivity
import piuk.blockchain.android.ui.airdrops.AirdropCentreActivity
import piuk.blockchain.android.ui.dashboard.model.LinkablePaymentMethodsForAction
import piuk.blockchain.android.ui.debug.FeatureFlagsHandlingActivity
import piuk.blockchain.android.ui.kyc.limits.KycLimitsActivity
import piuk.blockchain.android.ui.settings.v2.account.AccountFragment
import piuk.blockchain.android.ui.settings.v2.notificationpreferences.NotificationPreferencesAnalyticsEvents
import piuk.blockchain.android.ui.settings.v2.notificationpreferences.NotificationPreferencesFragment
import piuk.blockchain.android.ui.settings.v2.notificationpreferences.details.NotificationPreferenceDetailsFragment
import piuk.blockchain.android.ui.settings.v2.notifications.NotificationsFragment
import piuk.blockchain.android.ui.settings.v2.profile.ProfileActivity
import piuk.blockchain.android.ui.settings.v2.security.SecurityFragment
import piuk.blockchain.android.ui.settings.v2.security.password.PasswordChangeFragment
import piuk.blockchain.android.ui.settings.v2.security.pin.PinActivity
import piuk.blockchain.android.ui.thepit.PitPermissionsActivity

class SettingsActivity : BlockchainActivity(), SettingsNavigator {

    private val notificationReworkFeatureFlag: FeatureFlag by scopedInject(notificationPreferencesFeatureFlag)

    private val binding: ActivitySettingsBinding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    override val alwaysDisableScreenshots: Boolean = true

    override val toolbarBinding: ToolbarGeneralBinding
        get() = binding.toolbar

    private val startFor2Fa by lazy {
        intent.getBooleanExtra(START_FOR_2FA, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar()

        supportFragmentManager.beginTransaction()
            .addAnimationTransaction()
            .replace(
                binding.settingsContentFrame.id, RedesignSettingsFragment.newInstance()
            )
            .commitNowAllowingStateLoss()

        if (startFor2Fa) {
            goToSecurity()
        }
    }

    private fun setupToolbar() {
        updateToolbar(
            toolbarTitle = getString(R.string.toolbar_settings),
            backAction = { onBackPressed() }
        )
        setupSupportButton()
    }

    private fun setupSupportButton() {
        updateToolbarMenuItems(
            listOf(
                NavigationBarButton.Icon(R.drawable.ic_support_chat) {
                    analytics.logEvent(AnalyticsEvents.Support)
                    startActivity(SupportCentreActivity.newIntent(this))
                }
            )
        )
    }

    override fun goToAboutApp() {
        replaceCurrentFragment(AboutAppFragment.newInstance())
    }

    override fun goToPasswordChange() {
        replaceCurrentFragment(PasswordChangeFragment.newInstance())
    }

    override fun goToPinChange() {
        startActivity(
            PinActivity.newIntent(
                context = this,
                startForResult = false,
                originScreen = PinActivity.Companion.OriginScreenToPin.CHANGE_PIN_SECURITY,
                addFlagsToClear = false
            )
        )
    }

    override fun goToProfile(basicProfileInfo: BasicProfileInfo, tier: Tier) {
        startActivity(ProfileActivity.newIntent(this, basicProfileInfo, tier))
    }

    override fun goToAccount() {
        replaceCurrentFragment(AccountFragment.newInstance())
    }

    override fun goToNotifications() {
        if (notificationReworkFeatureFlag.isEnabled) {
            analytics.logEvent(NotificationPreferencesAnalyticsEvents.NotificationClicked)
            replaceCurrentFragment(NotificationPreferencesFragment.newInstance())
        } else {
            replaceCurrentFragment(NotificationsFragment.newInstance())
        }
    }

    override fun goToNotificationPreferences() {
        replaceCurrentFragment(NotificationPreferencesFragment.newInstance())
    }

    override fun goToNotificationPreferencesDetails(preference: ContactPreference) {
        replaceCurrentFragment(NotificationPreferenceDetailsFragment.newInstance(preference))
    }

    override fun goToSecurity() {
        replaceCurrentFragment(SecurityFragment.newInstance())
    }

    override fun goToFeatureFlags() {
        startActivity(FeatureFlagsHandlingActivity.newIntent(this))
    }

    override fun goToSupportCentre() {
        startActivity(SupportCentreActivity.newIntent(this))
    }

    override fun goToAirdrops() {
        startActivity(AirdropCentreActivity.newIntent(this))
    }

    override fun goToAddresses() {
        startActivity(AddressesActivity.newIntent(this))
    }

    override fun goToExchange() {
        PitPermissionsActivity.start(this, "")
    }

    override fun goToKycLimits() {
        startActivity(KycLimitsActivity.newIntent(this))
    }

    override fun goToWalletConnect() {
        replaceCurrentFragment(DappsListFragment.newInstance())
    }

    override fun goToOrderBlockchainCard(cardProduct: BlockchainCardProduct) {
        replaceCurrentFragment(BlockchainCardFragment.newInstance(cardProduct))
    }

    override fun goToManageBlockchainCard(blockchainCard: BlockchainCard) {
        replaceCurrentFragment(BlockchainCardFragment.newInstance(blockchainCard))
    }

    private fun replaceCurrentFragment(newFragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .addAnimationTransaction()
            .replace(
                binding.settingsContentFrame.id, newFragment, newFragment::class.simpleName
            ).addToBackStack(newFragment::class.simpleName)
            .commitAllowingStateLoss()
    }

    companion object {
        const val BASIC_INFO = "basic_info_user"
        const val USER_TIER = "user_tier"
        private const val START_FOR_2FA = "START_FOR_2FA"

        const val SETTINGS_RESULT_DATA = "SETTINGS_RESULT_DATA"

        enum class SettingsAction {
            Addresses,
            Exchange,
            Airdrops,
            WebLogin,
            Logout
        }

        fun newIntent(context: Context, startFor2Fa: Boolean = false): Intent =
            Intent(context, SettingsActivity::class.java).apply {
                putExtra(START_FOR_2FA, startFor2Fa)
            }
    }
}

interface SettingsNavigator {
    fun goToAboutApp()
    fun goToProfile(basicProfileInfo: BasicProfileInfo, tier: Tier)
    fun goToAccount()
    fun goToNotifications()
    fun goToNotificationPreferences()
    fun goToSecurity()
    fun goToFeatureFlags()
    fun goToSupportCentre()
    fun goToAirdrops()
    fun goToAddresses()
    fun goToWalletConnect()
    fun goToExchange()
    fun goToKycLimits()
    fun goToPasswordChange()
    fun goToPinChange()
    fun goToOrderBlockchainCard(cardProduct: BlockchainCardProduct)
    fun goToManageBlockchainCard(blockchainCard: BlockchainCard)
    fun goToNotificationPreferencesDetails(preference: ContactPreference)
}

interface SettingsScreen : FlowFragment {
    fun navigator(): SettingsNavigator
}

interface BankLinkingHost : SlidingModalBottomDialog.Host {
    fun onBankWireTransferSelected(currency: FiatCurrency)
    fun onLinkBankSelected(paymentMethodForAction: LinkablePaymentMethodsForAction)
}

data class LinkablePaymentMethods(
    val currency: FiatCurrency,
    val linkMethods: List<PaymentMethodType>
) : java.io.Serializable

data class BankItem(
    val bank: LinkedPaymentMethod.Bank,
    val canBeUsedToTransact: Boolean
)
