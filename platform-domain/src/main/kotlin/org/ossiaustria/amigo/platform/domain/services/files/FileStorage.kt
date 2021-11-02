package org.ossiaustria.amigo.platform.domain.services.files

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

abstract class FileStorage {

    protected val root: Path = Paths.get(ROOT_PATH)

    init {
        try {
            ensureFolderExists(root)
        } catch (e: IOException) {
            throw Error("Could not initialize folder for upload!")
        }
    }

    protected fun getFileInfo(
        multimediaPath: Path,
        overwrite: Boolean,
        multipartFile: MultipartFile
    ): FileInfo {
        val exists = Files.isReadable(multimediaPath)
        val writable = Files.isWritable(multimediaPath)
        if (!overwrite && exists) {
            throw FileStorageError.FileAlreadyExists(multimediaPath)
        } else if (overwrite && exists && !writable) {
            throw FileStorageError.FileNotOverwritable(multimediaPath)
        } else {
            if (exists) {
                Files.delete(multimediaPath)
            }
            Files.copy(multipartFile.inputStream, multimediaPath)
            val file = File(multimediaPath.toUri())
            return FileInfo(size = file.length(), absolutePath = file.absolutePath)
        }
    }

    protected fun loadResource(multimediaPath: Path): Resource {
        val exists = Files.isReadable(multimediaPath)

        if (!exists) {
            throw FileStorageError.FileNotFound(multimediaPath)
        }

        val resource: Resource = UrlResource(multimediaPath.toUri())
        return if (resource.exists() || resource.isReadable) resource
        else throw RuntimeException("Could not read the file!")
    }

    protected fun ensureFolderExists(path: Path) {
        if (!Files.isDirectory(path)) Files.createDirectory(path)
        if (!Files.isDirectory(path)) throw FileNotFoundException("Cannot use path as directory: $path")
    }

    protected fun getOwnerPath(ownerId: UUID) = Paths.get(ROOT_PATH, ownerId.toString())

    companion object {
        const val ROOT_PATH = "files"
    }
}
