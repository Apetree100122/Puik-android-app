package piuk.blockchain.android.ui.dashboard.announcements

import android.text.Spannable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.dashboard.model.DashboardItem
import piuk.blockchain.android.ui.dashboard.model.DashboardItem.Companion.ANNOUNCEMENT_INDEX

interface AnnouncementCard : DashboardItem {
    val name: String
    val dismissKey: String
    override val index: Int
        get() = ANNOUNCEMENT_INDEX
    override val id: String
        get() = name
}

class StandardAnnouncementCard(
    override val name: String,
    val dismissRule: DismissRule,
    val dismissEntry: DismissRecorder.DismissEntry,
    @StringRes val titleText: Int = 0,
    val title: String? = null,
    @StringRes val bodyText: Int = 0,
    val body: String? = null,
    val bodyTextSpannable: Spannable? = null,
    @StringRes val ctaText: Int = 0,
    @StringRes val dismissText: Int = 0,
    @DrawableRes val background: Int = 0,
    @DrawableRes val iconImage: Int = 0,
    val iconUrl: String = "",
    @ColorRes val buttonColor: Int = R.color.default_announce_button,
    val shouldWrapIconWidth: Boolean = false,
    private val ctaFunction: () -> Unit = { },
    private val dismissFunction: () -> Unit = { },
    val titleFormatParams: Array<String> = emptyArray(),
    val bodyFormatParams: Array<String> = emptyArray(),
    val ctaFormatParams: Array<String> = emptyArray()
) : AnnouncementCard {
    fun ctaClicked() {
        dismissEntry.done()
        ctaFunction.invoke()
    }

    fun dismissClicked() {
        dismissEntry.dismiss(dismissRule)
        dismissFunction.invoke()
    }

    override val dismissKey: String
        get() = dismissEntry.prefsKey
}

class MiniAnnouncementCard(
    override val name: String,
    val dismissRule: DismissRule,
    val dismissEntry: DismissRecorder.DismissEntry,
    @StringRes val titleText: Int = 0,
    @StringRes val bodyText: Int = 0,
    @DrawableRes val iconImage: Int = 0,
    @DrawableRes val background: Int = 0,
    private val ctaFunction: () -> Unit = { },
    val hasCta: Boolean
) : AnnouncementCard {
    fun ctaClicked() {
        ctaFunction.invoke()
    }

    override val dismissKey: String
        get() = ""
}

class ComponentAnnouncementCard(
    override val name: String,
    val dismissRule: DismissRule,
    val dismissEntry: DismissRecorder.DismissEntry,
    @StringRes val headerText: Int = 0,
    val header: String? = null,
    @StringRes val subHeaderText: Int = 0,
    val subHeader: String? = null,
    @ColorRes val subHeaderColour: Int = 0,
    @StringRes val subHeaderSuffixText: Int = 0,
    @ColorRes val subHeaderSuffixColour: Int = 0,
    @StringRes val titleText: Int = 0,
    val title: String? = null,
    @StringRes val bodyText: Int = 0,
    val body: String? = null,
    val bodyTextSpannable: Spannable? = null,
    @StringRes val ctaText: Int = 0,
    @DrawableRes val background: Int = 0,
    @DrawableRes val iconImage: Int = 0,
    val iconUrl: String = "",
    val buttonColor: Int = R.color.default_announce_button,
    private val ctaFunction: () -> Unit = { },
    private val dismissFunction: () -> Unit = { },
    val headerFormatParams: Array<String> = emptyArray(),
    val subheaderFormatParams: Array<String> = emptyArray(),
    val titleFormatParams: Array<String> = emptyArray(),
    val bodyFormatParams: Array<String> = emptyArray(),
    val ctaFormatParams: Array<String> = emptyArray()
) : AnnouncementCard {
    fun ctaClicked() {
        dismissEntry.done()
        ctaFunction.invoke()
    }

    fun dismissClicked() {
        dismissEntry.dismiss(dismissRule)
        dismissFunction.invoke()
    }

    override val dismissKey: String
        get() = dismissEntry.prefsKey
}
