package com.caerus.audit.server.exception;

import com.caerus.audit.server.enums.ErrorType;
import com.caerus.audit.server.service.LoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

  private final LoggingService loggingService;

    @ExceptionHandler(ServerSettingsException.class)
    public ResponseEntity<Object> handleServerSettingsException(ServerSettingsException ex) {
        log.error("ServerSettingsException: {}", ex.getMessage(), ex);
        loggingService.logError(ErrorType.NORMAL, ex.getMessage(), ex.getClass().getName());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Server settings error: " + ex.getMessage());
    }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleException(Exception ex) {
    log.error("Exception occurred: {}", ex.getMessage());
    loggingService.logError(ErrorType.NORMAL, ex.getMessage(), ex.getClass().getName());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("An unexpected error occurred: " + ex.getMessage());
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
    log.error("RuntimeException occurred: {}", ex.getMessage());
    loggingService.logError(ErrorType.NORMAL, ex.getMessage(), ex.getClass().getName());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Runtime error: " + ex.getMessage());
  }
}
