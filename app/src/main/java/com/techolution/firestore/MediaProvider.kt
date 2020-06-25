package com.techolution.firestore

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception

class MediaProvider : FileProvider() {
    companion object {
        const val Authority: String = "com.industrialbadger.media"
        @JvmStatic fun createImageFile(context: Context): File {
            try {
                return createFile(getExternalPicturesDir(context), "iba_${makeTimestamp()}", ".jpg")
            } catch (ex: Exception) {
                throw Exception("Failed to create file", ex)
            }
        }
        @JvmStatic fun createThumbnailFile(context: Context, filename: String): File {
            val thumb = filename.replace(".mp4", "-thumb")
            try {
                return createFile(getExternalPicturesDir(context), thumb, ".jpg")
            } catch (ex: Exception) {
                throw Exception("Failed to create file", ex)
            }
        }
        @JvmStatic fun createVideoFile(context: Context): File {
            try {
                return createFile(getExternalMoviesDir(context), "iba_${makeTimestamp()}", ".mp4")
            } catch (ex: Exception) {
                throw Exception("Failed to create file", ex)
            }
        }
        @JvmStatic
        @JvmOverloads
        fun createHtmlFile(context: Context, name: String = "iba_${makeTimestamp()}"): File {
            try {
                return createFile(getExternalDocumentsDir(context), name, ".html")
            } catch (ex: Exception) {
                throw Exception("Failed to create file", ex)
            }
        }
        @JvmStatic
        fun getContentFile(context: Context, parent: String, name: String): File {
            try {
                val dir = File(getExternalDocumentsDir(context), parent).apply {
                    if (!exists() && !mkdirs()) {
                        throw IOException("Unable to get documents subdirectory.")
                    }
                }
                return File(dir, name)
            } catch (ex: Exception) {
                throw Exception("Failed to get file", ex)
            }
        }
        @JvmStatic
        @JvmOverloads
        fun createJsonFile(context: Context, name: String = "iba_${makeTimestamp()}"): File {
            try {
                return createFile(getExternalDocumentsDir(context), name, ".json")
            } catch (ex: Exception) {
                throw Exception("Failed to create file", ex)
            }
        }
        @JvmStatic fun createAudioFile(context: Context): File {
            try {
                return createFile(getExternalAudioDir(context),"iba_${makeTimestamp()}", ".wav")
            } catch (ex: Exception) {
                throw Exception("Failed to create file", ex)
            }
        }
        @JvmStatic fun writeCacheFile(context: Context, bytes: ByteArray): File {
            try {
                val f = createFile(getInternalBlobCacheDirectory(context), generateCacheFileName(), ".bin")
                f.writeBytes(bytes)
                return f
            } catch (ex: Exception) {
                throw Exception("Failed to create cache file", ex)
            }
        }
        @JvmStatic fun getVideoFile(context: Context, filename: String): File {
            val video = filename.replace("-thumb.jpg", ".mp4")
            try {
                return File(getExternalMoviesDir(context), video)
            } catch (ex: Exception) {
                throw Exception("Failed to retrieve file", ex)
            }
        }
        @JvmStatic fun getImageFile(context: Context, filename: String): File {
            try {
                return File(getExternalPicturesDir(context), filename)
            } catch (ex: Exception) {
                throw Exception("Failed to retrieve file", ex)
            }
        }
        @JvmStatic fun getAudioFile(context: Context, filename: String, dir: String = "Audio"): File {
            try {
                return File(getExternalAudioSubdirectory(context, dir), "$filename.wav")
            } catch (ex: Exception) {
                throw Exception("Failed to retrieve file", ex)
            }
        }
        @JvmStatic fun getInternalBlobCacheFile(context: Context, filename: String): File {
            try {
                return File(getInternalBlobCacheDirectory(context), filename)
            } catch (ex: Exception) {
                throw Exception("Failed to retrieve file", ex)
            }
        }
        @JvmStatic fun readInternalBlobCacheFile(context: Context, filename: String): ByteArray {
            try {
                val f = getInternalBlobCacheFile(context, filename)
                return f.readBytes()
            } catch (ex: Exception) {
                throw Exception("Failed to read file", ex)
            }
        }
        @JvmStatic fun scanFile(context: Context, file: File) {
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(mime)) { string, uri ->
                Log.i("MediaProvider", "Scan Completed: $string\t$uri")
            }
        }
        @JvmStatic fun getExternalPicturesDir(context: Context): File {
            return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.apply {
                if (!exists() && !mkdirs()) {
                    throw IOException("Unable to get image directory.")
                }
            } ?: throw IOException("Unable to get image directory.")
        }
        @JvmStatic fun getExternalMoviesDir(context: Context): File {
            return context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.apply {
                if (!exists() && !mkdirs()) {
                    throw IOException("Unable to get video directory.")
                }
            } ?: throw IOException("Unable to get video directory.")
        }
        @JvmStatic fun getExternalDocumentsDir(context: Context): File {
            return context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.apply {
                if (!exists() && !mkdirs()) {
                    throw IOException("Unable to get documents directory.")
                }
            } ?: throw IOException("Unable to get documents directory.")
        }
        @JvmStatic fun getExternalAudioDir(context: Context): File {
            return context.getExternalFilesDir("Audio")?.apply {
                if (!exists() && !mkdirs()) {
                    throw IOException("Unable to get audio directory.")
                }
            } ?: throw IOException("Unable to get audio directory.")
        }
        @JvmStatic fun getExternalAudioSubdirectory(context: Context, sub: String): File {
            return File(getExternalAudioDir(context), sub).apply {
                if (!exists() && !mkdirs()) {
                    throw IOException("Unable to get audio subdirectory.")
                }
            }
        }
        @JvmStatic fun getInternalBlobCacheDirectory(context: Context): File {
            return File(context.cacheDir, "blob").apply {
                if (!exists() && !mkdirs()) {
                    throw IOException("Unable to get blob cache directory.")
                }
            }
        }
        @JvmStatic fun createFile(
                dir: File,
                name: String = "iba_${makeTimestamp()}",
                suffix: String = ".badger"): File {
            return File(dir, "$name$suffix")
        }
        @JvmStatic private fun generateCacheFileName(): String =
                UUID.randomUUID().toString().replace("-", "")
        @JvmStatic private fun makeTimestamp() =
                SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
    }
}