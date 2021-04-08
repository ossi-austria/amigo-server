package org.ossiaustria.amigo.platform.testcommons

import org.ossiaustria.amigo.platform.domain.models.Person
import java.util.*
import java.util.UUID.randomUUID

class EntityMocks {
    companion object {
        val authorId = randomUUID()
        val author = person(id = authorId, slug = "slug_author")
        fun person(id: UUID = randomUUID(), slug: String = "slug" + randomUUID()) = Person(id, slug, randomUUID())

    }
}
