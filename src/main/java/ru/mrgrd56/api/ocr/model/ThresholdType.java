package ru.mrgrd56.api.ocr.model;

import org.opencv.imgproc.Imgproc;

public enum ThresholdType {
    BINARY(Imgproc.THRESH_BINARY),
    BINARY_INV(Imgproc.THRESH_BINARY_INV),
    TRUNC(Imgproc.THRESH_TRUNC),
    TOZERO(Imgproc.THRESH_TOZERO),
    TOZERO_INV(Imgproc.THRESH_TOZERO_INV),
    MASK(Imgproc.THRESH_MASK),
    OTSU(Imgproc.THRESH_OTSU),
    TRIANGLE(Imgproc.THRESH_TRIANGLE);

    private final int value;

    ThresholdType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
