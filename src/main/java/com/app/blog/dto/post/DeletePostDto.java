package com.app.blog.dto.post;

import lombok.Data;

@Data
public class DeletePostDto {
	String message;

	public DeletePostDto(long id) {
		this.message = String.format("Post with id=%s deleted", id);
	}
}
