package piuk.blockchain.android.ui.dashboard.onboarding

import com.blockchain.commonarch.presentation.mvi.MviIntent
import com.blockchain.domain.paymentmethods.model.PaymentMethodType
import piuk.blockchain.android.domain.usecases.CompletableDashboardOnboardingStep
import piuk.blockchain.android.domain.usecases.DashboardOnboardingStep

sealed class DashboardOnboardingIntent : MviIntent<DashboardOnboardingState> {

    object FetchSteps : DashboardOnboardingIntent() {
        override fun reduce(oldState: DashboardOnboardingState): DashboardOnboardingState = oldState
    }

    data class FetchStepsSuccess(
        private val steps: List<CompletableDashboardOnboardingStep>
    ) : DashboardOnboardingIntent() {
        override fun reduce(oldState: DashboardOnboardingState): DashboardOnboardingState = oldState.copy(
            steps = steps
        )
    }

    data class StepClicked(val clickedStep: DashboardOnboardingStep) : DashboardOnboardingIntent() {
        override fun reduce(oldState: DashboardOnboardingState): DashboardOnboardingState = oldState
    }

    data class FetchFailed(private val error: Throwable) : DashboardOnboardingIntent() {
        override fun reduce(oldState: DashboardOnboardingState): DashboardOnboardingState = oldState.copy(
            errorState = DashboardOnboardingError.Error(error)
        )
    }

    data class PaymentMethodClicked(val type: PaymentMethodType) : DashboardOnboardingIntent() {
        override fun reduce(oldState: DashboardOnboardingState): DashboardOnboardingState = oldState
    }

    data class NavigateTo(private val navigationAction: DashboardOnboardingNavigationAction) :
        DashboardOnboardingIntent() {
        override fun reduce(oldState: DashboardOnboardingState): DashboardOnboardingState = oldState.copy(
            navigationAction = navigationAction
        )
    }

    object ClearNavigation : DashboardOnboardingIntent() {
        override fun reduce(oldState: DashboardOnboardingState): DashboardOnboardingState =
            oldState.copy(navigationAction = DashboardOnboardingNavigationAction.None)
    }

    object ClearError : DashboardOnboardingIntent() {
        override fun reduce(oldState: DashboardOnboardingState): DashboardOnboardingState = oldState.copy(
            errorState = DashboardOnboardingError.None
        )
    }
}
