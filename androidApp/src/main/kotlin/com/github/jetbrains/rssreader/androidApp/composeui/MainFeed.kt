package com.github.jetbrains.rssreader.androidApp.composeui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.jetbrains.rssreader.app.CurrencyStore
import com.github.jetbrains.rssreader.core.entity.Currency
import com.github.jetbrains.rssreader.core.entity.Feed

@Composable
fun MainFeed(
    store: CurrencyStore,
    onPostClick: (Currency) -> Unit,
) {
    val state = store.observeState().collectAsState()
    val currencies = remember(state.value.currencies) {
        state.value.currencies
    }
    Column {
        val coroutineScope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        PostList(
            modifier = Modifier.weight(1f),
            currencies = currencies,
            listState = listState
        ) { currency -> onPostClick(currency) }
        Spacer(
            Modifier
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .fillMaxWidth()
        )
    }
}

private sealed class Icons {
    object All : Icons()
    class FeedIcon(val feed: Feed) : Icons()
    object Edit : Icons()
}

@Composable
fun MainFeedBottomBar(
    feeds: List<Feed>,
    selectedFeed: Feed?,
    onFeedClick: (Feed?) -> Unit,
    onEditClick: () -> Unit
) {
    val items = buildList {
        add(Icons.All)
        addAll(feeds.map { Icons.FeedIcon(it) })
        add(Icons.Edit)
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        this.items(items) { item ->
            when (item) {
                is Icons.All -> FeedIcon(
                    feed = null,
                    isSelected = selectedFeed == null,
                    onClick = { onFeedClick(null) }
                )

                is Icons.FeedIcon -> FeedIcon(
                    feed = item.feed,
                    isSelected = selectedFeed == item.feed,
                    onClick = { onFeedClick(item.feed) }
                )

                is Icons.Edit -> EditIcon(onClick = onEditClick)
            }
            Spacer(modifier = Modifier.size(16.dp))
        }
    }
}