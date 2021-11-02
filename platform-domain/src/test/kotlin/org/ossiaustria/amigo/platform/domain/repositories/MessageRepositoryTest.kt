package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Message
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID


internal class MessageRepositoryTest : AbstractRepositoryTest<Message, MessageRepository>() {

    @Autowired
    override lateinit var repository: MessageRepository

    override fun initTest() {
        initGroupAccountPerson()
    }

    override fun cleanTables() {
        repository.deleteAll()
        super.cleanTables()
    }

    override fun createDefaultEntityPair(id: UUID): Pair<UUID, Message> {
        val entity = Message(id, person.id, person.id, "text")
        return Pair(id, entity)
    }

    override fun changeEntity(entity: Message) = entity.copy(text = "changed")


}
