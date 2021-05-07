package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.ApplicationConfiguration
import org.ossiaustria.amigo.platform.domain.repositories.AbstractRepositoryTest
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractServiceTest : AbstractRepositoryTest() {

    @Autowired
    lateinit var config: ApplicationConfiguration

}
