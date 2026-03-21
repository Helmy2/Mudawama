package io.github.helmy2.mudawama.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme

@Composable
fun DesignSystemGallery(modifier: Modifier = Modifier) {
    MudawamaTheme {
        Column(modifier = modifier) {
            PrimaryButton(onClick = {}, text = "Start")
            Spacer(modifier = Modifier.height(8.dp))
            MudawamaGhostButton(onClick = {}, text = "Cancel")
            Spacer(modifier = Modifier.height(8.dp))
            MudawamaSurfaceCard(onClick = null) {
                Text("Card title", modifier = Modifier)
            }
        }
    }
}

