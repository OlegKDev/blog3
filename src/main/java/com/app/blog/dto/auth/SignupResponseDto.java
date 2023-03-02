package com.app.blog.dto.auth;

import lombok.Data;

@Data
public class SignupResponseDto {
	Long id;
	String email;
	String username;
	String name;
}
