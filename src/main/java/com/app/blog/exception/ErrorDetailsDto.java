package com.app.blog.exception;

import org.springframework.http.HttpStatus;

import java.util.Date;

public class ErrorDetailsDto {
	private Date timestamp;
	private String message;
	private String details;
	private HttpStatus httpStatus;
	private int code;
	private StackTraceElement[] stackTrace;

	public ErrorDetailsDto(Date timestamp, String message, String details, HttpStatus httpStatus,
			StackTraceElement[] stackTrace) {
		this.timestamp = timestamp;
		this.message = message;
		this.details = details;
		this.httpStatus = httpStatus;
		this.stackTrace = stackTrace;
		this.code = httpStatus.value();
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getMessage() {
		return message;
	}

	public String getDetails() {
		return details;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public int getCode() {
		return code;
	}

	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}
}
