package com.app.blog.exception;

import com.app.blog.exception.BlogApiException;
import com.app.blog.exception.ErrorDetailsDto;
import com.app.blog.exception.PostCommentMismatchException;
import com.app.blog.exception.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorDetailsDto> handleResourceNotFoundException(ResourceNotFoundException exception,
			WebRequest webRequest) {
		ErrorDetailsDto errorDetailsDto = new ErrorDetailsDto(new Date(), exception.getMessage(),
				webRequest.getDescription(false), exception.getStatus(), exception.getStackTrace());

		return new ResponseEntity<ErrorDetailsDto>(errorDetailsDto, exception.getStatus());
	}

	@ExceptionHandler(PostCommentMismatchException.class)
	public ResponseEntity<ErrorDetailsDto> handlePostCommentMismatchException(PostCommentMismatchException exception,
			WebRequest webRequest) {
		ErrorDetailsDto errorDetailsDto = new ErrorDetailsDto(new Date(), exception.getMessage(),
				webRequest.getDescription(false), exception.getStatus(), exception.getStackTrace());

		return new ResponseEntity<ErrorDetailsDto>(errorDetailsDto, exception.getStatus());
	}

	@ExceptionHandler(BlogApiException.class)
	public ResponseEntity<ErrorDetailsDto> handleBlogApiException(BlogApiException exception, WebRequest webRequest) {
		ErrorDetailsDto errorDetailsDto = new ErrorDetailsDto(new Date(), exception.getMessage(),
				webRequest.getDescription(false), exception.getStatus(), exception.getStackTrace());

		return new ResponseEntity<ErrorDetailsDto>(errorDetailsDto, exception.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String message = error.getDefaultMessage();
			errors.put(fieldName, message);
		});

		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
	}
}
