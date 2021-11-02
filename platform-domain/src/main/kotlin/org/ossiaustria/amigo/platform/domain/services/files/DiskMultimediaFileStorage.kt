package org.ossiaustria.amigo.platform.domain.services.files

import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.nio.file.Paths
import java.util.UUID

@Service
class DiskMultimediaFileStorage : FileStorage(), MultimediaFileStorage {

    private fun getMultimediaPath(ownerId: UUID, id: UUID) = Paths.get(ROOT_PATH, ownerId.toString(), id.toString())

    override fun saveFile(
        multimedia: Multimedia,
        multipartFile: MultipartFile,
        overwrite: Boolean
    ): FileInfo {
        val ownerPath = getOwnerPath(multimedia.ownerId)
        ensureFolderExists(root)
        ensureFolderExists(ownerPath)

        val multimediaPath = getMultimediaPath(multimedia.ownerId, multimedia.id)
        return getFileInfo(multimediaPath, overwrite, multipartFile)
    }

    override fun loadFile(multimedia: Multimedia): Resource {
        val multimediaPath = getMultimediaPath(multimedia.ownerId, multimedia.id)
        return loadResource(multimediaPath)
    }

    override fun deleteFile(multimedia: Multimedia): Boolean {
        val multimediaPath = getMultimediaPath(multimedia.ownerId, multimedia.id)
        return FileSystemUtils.deleteRecursively(multimediaPath)
    }

    override fun getUrl(multimedia: Multimedia): URI {
        return getMultimediaPath(multimedia.ownerId, multimedia.id).toUri()
    }

}
