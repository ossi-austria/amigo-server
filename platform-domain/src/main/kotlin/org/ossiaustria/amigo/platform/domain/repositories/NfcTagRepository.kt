package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.NfcTag
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface NfcTagRepository : CrudRepository<NfcTag, UUID> {}