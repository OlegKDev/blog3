package com.app.blog.dto.post;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
//@AllArgsConstructor
public class CreatePostDto {
	@NotEmpty
	@Size(min = 2, message = "Post title should have at least 2 characters")
	private String title;

	@NotEmpty
	@Size(min = 10, message = "Post description should have at least 10 characters")
	private String description;

	@NotEmpty
	@Size(min = 10, message = "Post content should have at least 10 characters")
	private String content;
}
