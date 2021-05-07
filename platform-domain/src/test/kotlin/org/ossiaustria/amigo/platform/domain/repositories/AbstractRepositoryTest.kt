package org.ossiaustria.amigo.platform.domain.repositories

import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.domain.ApplicationProfiles
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.sql.DataSource

@TestPropertySource("classpath:application-test.yml")
@SpringBootTest
@ActiveProfiles(ApplicationProfiles.TEST)
@ComponentScan("org.ossiaustria.amigo.platform.domain")
@AutoConfigureTestDatabase(connection = org.springframework.boot.jdbc.EmbeddedDatabaseConnection.H2)
class AbstractRepositoryTest {

    @Autowired
    val dataSource: DataSource? = null

    @Autowired
    val jdbcTemplate: JdbcTemplate? = null

    @Autowired
    val entityManager: EntityManager? = null


    protected fun truncateAllTables() {
        truncateDbTables(
            listOf(
                "account",
                "person",
            ), cascade = true
        )
    }

    @Transactional
    protected fun truncateDbTables(tables: List<String>, cascade: Boolean = true) {
        println("Truncating tables: $tables")
        println("Truncating tables: $tables")
        val joinToString = tables.joinToString("\", \"", "\"", "\"")

        try {
            val createNativeQuery = if (cascade) {
                entityManager!!.createNativeQuery("truncate table $joinToString CASCADE ")
            } else {
                entityManager!!.createNativeQuery("truncate table $joinToString ")
            }
            entityManager!!.joinTransaction()
            createNativeQuery.executeUpdate()
        } catch (e: Exception) {
            println(e)
        }

    }

    fun commitAndFail(f: () -> Unit) {
        assertThrows<Exception> {
            withinTransaction {
                f.invoke()
            }
        }
    }

    fun <T> withinTransaction(commit: Boolean = true, func: () -> T): T {
        if (!TestTransaction.isActive()) TestTransaction.start()
        val result = func.invoke()
        if (commit) {
            TestTransaction.flagForCommit()
        } else {
            TestTransaction.flagForRollback()
        }
        try {
            TestTransaction.end()
        } catch (e: Exception) {
            throw e
        }
        return result
    }
}
