package com.blockchain.nfts.help.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.blockchain.componentlib.basic.Image
import com.blockchain.componentlib.basic.ImageResource
import com.blockchain.componentlib.button.MinimalButton
import com.blockchain.componentlib.sheets.SheetNub
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.componentlib.theme.Grey400
import com.blockchain.componentlib.theme.Grey700
import com.blockchain.nfts.R

@Composable
fun NftHelpScreen(onBuyClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            SheetNub(modifier = Modifier.padding(AppTheme.dimensions.tinySpacing))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AppTheme.dimensions.smallSpacing,
                    top = AppTheme.dimensions.verySmallSpacing,
                    end = AppTheme.dimensions.smallSpacing,
                    bottom = AppTheme.dimensions.standardSpacing
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.nft_help_title),
                style = AppTheme.typography.title3,
                color = AppTheme.colors.title
            )

            Spacer(modifier = Modifier.size(AppTheme.dimensions.largeSpacing))

            Image(ImageResource.Local(R.drawable.ic_nft_help))

            Spacer(modifier = Modifier.size(AppTheme.dimensions.smallSpacing))

            Text(
                text = stringResource(R.string.nft_help_buy_title),
                style = AppTheme.typography.title2,
                color = AppTheme.colors.title
            )

            Spacer(modifier = Modifier.size(AppTheme.dimensions.smallestSpacing))

            Text(
                text = stringResource(R.string.nft_help_buy_description),
                style = AppTheme.typography.paragraph1,
                color = Grey700
            )

            Spacer(modifier = Modifier.size(AppTheme.dimensions.standardSpacing))

            MinimalButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.nft_help_buy_cta_opensea),
                icon = ImageResource.Local(
                    id = R.drawable.ic_opensea,
                    size = AppTheme.dimensions.standardSpacing
                ),
                onClick = onBuyClick
            )

            Spacer(modifier = Modifier.size(AppTheme.dimensions.standardSpacing))

            Instructions()
        }
    }
}

@Composable
fun Instructions() {
    val instructions = remember {
        listOf(
            Instruction(
                title = R.string.nft_help_instructions_1_title
            ),
            Instruction(
                title = R.string.nft_help_instructions_2_title
            ),
            Instruction(
                title = R.string.nft_help_instructions_3_title,
                description = R.string.nft_help_instructions_3_description
            ),
            Instruction(
                title = R.string.nft_help_instructions_4_title,
                description = R.string.nft_help_instructions_4_description
            ),
            Instruction(
                title = R.string.nft_help_instructions_5_title,
                description = R.string.nft_help_instructions_5_description
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = AppTheme.dimensions.borderSmall,
                color = AppTheme.colors.light,
                shape = RoundedCornerShape(AppTheme.dimensions.borderRadiiMedium)
            )
    ) {
        Text(
            modifier = Modifier.padding(AppTheme.dimensions.smallSpacing),
            text = stringResource(R.string.nft_help_instructions),
            style = AppTheme.typography.paragraph2,
            color = AppTheme.colors.title
        )

        Separator()

        instructions.forEachIndexed { index, instruction ->
            InstructionItem(
                number = index + 1,
                title = stringResource(instruction.title),
                description = instruction.description?.let { stringResource(it) }
            )

            if (index < instructions.lastIndex) {
                Separator()
            }
        }
    }
}

@Composable
fun InstructionItem(
    number: Int,
    title: String,
    description: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.dimensions.smallSpacing)
    ) {
        Text(
            modifier = Modifier
                .size(AppTheme.dimensions.standardSpacing)
                .clip(CircleShape)
                .background(AppTheme.colors.light),
            style = AppTheme.typography.body2,
            color = Grey400,
            text = number.toString(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(AppTheme.dimensions.smallSpacing))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = AppTheme.typography.paragraph2,
                color = AppTheme.colors.title
            )

            description?.let {
                Spacer(modifier = Modifier.size(AppTheme.dimensions.smallestSpacing))

                Text(
                    text = description,
                    style = AppTheme.typography.paragraph1,
                    color = Grey700
                )
            }
        }
    }
}

@Composable
fun Separator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppTheme.dimensions.borderSmall)
            .background(AppTheme.colors.light)
    )
}

private data class Instruction(
    @StringRes val title: Int,
    @StringRes val description: Int? = null
)

// ///////////////
// PREVIEWS
// ///////////////

@Preview(showBackground = true)
@Composable
fun PreviewNftHelpScreen() {
    NftHelpScreen(onBuyClick = {})
}
