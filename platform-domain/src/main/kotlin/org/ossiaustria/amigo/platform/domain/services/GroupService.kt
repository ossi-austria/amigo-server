package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.repositories.GroupRepository
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*
import java.util.UUID.randomUUID
import javax.transaction.Transactional

interface GroupService {
    fun findGroupsOfUser(account: Account): List<Group>
    fun findGroup(account: Account, groupId: UUID): Group
    fun filterGroups(account: Account, personId: UUID? = null, name: String? = null): List<Group>
    fun create(id: UUID = randomUUID(), name: String): Group
    fun update(group: Group): Group
    fun findById(id: UUID): Group?
}

@Service
class GroupServiceImpl : GroupService {

    @Autowired
    private lateinit var groupRepository: GroupRepository

    override fun findById(id: UUID) = groupRepository.findByIdOrNull(id)

    @Transactional
    override fun findGroupsOfUser(account: Account): List<Group> {
        val ids = groupIdsOfAccount(account)
        return groupRepository.findByIdIn(ids)
    }

    override fun create(id: UUID, name: String): Group {
        return groupRepository.save(
            Group(
                id = id,
                name = name
            )
        )
    }

    override fun update(group: Group): Group {
        groupRepository.findByIdOrNull(group.id)
            ?: throw NotFoundException(ErrorCode.NotFound, "Group was not yet saved")
        groupRepository.save(group)
        return groupRepository.findByIdOrNull(group.id)!!
    }

    @Transactional
    override fun findGroup(account: Account, groupId: UUID): Group {
        val ids = groupIdsOfAccount(account)
//        val findOneByEmail = accountRepository.findOneByEmail(account.email)
        val group = if (ids.contains(groupId)) {
            groupRepository.findByIdOrNull(groupId)
                ?: throw NotFoundException(ErrorCode.NotFound, "Cannot find Group with id $groupId")
        } else {
            log.warn("User requested group, which they are not a member of: ${account.email}: $groupId")
            throw NotFoundException(ErrorCode.NotFound, "Cannot find Group with id $groupId")
        }
        group.members.size
        return group
    }

    @Transactional
    override fun filterGroups(account: Account, personId: UUID?, name: String?): List<Group> {
        return arrayListOf<Group>().apply {
            if (personId != null) {
                account.persons
                    .filter { it.id == personId }
                    .map { it.groupId }
                    .firstOrNull()
                    ?.let { addAll(listOfNotNull(groupRepository.findByIdOrNull(it))) }
            }
            if (name != null) {
                val ids = account.persons.map { it.groupId }
                addAll(groupRepository.findByIdIn(ids).filter { it.name == name })
            }
        }
    }

    private fun groupIdsOfAccount(account: Account) =
        account.persons.map { it.groupId }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
