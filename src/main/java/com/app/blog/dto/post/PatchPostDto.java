package com.app.blog.dto.post;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Data
public class PatchPostDto {
	@NotEmpty
	String fieldName; // setTitle, setContent, setDescription

	@NotEmpty
	String fieldValue;
}
