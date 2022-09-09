package com.mayosen.academy.controllers;

import com.mayosen.academy.exceptions.ItemNotFoundException;
import com.mayosen.academy.exceptions.ParentItemNotFoundException;
import com.mayosen.academy.responses.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ValidationException;

@ControllerAdvice
public class ExceptionAdvice {
    // TODO: Сделать сообщения стандартными

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationException() {
        return ResponseEntity.badRequest().body(new ErrorResponse(400, "Validation failed"));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleServiceValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                400, String.format("Validation failed. %s", e.getMessage())
        ));
    }

    @ExceptionHandler({
            ItemNotFoundException.class,
            ParentItemNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundException(Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                404, String.format("Item not found. %s", e.getMessage())));
    }
}
