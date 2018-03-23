package org.galatea.starter.entrypoint;

import lombok.extern.slf4j.Slf4j;

import org.galatea.starter.entrypoint.exception.EntityNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

/**
 * A centralized REST handler that intercepts exceptions thrown by controller calls, enabling a
 * custom response to be returned.
 *
 * The returned ResponseEntity body object will be serialised into JSON (hence the need for the
 * ApiError wrapper class).
 */
@ControllerAdvice
@Slf4j
public class RestExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  protected ResponseEntity<Object> handleEntityNotFound(final EntityNotFoundException exception) {
    ApiError error = new ApiError(HttpStatus.NOT_FOUND, exception.getMessage());
    return buildResponseEntity(error);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      final HttpMessageNotReadableException exception) {
    String errorMessage = "Incorrectly formatted message.  Please consult the documentation.";
    ApiError error = new ApiError(HttpStatus.BAD_REQUEST, errorMessage);
    return buildResponseEntity(error);
  }

  @ExceptionHandler(DataAccessException.class)
  protected ResponseEntity<Object> handleDataAccessException(final DataAccessException exception) {
    log.error("Unexpected data access error", exception);

    String errorMessage = "An internal application error occurred.";
    ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    return buildResponseEntity(error);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<Object> handleConstraintViolation(
      final ConstraintViolationException exception) {
    log.debug("Invalid input data sent", exception);
    String errorMessage = ConstraintViolationMessageFormatter.toMessage(exception);

    ApiError error = new ApiError(HttpStatus.BAD_REQUEST, errorMessage);
    return buildResponseEntity(error);
  }

  private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
    return new ResponseEntity<>(apiError, apiError.getStatus());
  }

}
