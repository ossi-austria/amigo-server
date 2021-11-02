package org.ossiaustria.amigo.platform.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class APObjectMapper : ObjectMapper() {

    init {
        this.registerKotlinModule()
        this.registerModule(JavaTimeModule())
        this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        this.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        this.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        this.propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
    }

    override fun copy(): ObjectMapper {
        return APObjectMapper()
    }
}
