package org.ossiaustria.amigo.platform.rest.v1

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName

object TestTags {
    const val SLOW = "slow"
    const val UNIT = "unit"
    const val INTEGRATION = "integration"
    const val RESTDOC = "restdoc"
}

internal fun field(path: String, type: JsonFieldType, description: String = "descr") =
    fieldWithPath(path).type(type).description(description)

internal fun param(path: String, description: String = "descr") =
    parameterWithName(path).description(description)


internal fun personFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        field(prefix + "id", STRING, "Unique UUID of this Person").optional(),
        field(prefix + "accountId", STRING, "Unique UUID the parent Account").optional(),
        field(prefix + "name", STRING, "Fullname of this Person").optional(),
        field(prefix + "groupId", STRING, "References the Group").optional(),
        field(prefix + "memberType", STRING, "MemberType: can be ADMIN, MEMBER, ANALOGE").optional(),
    )
}

internal fun multimediasResponseFields(prefix: String = ""): List<FieldDescriptor> {
    return arrayListOf(
        field(prefix + "id", STRING, "UUID"),
        field(prefix + "ownerId", STRING, "UUID of sending Person"),
        field(prefix + "createdAt", STRING, "LocalDateTime of Multimedia creation"),
        field(prefix + "ownerId", STRING, "UUID of Owner"),
        field(prefix + "filename", STRING, "File to name that file locally"),
        field(prefix + "type", STRING, "MultimediaType: IMAGE, VIDEO, AUDIO"),
        field(prefix + "contentType", STRING, "ContentType / MIME type of that file").optional(),
        field(prefix + "size", JsonFieldType.NUMBER, "Size of file in bytes").optional(),
        field(prefix + "albumId", STRING, "UUID of parent Album").optional(),
    )
}

private fun sortFields(prefix: String = ""): List<FieldDescriptor> {
    return listOf(
        fieldWithPath(prefix + "sort.sorted").type(JsonFieldType.BOOLEAN)
            .description("Is the result sorted. Request parameter 'sort', values '=field,direction(asc,desc)'"),
        fieldWithPath(prefix + "sort.unsorted").type(JsonFieldType.BOOLEAN).description("Is the result unsorted"),
        fieldWithPath(prefix + "sort.empty").type(JsonFieldType.BOOLEAN).description("Is the sort empty")
    )
}




