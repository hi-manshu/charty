package com.himanshoe.charty

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Main entry point for the Charty library
 */
@Composable
fun Charty(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {
        Text("Charty Library - Compose Multiplatform")
    }
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

