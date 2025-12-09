package com.sales.sales.exceptions;

public class JwtTokenExpiredException extends RuntimeException {
    public JwtTokenExpiredException(String message ) {
        super(message);
    }
}
