package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Call
import org.springframework.stereotype.Repository

@Repository
internal interface CallRepository : SendableRepository<Call>