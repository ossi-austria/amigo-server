package org.ossiaustria.amigo.platform.config

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class APObjectMapper : ObjectMapper() {

    init {
        this.registerKotlinModule()
        this.registerModule(JavaTimeModule())
        this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        this.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        this.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        this.propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    }

    override fun copy(): ObjectMapper {
        return APObjectMapper()
    }
}