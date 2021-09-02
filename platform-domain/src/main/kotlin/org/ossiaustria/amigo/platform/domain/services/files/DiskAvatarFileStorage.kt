package org.ossiaustria.amigo.platform.domain.services.files

import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.nio.file.Paths
import java.util.*

@Service
class DiskAvatarFileStorage : FileStorage(), AvatarFileStorage {

    private fun getAvatarPath(id: UUID) = Paths.get(ROOT_PATH, id.toString(), AVATAR_SUFFIX)

    override fun saveAvatar(id: UUID, multipartFile: MultipartFile, overwrite: Boolean): FileInfo {
        val ownerPath = getOwnerPath(id)
        ensureFolderExists(root)
        ensureFolderExists(ownerPath)

        val path = getAvatarPath(id)
        return getFileInfo(path, overwrite, multipartFile)
    }

    override fun loadAvatar(id: UUID): Resource {
        val path = getAvatarPath(id)
        return loadResource(path)
    }

    override fun deleteAvatar(id: UUID): Boolean = try {


        FileSystemUtils.deleteRecursively(getAvatarPath(id))
    } catch (e: Exception) {
        false
    }

    override fun getUrl(id: UUID): URI {
        return getAvatarPath(id).toUri()
    }

    companion object {
        const val AVATAR_SUFFIX = "avatar"
    }
}