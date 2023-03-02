package com.app.blog.dto.auth;

import lombok.Data;

@Data
public class SignupCreateDto {
    private String name;
    private String username;
    private String email;
    private String password;
}
