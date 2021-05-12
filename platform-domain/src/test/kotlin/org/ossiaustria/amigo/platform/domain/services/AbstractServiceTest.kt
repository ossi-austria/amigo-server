package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.ApplicationConfiguration
import org.ossiaustria.amigo.platform.domain.repositories.AbstractWithJpaTest
import org.springframework.beans.factory.annotation.Autowired

internal abstract class AbstractServiceTest : AbstractWithJpaTest() {

    @Autowired
    lateinit var config: ApplicationConfiguration

}
