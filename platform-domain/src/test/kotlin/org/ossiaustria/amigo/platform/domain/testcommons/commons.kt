package org.ossiaustria.amigo.platform.domain.testcommons

import org.assertj.core.api.AssertionsForClassTypes
import org.assertj.core.api.ObjectAssert
import org.hamcrest.Matcher
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import java.util.UUID
import java.util.UUID.randomUUID


internal fun <T> then(actual: T, matcher: Matcher<T>) = org.hamcrest.MatcherAssert.assertThat(actual, matcher)
internal fun <T> then(actual: T): ObjectAssert<T> = AssertionsForClassTypes.assertThat(actual)


internal object Mocks {

    private var groups: Int = 1
    private var accounts: Int = 1
    private var persons: Int = 1

    fun group(id: UUID = randomUUID(), name: String = "Group $groups") = Group(id, name).also {
        groups++
    }

    fun account(id: UUID = randomUUID(), email: String = "user-${accounts}@example.com") =
        Account(id, email, "password").also {
            accounts++
        }

    fun person(
        id: UUID = randomUUID(),
        accountId: UUID = randomUUID(),
        groupId: UUID = randomUUID(),
        name: String = "Person $persons",
        memberType: MembershipType = MembershipType.MEMBER,
    ) =
        Person(id, accountId, name, groupId, memberType).also {
            persons++
        }

    fun multimedia(
        id: UUID = randomUUID(),
        ownerId: UUID,
        filename: String = "filename",
    ): Multimedia {
        return Multimedia(id, ownerId = ownerId, filename = filename, type = MultimediaType.IMAGE)
    }
}
