package com.thusithakit.purchaseservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail handleNotFound(
      ResourceNotFoundException exception
  ) {
    ProblemDetail detail = ProblemDetail.forStatusAndDetail(
        HttpStatus.NOT_FOUND,
        exception.getMessage()
    );

    detail.setTitle("Resource not found");

    return detail;
  }
}
