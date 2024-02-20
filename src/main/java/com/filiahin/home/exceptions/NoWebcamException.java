package com.filiahin.home.exceptions;

public class NoWebcamException extends RuntimeException {
    public NoWebcamException() {
        throw new RuntimeException("No webcam found!");
    }
}
