package com.mikrochek

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.mikrochek.composables.layout.MainLayout
import com.mikrochek.plugins.configurePlugins
import com.mikrochek.theme.AppTheme
import com.mikrochek.utils.ResourceLoader
import java.io.InputStream

fun main() {
    configurePlugins()
    
    // Load application icon
    val iconStream: InputStream = ResourceLoader.loadIcon("logo.png")
    val iconPainter = BitmapPainter(loadImageBitmap(iconStream))
    
    application {
        val windowState = remember { WindowState(placement = WindowPlacement.Maximized) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "MikroChek",
            state = windowState,
            icon = iconPainter
        ) {
            AppTheme {
                Surface(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainLayout()
                }
            }
        }
    }
}