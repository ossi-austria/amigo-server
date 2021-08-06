package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.ApplicationConfiguration
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.repositories.AbstractWithJpaTest
import org.ossiaustria.amigo.platform.domain.testcommons.Mocks
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

internal abstract class AbstractServiceTest : AbstractWithJpaTest() {

    @Autowired
    lateinit var config: ApplicationConfiguration

    val existingId: UUID = UUID.randomUUID()

    val personId1: UUID = UUID.randomUUID()
    val personId2: UUID = UUID.randomUUID()
    val personId3: UUID = UUID.randomUUID()

    lateinit var person1: Person
    lateinit var person2: Person
    lateinit var person3: Person

    val groupId1: UUID = UUID.randomUUID()
    val groupId2: UUID = UUID.randomUUID()

    protected fun mockPersons() {
        groups.save(Mocks.group(groupId1))
        groups.save(Mocks.group(groupId2))
        accounts.save(Mocks.account()).also {
            person1 = persons.save(Mocks.person(personId1, it.id, groupId1))
        }
        accounts.save(Mocks.account()).also {
            person2 = persons.save(Mocks.person(personId2, it.id, groupId1))
        }
        accounts.save(Mocks.account()).also {
            person3 = persons.save(Mocks.person(personId3, it.id, groupId2))
        }
    }
}
