package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.springframework.stereotype.Repository

@Repository
internal interface MultimediaRepository : SendableRepository<Multimedia>