package com.mayosen.academy.controllers;

import com.mayosen.academy.responses.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ValidationException;

@ControllerAdvice
public class ExceptionAdvice {
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            ValidationException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationException() {
        return ResponseEntity.badRequest().body(new ErrorResponse(400, "Validation failed"));
    }
}
