package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.LoginToken
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID


internal class LoginTokenRepositoryTest : AbstractRepositoryTest<LoginToken, LoginTokenRepository>() {

    @Autowired
    override lateinit var repository: LoginTokenRepository

    override fun initTest() {
        initGroupAccountPerson()
    }

    override fun cleanTables() {
        repository.deleteAll()
        super.cleanTables()
    }

    override fun createDefaultEntityPair(id: UUID): Pair<UUID, LoginToken> {
        val entity = LoginToken(id, person.id, "token")
        return Pair(id, entity)
    }

    override fun changeEntity(entity: LoginToken) =
        entity.copy(token = "changed-${System.currentTimeMillis()}")


}
