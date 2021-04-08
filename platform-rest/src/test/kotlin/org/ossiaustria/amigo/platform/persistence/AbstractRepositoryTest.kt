package org.ossiaustria.amigo.platform.persistence

import org.ossiaustria.amigo.platform.ApplicationProfiles
import org.ossiaustria.amigo.platform.testcommons.TestPostgresContainer
import org.ossiaustria.amigo.platform.testcommons.TestRedisContainer
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.sql.DataSource

@TestPropertySource("classpath:application-integration-test.yml")
@SpringBootTest
@ActiveProfiles(ApplicationProfiles.INTEGRATION_TEST)
@ContextConfiguration(initializers = [TestPostgresContainer.Initializer::class, TestRedisContainer.Initializer::class])
class AbstractRepositoryTest {
    @Autowired
    val dataSource: DataSource? = null

    @Autowired
    val jdbcTemplate: JdbcTemplate? = null

    @Autowired
    val entityManager: EntityManager? = null



    protected fun truncateAllTables() {
        truncateDbTables(listOf(
            "account", "account_token",

            "subject",
        ), cascade = true)
    }

    @Transactional
    protected fun truncateDbTables(tables: List<String>, cascade: Boolean = true) {
        println("Truncating tables: $tables")
        val joinToString = tables.joinToString("\", \"", "\"", "\"")

        val createNativeQuery = if (cascade) {
            entityManager!!.createNativeQuery("truncate table $joinToString CASCADE ")

        } else {
            entityManager!!.createNativeQuery("truncate table $joinToString ")
        }
        entityManager!!.joinTransaction()
        createNativeQuery.executeUpdate()
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
