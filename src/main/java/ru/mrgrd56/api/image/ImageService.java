package ru.mrgrd56.api.image;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageService {
    public byte[] generateFilledImage(
            Color color,
            Dimension size
    ) {
        validateSize(size);

        int width = size.width;
        int height = size.height;

        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);

        var result = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toByteArray();
    }

    public Color parseColor(String input) throws IllegalArgumentException {
        var result = Color.getColor(input);
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

        throw new IllegalArgumentException("Invalid size provided");
    }

    private void validateSize(Dimension dimension) throws IllegalArgumentException {
        if (dimension.width <= 0 || dimension.width > 3000 || dimension.height < 0 || dimension.height > 3000) {
            throw new IllegalArgumentException("Invalid size provided: both sides must be in range [1; 3000]");
        }
    }
}
