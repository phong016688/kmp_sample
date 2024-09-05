package com.github.jetbrains.rssreader.androidApp.composeui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.jetbrains.rssreader.core.entity.Currency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PostList(
    modifier: Modifier,
    currencies: List<Currency>,
    listState: LazyListState,
    onClick: (Currency) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        state = listState
    ) {
        itemsIndexed(currencies) { i, currency ->
            if (i == 0) Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            PostItem(currency) { onClick(currency) }
            if (i != currencies.size - 1) Spacer(modifier = Modifier.size(16.dp))
        }
    }
}

private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

@Composable
fun PostItem(
    item: Currency,
    onClick: () -> Unit
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
                        text = dateFormatter.format(Date(item.openTime))
                    )
                    Text(
                        modifier = Modifier.padding(start = padding, end = padding),
                        style = MaterialTheme.typography.body1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = dateFormatter.format(Date(item.closeTime))
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
                        text = item.openPrice.toString()
                    )
                    Text(
                        modifier = Modifier.padding(start = padding, end = padding),
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = item.closePrice.toString()
                    )
                }
                Spacer(modifier = Modifier.size(padding))
            }
        }
    }
}