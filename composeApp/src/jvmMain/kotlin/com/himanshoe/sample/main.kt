@file:Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "FunctionNaming",
    "MatchingDeclarationName",
    "PreviewPublic",
    "ModifierMissing",
)

package com.himanshoe.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "chartyv3",
        ) {
            App()
        }
    }
