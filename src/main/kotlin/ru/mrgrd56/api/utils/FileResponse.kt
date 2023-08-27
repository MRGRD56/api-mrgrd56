package ru.mrgrd56.api.utils

import org.springframework.http.*

class FileResponse private constructor(
    file: ByteArray,
    mime: MediaType,
    isDownloadable: Boolean,
    val fileName: String?
) : ResponseEntity<ByteArray?>(file, getHeaders(file, mime, isDownloadable, fileName), HttpStatus.OK) {

    companion object {
        fun inline(file: ByteArray, mime: MediaType): FileResponse {
            return FileResponse(file, mime, false, null)
        }

        fun inline(file: ByteArray, mime: MediaType, fileName: String?): FileResponse =
            FileResponse(file, mime, false, fileName)

        fun attachment(file: ByteArray, mime: MediaType, fileName: String?): FileResponse =
            FileResponse(file, mime, true, fileName)

        fun attachment(file: ByteArray, mime: MediaType): FileResponse =
            FileResponse(file, mime, true, null)

        private fun getHeaders(
            file: ByteArray,
            mime: MediaType,
            isDownloadable: Boolean,
            fileName: String?
        ): HttpHeaders {
            return HttpHeaders().apply {
                contentType = mime
                contentLength = file.size.toLong()
                contentDisposition = buildContentDisposition(isDownloadable, fileName)
            }
        }

        private fun buildContentDisposition(isAttachment: Boolean, fileName: String?): ContentDisposition {
            val builder =
                if (isAttachment) ContentDisposition.attachment()
                else ContentDisposition.inline()

            fileName?.let { builder.filename(it) }

            return builder.build()
        }
    }
}
