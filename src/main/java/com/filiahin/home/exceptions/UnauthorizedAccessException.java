package com.filiahin.home.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = UnauthorizedAccessException.ACCESS_DENIED)
public class UnauthorizedAccessException extends RuntimeException {

    public static final String ACCESS_DENIED = "Access denied";

    public UnauthorizedAccessException() {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ACCESS_DENIED);
    }

    public UnauthorizedAccessException(String message) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }
}
