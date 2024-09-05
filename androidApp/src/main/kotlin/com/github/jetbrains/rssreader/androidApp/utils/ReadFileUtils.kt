package com.github.jetbrains.rssreader.androidApp.utils

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

fun readTextFileFromRaw(context: Context, rawResId: Int): List<String> {
    val inputStream = context.resources.openRawResource(rawResId)
    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
    return bufferedReader.use { it.readLines() }
}