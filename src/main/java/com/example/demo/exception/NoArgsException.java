package com.example.demo.exception;

public class NoArgsException extends Exception {
    private static final long serialVersionUID = -186139195386774363L;

    public NoArgsException() {
        super("Argument could not be nullable");
    }
}
