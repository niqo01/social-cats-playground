package me.niqo.common

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    Button(onClick = {
        text = "Hello, ${getPlatformName()}"
    }) {
        Text(text)
    }
}