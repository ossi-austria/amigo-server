package org.ossiaustria.amigo.platform.domain.testcommons

import org.assertj.core.api.AssertionsForClassTypes
import org.assertj.core.api.ObjectAssert
import org.hamcrest.Matcher


internal fun <T> then(actual: T, matcher: Matcher<T>) = org.hamcrest.MatcherAssert.assertThat(actual, matcher)
internal fun <T> then(actual: T): ObjectAssert<T> = AssertionsForClassTypes.assertThat(actual)
