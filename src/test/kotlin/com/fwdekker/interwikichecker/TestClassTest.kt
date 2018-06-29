package com.fwdekker.interwikichecker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal object TestClassTest {
    @Test
    fun fooTest() {
        assertThat(TestClass().foo()).isEqualTo(3)
    }

    @Test
    fun fooTestFail() {
        assertThat(TestClass().foo()).isEqualTo(4)
    }
}
