package org.ossiaustria.amigo.platform.domain.models.enums

enum class CallState {
    CALLING, // Notification sent, should display Calling window
    CANCELLED, // caller cancels
    DENIED, // callee denies/cancels
    ACCEPTED, // callee accepted

    //    STARTED, // both parties entered the room
    FINISHED, // success to finish
    TIMEOUT, // timeout or technical timeout
}
