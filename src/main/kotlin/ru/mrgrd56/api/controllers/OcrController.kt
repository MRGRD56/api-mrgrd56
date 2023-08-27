package ru.mrgrd56.api.controllers

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.mrgrd56.api.ocr.model.ThresholdType
import ru.mrgrd56.api.ocr.services.OpenCvService
import ru.mrgrd56.api.ocr.services.TesseractService
import ru.mrgrd56.api.utils.FileResponse
import java.io.IOException

@RestController
@RequestMapping("/ocr")
class OcrController(
    private val openCvService: OpenCvService,
    private val tesseractService: TesseractService
) {
    @PostMapping("/prepare")
    @Throws(IOException::class)
    fun threshold(
        @RequestParam("image") file: MultipartFile,
        @RequestParam thresholdValue: Double,
        @RequestParam(defaultValue = "255") thresholdMaxval: Double,
        @RequestParam thresholdType: ThresholdType,
        @RequestParam(defaultValue = "true") cropMain: Boolean
    ): FileResponse {
        val image = openCvService.loadImage(file.bytes, true)
        val resultImage = openCvService.threshold(image, thresholdValue, thresholdMaxval, thresholdType).let {
            if (cropMain) {
                openCvService.cropMain(it)
            } else {
                it
            }
        }

        return openCvService.matToResponse(resultImage)
    }

    @PostMapping(value = ["/recognize/original"], produces = [MediaType.TEXT_PLAIN_VALUE])
    @Throws(IOException::class)
    fun recognizeOriginal(
        @RequestParam("image") file: MultipartFile,
        @RequestParam recognitionLanguage: String
    ): ResponseEntity<*> {
        val image = tesseractService.createTempFile(file.bytes, file.originalFilename!!)
        return ResponseEntity.ok(tesseractService.recognizeTextFromImage(image, recognitionLanguage))
    }

    @PostMapping(value = ["/recognize/prepared"])
    @Throws(IOException::class)
    fun recognizePrepared(
        @RequestParam("image") file: MultipartFile,
        @RequestParam thresholdValue: Double,
        @RequestParam(defaultValue = "255") thresholdMaxval: Double,
        @RequestParam thresholdType: ThresholdType,
        @RequestParam(defaultValue = "true") cropMain: Boolean,
        @RequestParam recognitionLanguage: String
    ): ResponseEntity<*> {
        val prepared = threshold(file, thresholdValue, thresholdMaxval, thresholdType, cropMain)
        val image = tesseractService.createTempFile(prepared.body!!, prepared.fileName!!)
        return ResponseEntity.ok(tesseractService.recognizeTextFromImage(image, recognitionLanguage))
    }
}
