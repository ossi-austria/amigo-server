package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Call
import org.ossiaustria.amigo.platform.domain.models.enums.CallType
import org.springframework.beans.factory.annotation.Autowired
import java.util.*


internal class CallRepositoryTest : AbstractRepositoryTest<Call, CallRepository>() {

    @Autowired
    override lateinit var repository: CallRepository

    override fun initTest() {
        initGroupAccountPerson()
    }

    override fun cleanTables() {
        repository.deleteAll()
        super.cleanTables()
    }

    override fun createDefaultEntityPair(id: UUID): Pair<UUID, Call> {
        val entity = Call(id, person.id, person.id, CallType.VIDEO)
        return Pair(id, entity)
    }

    override fun changeEntity(entity: Call) = entity.copy(senderToken = "sender")


}
