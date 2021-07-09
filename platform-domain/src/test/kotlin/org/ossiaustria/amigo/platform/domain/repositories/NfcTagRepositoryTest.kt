package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.NfcTag
import org.ossiaustria.amigo.platform.domain.models.enums.NfcTagType
import org.springframework.beans.factory.annotation.Autowired
import java.util.*


internal class NfcTagRepositoryTest : AbstractRepositoryTest<NfcTag, NfcTagRepository>() {

    @Autowired
    override lateinit var repository: NfcTagRepository

    override fun initTest() {
        initGroupAccountPerson()
    }

    override fun cleanTables() {
        repository.deleteAll()
        super.cleanTables()
    }

    override fun createDefaultEntityPair(id: UUID): Pair<UUID, NfcTag> {
        val entity = NfcTag(id, person, person, NfcTagType.PERSON)
        return Pair(id, entity)
    }

    override fun changeEntity(entity: NfcTag) = entity.copy(type = NfcTagType.MULTIMEDIA)


}
