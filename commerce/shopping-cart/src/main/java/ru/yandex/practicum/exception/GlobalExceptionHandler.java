package ru.yandex.practicum.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            NotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(NOT_FOUND.value())
                .error(NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DeactivateShoppingCart.class)
    public ResponseEntity<ErrorResponse> handleDeactivateShoppingCart(
            DeactivateShoppingCart ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(CONFLICT.value())
                .error(CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    public ResponseEntity<ErrorResponse> handleNoProductsInShoppingCart(
            NoProductsInShoppingCartException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(BAD_REQUEST.value())
                .error(BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NotAuthorizedUserException.class)
    public ResponseEntity<ErrorResponse> handleNotAuthorizedUser(
            NotAuthorizedUserException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(UNAUTHORIZED.value())
                .error(UNAUTHORIZED.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(UNAUTHORIZED).body(errorResponse);
    }
}