package com.blockchain.logging

import org.junit.Test

class NullLoggerCoverageTest {

    private val nullLogger: ILogger = NullLogger

    @Test
    fun `does not crash - tests for coverage`() {
        nullLogger.d("d")
        nullLogger.v("v")
        nullLogger.e("e")
    }
}
