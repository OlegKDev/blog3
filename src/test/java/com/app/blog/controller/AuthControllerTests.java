package com.app.blog.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.app.blog.dto.auth.LoginDto;
import com.app.blog.dto.auth.SignupCreateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class contains tests of the Auth controller. It utilizes H2 in-memory
 * database and data.sql initialization script.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerTests {
	private final String BASE_URI = "/api/v1/auth";

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	// Create a new user

	@Test
	@Order(1)
	public void registerUserTestNegativeUsernameAlreadyExists() throws JsonProcessingException, Exception {
		String POSTFIX = "/signup";
		SignupCreateDto signupCreateDto = new SignupCreateDto();
		signupCreateDto.setEmail("user-1@email.com");
		signupCreateDto.setUsername("user-1@email.com");
		signupCreateDto.setName("user-1");
		signupCreateDto.setPassword("user-1");

		String errorMessage = String.format("User with username %s already exists", signupCreateDto.getUsername());

		mockMvc.perform(post(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupCreateDto))).andDo(print())
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorMessage", is(equalTo(errorMessage))));
	}

	@Test
	@Order(2)
	public void registerUserTestNegativeEmailAlreadyExists() throws JsonProcessingException, Exception {
		String POSTFIX = "/signup";
		SignupCreateDto signupCreateDto = new SignupCreateDto();
		signupCreateDto.setEmail("user-1@email.com");
		signupCreateDto.setUsername("user-new@email.com");
		signupCreateDto.setName("user-1");
		signupCreateDto.setPassword("user-1");

		String errorMessage = String.format("User with email %s already exists", signupCreateDto.getEmail());

		mockMvc.perform(post(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupCreateDto))).andDo(print())
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorMessage", is(equalTo(errorMessage))));
	}

	@Test
	@Order(3)
	public void registerUserTestPositive() throws JsonProcessingException, Exception {
		String POSTFIX = "/signup";
		SignupCreateDto signupCreateDto = new SignupCreateDto();
		signupCreateDto.setEmail("user-new@email.com");
		signupCreateDto.setUsername("user-new@email.com");
		signupCreateDto.setName("user-new");
		signupCreateDto.setPassword("user-new");

		int createdId = 3;

		mockMvc.perform(post(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupCreateDto))).andDo(print())
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id", is(equalTo(createdId))))
				.andExpect(jsonPath("$.name", is(equalTo(signupCreateDto.getName()))))
				.andExpect(jsonPath("$.username", is(equalTo(signupCreateDto.getUsername()))))
				.andExpect(jsonPath("$.email", is(equalTo(signupCreateDto.getEmail()))));
	}

	// Login the user

	@Test
	@Order(4)
	public void loginUserTestPositive() throws JsonProcessingException, Exception {
		// Get a Jwt token
		String POSTFIX = "/login";
		LoginDto loginDto = new LoginDto();
		loginDto.setUsernameOrEmail("user-1@email.com");
		loginDto.setPassword("user-1");
		String tokenType = "Bearer";
		int tokenExpiration = 60_000; // 1 second = 1000 milliseconds

		MvcResult response = mockMvc.perform(post(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDto)))
//				.andDo(print())
				.andExpect(status().isOk()).andExpect(jsonPath("$.tokenType", is(equalTo(tokenType))))
				.andExpect(jsonPath("$.accessToken", is(notNullValue()))).andReturn();

		String body = response.getResponse().getContentAsString();
		JsonNode root = new ObjectMapper().readTree(body);
		String token = root.path("accessToken").asText();
		String headerToken = String.format("Bearer %s", token);
//		System.out.println(String.format("headerToken: %s", headerToken));

		// Request a protected resource
		String protectedRequestURL = "/api/v1/posts";
		mockMvc.perform(
				get(protectedRequestURL).contentType(MediaType.APPLICATION_JSON).header("Authorization", headerToken))
//				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	@Order(5)
	public void loginUserTestNegativeTokenExpired() throws JsonProcessingException, Exception {
		// Get a Jwt token
		String POSTFIX = "/login";
		LoginDto loginDto = new LoginDto();
		loginDto.setUsernameOrEmail("user-1@email.com");
		loginDto.setPassword("user-1");
		String tokenType = "Bearer";
		int tokenExpiration = 60_000; // 1 second = 1000 milliseconds

		MvcResult response = mockMvc.perform(post(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDto)))
//				.andDo(print())
				.andExpect(status().isOk()).andExpect(jsonPath("$.tokenType", is(equalTo(tokenType))))
				.andExpect(jsonPath("$.accessToken", is(notNullValue()))).andReturn();

		String body = response.getResponse().getContentAsString();
		JsonNode root = new ObjectMapper().readTree(body);
		String token = root.path("accessToken").asText();
		String headerToken = String.format("Bearer %s", token);
//		System.out.println(String.format("headerToken: %s", headerToken));

		// Wait to expire the token
		System.out.println(String.format("\nWaiting %s seconds for the token expiration", tokenExpiration / 1000));
		Thread.sleep(tokenExpiration);

		// Request a protected resource
		String protectedRequestURL = "/api/v1/posts/1";
		mockMvc.perform(
				get(protectedRequestURL).contentType(MediaType.APPLICATION_JSON).header("Authorization", headerToken))
				.andDo(print()).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.errorMessage", is(equalTo("Expired Jwt token"))));
	}
}
