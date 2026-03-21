package io.github.helmy2.mudawama.designsystem

import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals

class TokensTest {
    @Test
    fun primaryColor_isExact() {
        assertEquals(0xFF02594F.toInt(), DeepTeal.value.toInt())
    }

    @Test
    fun typography_buttonSize_is14sp() {
        val size = MudawamaTypography().button.fontSize
        assertEquals(14.sp, size)
    }
}

