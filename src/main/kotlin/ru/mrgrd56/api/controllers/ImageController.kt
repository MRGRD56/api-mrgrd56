package ru.mrgrd56.api.controllers

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.mrgrd56.api.image.ImageGenerationService

@RestController
@RequestMapping("image")
class ImageController(private val imageGenerationService: ImageGenerationService) {
    @GetMapping("color/{color}")
    fun generateColoredImage(
        @PathVariable color: String?,
        @RequestParam(value = "s", defaultValue = "10x10") size: String,
        @RequestParam(value = "r", required = false) borderRadius: Int?
    ): ResponseEntity<*> {
        return try {
            val image = imageGenerationService.generateFilledImage(
                imageGenerationService.parseColor(color),
                imageGenerationService.parseSize(size),
                borderRadius
            )

            ResponseEntity.ok()
                .header("Content-Type", MediaType.IMAGE_PNG_VALUE)
                .body(image)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(e.message)
        }
    }
}
