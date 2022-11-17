package ru.mrgrd56.api;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiMrgrd56Application {

    static {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        OpenCV.loadLocally();
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiMrgrd56Application.class, args);
    }

}
