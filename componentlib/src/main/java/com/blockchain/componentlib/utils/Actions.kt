package com.blockchain.componentlib.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.blockchain.componentlib.R

fun Context.copyToClipboard(label: String, text: String) {
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).apply {
        ClipData.newPlainText(label, text).also { clipData ->
            setPrimaryClip(clipData)
        }
    }
}

@Composable
fun CopyText(
    label: String = stringResource(id = R.string.app_name),
    textToCopy: String
) {
    LocalContext.current.copyToClipboard(
        label = label,
        text = textToCopy
    )
}

fun Context.openUrl(url: String) {
    openUrl(Uri.parse(url))
}

fun Context.openUrl(url: Uri) {
    startActivity(Intent(Intent.ACTION_VIEW, url))
}

@Composable
fun OpenUrl(
    url: String
) {
    LocalContext.current.openUrl(url = url)
}
