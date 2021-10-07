package org.ossiaustria.amigo.platform.domain.repositories

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.config.NoopMessagingConfig
import org.ossiaustria.amigo.platform.domain.config.ApplicationProfiles
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

@TestPropertySource("classpath:application-test.yml")
@SpringBootTest
@ActiveProfiles(ApplicationProfiles.TEST)
@ComponentScan("org.ossiaustria.amigo.platform.domain")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ContextConfiguration(classes = [NoopMessagingConfig::class])
internal abstract class AbstractWithJpaTest {

    @Autowired
    internal lateinit var persons: PersonRepository

    @Autowired
    internal lateinit var groups: GroupRepository

    @Autowired
    internal lateinit var accounts: AccountRepository

    fun cleanTables() {
        persons.deleteAll()
        groups.deleteAll()
        accounts.deleteAll()
    }

    protected fun violatesConstraints(f: () -> Unit) {
        assertThrows<DataIntegrityViolationException> { f.invoke() }
    }

}
