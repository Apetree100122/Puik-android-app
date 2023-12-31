package com.blockchain.data.appversion

import com.blockchain.core.walletoptions.SemanticVersion
import org.junit.Assert
import org.junit.Test

class SemanticVersionTest {
    @Test
    fun `version 1 should be greater than 2`() {
        val version1 = "2.21.3"
        val version2 = "1.22.4"
        Assert.assertTrue(SemanticVersion(version1) > SemanticVersion(version2))
    }

    @Test
    fun `version 1 should be equal with 2`() {
        val version1 = "2.21.3"
        val version2 = "2.21.3"
        Assert.assertTrue(SemanticVersion(version1) == SemanticVersion(version2))
    }
}
