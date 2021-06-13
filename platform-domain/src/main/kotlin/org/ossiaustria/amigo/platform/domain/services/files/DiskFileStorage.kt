package org.ossiaustria.amigo.platform.domain.services.files

import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


@Service
class DiskFileStorage : FileStorage {

    private val root: Path = Paths.get(ROOT_PATH)

    init {
        try {
            ensureFolderExists(root)
        } catch (e: IOException) {
            throw Error("Could not initialize folder for upload!")
        }
    }

    override fun saveFile(
        multimedia: Multimedia,
        file: MultipartFile,
        overwrite: Boolean
    ): FileInfo {
        val ownerPath = getOwnerPath(multimedia.ownerId)
        ensureFolderExists(root)
        ensureFolderExists(ownerPath)

        val multimediaPath = getMultimediaPath(multimedia.ownerId, multimedia.id)
        val exists = Files.isReadable(multimediaPath)
        val writable = Files.isWritable(multimediaPath)
        if (!overwrite && exists) {
            throw FileStorageError.FileAlreadyExists(multimediaPath)
        } else if (overwrite && exists && !writable) {
            throw FileStorageError.FileNotOverwritable(multimediaPath)
        } else {
            Files.copy(file.inputStream, multimediaPath)
            val file = File(multimediaPath.toUri())
            return FileInfo(size = file.length(), absolutePath = file.absolutePath)
        }
    }

    override fun loadFile(multimedia: Multimedia): Resource {
        val multimediaPath = getMultimediaPath(multimedia.ownerId, multimedia.id)
        val exists = Files.isReadable(multimediaPath)

        if (!exists) {
            throw FileStorageError.FileNotFound(multimediaPath)
        }

        val resource: Resource = UrlResource(multimediaPath.toUri())
        return if (resource.exists() || resource.isReadable) resource
        else throw RuntimeException("Could not read the file!")
    }

    override fun deleteFile(multimedia: Multimedia): Boolean {
        val multimediaPath = getMultimediaPath(multimedia.ownerId, multimedia.id)
        return FileSystemUtils.deleteRecursively(multimediaPath)
    }

    override fun getUrl(multimedia: Multimedia): URI {
        return getMultimediaPath(multimedia.ownerId, multimedia.id).toUri()
    }

    private fun ensureFolderExists(path: Path) {
        if (!Files.isDirectory(path)) Files.createDirectory(path)
        if (!Files.isDirectory(path)) throw FileNotFoundException("Cannot use path as directory: $path")
    }

    private fun getOwnerPath(ownerId: UUID) = Paths.get(ROOT_PATH, ownerId.toString())

    private fun getMultimediaPath(ownerId: UUID, id: UUID) = Paths.get(ROOT_PATH, ownerId.toString(), id.toString())

    companion object {
        const val ROOT_PATH = "files"
    }
}