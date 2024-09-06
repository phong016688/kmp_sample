package com.github.jetbrains.rssreader.androidApp.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CompressSettingIcon(
    modifier: Modifier,
    selected: String,
    onClick: (() -> Unit)
) {
    Box(
        modifier = modifier
            .height(60.dp)
            .padding(start = 4.dp, end = 4.dp)
            .clip(RoundedCornerShape(10))
    ) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(10))
                .align(Alignment.Center)
                .background(color = Color(75, 98, 66, 255))
                .clickable(onClick = onClick)
                .fillMaxWidth()

        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colors.onPrimary,
                style = MaterialTheme.typography.subtitle1,
                text = selected.lowercase()
            )
        }
    }
}

@Preview
@Composable
private fun CompressSettingIconPreview() {
    AppTheme {
        CompressSettingIcon(modifier = Modifier.height(40.dp), selected = "1h") { }
    }
}