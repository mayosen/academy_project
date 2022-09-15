package com.mayosen.academy.exceptions;

/**
 * Исключение для случая, когда запрашиваемый элемент не найден.
 */
public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException() {
        super("Item с таким id не существует");
    }
}
