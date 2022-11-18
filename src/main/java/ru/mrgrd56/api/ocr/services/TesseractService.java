package ru.mrgrd56.api.ocr.services;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class TesseractService {

    @Value("${tesseract.datapath}")
    private String tesseractDatapath;

    public File createTempFile(byte[] bytes, String fileName) {
        var fileExtension = FilenameUtils.getExtension(fileName);

        try {
            var tempFilePath = Files.createTempFile("image", "." + fileExtension);
            Files.write(tempFilePath, bytes);
            return tempFilePath.toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String recognizeTextFromImage(File image, String language) {
        var tesseract = new Tesseract();
        tesseract.setDatapath(tesseractDatapath);
        tesseract.setLanguage(language);
        tesseract.setPageSegMode(1);
        tesseract.setOcrEngineMode(1);

        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            throw new RuntimeException(e);
        }
    }
}
