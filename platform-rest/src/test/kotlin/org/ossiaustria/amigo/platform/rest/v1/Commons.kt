package org.ossiaustria.amigo.platform.rest.v1

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.partWithName

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

internal fun part(path: String, description: String = "descr") =
    partWithName(path).description(description)


internal fun personFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        field(prefix + "id", STRING, "Unique UUID").optional(),
        field(prefix + "accountId", STRING, "Unique UUID").optional(),
        field(prefix + "name", STRING, "Unique UUID").optional(),
        field(prefix + "groupId", STRING, "Unique UUID").optional(),
        field(prefix + "memberType", STRING, "Unique UUID").optional(),
    )
}

fun pageableResourceParameters(): Array<ParameterDescriptor> {
    return arrayOf(
        parameterWithName("page").optional().description("Page number (starting from 0)"),
        parameterWithName("size").optional().description("Number elements on the page"),
        parameterWithName("sort").optional().description("Sort by field (eg. &sort=id,asc)")
    )
}

fun wrapToPage(content: List<FieldDescriptor>): List<FieldDescriptor> {
    return mutableListOf(
        fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("Is the last page"),
        fieldWithPath("total_pages").type(JsonFieldType.NUMBER).description("Total pages count"),
        fieldWithPath("total_elements").type(JsonFieldType.NUMBER)
            .description("Total elements count ([pages count] x [page size])"),
        fieldWithPath("size").type(JsonFieldType.NUMBER)
            .description("Requested elements count per page. Request parameter 'size'. Default 20"),
        fieldWithPath("number").type(JsonFieldType.NUMBER).description("Current page number"),
        fieldWithPath("number_of_elements").type(JsonFieldType.NUMBER).description("Elements count in current page"),
        fieldWithPath("first").type(JsonFieldType.BOOLEAN).description("Is the first page"),
        fieldWithPath("empty").type(JsonFieldType.BOOLEAN).description("Is the current page empty")
    ).apply {
        addAll(content.map { it.copy("content[].${it.path}") })
        addAll(pageableFields())
        addAll(sortFields())
    }
}

private fun pageableFields(): List<FieldDescriptor> {
    val prefix = "pageable."
    return mutableListOf(
        fieldWithPath(prefix + "offset").type(JsonFieldType.NUMBER)
            .description("Current offset (starting from 0). Request parameter 'page' or 'offset'"),
        fieldWithPath(prefix + "page_size").type(JsonFieldType.NUMBER)
            .description("Requested elements count per page. Request parameter 'size'. Default 20"),
        fieldWithPath(prefix + "page_number").type(JsonFieldType.NUMBER).description("Current page number"),
        fieldWithPath(prefix + "unpaged").type(JsonFieldType.BOOLEAN).description("Is the result unpaged"),
        fieldWithPath(prefix + "paged").type(JsonFieldType.BOOLEAN).description("Is the result paged")
    ).apply {
        addAll(sortFields(prefix))
    }
}

private fun sortFields(prefix: String = ""): List<FieldDescriptor> {
    return listOf(
        fieldWithPath(prefix + "sort.sorted").type(JsonFieldType.BOOLEAN)
            .description("Is the result sorted. Request parameter 'sort', values '=field,direction(asc,desc)'"),
        fieldWithPath(prefix + "sort.unsorted").type(JsonFieldType.BOOLEAN).description("Is the result unsorted"),
        fieldWithPath(prefix + "sort.empty").type(JsonFieldType.BOOLEAN).description("Is the sort empty")
    )
}

internal fun pageable(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        fieldWithPath(prefix + "content").type(JsonFieldType.ARRAY).optional().description(""),
        fieldWithPath(prefix + "pageable.sort").type(JsonFieldType.OBJECT).optional().description(""),
        fieldWithPath(prefix + "pageable.sort.unsorted").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "pageable.sort.sorted").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "pageable.sort.empty").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "pageable.page_size").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "pageable.page_number").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "pageable.offset").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "pageable.paged").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "pageable.unpaged").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "total_elements").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "total_pages").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "last").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "first").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "number_of_elements").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "sort").type(JsonFieldType.OBJECT).optional().description(""),
        fieldWithPath(prefix + "sort.unsorted").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "sort.sorted").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "sort.empty").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "size").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "number").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "empty").type(JsonFieldType.BOOLEAN).optional().description("")
    )
}




