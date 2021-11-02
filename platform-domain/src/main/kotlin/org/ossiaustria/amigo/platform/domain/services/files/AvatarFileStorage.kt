package org.ossiaustria.amigo.platform.domain.services.files

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.util.UUID

interface AvatarFileStorage {

    fun saveAvatar(id: UUID, multipartFile: MultipartFile, overwrite: Boolean = false): FileInfo
    fun loadAvatar(id: UUID): Resource
    fun deleteAvatar(id: UUID): Boolean
    fun getUrl(id: UUID): URI
}
