package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import org.springframework.beans.factory.annotation.Autowired
import java.util.*


internal class MultimediaRepositoryTest : AbstractRepositoryTest<Multimedia, MultimediaRepository>() {

    @Autowired
    override lateinit var repository: MultimediaRepository

    override fun initTest() {
        initGroupAccountPerson()
    }

    override fun cleanTables() {
        repository.deleteAll()
        super.cleanTables()
    }

    override fun createDefaultEntityPair(id: UUID): Pair<UUID, Multimedia> {
        val entity = Multimedia(id, person.id, MultimediaType.IMAGE, "filename")
        return Pair(id, entity)
    }

    override fun changeEntity(entity: Multimedia) = entity.copy(filename = "changed")

}
