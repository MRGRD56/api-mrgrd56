package ru.mrgrd56.api.ocr.services

import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import ru.mrgrd56.api.ocr.model.ThresholdType
import ru.mrgrd56.api.utils.FileResponse
import ru.mrgrd56.api.utils.FileResponse.Companion.inline
import java.util.*
import kotlin.math.max
import kotlin.math.min

@Service
class OpenCvService {
    fun loadImage(bytes: ByteArray, isGrayscale: Boolean): Mat {
        return Imgcodecs.imdecode(
            MatOfByte(*bytes),
            if (isGrayscale) Imgcodecs.IMREAD_GRAYSCALE else Imgcodecs.IMREAD_UNCHANGED
        )
    }

    fun matToBytes(mat: Mat?): ByteArray {
        val result = MatOfByte()
        Imgcodecs.imencode(".png", mat, result)
        return result.toArray()
    }

    fun matToResponse(mat: Mat?): FileResponse {
        val bytes = matToBytes(mat)
        return inline(bytes, MediaType.IMAGE_PNG, "image.png")
    }

    fun threshold(image: Mat?, threshold: Double, maxval: Double, type: ThresholdType): Mat {
        val result = Mat()
        Imgproc.threshold(image, result, threshold, maxval, type.value)
        //        Imgproc.adaptiveThreshold(image, result, maxval, Imgproc.ADAPTIVE_THRESH_MEAN_C, type.getValue(), 11, 12);
        return result
    }

    fun cropMain(image: Mat): Mat {
        val result = image.clone()

//        var rectKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
//        Imgproc.dilate(image, result, rectKernel);
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(result, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        val matOfPoint2f = MatOfPoint2f()
        matOfPoint2f.fromList(
            contours.stream()
                .flatMap { a: MatOfPoint -> Arrays.stream(a.toArray()) }
                .toList()
        )
        val minAreaRectRotated = Imgproc.minAreaRect(matOfPoint2f)
        val minAreaRect = minAreaRectRotated.boundingRect()
        val padding = 3
        minAreaRect.x = max(0.0, (minAreaRect.x - padding).toDouble()).toInt()
        minAreaRect.y = max(0.0, (minAreaRect.y - padding).toDouble()).toInt()
        minAreaRect.width =
            min((result.width() - minAreaRect.x).toDouble(), (minAreaRect.width + padding * 2).toDouble())
                .toInt()
        minAreaRect.height =
            min((result.height() - minAreaRect.y).toDouble(), (minAreaRect.height + padding * 2).toDouble())
                .toInt()

//        var minAreaPoints = new Mat();
//        Imgproc.boxPoints(minAreaRectRotated, minAreaPoints);
        //        Imgproc.drawContours(result, contours, 0, new Scalar(0x00, 0xff, 0x00), 2);
        return Mat(result, minAreaRect)
    }
}
