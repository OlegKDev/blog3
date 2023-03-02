package com.app.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import com.app.blog.dto.post.CreatePostDto;
import com.app.blog.dto.post.DeletePostDto;
import com.app.blog.dto.post.PatchPostDto;
import com.app.blog.dto.post.ResponsePostDto;
import com.app.blog.dto.post.ResponsePostPagesDto;
import com.app.blog.dto.post.UpdatePostDto;
import com.app.blog.exception.AlreadyExistsException;
import com.app.blog.exception.BlogApiException;
import com.app.blog.exception.ResourceNotFoundException;
import com.app.blog.repository.CommentRepository;
import com.app.blog.repository.PostRepository;
import com.app.blog.service.impl.PostServiceImpl;

/**
 * This class contains tests of the service methods for Post resource. It
 * utilizes H2 in-memory database and data.sql initialization script.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostServiceTests {
	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private PostServiceImpl postService;

	private int totalPosts = 5; // see data.sql

	private int newPostId = 6; // as 5 created within data.sql during initialization

	/**
	 * Creates new post, positive
	 */
	@Test
	@Order(1)
	public void createPostTestPositive() {
		CreatePostDto expectedPost = new CreatePostDto();
		expectedPost.setTitle("NewPostTitle");
		expectedPost.setDescription("NewPostDescription");
		expectedPost.setContent("NewPostContent");

		ResponsePostDto actualPost = postService.createPost(expectedPost);
		assertThat(actualPost.getId()).isGreaterThanOrEqualTo(newPostId);
		assertThat(actualPost.getTitle()).isEqualTo(expectedPost.getTitle());
		assertThat(actualPost.getDescription()).isEqualTo(expectedPost.getDescription());
		assertThat(actualPost.getContent()).isEqualTo(expectedPost.getContent());
		assertThat(actualPost.getComments()).isEmpty();
	}

	/**
	 * Creates new post. Negative, post with same title already exists
	 */
	@Test
	@Order(2)
	public void createPostTestNegativeAlreadyExists() {
		CreatePostDto postThatAlreadyExists = new CreatePostDto();
		postThatAlreadyExists.setTitle("PostTitle-1");
		postThatAlreadyExists.setDescription("PostDescription");
		postThatAlreadyExists.setContent("PostContent");

		AlreadyExistsException actualException = assertThrows(AlreadyExistsException.class, () -> {
			postService.createPost(postThatAlreadyExists);
		});

		AlreadyExistsException expectedException = new AlreadyExistsException("Post", "title",
				postThatAlreadyExists.getTitle());
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
	}

	/**
	 * Retrieves post by id, positive
	 */
	@Test
	@Order(3)
	public void getPostByIdTestPositive() {
		long postId = 1L;
		String expectedTitle = "PostTitle-1";
		String expectedContent = "PostContent";
		String expectedDescription = "PostDescription";

		ResponsePostDto actualPost = postService.getPostById(postId);
		assertThat(actualPost.getId()).isEqualTo(postId);
		assertThat(actualPost.getTitle()).isEqualTo(expectedTitle);
		assertThat(actualPost.getContent()).isEqualTo(expectedContent);
		assertThat(actualPost.getDescription()).isEqualTo(expectedDescription);
		assertThat(actualPost.getComments().size()).isEqualTo(2);
	}

	/**
	 * Retrieves post by id. Negative, resource not found
	 */
	@Test
	@Order(4)
	public void getPostByIdTestNegativeNotFound() {
		long wrongId = 100L;
		ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
			postService.getPostById(wrongId);
		});

		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongId);
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
	}

	/**
	 * Retrieves all posts, positive (not empty)
	 */
	@Test
	@Order(5)
	public void getAllPostsNotEmpty() {
		int pageNo = 0;
		int pageSize = 2;
		int totalPages = 3;
		String sortBy = "id";
		String sortDir = "asc";
		ResponsePostPagesDto actualPosts = postService.getAllPosts(pageNo, pageSize, sortBy, sortDir);

		assertThat(actualPosts).isNotNull();
		assertThat(actualPosts.getPageNo()).isEqualTo(pageNo);
		assertThat(actualPosts.getPageSize()).isEqualTo(pageSize);
		assertThat(actualPosts.getTotalPages()).isEqualTo(totalPages);
		assertThat(actualPosts.getTotalElements()).isEqualTo(totalPosts + 1);
		assertThat(actualPosts.isLast()).isFalse();
		assertThat(actualPosts.getPosts().size()).isEqualTo(pageSize);

		for (int i = 0; i < pageSize; i++) {
			assertThat(actualPosts.getPosts().get(i).getId()).isGreaterThanOrEqualTo(1);
			assertThat(actualPosts.getPosts().get(i).getTitle()).isNotEmpty();
			assertThat(actualPosts.getPosts().get(i).getDescription()).isNotEmpty();
			assertThat(actualPosts.getPosts().get(i).getContent()).isNotEmpty();
		}
	}

	/**
	 * Retrieves all posts, positive (empty)
	 */
	@Test
	@Order(6)
	public void getAllPostsEmpty() {
		int pageNo = 4; // doesn't exist
		int pageSize = 2;
		int totalPages = 3;
		String sortBy = "id";
		String sortDir = "asc";
		ResponsePostPagesDto actualPosts = postService.getAllPosts(pageNo, pageSize, sortBy, sortDir);

		assertThat(actualPosts).isNotNull();
		assertThat(actualPosts.getPageNo()).isEqualTo(pageNo);
		assertThat(actualPosts.getPageSize()).isEqualTo(pageSize);
		assertThat(actualPosts.getTotalPages()).isEqualTo(totalPages);
		assertThat(actualPosts.getTotalElements()).isEqualTo(totalPosts + 1);
		assertThat(actualPosts.isLast()).isTrue();
		assertThat(actualPosts.getPosts().size()).isEqualTo(0);
	}

	/**
	 * Updates post by id, positive
	 */
	@Test
	@Order(7)
	public void updatePostByIdTestPositive() {
		// Use post with id=3 for update testing
		int postId = 3; // see data.sql
		UpdatePostDto expectedPost = new UpdatePostDto();
		expectedPost.setTitle("PostTitle-3 Updated");
		expectedPost.setDescription("PostDescription Updated");
		expectedPost.setContent("PostContent Updated");

		ResponsePostDto actualPost = postService.updatePost(expectedPost, postId);
		assertThat(actualPost.getId()).isEqualTo(postId);
		assertThat(actualPost.getTitle()).isEqualTo(expectedPost.getTitle());
		assertThat(actualPost.getDescription()).isEqualTo(expectedPost.getDescription());
		assertThat(actualPost.getContent()).isEqualTo(expectedPost.getContent());
	}

	/**
	 * Updates post by id. Negative, no post with provided id
	 */
	@Test
	@Order(8)
	public void updatePostByIdTestNegativeNotFound() {
		long wrongId = 100L;
		UpdatePostDto expectedPost = new UpdatePostDto();
		expectedPost.setTitle("PostTitle-3 Updated");
		expectedPost.setDescription("PostDescription Updated");
		expectedPost.setContent("PostContent Updated");

		ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
			postService.updatePost(expectedPost, wrongId);
		});
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongId);
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
	}

	/**
	 * Updates post by id. Negative, post with same title already exists
	 */
	@Test
	@Order(9)
	public void updatePostByIdTestNegativeAlreadyExists() {
		long postId = 3;
		UpdatePostDto expectedPost = new UpdatePostDto();
		expectedPost.setTitle("PostTitle-1"); // already exists with post_id=1
		expectedPost.setDescription("PostDescription Updated");
		expectedPost.setContent("PostContent Updated");

		AlreadyExistsException actualException = assertThrows(AlreadyExistsException.class, () -> {
			postService.updatePost(expectedPost, postId);
		});
		AlreadyExistsException expectedException = new AlreadyExistsException("Post", "title", expectedPost.getTitle());
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
	}

	/**
	 * Partially updates post by id, positive (title)
	 */
	@Test
	@Order(10)
	public void partiallyUpdatePostByIdTestPositiveTitle() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// Use post with id=4 for patch testing
		long postId = 4; // see data.sql
		PatchPostDto expectedPatch = new PatchPostDto();
		expectedPatch.setFieldName("setTitle");
		expectedPatch.setFieldValue("PostTitle-4 Patched");
		String expectedTitle = "PostTitle-4 Patched";
		String expectedContent = "PostContent";
		String expectedDescription = "PostDescription";

		ResponsePostDto actualPost = postService.partialUpdatePost(postId, expectedPatch);
		assertThat(actualPost.getId()).isEqualTo(postId);
		assertThat(actualPost.getTitle()).isEqualTo(expectedTitle);
		assertThat(actualPost.getDescription()).isEqualTo(expectedDescription);
		assertThat(actualPost.getContent()).isEqualTo(expectedContent);
	}

	/**
	 * Partially updates post by id, positive (content)
	 */
	@Test
	@Order(11)
	public void partiallyUpdatePostByIdTestPositiveContent() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// Use post with id=4 for patch testing
		long postId = 4; // see data.sql
		PatchPostDto expectedPatch = new PatchPostDto();
		expectedPatch.setFieldName("setContent");
		expectedPatch.setFieldValue("PostContent Patched");
		String expectedTitle = "PostTitle-4 Patched";
		String expectedContent = "PostContent Patched";
		String expectedDescription = "PostDescription";

		ResponsePostDto actualPost = postService.partialUpdatePost(postId, expectedPatch);
		assertThat(actualPost.getId()).isEqualTo(postId);
		assertThat(actualPost.getTitle()).isEqualTo(expectedTitle);
		assertThat(actualPost.getDescription()).isEqualTo(expectedDescription);
		assertThat(actualPost.getContent()).isEqualTo(expectedContent);
	}

	/**
	 * Partially updates post by id, positive (description)
	 */
	@Test
	@Order(12)
	public void partiallyUpdatePostByIdTestPositiveDescription() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// Use post with id=4 for patch testing
		long postId = 4; // see data.sql
		PatchPostDto expectedPatch = new PatchPostDto();
		expectedPatch.setFieldName("setDescription");
		expectedPatch.setFieldValue("PostDescription Patched");
		String expectedTitle = "PostTitle-4 Patched";
		String expectedContent = "PostContent Patched";
		String expectedDescription = "PostDescription Patched";

		ResponsePostDto actualPost = postService.partialUpdatePost(postId, expectedPatch);
		assertThat(actualPost.getId()).isEqualTo(postId);
		assertThat(actualPost.getTitle()).isEqualTo(expectedTitle);
		assertThat(actualPost.getDescription()).isEqualTo(expectedDescription);
		assertThat(actualPost.getContent()).isEqualTo(expectedContent);
	}

	/**
	 * Partially updates post by id. Negative, post with same title already exists
	 */
	@Test
	@Order(13)
	public void partiallyUpdatePostByIdTestNegativeAlreadyExists() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// Use post with id=4 for patch testing
		long postId = 4; // see data.sql
		PatchPostDto expectedPatch = new PatchPostDto();
		expectedPatch.setFieldName("setTitle");
		expectedPatch.setFieldValue("PostTitle-1"); // already exists with post_id=1

		AlreadyExistsException actualException = assertThrows(AlreadyExistsException.class, () -> {
			postService.partialUpdatePost(postId, expectedPatch);
		});
		AlreadyExistsException expectedException = new AlreadyExistsException("Post", "title",
				expectedPatch.getFieldValue());
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
	}

	/**
	 * Partially updates post by id. Negative, resource not found
	 */
	@Test
	@Order(14)
	public void partiallyUpdatePostByIdTestNegativeNotFound() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// Use post with id=4 for patch testing, see data.sql
		long wrongPostId = 100;
		PatchPostDto expectedPatch = new PatchPostDto();
		expectedPatch.setFieldName("wrongFieldName");
		expectedPatch.setFieldValue("Value");

		ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
			postService.partialUpdatePost(wrongPostId, expectedPatch);
		});
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongPostId);
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
	}

	/**
	 * Partially updates post by id. Negative, invalid patch method
	 */
	@Test
	@Order(15)
	public void partiallyUpdatePostByIdTestNegativeInvalidPatchNoSuchMethod() throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// Use post with id=4 for patch testing, see data.sql
		long wrongPostId = 4;
		PatchPostDto expectedPatch = new PatchPostDto();
		expectedPatch.setFieldName("wrongFieldName");
		expectedPatch.setFieldValue("Value");

		BlogApiException actualException = assertThrows(BlogApiException.class, () -> {
			postService.partialUpdatePost(wrongPostId, expectedPatch);
		});
		assertThat(actualException.getMessage()).isNotNull();
		assertThat(actualException.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	/**
	 * Removes post by id, positive
	 */
	@Test
	@Order(16)
	public void deletePostByIdTestPositive() {
		long postId = 6; // post that has been created with test order #1 createPostTestPositive()
		DeletePostDto deletePostDto = postService.deletePost(postId);
		String message = String.format("Post with id=%s deleted", postId);
		assertThat(deletePostDto.getMessage()).isEqualTo(message);

		ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
			postService.getPostById(postId);
		});
		
		String errorMessage = String.format("%s not found with %s: %s", "Post", "id", postId);
		HttpStatus errorStatus = HttpStatus.NOT_FOUND;
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", postId);
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
	}

	/**
	 * Remove post by id. Negative, no post with provided id
	 */
	@Test
	@Order(17)
	public void deletePostByIdTestNegativeNotFound() {
		long wrongPostId = 100L;
		ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
			postService.deletePost(wrongPostId);
		});
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongPostId);
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
	}
}
