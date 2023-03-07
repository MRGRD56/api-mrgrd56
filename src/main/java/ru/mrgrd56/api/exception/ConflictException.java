package ru.mrgrd56.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ConflictException extends ResponseStatusException {
    public ConflictException() {
        super(HttpStatus.CONFLICT);
    }

    public ConflictException(String reason) {
        super(HttpStatus.CONFLICT, reason);
    }

    public ConflictException(String reason, Throwable cause) {
        super(HttpStatus.CONFLICT, reason, cause);
    }
}
