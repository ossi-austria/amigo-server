package org.ossiaustria.amigo.platform.testcommons

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.UUID


abstract class AbstractRestTest {

    lateinit var mockMvc: MockMvc

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
        const val HEADER_PRIVATE_TOKEN = "Authorization"
        const val HEADER_PERSON_ID = "Amigo-Person-Id"
    }

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    protected fun acceptContentAuth(
        requestBuilder: MockHttpServletRequestBuilder,
        jwtToken: String,
        personId: UUID? = null,
        mediaType: MediaType = MediaType.APPLICATION_JSON
    ): MockHttpServletRequestBuilder {
        return requestBuilder
            .accept(MediaType.APPLICATION_JSON, MediaType.ALL)
            .header(HEADER_PRIVATE_TOKEN, "Bearer $jwtToken")
            .apply {
                if (personId != null) {
                    header(HEADER_PERSON_ID, personId)
                }
            }
            .contentType(mediaType)
    }

    protected fun acceptAnonymousAuth(requestBuilder: MockHttpServletRequestBuilder): MockHttpServletRequestBuilder {
        return requestBuilder
            .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
            .contentType(MediaType.APPLICATION_JSON)
    }


    protected fun performPost(
        url: String,
        accessToken: String? = null,
        body: Any? = null,
        personId: UUID? = null
    ) = mockMvc.perform(
        generateRequestBuilder(url, accessToken, body, HttpMethod.POST, personId)
    )

    protected fun performPartPost(
        url: String,
        accessToken: String,
        filePart: MockMultipartFile? = null,
        bodyPart: MockPart? = null,
        personId: UUID? = null
    ) =
        mockMvc.perform(generatePartRequestBuilder(url, accessToken, filePart, bodyPart,personId))

    protected fun performPatch(
        url: String,
        accessToken: String? = null,
        body: Any? = null,
        personId: UUID? = null
    ) = mockMvc.perform(
        generateRequestBuilder(url, accessToken, body, HttpMethod.PATCH,personId)
    )

    protected fun performPut(
        url: String,
        accessToken: String? = null,
        body: Any? = null,
        personId: UUID? = null
    ) = mockMvc.perform(
        generateRequestBuilder(url, accessToken, body, HttpMethod.PUT,personId)
    )

    protected fun performGet(
        url: String,
        accessToken: String? = null,
        personId: UUID? = null
    ) = mockMvc.perform(
        generateRequestBuilder(url, accessToken, null, HttpMethod.GET,personId)
    )

    protected fun performDelete(
        url: String,
        accessToken: String? = null,
        personId: UUID? = null
    ) = mockMvc.perform(
        generateRequestBuilder(url, accessToken, null, HttpMethod.DELETE,personId)
    )

    private fun generateRequestBuilder(
        url: String,
        jwtToken: String?,
        body: Any?,
        method: HttpMethod = HttpMethod.GET,
        personId: UUID? = null
    ): MockHttpServletRequestBuilder {
        val builder = when (method) {
            HttpMethod.GET -> RestDocumentationRequestBuilders.get(url)
            HttpMethod.POST -> RestDocumentationRequestBuilders.post(url)
            HttpMethod.PUT -> RestDocumentationRequestBuilders.put(url)
            HttpMethod.DELETE -> RestDocumentationRequestBuilders.delete(url)
            HttpMethod.PATCH -> RestDocumentationRequestBuilders.patch(url)
            else -> throw RuntimeException("Method not implemented")
        }

        if (body != null) {
            builder.content(objectMapper.writeValueAsString(body))
        }

        return if (jwtToken == null) {
            acceptAnonymousAuth(builder)
        } else {
            acceptContentAuth(builder, jwtToken, personId)
        }
    }

    private fun generatePartRequestBuilder(
        url: String,
        jwtToken: String,
        filePart: MockMultipartFile?,
        bodyPart: MockPart?,
        personId: UUID?=null,
    ): MockMultipartHttpServletRequestBuilder {
        val builder = multipart(url)

        if (filePart != null) {
            builder.file(filePart)
        }
        if (bodyPart != null) {
            builder.part(bodyPart)
        }
        return acceptContentAuth(
            builder,
            jwtToken,
            mediaType = MediaType.MULTIPART_FORM_DATA,
            personId = personId
        ) as MockMultipartHttpServletRequestBuilder
    }

    fun ResultActions.checkStatus(status: HttpStatus): ResultActions {
        return this.andExpect(MockMvcResultMatchers.status().`is`(status.value()))
    }

    fun <T> ResultActions.returnsList(clazz: Class<T>): List<T> {
        return this.andReturn().let {
            val constructCollectionType = objectMapper.typeFactory.constructCollectionType(List::class.java, clazz)
            objectMapper.readValue(it.response.contentAsByteArray, constructCollectionType)
        }
    }

    inline fun <reified T : Any> ResultActions.returns(): T {
        return this.andReturn().let {
            `access$objectMapper`.readValue(it.response.contentAsByteArray)
        }
    }

    fun <T> ResultActions.returns(clazz: Class<T>): T {
        return this.andReturn().let {
            objectMapper.readValue(it.response.contentAsByteArray, clazz)
        }
    }

    fun <T> ResultActions.returns(valueTypeRef: TypeReference<T>): T {
        return this.andReturn().let {
            objectMapper.readValue(it.response.contentAsByteArray, valueTypeRef)
        }
    }

    fun ResultActions.expectOk(): ResultActions {
        return this.andExpect(MockMvcResultMatchers.status().isOk)
    }

    fun ResultActions.expectForbidden(): ResultActions {
        return this.andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    fun ResultActions.expectUnauthorized(): ResultActions {
        return this.andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    fun ResultActions.expect4xx(): ResultActions {
        return this.andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    fun ResultActions.expectNoContent(): ResultActions {
        return this.andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    fun ResultActions.expectBadRequest(): ResultActions {
        return this.andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    fun ResultActions.isNotFound(): ResultActions {
        return this.andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    fun ResultActions.isConflict(): ResultActions {
        return this.andExpect(MockMvcResultMatchers.status().isConflict)
    }

    fun ResultActions.isUnavailableForLegalReasons(): ResultActions {
        return this.andExpect(MockMvcResultMatchers.status().isUnavailableForLegalReasons)
    }

    @PublishedApi
    internal var `access$objectMapper`: ObjectMapper
        get() = objectMapper
        set(value) {
            objectMapper = value
        }
}



