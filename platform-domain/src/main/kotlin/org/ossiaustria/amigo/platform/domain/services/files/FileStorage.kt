package org.ossiaustria.amigo.platform.domain.services.files

import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.services.ServiceError
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.nio.file.Path

interface FileStorage {

    fun saveFile(multimedia: Multimedia, file: MultipartFile, overwrite: Boolean = false): FileInfo
    fun loadFile(multimedia: Multimedia): Resource
    fun deleteFile(multimedia: Multimedia): Boolean
    fun getUrl(multimedia: Multimedia): URI
}

sealed class FileStorageError(errorName: String, message: String) : ServiceError(errorName, message, null) {
    class FileAlreadyExists(path: Path) : FileStorageError("FILE_ALREADY_EXISTS", "FileAlreadyExists: $path")
    class FileNotOverwritable(path: Path) : FileStorageError("FILE_NOT_OVERWRITABLE", "FileNotOverwritable: $path")
    class FileNotFound(path: Path) : FileStorageError("FILE_NOT_FOUND", "$path")
}

data class FileInfo(
    val size: Long,
    val absolutePath: String
)