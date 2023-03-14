package ru.mrgrd56.api.image;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class ImageGenerationService {
    private final Cache<String, byte[]> filledImagesCache = Caffeine.newBuilder()
            .maximumSize(512)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    public byte[] generateFilledImage(
            Color color,
            Dimension size,
            Integer borderRadius
    ) {
        var hash = color + "/" + size + "/" + borderRadius;

        var cached = filledImagesCache.getIfPresent(hash);
        if (cached != null) {
            return cached;
        }

        validateSize(size);
        validateBorderRadius(borderRadius);

        int width = size.width;
        int height = size.height;

        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        if (borderRadius == null) {
            graphics.fillRect(0, 0, width, height);
        } else {
            graphics.fillRoundRect(0, 0, width, height, borderRadius, borderRadius);
        }

        var output = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] result = output.toByteArray();

        if (width <= 512 && height <= 512) {
            filledImagesCache.put(hash, result);
        }

        return result;
    }

    public Color parseColor(String input) throws IllegalArgumentException {
        var result = Color.decode(input);
        if (result == null) {
            throw new IllegalArgumentException("Invalid color provided");
        }

        return result;
    }

    public Dimension parseSize(String input) throws IllegalArgumentException {
        String[] parts = input.split("[xх]", 2);
        if (parts.length == 1) {
            int size = Integer.parseInt(parts[0]);
            return new Dimension(size, size);
        }
        if (parts.length == 2) {
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);
            return new Dimension(width, height);
        }

        throw new IllegalArgumentException("Invalid size (s) provided");
    }

    private void validateSize(Dimension dimension) throws IllegalArgumentException {
        if (dimension.width <= 0 || dimension.width > 3000 || dimension.height < 0 || dimension.height > 3000) {
            throw new IllegalArgumentException("Invalid size (s) provided: both sides must be in range [1; 3000]");
        }
    }

    private void validateBorderRadius(Integer borderRadius) {
        if (borderRadius == null) {
            return;
        }

        if (borderRadius < 0) {
            throw new IllegalArgumentException("Invalid borderRadius (r) provided: the value must not be less than 0");
        }
    }
}
