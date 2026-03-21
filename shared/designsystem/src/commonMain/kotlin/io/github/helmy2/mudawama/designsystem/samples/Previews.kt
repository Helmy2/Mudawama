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
import io.github.helmy2.mudawama.designsystem.components.PrimaryButton

@Preview(showBackground = true)
@Composable
fun PrimaryButtonPreview() {
	MudawamaTheme {
		Column {
			PrimaryButton(onClick = {}, text = "Start")
			Spacer(modifier = Modifier.height(8.dp))
			PrimaryButton(onClick = {}, text = "Disabled", enabled = false)
			Spacer(modifier = Modifier.height(8.dp))
			PrimaryButton(onClick = {}, text = "With Icon", leadingIcon = { Text("✓", color = MudawamaTheme.colors.onPrimary) })
		}
	}
}

@Preview(showBackground = true)
@Composable
fun PrimaryButtonDarkPreview() {
	MudawamaTheme(darkTheme = true) {
        Column {
            PrimaryButton(onClick = {}, text = "Start")
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(onClick = {}, text = "Disabled", enabled = false)
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(onClick = {}, text = "With Icon", leadingIcon = { Text("✓", color = MudawamaTheme.colors.onPrimary) })
        }
	}
}
