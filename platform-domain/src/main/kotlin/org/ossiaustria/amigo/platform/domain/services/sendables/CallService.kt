package org.ossiaustria.amigo.platform.domain.services.sendables

import org.ossiaustria.amigo.platform.domain.models.Call
import org.ossiaustria.amigo.platform.domain.models.enums.CallState
import org.ossiaustria.amigo.platform.domain.models.enums.CallType
import org.ossiaustria.amigo.platform.domain.repositories.CallRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.services.jitsi.JitsiJwtService
import org.ossiaustria.amigo.platform.domain.services.messaging.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*
import java.util.UUID.randomUUID

interface CallService : SendableService<Call> {
    fun createCall(senderId: UUID, receiverId: UUID, callType: CallType): Call
    fun denyCall(call: Call): Call
    fun cancelCall(call: Call): Call
    fun acceptCall(call: Call): Call
    fun finishCall(call: Call): Call
    fun timeoutCall(call: Call): Call
    fun count(): Long
}

@Service
class CallServiceImpl : CallService {

    @Autowired
    private lateinit var repository: CallRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var notificationService: NotificationService

    @Autowired
    private lateinit var jitsiJwtService: JitsiJwtService

    private val wrapper: SendableServiceMixin<Call> by lazy { SendableServiceMixin(repository, personRepository) }

    override fun createCall(senderId: UUID, receiverId: UUID, callType: CallType): Call {

        val (sender, receiver) = wrapper.validateSenderReceiver(senderId, receiverId)
        val call = Call(
            id = randomUUID(),
            senderId = senderId,
            receiverId = receiverId,
            callType = callType,
            createdAt = ZonedDateTime.now(),
            retrievedAt = null,
            sentAt = null,
        )

        Log.info("createCall: senderId=$senderId receiverId=$receiverId -> $callType")

        val senderToken = jitsiJwtService.generateToken(call.id.toString(), sender, receiver)
        val receiverToken = jitsiJwtService.generateToken(call.id.toString(), receiver, sender)

        val callWithTokens = call.copy(senderToken = senderToken, receiverToken = receiverToken)
        return if (sendNotification(callWithTokens, receiverId)) {
            repository.save(callWithTokens.copy(sentAt = ZonedDateTime.now(), callState = CallState.CALLING))
        } else {
            repository.save(callWithTokens)
        }

    }

    override fun cancelCall(call: Call): Call =
        sendNotificationAndPersist(call.cancel(), call.receiverId)

    override fun denyCall(call: Call): Call =
        sendNotificationAndPersist(call.deny(), call.senderId)

    override fun acceptCall(call: Call): Call =
        sendNotificationAndPersist(call.accept(), call.senderId)


    /**
     * Finish and timout must be sent to both parties
     */
    override fun finishCall(call: Call): Call {
        val finishedCall = sendNotificationAndPersist(call.finish(), call.senderId)
        sendNotification(finishedCall, call.receiverId)
        return finishedCall
    }

    override fun timeoutCall(call: Call): Call {
        val timeoutCall = sendNotificationAndPersist(call.timeout(), call.senderId)
        sendNotification(timeoutCall, call.receiverId)
        return timeoutCall
    }

    private fun sendNotificationAndPersist(call: Call, notificationReceiverId: UUID): Call {
        sendNotification(call, notificationReceiverId)
        return repository.save(call)
    }


    private fun sendNotification(call: Call, notificationReceiverId: UUID): Boolean {
        val success = notificationService.callChanged(notificationReceiverId, call)
        if (success) {
            Log.info("notify Call: ${call.id}-${call.callState} senderId=${call.senderId} receiverId=${call.receiverId} -> ${call.sentAt}")
        } else {
            Log.warn("could not notify for Call: ${call.id}-${call.callState} senderId=${call.senderId} receiverId=${call.receiverId}")
        }
        return success
    }


    override fun getOne(id: UUID): Call = wrapper.getOne(id)

    override fun getAll(): List<Call> = wrapper.getAll()

    override fun findWithPersons(senderId: UUID?, receiverId: UUID?) =
        wrapper.findWithPersons(senderId, receiverId)

    override fun findWithSender(senderId: UUID) = wrapper.findWithSender(senderId)

    override fun findWithReceiver(receiverId: UUID) = wrapper.findWithReceiver(receiverId)

    @Deprecated("Use acceptCall() or denyCall() explicitly")
    override fun markAsRetrieved(id: UUID, time: ZonedDateTime) = wrapper.markAsRetrieved(id, time)

    override fun count(): Long = repository.count()

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
    }
}