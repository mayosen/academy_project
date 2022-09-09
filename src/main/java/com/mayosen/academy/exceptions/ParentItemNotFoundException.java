package com.mayosen.academy.exceptions;

public class ParentItemNotFoundException extends RuntimeException {
    public ParentItemNotFoundException() {
        super("Родитель с таким id не существует");
    }
}
