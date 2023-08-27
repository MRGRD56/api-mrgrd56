package ru.mrgrd56.api.ocr.services

import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.TesseractException
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.file.Files

@Service
class TesseractService(
    @Value("\${tesseract.datapath}")
    private val tesseractDatapath: String
) {
    fun createTempFile(bytes: ByteArray, fileName: String): File {
        val fileExtension = FilenameUtils.getExtension(fileName)
        try {
            val tempFilePath = Files.createTempFile("image", ".$fileExtension")
            Files.write(tempFilePath, bytes)
            return tempFilePath.toFile()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun recognizeTextFromImage(image: File?, language: String?): String {
        val tesseract = Tesseract().apply {
            setDatapath(tesseractDatapath)
            setLanguage(language)
            setPageSegMode(1)
            setOcrEngineMode(1)
        }

        try {
            return tesseract.doOCR(image)
        } catch (e: TesseractException) {
            throw RuntimeException(e)
        }
    }
}
