package ru.mrgrd56.api.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.mrgrd56.api.ocr.model.ThresholdType;
import ru.mrgrd56.api.ocr.services.OpenCvService;
import ru.mrgrd56.api.ocr.services.TesseractService;
import ru.mrgrd56.api.utils.FileResponse;

import java.io.IOException;

@RestController
@RequestMapping("/ocr")
public class OcrController {

    private final OpenCvService openCvService;
    private final TesseractService tesseractService;

    public OcrController(
            OpenCvService openCvService,
            TesseractService tesseractService) {
        this.openCvService = openCvService;
        this.tesseractService = tesseractService;
    }

    @PostMapping("/prepare")
    public FileResponse threshold(
            @RequestParam("image") MultipartFile file,
            @RequestParam double thresholdValue,
            @RequestParam(defaultValue = "255") double thresholdMaxval,
            @RequestParam ThresholdType thresholdType,
            @RequestParam(defaultValue = "true") boolean cropMain) throws IOException {
        var image = openCvService.loadImage(file.getBytes(), true);
        var resultImage = openCvService.threshold(image, thresholdValue, thresholdMaxval, thresholdType);
        if (cropMain) {
            resultImage = openCvService.cropMain(resultImage);
        }
        return openCvService.matToResponse(resultImage);
    }

    @PostMapping(value = "/recognize/original", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> recognizeOriginal(
            @RequestParam("image") MultipartFile file,
            @RequestParam String recognitionLanguage) throws IOException {
        var image = tesseractService.createTempFile(file.getBytes(), file.getOriginalFilename());
        return ResponseEntity.ok(tesseractService.recognizeTextFromImage(image, recognitionLanguage));
    }

    @PostMapping(value = "/recognize/prepared")
    public ResponseEntity<?> recognizePrepared(
            @RequestParam("image") MultipartFile file,
            @RequestParam double thresholdValue,
            @RequestParam(defaultValue = "255") double thresholdMaxval,
            @RequestParam ThresholdType thresholdType,
            @RequestParam(defaultValue = "true") boolean cropMain,
            @RequestParam String recognitionLanguage) throws IOException {
        var prepared = threshold(file, thresholdValue, thresholdMaxval, thresholdType, cropMain);
        var image = tesseractService.createTempFile(prepared.getBody(), prepared.getFileName());
        return ResponseEntity.ok(tesseractService.recognizeTextFromImage(image, recognitionLanguage));
    }
}
