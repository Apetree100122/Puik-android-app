package piuk.blockchain.android.ui.settings.v2.notificationpreferences.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.blockchain.componentlib.basic.ComposeColors
import com.blockchain.componentlib.basic.ComposeGravities
import com.blockchain.componentlib.basic.ComposeTypographies
import com.blockchain.componentlib.basic.SimpleText
import com.blockchain.componentlib.button.MinimalButton
import com.blockchain.componentlib.button.PrimaryButton
import com.blockchain.componentlib.theme.AppTheme
import piuk.blockchain.android.R

@Preview
@Composable
fun PreferenceLoadingErrorPreview() {
    PreferenceLoadingError({ }, { })
}

@Composable
fun PreferenceLoadingError(onRetryClicked: () -> Unit, onBackClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = dimensionResource(id = R.dimen.epic_margin))
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(AppTheme.dimensions.paddingLarge)
        ) {

            SimpleText(
                text = stringResource(id = R.string.settings_notification_error),
                style = ComposeTypographies.Title3,
                color = ComposeColors.Title,
                gravity = ComposeGravities.Centre
            )

            SimpleText(
                text = stringResource(id = R.string.settings_notification_error_details),
                style = ComposeTypographies.Caption1,
                color = ComposeColors.Body,
                gravity = ComposeGravities.Centre
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(AppTheme.dimensions.paddingLarge),
        ) {
            PrimaryButton(
                text = stringResource(id = R.string.retry),
                onClick = onRetryClicked,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.size(AppTheme.dimensions.paddingSmall))

            MinimalButton(
                text = stringResource(id = R.string.settings_notification_error_back),
                onClick = onBackClicked,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
