package com.shipdream.lib.android.mvc;

public class MvcGraphException extends RuntimeException {
    public MvcGraphException(String message) {
        super(message);
    }

    public MvcGraphException(String message, Throwable cause) {
        super(message, cause);
    }
}