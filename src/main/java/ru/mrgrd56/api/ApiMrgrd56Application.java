package ru.mrgrd56.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.List;

@SpringBootApplication
public class ApiMrgrd56Application {
    public ApiMrgrd56Application(Environment environment) {
        var activeProfiles = List.of(environment.getActiveProfiles());
        if (activeProfiles.contains("prod")) {
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        } else {
            nu.pattern.OpenCV.loadLocally();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiMrgrd56Application.class, args);
    }
}
