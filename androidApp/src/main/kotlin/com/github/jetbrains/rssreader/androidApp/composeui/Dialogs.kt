package com.github.jetbrains.rssreader.androidApp.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.jetbrains.rssreader.androidApp.R

@Composable
fun CompressSettingDialog(
    data: List<String>,
    onSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) = Dialog(
    onDismissRequest = onDismiss
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.select),
            style = MaterialTheme.typography.h4
        )
        Spacer(modifier = Modifier.size(16.dp))
        LazyColumn {
            itemsIndexed(data) { index, item ->
                Box(
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                        .clip(CircleShape)
                        .background(color = Color(58, 98, 59, 255))
                        .clickable(onClick = {
                            onSelected(index)
                            onDismiss()
                        })
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colors.onPrimary,
                        text = item,
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CompressSettingIconPreview() {
    AppTheme {
        CompressSettingDialog(data = listOf("aaa", "aaaa", "aaaa"), onSelected = {}, onDismiss = {})
    }
}