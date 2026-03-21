package io.github.helmy2.mudawama.designsystem.samples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.designsystem.components.MudawamaPrimaryButton
import io.github.helmy2.mudawama.designsystem.components.MudawamaGhostButton
import io.github.helmy2.mudawama.designsystem.components.MudawamaSurfaceCard
import io.github.helmy2.mudawama.designsystem.components.DesignSystemGallery

@Preview(showBackground = true)
@Composable
fun MudawamaPrimaryButtonPreview() {
	MudawamaTheme {
		Column {
			MudawamaPrimaryButton(onClick = {}, text = "Start")
			Spacer(modifier = Modifier.height(8.dp))
			MudawamaPrimaryButton(onClick = {}, text = "Disabled", enabled = false)
			Spacer(modifier = Modifier.height(8.dp))
			MudawamaPrimaryButton(onClick = {}, text = "With Icon", leadingIcon = { Text("✓", color = MudawamaTheme.colors.onPrimary) })
		}
	}
}

@Preview(showBackground = true)
@Composable
fun GhostAndCardPreview() {
	MudawamaTheme {
		Column {
			MudawamaGhostButton(onClick = {}, text = "Cancel")
			Spacer(modifier = Modifier.height(8.dp))
			MudawamaSurfaceCard(onClick = null) {
				Text("Card title")
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun DesignSystemGalleryPreview() {
	DesignSystemGallery()
}

@Preview(showBackground = true)
@Composable
fun MudawamaPrimaryButtonDarkPreview() {
	MudawamaTheme(darkTheme = true) {
        Column {
            MudawamaPrimaryButton(onClick = {}, text = "Start")
            Spacer(modifier = Modifier.height(8.dp))
            MudawamaPrimaryButton(onClick = {}, text = "Disabled", enabled = false)
            Spacer(modifier = Modifier.height(8.dp))
            MudawamaPrimaryButton(onClick = {}, text = "With Icon", leadingIcon = { Text("✓", color = MudawamaTheme.colors.onPrimary) })
        }
	}
}
