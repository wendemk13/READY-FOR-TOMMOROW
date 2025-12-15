package com.example.IPS.IPS.exceptions;

public class MissingJwtTokenException extends RuntimeException {
    public MissingJwtTokenException(String message) {
        super(message);
    }
}
