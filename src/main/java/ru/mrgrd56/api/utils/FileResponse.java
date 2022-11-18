package ru.mrgrd56.api.utils;

import org.springframework.http.*;
import org.springframework.lang.NonNull;

public class FileResponse extends ResponseEntity<byte[]> {

    private final MediaType mime;
    private final String fileName;

    private FileResponse(@NonNull byte[] file, MediaType mime, boolean isDownloadable, String fileName) {
        super(file, getHeaders(file, mime, isDownloadable, fileName), HttpStatus.OK);
        this.mime = mime;
        this.fileName = fileName;
    }

    public static FileResponse inline(@NonNull byte[] file, MediaType mime) {
        return new FileResponse(file, mime, false, null);
    }

    public static FileResponse inline(@NonNull byte[] file, MediaType mime, String fileName) {
        return new FileResponse(file, mime, false, fileName);
    }

    public static FileResponse attachment(@NonNull byte[] file, MediaType mime) {
        return new FileResponse(file, mime, true, null);
    }

    public static FileResponse attachment(@NonNull byte[] file, MediaType mime, String fileName) {
        return new FileResponse(file, mime, true, fileName);
    }

    public MediaType getMime() {
        return mime;
    }

    public String getFileName() {
        return fileName;
    }

    private static HttpHeaders getHeaders(@NonNull byte[] file, MediaType mime, boolean isDownloadable, String fileName) {
        var headers = new HttpHeaders();
        headers.setContentType(mime);
        headers.setContentLength(file.length);

        var contentDispositionBuilder = isDownloadable
                ? ContentDisposition.attachment()
                : ContentDisposition.inline();

        if (fileName != null) {
            contentDispositionBuilder.filename(fileName);
        }

        headers.setContentDisposition(contentDispositionBuilder.build());

        return headers;
    }
}
