package io.github.helmy2.mudawama.designsystem.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

fun Modifier.mudawamaButtonSemantics(description: String?, enabled: Boolean): Modifier =
    if (description != null) this.semantics { this.contentDescription = description } else this


