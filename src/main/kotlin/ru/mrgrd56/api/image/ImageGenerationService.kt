package ru.mrgrd56.api.image

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

@Service
class ImageGenerationService {
    private val filledImagesCache: Cache<String, ByteArray> =
        Caffeine.newBuilder()
            .maximumSize(512)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build()

    fun generateFilledImage(
        color: Color,
        size: Dimension,
        borderRadius: Int?
    ): ByteArray {
        val hash = "$color/$size/$borderRadius"

        filledImagesCache.getIfPresent(hash)?.let {
            return it
        }

        validateSize(size)
        validateBorderRadius(borderRadius)

        val width = size.width
        val height = size.height
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        val graphics = image.createGraphics()
        graphics.color = color

        if (borderRadius == null) {
            graphics.fillRect(0, 0, width, height)
        } else {
            graphics.fillRoundRect(0, 0, width, height, borderRadius, borderRadius)
        }

        ByteArrayOutputStream().use { output ->
            try {
                ImageIO.write(image, "png", output)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

            val result = output.toByteArray()

            if (width <= 512 && height <= 512) {
                filledImagesCache.put(hash, result)
            }

            return result
        }
    }

    @Throws(IllegalArgumentException::class)
    fun parseColor(input: String?): Color {
        return Color.decode(input) ?: throw IllegalArgumentException("Invalid color provided")
    }

    @Throws(IllegalArgumentException::class)
    fun parseSize(input: String): Dimension {
        val parts = input.split("[xх]".toRegex(), limit = 2)

        return when (parts.size) {
            1 -> {
                val size = parts[0].toInt()
                Dimension(size, size)
            }

            2 -> {
                val width = parts[0].toInt()
                val height = parts[1].toInt()
                Dimension(width, height)
            }

            else -> throw IllegalArgumentException("Invalid size (s) provided")
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun validateSize(dimension: Dimension) {
        require(dimension.width in 1..3000 && dimension.height in 0..3000) {
            "Invalid size (s) provided: both sides must be in range [1; 3000]"
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun validateBorderRadius(borderRadius: Int?) {
        borderRadius?.let {
            require(it >= 0) { "Invalid borderRadius (r) provided: the value must not be less than 0" }
        }
    }
}
