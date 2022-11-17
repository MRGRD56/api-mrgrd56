package ru.mrgrd56.api.ocr.services;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import ru.mrgrd56.api.ocr.model.ThresholdType;
import ru.mrgrd56.api.utils.FileResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OcrService {
    public Mat loadImage(byte[] bytes, boolean isGrayscale) {
        return Imgcodecs.imdecode(new MatOfByte(bytes), isGrayscale ? Imgcodecs.IMREAD_GRAYSCALE : Imgcodecs.IMREAD_UNCHANGED);
    }

    public byte[] matToBytes(Mat mat) {
        var result = new MatOfByte();
        Imgcodecs.imencode(".png", mat, result);
        return result.toArray();
    }

    public FileResponse matToResponse(Mat mat) {
        var bytes = matToBytes(mat);
        return FileResponse.inline(bytes, MediaType.IMAGE_PNG);
    }

    public Mat threshold(Mat image, double threshold, double maxval, ThresholdType type) {
        var result = new Mat();
        Imgproc.threshold(image, result, threshold, maxval, type.getValue());
//        Imgproc.adaptiveThreshold(image, result, maxval, Imgproc.ADAPTIVE_THRESH_MEAN_C, type.getValue(), 11, 12);
        return result;
    }

    public Mat cropMain(Mat image) {
        var result = image.clone();

//        var rectKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
//        Imgproc.dilate(image, result, rectKernel);

        var contours = new ArrayList<MatOfPoint>();
        var hierarchy = new Mat();
        Imgproc.findContours(result, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        var matOfPoint2f = new MatOfPoint2f();
        matOfPoint2f.fromList(contours.stream().flatMap(a -> Arrays.stream(a.toArray())).toList());

        var minAreaRectRotated = Imgproc.minAreaRect(matOfPoint2f);
        var minAreaRect = minAreaRectRotated.boundingRect();
        var padding = 3;
        minAreaRect.x = Math.max(0, minAreaRect.x - padding);
        minAreaRect.y = Math.max(0, minAreaRect.y - padding);
        minAreaRect.width = Math.min(result.width() - minAreaRect.x, minAreaRect.width + padding * 2);
        minAreaRect.height = Math.min(result.height() - minAreaRect.y, minAreaRect.height + padding * 2);

//        var minAreaPoints = new Mat();
//        Imgproc.boxPoints(minAreaRectRotated, minAreaPoints);
        var mainRect = new Mat(result, minAreaRect);
//        Imgproc.drawContours(result, contours, 0, new Scalar(0x00, 0xff, 0x00), 2);
        return mainRect;
    }
}
