package com.github.jetbrains.rssreader.androidApp.composeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.jetbrains.rssreader.core.entity.CompressResult
import com.github.jetbrains.rssreader.core.entity.Currency
import com.github.jetbrains.rssreader.core.entity.Feed
import com.github.jetbrains.rssreader.core.entity.Post


@Preview
@Composable
private fun PostPreview() {
    AppTheme {
        CompressResultItem(item = PreviewData.compressResult, lastTime = 0, total = 10,onClick = {})
    }
}


@Preview
@Composable
private fun FeedIconSelectedPreview() {
    AppTheme {}
}

private object PreviewData {
    val post = Post(
        title = "Productive Server-Side Development With Kotlin: Stories From The Industry",
        desc = "Kotlin was created as an alternative to Java, meaning that its application area within the JVM ecosystem was meant to be the same as Java’s. Obviously, this includes server-side development. We would love...",
        imageUrl = "https://blog.jetbrains.com/wp-content/uploads/2020/11/server.png",
        link = "https://blog.jetbrains.com/kotlin/2020/11/productive-server-side-development-with-kotlin-stories/",
        date = 42L
    )
    val currency = Currency(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0.0, 0.0, 0.0, 0.0)
    val compressResult = CompressResult(0.0, 0.0, 12313131313131)
    val feed = Feed(
        title = "Kotlin Blog",
        link = "blog.jetbrains.com/kotlin/",
        desc = "blog.jetbrains.com/kotlin/",
        imageUrl = null,
        posts = listOf(post),
        sourceUrl = "https://blog.jetbrains.com/feed/",
        isDefault = true
    )
}
