package ru.mrgrd56.api.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mrgrd56.api.image.ImageService;

@RestController
@RequestMapping("image")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("color/{color}")
    public ResponseEntity<?> generateColoredImage(
            @PathVariable String color,
            @RequestParam(value = "s", defaultValue = "10x10") String size
    ) {
        try {
            byte[] image = imageService.generateFilledImage(imageService.parseColor(color), imageService.parseSize(size));

            return ResponseEntity.ok()
                    .header("Content-Type", MediaType.IMAGE_PNG_VALUE)
                    .body(image);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
