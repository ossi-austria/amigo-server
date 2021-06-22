package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.ApplicationConfiguration
import org.ossiaustria.amigo.platform.domain.repositories.AbstractWithJpaTest
import org.ossiaustria.amigo.platform.domain.testcommons.Models
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

internal abstract class AbstractServiceTest : AbstractWithJpaTest() {

    @Autowired
    lateinit var config: ApplicationConfiguration

    val existingId: UUID = UUID.randomUUID()

    val personId1: UUID = UUID.randomUUID()
    val personId2: UUID = UUID.randomUUID()
    val personId3: UUID = UUID.randomUUID()

    val groupId1: UUID = UUID.randomUUID()
    val groupId2: UUID = UUID.randomUUID()


    protected fun mockPersons() {
        groups.save(Models.group(groupId1))
        groups.save(Models.group(groupId2))
        accounts.save(Models.account()).also {
            persons.save(Models.persons(personId1, it.id, groupId1))
        }
        accounts.save(Models.account()).also {
            persons.save(Models.persons(personId2, it.id, groupId1))
        }
        accounts.save(Models.account()).also {
            persons.save(Models.persons(personId3, it.id, groupId2))
        }
    }
}
