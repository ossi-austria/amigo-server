package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.NfcInfo
import org.ossiaustria.amigo.platform.domain.models.enums.NfcInfoType
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID


internal class NfcInfoRepositoryTest : AbstractRepositoryTest<NfcInfo, NfcInfoRepository>() {

    @Autowired
    override lateinit var repository: NfcInfoRepository

    override fun initTest() {
        initGroupAccountPerson()
    }

    override fun cleanTables() {
        repository.deleteAll()
        super.cleanTables()
    }

    override fun createDefaultEntityPair(id: UUID): Pair<UUID, NfcInfo> {
        val entity = NfcInfo(id, person.id, person.id, NfcInfoType.LOGIN, "name")
        return Pair(id, entity)
    }

    override fun changeEntity(entity: NfcInfo) = entity.copy(type = NfcInfoType.OPEN_ALBUM)


}
