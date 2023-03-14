package ru.mrgrd56.api.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mrgrd56.api.image.ImageGenerationService;

@RestController
@RequestMapping("image")
public class ImageController {
    private final ImageGenerationService imageGenerationService;

    public ImageController(ImageGenerationService imageGenerationService) {
        this.imageGenerationService = imageGenerationService;
    }

    @GetMapping("color/{color}")
    public ResponseEntity<?> generateColoredImage(
            @PathVariable String color,
            @RequestParam(value = "s", defaultValue = "10x10") String size,
            @RequestParam(value = "r", required = false) Integer borderRadius
    ) {
        try {
            byte[] image = imageGenerationService.generateFilledImage(
                    imageGenerationService.parseColor(color),
                    imageGenerationService.parseSize(size),
                    borderRadius);

            return ResponseEntity.ok()
                    .header("Content-Type", MediaType.IMAGE_PNG_VALUE)
                    .body(image);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
