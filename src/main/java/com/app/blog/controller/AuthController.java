package com.app.blog.controller;

import com.app.blog.dto.auth.ErrorMessageDto;
import com.app.blog.dto.auth.LoginDto;
import com.app.blog.dto.auth.SignupCreateDto;
import com.app.blog.dto.auth.SignupResponseDto;
import com.app.blog.entity.Role;
import com.app.blog.entity.User;
import com.app.blog.repository.RoleRepository;
import com.app.blog.repository.UserRepository;
import com.app.blog.security.JwtAuthResponseDto;
import com.app.blog.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	/**
	 * Creates a new user
	 * 
	 * @param signupDto data for a new user creation
	 * @return ResponseEntity<SignupResponseDto> object (201 Created)
	 * @exception ErrorMessageDto if the user with the same name already exists (400
	 *                            Bad request)
	 * @exception ErrorMessageDto if the user with the same email already exists
	 *                            (400 Bad request)
	 */
	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody SignupCreateDto signupDto) {
		if (userRepository.existsByUsername(signupDto.getUsername())) {
			String message = String.format("User with username %s already exists", signupDto.getUsername());
			ErrorMessageDto errorMessageDto = new ErrorMessageDto();
			errorMessageDto.setErrorMessage(message);
			return new ResponseEntity<>(errorMessageDto, HttpStatus.BAD_REQUEST);
		}

		if (userRepository.existsByEmail(signupDto.getEmail())) {
			String message = String.format("User with email %s already exists", signupDto.getEmail());
			ErrorMessageDto errorMessageDto = new ErrorMessageDto();
			errorMessageDto.setErrorMessage(message);
			return new ResponseEntity<>(errorMessageDto, HttpStatus.BAD_REQUEST);
		}

		User user = new User();
		user.setName(signupDto.getName());
		user.setUsername(signupDto.getUsername());
		user.setEmail(signupDto.getEmail());
		user.setPassword(passwordEncoder.encode(signupDto.getPassword()));

		Role role = roleRepository.findByName("ROLE_ADMIN").get();
		user.setRoles(Collections.singleton(role));
		User newUser = userRepository.save(user);

		SignupResponseDto signupResponseDto = new SignupResponseDto();
		signupResponseDto.setId(newUser.getId());
		signupResponseDto.setEmail(newUser.getEmail());
		signupResponseDto.setUsername(newUser.getUsername());
		signupResponseDto.setName(newUser.getName());

		return new ResponseEntity<>(signupResponseDto, HttpStatus.CREATED);
	}

	/**
	 * Logins a user
	 * 
	 * @param loginDto credentials data
	 * @return ResponseEntity<JwtAuthResponseDto> object (token) (200 OK)
	 * @exception BlogApiException if a token is invalid
	 */
	@PostMapping("/login")
	public ResponseEntity<JwtAuthResponseDto> login(@RequestBody LoginDto loginDto) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail(), loginDto.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);

		String token = jwtTokenProvider.generateToken(authentication);
		JwtAuthResponseDto jwtAuthResponseDto = new JwtAuthResponseDto(token);
		return ResponseEntity.ok(jwtAuthResponseDto);
	}
}
