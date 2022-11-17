package ru.mrgrd56.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.mrgrd56.api.ocr.model.ThresholdType;
import ru.mrgrd56.api.ocr.services.OcrService;

import java.io.IOException;

@RestController
@RequestMapping("/ocr")
public class OcrController {

    private final OcrService ocrService;

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/prepare")
    public ResponseEntity<?> threshold(
            @RequestParam("image") MultipartFile file,
            @RequestParam double thresholdValue,
            @RequestParam(defaultValue = "255") double thresholdMaxval,
            @RequestParam ThresholdType thresholdType,
            @RequestParam(defaultValue = "true") boolean cropMain) throws IOException {
        var image = ocrService.loadImage(file.getBytes(), true);
        var resultImage = ocrService.threshold(image, thresholdValue, thresholdMaxval, thresholdType);
        if (cropMain) {
            resultImage = ocrService.cropMain(resultImage);
        }
        return ocrService.matToResponse(resultImage);
    }
}
