package com.blockchain.componentlib.tablerow

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.blockchain.componentlib.tag.TagViewState
import com.blockchain.componentlib.tag.TagsRow
import com.blockchain.componentlib.theme.AppTheme

@Composable
fun BalanceTableRow(
    titleStart: AnnotatedString,
    titleEnd: AnnotatedString? = null,
    bodyStart: AnnotatedString,
    bodyEnd: AnnotatedString? = null,
    startIconUrl: String,
    tags: List<TagViewState>,
    onClick: () -> Unit,
) {
    TableRow(
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                TableRowText(
                    startText = titleStart,
                    endText = titleEnd,
                    textStyle = AppTheme.typography.body2,
                    textColor = AppTheme.colors.title,
                )
                TableRowText(
                    startText = bodyStart,
                    endText = bodyEnd,
                    textStyle = AppTheme.typography.paragraph1,
                    textColor = AppTheme.colors.body,
                )
            }
        },
        contentStart = {
            Image(
                painter = rememberImagePainter(
                    data = startIconUrl,
                    builder = {
                        crossfade(true)
                        placeholder(ColorDrawable(AppTheme.colors.light.toArgb()))
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 16.dp)
                    .size(24.dp)
            )
        },
        contentBottom = {
            if (!tags.isNullOrEmpty()) {
                TagsRow(
                    tags = tags,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        onContentClicked = onClick,
    )
}