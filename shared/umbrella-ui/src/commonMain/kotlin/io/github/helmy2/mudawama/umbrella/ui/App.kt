package io.github.helmy2.mudawama.umbrella.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.core.domain.platform

@Composable
fun App() {
    Scaffold {
        Card(
            modifier = Modifier.padding(it).padding(16.dp)
        ) {
            Text(
                "Hello from ${platform()}", modifier = Modifier.padding(16.dp)
            )
        }
    }
}