package com.app.blog.controller;

import com.app.blog.dto.comment.CommentDto;
import com.app.blog.dto.comment.CreateCommentDto;
import com.app.blog.exception.PostCommentMismatchException;
import com.app.blog.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This class contains tests of the controller for Comment resource. It utilizes
 * H2 in-memory database and data.sql initialization script.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentControllerTests {
	private final String BASE_URI = "/api/v1";

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	/**
	 * Creates new comment (User role), positive (200 OK)
	 */
	@Test
	@Order(1)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void createCommentTestPositive() throws Exception {
		long postId = 1; // see data.sql
		String POSTFIX = String.format("/posts/%s/comments", postId);

		CreateCommentDto createCommentDto = new CreateCommentDto();
		createCommentDto.setName("CommentName-5");
		createCommentDto.setEmail("email-5@email.com");
		createCommentDto.setBody("CommentBody-5");
		int commentId = 5; // see data.sql

		mockMvc.perform(post(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createCommentDto))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(equalTo(commentId))))
				.andExpect(jsonPath("$.name", is(equalTo(createCommentDto.getName()))))
				.andExpect(jsonPath("$.email", is(equalTo(createCommentDto.getEmail()))))
				.andExpect(jsonPath("$.body", is(equalTo(createCommentDto.getBody()))));
	}

	/**
	 * Creates new comment (User role). Negative, no post with given id (404 Not
	 * found)
	 */
	@Test
	@Order(2)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void createCommentTestNegativePostNotFound() throws Exception {
		long wrongPostId = 100;
		String POSTFIX = String.format("/posts/%s/comments", wrongPostId);

		CreateCommentDto createCommentDto = new CreateCommentDto();
		createCommentDto.setName("CommentName-5");
		createCommentDto.setEmail("email-5@email.com");
		createCommentDto.setBody("CommentBody-5");

		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongPostId);

		mockMvc.perform(post(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createCommentDto))).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is(expectedException.getMessage())))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // NOT_FOUND
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 404
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}

	/**
	 * Retrieves comments by post id, positive (200 OK)
	 */
	@Test
	@Order(3)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void getCommentsByPostIdTestPositive() throws Exception {
		long postId = 2; // see data.sql
		String POSTFIX = String.format("/posts/%s/comments", postId);

		MvcResult mvcResult = mockMvc.perform(get(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		String responseBody = mvcResult.getResponse().getContentAsString();
		JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
		Iterator<JsonNode> elements = jsonNode.elements();
		List<JsonNode> commentNodes = new ArrayList<>();
		while (elements.hasNext())
			commentNodes.add(elements.next());

		List<CommentDto> expectedComments = new ArrayList<>();
		// comment_id=1, see data.sql
		CommentDto commentDto1 = new CommentDto();
		commentDto1.setName("CommentName-3");
		commentDto1.setEmail("email-3@email.com");
		commentDto1.setBody("CommentBody-3");
		expectedComments.add(commentDto1);
		// comment_id=2, see data.sql
		CommentDto commentDto2 = new CommentDto();
		commentDto2.setName("CommentName-4");
		commentDto2.setEmail("email-4@email.com");
		commentDto2.setBody("CommentBody-4");
		expectedComments.add(commentDto2);

		int i = 0;
		for (JsonNode commentNode : commentNodes) {
			String id = commentNode.path("id").asText();
			String name = commentNode.path("name").asText();
			String email = commentNode.path("email").asText();
			String body = commentNode.path("body").asText();
			assertThat(Integer.valueOf(id)).isEqualTo(i + 3); // id=3, id=4
			assertThat(name).isEqualTo(expectedComments.get(i).getName());
			assertThat(email).isEqualTo(expectedComments.get(i).getEmail());
			assertThat(body).isEqualTo(expectedComments.get(i).getBody());
			i++;
		}
	}

	/**
	 * Retrieves comments by post id, positive (200 OK), empty list
	 */
	@Test
	@Order(4)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void getCommentsByPostIdTestPositiveEmpty() throws Exception {
		long postId = 3; // see data.sql
		String POSTFIX = String.format("/posts/%s/comments", postId);

		MvcResult mvcResult = mockMvc.perform(get(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		String responseBody = mvcResult.getResponse().getContentAsString();
		assertThat(responseBody).isEqualTo("[]");
	}

	/**
	 * Retrieves comments by post id. Negative, no post with given id (404 Not
	 * found)
	 */
	@Test
	@Order(5)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void getCommentsByPostIdTestNegativePostNotFound() throws Exception {
		long wrongPostId = 100L;
		String POSTFIX = String.format("/posts/%s/comments", wrongPostId);
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongPostId);

		mockMvc.perform(get(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is(equalTo(expectedException.getMessage()))))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // NOT_FOUND
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 404
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}

	/**
	 * Retrieves comment by id, positive (200 OK)
	 */
	@Test
	@Order(6)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void getCommentByIdTestPositive() throws Exception {
		long postId = 1;
		long commentId = 1;
		String POSTFIX = String.format("/posts/%s/comments/%s", postId, commentId);

		mockMvc.perform(get(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is(equalTo("CommentName-1"))))
				.andExpect(jsonPath("$.email", is(equalTo("email-1@email.com"))))
				.andExpect(jsonPath("$.body", is(equalTo("CommentBody-1"))));
	}

	/**
	 * Retrieves comment by id. Negative, no post with provided id (404 Not found)
	 */
	@Test
	@Order(7)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void getCommentByIdTestNegativePostNotFound() throws Exception {
		long wrongPostId = 100;
		long randomCommentId = 100;
		String POSTFIX = String.format("/posts/%s/comments/%s", wrongPostId, randomCommentId);
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongPostId);

		mockMvc.perform(get(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is(equalTo(expectedException.getMessage()))))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // NOT_FOUND
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 404
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}

	/**
	 * Retrieves comment by id. Negative, valid post id, but no comment with given
	 * id (404 Not found)
	 */
	@Test
	@Order(8)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void getCommentByIdTestNegativeCommentNotFound() throws Exception {
		long postId = 1;
		long wrongCommentId = 100;
		String POSTFIX = String.format("/posts/%s/comments/%s", postId, wrongCommentId);
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Comment", "id", wrongCommentId);

		mockMvc.perform(get(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is(equalTo(expectedException.getMessage()))))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // NOT_FOUND
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 404
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}

	/**
	 * Retrieves comment by id. Negative, valid post id and comment id, but comment
	 * does not belong to the post (400 Bad request)
	 */
	@Test
	@Order(9)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void getCommentByIdTestNegativePostCommentMismatch() throws Exception {
		long postId = 1;
		long commentId = 3;
		String POSTFIX = String.format("/posts/%s/comments/%s", postId, commentId);
		PostCommentMismatchException expectedException = new PostCommentMismatchException(postId, commentId);

		mockMvc.perform(get(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is(equalTo(expectedException.getMessage()))))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // BAD_REQUEST
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 400
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}

	/**
	 * Updates comment by id, positive (200 OK)
	 */
	@Test
	@Order(10)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void updateCommentByIdPositive() throws Exception {
		int postId = 2;
		int commentId = 4;
		String POSTFIX = String.format("/posts/%s/comments/%s", postId, commentId);
		CommentDto updateBodyDto = new CommentDto();
		updateBodyDto.setName("CommentName-4 Updated");
		updateBodyDto.setEmail("email-4-updated@email.com");
		updateBodyDto.setBody("CommentBody-4 Updated");

		mockMvc.perform(put(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateBodyDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(equalTo(commentId))))
				.andExpect(jsonPath("$.name", is(equalTo("CommentName-4 Updated"))))
				.andExpect(jsonPath("$.email", is(equalTo("email-4-updated@email.com"))))
				.andExpect(jsonPath("$.body", is(equalTo("CommentBody-4 Updated"))));
	}

	/**
	 * Updates comment by id. Negative, post with given id not found (404 Not found)
	 */
	@Test
	@Order(11)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void updateCommentByIdNegativePostNotFound() throws JsonProcessingException, Exception {
		long wrongPostId = 100;
		long randomCommentId = 100;
		String POSTFIX = String.format("/posts/%s/comments/%s", wrongPostId, randomCommentId);
		CommentDto updateBodyDto = new CommentDto();
		updateBodyDto.setName("CommentName-4 Updated");
		updateBodyDto.setEmail("email-4-updated@email.com");
		updateBodyDto.setBody("CommentBody-4 Updated");
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongPostId);

		mockMvc.perform(put(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateBodyDto))).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is(equalTo(expectedException.getMessage()))))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // NOT_FOUND
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 404
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}

	/**
	 * Updates comment by id. Negative, comment with given id not found (404 Not
	 * found)
	 */
	@Test
	@Order(12)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void updateCommentByIdNegativeCommentNotFound() throws JsonProcessingException, Exception {
		long postId = 2;
		long wrongCommentId = 100;
		String POSTFIX = String.format("/posts/%s/comments/%s", postId, wrongCommentId);
		CommentDto updateBodyDto = new CommentDto();
		updateBodyDto.setName("CommentName-4 Updated");
		updateBodyDto.setEmail("email-4-updated@email.com");
		updateBodyDto.setBody("CommentBody-4 Updated");
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Comment", "id", wrongCommentId);

		mockMvc.perform(put(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateBodyDto))).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is(equalTo(expectedException.getMessage()))))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // NOT_FOUND
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 404
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}

	/**
	 * Updates comment by id. Negative, post comment mismatch (400 Bad request)
	 */
	@Test
	@Order(13)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	public void updateCommentByIdNegativePostCommentMismatch() throws JsonProcessingException, Exception {
		long postId = 2;
		long commentId = 2;
		String POSTFIX = String.format("/posts/%s/comments/%s", postId, commentId);
		CommentDto updateBodyDto = new CommentDto();
		updateBodyDto.setName("CommentName-4 Updated");
		updateBodyDto.setEmail("email-4-updated@email.com");
		updateBodyDto.setBody("CommentBody-4 Updated");
		PostCommentMismatchException expectedException = new PostCommentMismatchException(postId, commentId);

		mockMvc.perform(put(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateBodyDto))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is(equalTo(expectedException.getMessage()))))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // BAD_REQUEST
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 400
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}
}
