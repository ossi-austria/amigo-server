package org.ossiaustria.amigo.platform.service

import org.ossiaustria.amigo.platform.ApplicationConfiguration
import org.ossiaustria.amigo.platform.persistence.AbstractRepositoryTest
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractServiceTest : AbstractRepositoryTest() {

    @Autowired
    lateinit var config: ApplicationConfiguration

}
