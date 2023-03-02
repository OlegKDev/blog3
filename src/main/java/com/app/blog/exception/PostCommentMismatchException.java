package com.app.blog.exception;

import org.springframework.http.HttpStatus;

public class PostCommentMismatchException extends RuntimeException {
	private final HttpStatus status = HttpStatus.BAD_REQUEST;

	public PostCommentMismatchException(String message) {
		super(message);
	}

	public PostCommentMismatchException(long postId, long commentId) {
		super(String.format("Comment with id=%s does not belong to the post with id=%s", commentId, postId));
	}

	public HttpStatus getStatus() {
		return status;
	}
}
