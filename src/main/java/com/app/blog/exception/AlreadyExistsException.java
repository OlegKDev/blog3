package com.app.blog.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends BlogApiException {
	public AlreadyExistsException(String resourceName, String fieldName, String fieldValue) {
		super(String.format("%s with %s='%s' is not correct, it might be unique", resourceName, fieldName, fieldValue),
				HttpStatus.BAD_REQUEST);
	}
}
