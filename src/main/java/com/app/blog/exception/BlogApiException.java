package com.app.blog.exception;

import org.springframework.http.HttpStatus;

public class BlogApiException extends RuntimeException {
	private HttpStatus status;

	public BlogApiException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
