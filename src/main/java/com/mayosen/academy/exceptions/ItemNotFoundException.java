package com.mayosen.academy.exceptions;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException() {
        super("Item с таким id не существует");
    }
}
