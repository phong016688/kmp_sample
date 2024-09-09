package com.github.jetbrains.rssreader.androidApp.composeui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.jetbrains.rssreader.core.entity.CompressResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CompressResultList(
    modifier: Modifier,
    compressResult: List<CompressResult>,
    listState: LazyListState,
    lastTime: Long,
    onClick: (CompressResult) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        state = listState
    ) {
        itemsIndexed(compressResult) { i, result ->
            if (i == 0) Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            CompressResultItem(result, lastTime, compressResult.size) { onClick(result) }
            if (i != compressResult.size - 1) Spacer(modifier = Modifier.size(16.dp))
        }
    }
}

private val dateFormatter = SimpleDateFormat("HH:dd/MM/yyyy", Locale.getDefault())

@Composable
fun CompressResultItem(
    item: CompressResult,
    lastTime: Long,
    total: Int,
    onClick: () -> Unit,
) {
    val padding = 8.dp
    Box {
        Card(
            elevation = 16.dp,
            shape = RoundedCornerShape(padding)
        ) {
            Column(
                modifier = Modifier
                    .clickable(onClick = onClick)
                    .fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.size(padding))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(start = padding, end = padding),
                        style = MaterialTheme.typography.body1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = dateFormatter.format(Date(item.currencyId))
                    )
                    Text(
                        modifier = Modifier.padding(start = padding, end = padding),
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = dateFormatter.format(Date(lastTime))
                    )
                }
                Spacer(modifier = Modifier.size(padding))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(start = padding, end = padding),
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = getDistanceColor(item, total),
                        text = String.format(Locale.getDefault(), "%.2f", item.distance / total)
                    )
                    Text(
                        modifier = Modifier.padding(start = padding, end = padding),
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = String.format(Locale.getDefault(), "%.5f", item.cosine)
                    )
                }
                Spacer(modifier = Modifier.size(padding))
            }
        }
    }
}

@Composable
private fun getDistanceColor(
    item: CompressResult,
    total: Int
) = if (item.distance / total < 10) Color(201, 56, 42, 255) else Color.Black