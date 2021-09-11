package org.ossiaustria.amigo.platform.domain.models.enums

enum class MembershipType(private val level: Int) {
    OWNER(10),
    ADMIN(9),
    ANALOGUE(4),
    MEMBER(2);

    fun isAtLeast(type: MembershipType): Boolean {
        return this.level >= type.level
    }
}