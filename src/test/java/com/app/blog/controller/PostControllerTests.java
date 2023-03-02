package com.app.blog.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.app.blog.dto.post.CreatePostDto;
import com.app.blog.dto.post.PatchPostDto;
import com.app.blog.dto.post.UpdatePostDto;
import com.app.blog.exception.AlreadyExistsException;
import com.app.blog.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class contains tests of the controller for Post resource. It utilizes H2
 * in-memory database and data.sql initialization script.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostControllerTests {
	private static final String BASE_URI = "/api/v1/posts";

	private static long createdPostId;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Creates new post (User role), positive (201 Created)
	 */
	@Test
	@Order(1)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void createPostAdminTestPositive() throws Exception {
		CreatePostDto createPostDto = new CreatePostDto();
		createPostDto.setTitle("TestTitle");
		createPostDto.setDescription("TestDescription");
		createPostDto.setContent("TestContent");

		MvcResult response = mockMvc
				.perform(post(BASE_URI).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createPostDto)))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id", is(anything())))
				.andExpect(jsonPath("$.title", is(createPostDto.getTitle())))
				.andExpect(jsonPath("$.description", is(createPostDto.getDescription())))
				.andExpect(jsonPath("$.content", is(createPostDto.getContent())))
				.andExpect(jsonPath("$.comments", is(notNullValue()))).andReturn();

		// Extract created post id to use with delete post test
		String body = response.getResponse().getContentAsString();
		JsonNode root = new ObjectMapper().readTree(body);
		String id = root.path("id").asText();
		PostControllerTests.createdPostId = Long.valueOf(id);
	}

	/**
	 * Creates new post (User role). Negative, post with same title already exists
	 * (400 Bad request). See ErrorDetailsDto for BlogApiException.
	 */
	@Test
	@Order(2)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void createPostTestNegativeAlreadyExists() throws Exception {
		CreatePostDto createPostDto = new CreatePostDto();
		createPostDto.setTitle("TestTitle");
		createPostDto.setDescription("TestDescription");
		createPostDto.setContent("TestContent");

		AlreadyExistsException expectedException = new AlreadyExistsException("Post", "title",
				createPostDto.getTitle());

		mockMvc.perform(post(BASE_URI).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createPostDto))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is(equalTo(expectedException.getMessage()))))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // BAD_REQUEST
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 404
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())))
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())));
	}

	/**
	 * Creates new post. Negative, no role has been provided (401 Unauthorized).
	 */
	@Test
	@Order(3)
	void createPostTestUnauthorised() throws Exception {
		CreatePostDto createPostDto = new CreatePostDto();
		createPostDto.setTitle("TestTitleRandom");
		createPostDto.setDescription("TestDescription");
		createPostDto.setContent("TestContent");

		mockMvc.perform(post(BASE_URI).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createPostDto))).andExpect(status().isUnauthorized());
	}

	/**
	 * Creates new post (User role). Negative, violates request body constraints
	 * (400 Bad request).
	 */
	@Test
	@Order(4)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void createPostUserTestNegativeInvalidBody() throws Exception {
		CreatePostDto createPostDto = new CreatePostDto();
		createPostDto.setTitle("T");
		createPostDto.setDescription("D");
		createPostDto.setContent("C");

		String expectedMessageTitle = "Post title should have at least 2 characters";
		String expectedMessageDescription = "Post description should have at least 10 characters";
		String expectedMessageContent = "Post content should have at least 10 characters";

		mockMvc.perform(post(BASE_URI).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createPostDto))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title", is(expectedMessageTitle)))
				.andExpect(jsonPath("$.description", is(expectedMessageDescription)))
				.andExpect(jsonPath("$.content", is(expectedMessageContent)));
	}

	/**
	 * Retrieves post by id (User role), positive (200 OK)
	 */
	@Test
	@Order(5)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void getPostByIdTestPositive() throws Exception {
		int postId = 1; // see data.sql
		String expectedTitle = "PostTitle-1";
		String expectedContent = "PostContent";
		String expectedDescription = "PostDescription";

		String POSTFIX = String.format("/%s", postId);
		mockMvc.perform(get(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(equalTo(postId))))
				.andExpect(jsonPath("$.title", is(equalTo(expectedTitle))))
				.andExpect(jsonPath("$.description", is(equalTo(expectedDescription))))
				.andExpect(jsonPath("$.content", is(equalTo(expectedContent))))
				.andExpect(jsonPath("$.comments", is(notNullValue())));
	}

	/**
	 * Retrieves post by id (User role). Negative, no post with provided id (404 Not
	 * found). See ErrorDetailsDto for ResourceNotFoundException.
	 */
	@Test
	@Order(6)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void getPostByIdTestNegativeNotFound() throws Exception {
		long wrongId = 100;
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongId);

		String POSTFIX = String.format("/%s", wrongId);
		mockMvc.perform(get(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is(equalTo(expectedException.getMessage()))))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // NOT_FOUND
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 400
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())))
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())));
	}

	/**
	 * Retrieves all posts (User role), positive (200 OK)
	 */
	@Test
	@Order(7)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void getAllPostsAdminTest() throws Exception {
		int pageNo = 0;
		int pageSize = 10;
		String sortBy = "id";
		String sortDir = "asc";
		String POSTFIX = String.format("?pageNo=%s&pageSize=%s&sortBy=%s&sortDir=%s", pageNo, pageSize, sortBy,
				sortDir);

		MvcResult mvcResult = mockMvc
				.perform(MockMvcRequestBuilders.get(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.pageNo", is(equalTo(pageNo))))
				.andExpect(jsonPath("$.pageSize", is(equalTo(pageSize))))
				.andExpect(jsonPath("$.totalElements", is(notNullValue())))
				.andExpect(jsonPath("$.totalPages", is(equalTo(1)))).andExpect(jsonPath("$.last", is(equalTo(true))))
				.andExpect(jsonPath("$.posts", is(notNullValue()))).andReturn();

		String body = mvcResult.getResponse().getContentAsString();
		JsonNode root = new ObjectMapper().readTree(body);
		JsonNode posts = root.path("posts");
		// Within data.sql there are at least 4 posts
		for (int i = 0; i < 4; i++) {
			String id = String.valueOf(posts.get(i).path("id"));
			String title = String.valueOf(posts.get(i).path("title"));
			String content = String.valueOf(posts.get(i).path("content"));
			String description = String.valueOf(posts.get(i).path("description"));
			assertThat(id).isNotEmpty();
			assertThat(title).isNotEmpty();
			assertThat(content).isNotEmpty();
			assertThat(description).isNotEmpty();
		}
	}

	/**
	 * Updates post by id (User role), positive (200 OK)
	 */
	@Test
	@Order(8)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void updatePostByIdTestPositive() throws Exception {
		// User post_id=3 for put testing
		int postId = 3;
		UpdatePostDto updatePostDto = new UpdatePostDto();
		updatePostDto.setTitle("PostTitle-3 Updated");
		updatePostDto.setDescription("PostDescription Updated");
		updatePostDto.setContent("PostContent Updated");
		String POSTFIX = String.format("/%s", postId);

		mockMvc.perform(put(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatePostDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(equalTo(postId))))
				.andExpect(jsonPath("$.title", is(equalTo(updatePostDto.getTitle()))))
				.andExpect(jsonPath("$.description", is(equalTo(updatePostDto.getDescription()))))
				.andExpect(jsonPath("$.content", is(equalTo(updatePostDto.getContent()))));
	}

	/**
	 * Updates post by id (User role). Negative, no post with provided id (404 Not
	 * found)
	 */
	@Test
	@Order(9)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void updatePostByIdTestNegativeNotFound() throws Exception {
		int wrongPostId = 100;
		UpdatePostDto updatePostDto = new UpdatePostDto();
		updatePostDto.setTitle("PostTitle-3 Updated");
		updatePostDto.setDescription("PostDescription Updated");
		updatePostDto.setContent("PostContent Updated");
		String POSTFIX = String.format("/%s", wrongPostId);
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongPostId);

		mockMvc.perform(put(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatePostDto))).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is(expectedException.getMessage())))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // NOT_FOUND
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 404
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}

	/**
	 * Updates post by id (User role). Negative, post with given title already
	 * exists (400 Bad request)
	 */
	@Test
	@Order(10)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void updatePostByIdTestNegativeAlreadyExists() throws Exception {
		int postId = 3;
		UpdatePostDto updatePostDto = new UpdatePostDto();
		updatePostDto.setTitle("PostTitle-1"); // already exists with post_id=1
		updatePostDto.setDescription("PostDescription Updated");
		updatePostDto.setContent("PostContent Updated");
		String POSTFIX = String.format("/%s", postId);

		AlreadyExistsException expectedException = new AlreadyExistsException("Post", "title",
				updatePostDto.getTitle());

		mockMvc.perform(put(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatePostDto))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is(expectedException.getMessage())))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // BAD_REQUEST
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 400
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}

	/**
	 * Partially updates post by id (User role), positive (200 OK)
	 */
	@Test
	@Order(11)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void partiallyUpdatePostByIdTestPositive() throws Exception {
		// User post_id=4 for patch testing
		int postId = 4; // see data.sql
		PatchPostDto patchPostDto = new PatchPostDto();
		patchPostDto.setFieldName("setTitle");
		patchPostDto.setFieldValue("PostTitle-4 Patched");
		String POSTFIX = String.format("/%s", postId);

		mockMvc.perform(patch(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(patchPostDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(equalTo(postId))))
				.andExpect(jsonPath("$.title", is(equalTo("PostTitle-4 Patched"))))
				.andExpect(jsonPath("$.description", is(equalTo("PostDescription"))))
				.andExpect(jsonPath("$.content", is(equalTo("PostContent"))));
	}

	/**
	 * Partially updates post by id (User role). Negative, no post with provided id
	 * (404 Not found).
	 */
	@Test
	@Order(12)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void partiallyUpdatePostByIdTestNegativeNotFound() throws Exception {
		long wrongId = 100;
		PatchPostDto patchPostDto = new PatchPostDto();
		patchPostDto.setFieldName("setTitle");
		patchPostDto.setFieldValue("Patched Title");
		String POSTFIX = String.format("/%s", wrongId);
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongId);

		mockMvc.perform(patch(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(patchPostDto))).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is(equalTo(expectedException.getMessage()))))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // NOT_FOUND
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 404
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}

	/**
	 * Partially updates post by id (User role). Negative, post with same title
	 * already exists (400 Bad request)
	 */
	@Test
	@Order(12)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void partiallyUpdatePostByIdTestNegativeAlreadyExists() throws Exception {
		long postId = 4;
		PatchPostDto patchPostDto = new PatchPostDto();
		patchPostDto.setFieldName("setTitle");
		patchPostDto.setFieldValue("PostTitle-1"); // title already exists with post_id=1, see data.sql
		String POSTFIX = String.format("/%s", postId);

		AlreadyExistsException expectedException = new AlreadyExistsException("Post", "title",
				patchPostDto.getFieldValue());

		mockMvc.perform(patch(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(patchPostDto))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is(equalTo(expectedException.getMessage()))))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // BAD_REQUEST
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 400
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}

	/**
	 * Partially updates post by id (User role). Negative, no such method (400 Bad
	 * request)
	 */
	@Test
	@Order(13)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void partiallyUpdatePostByIdTestNegativeNoSuchMethod() throws Exception {
		long postId = 4;
		PatchPostDto patchPostDto = new PatchPostDto();
		patchPostDto.setFieldName("wrongMethodName");
		patchPostDto.setFieldValue("RandomValue");
		String POSTFIX = String.format("/%s", postId);

		mockMvc.perform(patch(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(patchPostDto))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is(notNullValue())))
				.andExpect(jsonPath("$.httpStatus", is(equalTo("BAD_REQUEST"))))
				.andExpect(jsonPath("$.code", is(equalTo(400)))).andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}

	/**
	 * Removes post by id (User role), positive (200 OK)
	 */
	@Test
	@Order(15)
	@WithMockUser(username = "user", password = "user", roles = "USER")
	void deletePostByIdTestPositive() throws Exception {
		// Delete post with id=5, created by create post test
		int postId = 5;
		String message = String.format("Post with id=%s deleted", postId);
		String POSTFIX = String.format("/%s", postId);
		mockMvc.perform(delete(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is(equalTo(message))));
	}

	/**
	 * Removes post by id (User role). Negative, no post with provided id (404 Not
	 * found).
	 */
	@Test
	@Order(16)
	@WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
	void deletePostByIdTestNegativeNotFound() throws Exception {
		int wrongPostId = 100;
		String POSTFIX = String.format("/%s", wrongPostId);
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongPostId);

		mockMvc.perform(delete(BASE_URI + POSTFIX).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.timestamp", is(notNullValue())))
				.andExpect(jsonPath("$.message", is(equalTo(expectedException.getMessage()))))
				.andExpect(jsonPath("$.httpStatus", is(equalTo(expectedException.getStatus().name())))) // NOT_FOUND
				.andExpect(jsonPath("$.code", is(equalTo(expectedException.getStatus().value())))) // 404
				.andExpect(jsonPath("$.details", is(notNullValue())))
				.andExpect(jsonPath("$.stackTrace", is(notNullValue())));
	}
}
