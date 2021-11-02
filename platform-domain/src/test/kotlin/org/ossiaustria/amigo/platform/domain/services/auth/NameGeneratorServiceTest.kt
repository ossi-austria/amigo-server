package org.ossiaustria.amigo.platform.domain.services.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.services.NameGeneratorService

internal class NameGeneratorServiceTest() {

    private val subject = NameGeneratorService()


    @Test
    fun `generateName should have 2 dashes`() {
        for (i in 0..100) {
            val result = subject.generateName()
            println(result)
            assertThat(result).isNotNull
            assertThat(result).isNotBlank
            assertThat(result.length).isGreaterThan(8)
            assertThat(result.length).isLessThan(30)
        }
    }

}
