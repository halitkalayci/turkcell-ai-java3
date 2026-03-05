package com.turkcell.orderservice.adapter.in.web;

import com.turkcell.orderservice.adapter.in.web.dto.response.ErrorResponse;
import com.turkcell.orderservice.domain.model.InsufficientStockException;
import com.turkcell.orderservice.domain.model.InvalidStatusTransitionException;
import com.turkcell.orderservice.domain.model.OrderAlreadyCancelledException;
import com.turkcell.orderservice.domain.model.OrderNotFoundException;
import com.turkcell.orderservice.domain.model.ProductNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("ORDER_NOT_FOUND", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        ErrorResponse error = new ErrorResponse("INSUFFICIENT_STOCK", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_STATUS_TRANSITION", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(OrderAlreadyCancelledException.class)
    public ResponseEntity<ErrorResponse> handleOrderAlreadyCancelled(OrderAlreadyCancelledException ex) {
        ErrorResponse error = new ErrorResponse("ORDER_ALREADY_CANCELLED", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", "Validation failed", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", "Validation failed", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
