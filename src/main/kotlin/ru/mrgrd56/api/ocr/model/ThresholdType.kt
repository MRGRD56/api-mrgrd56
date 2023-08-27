package ru.mrgrd56.api.ocr.model

import org.opencv.imgproc.Imgproc

enum class ThresholdType(val value: Int) {
    BINARY(Imgproc.THRESH_BINARY),
    BINARY_INV(Imgproc.THRESH_BINARY_INV),
    TRUNC(Imgproc.THRESH_TRUNC),
    TOZERO(Imgproc.THRESH_TOZERO),
    TOZERO_INV(Imgproc.THRESH_TOZERO_INV),
    MASK(Imgproc.THRESH_MASK),
    OTSU(Imgproc.THRESH_OTSU),
    TRIANGLE(Imgproc.THRESH_TRIANGLE)
}
