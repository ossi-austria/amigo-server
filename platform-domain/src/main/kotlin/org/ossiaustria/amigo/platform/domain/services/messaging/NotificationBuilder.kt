package org.ossiaustria.amigo.platform.domain.services.messaging

import org.ossiaustria.amigo.platform.domain.models.AlbumShare
import org.ossiaustria.amigo.platform.domain.models.Call
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.Sendable

object NotificationBuilder {

    const val TYPE = "type"
    const val ACTION = "action"
    const val ENTITY_ID = "entity_id"
    const val RECEIVER_ID = "receiver_id"

    const val ACTION_SENT = "sent"

    const val TYPE_ALBUM_SHARE = "album_share"
    const val TYPE_MESSAGE = "message"
    const val TYPE_MULTIMEDIA = "multimedia"
    const val TYPE_CALL = "call"


    fun <S> buildSendableSent(sendable: Sendable<S>): HashMap<String, String> {
        return hashMapOf(
            TYPE to chooseSendableType(sendable),
            ACTION to ACTION_SENT,
            ENTITY_ID to sendable.id.toString(),
            RECEIVER_ID to sendable.receiverId.toString(),
        )
    }

    private fun <S> chooseSendableType(sendable: Sendable<S>): String = when (sendable) {
        is AlbumShare -> TYPE_ALBUM_SHARE
        is Call -> TYPE_CALL
        is Message -> TYPE_MESSAGE
        is Multimedia -> TYPE_MULTIMEDIA
        else -> throw IllegalArgumentException("No TYPE handling for $sendable")
    }
}
