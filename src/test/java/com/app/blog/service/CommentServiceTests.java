package com.app.blog.service;

import com.app.blog.dto.comment.CommentDto;
import com.app.blog.dto.comment.CreateCommentDto;
import com.app.blog.dto.post.CreatePostDto;
import com.app.blog.dto.post.ResponsePostDto;
import com.app.blog.entity.Post;
import com.app.blog.exception.BlogApiException;
import com.app.blog.exception.PostCommentMismatchException;
import com.app.blog.exception.ResourceNotFoundException;
import com.app.blog.repository.CommentRepository;
import com.app.blog.repository.PostRepository;
import com.app.blog.service.impl.CommentServiceImpl;
import com.app.blog.service.impl.PostServiceImpl;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This class contains tests of the service methods for Comment resource. It
 * utilizes H2 in-memory database and data.sql initialization script.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentServiceTests {
	@Autowired
	CommentRepository commentRepository;

	@Autowired
	CommentServiceImpl commentService;

	@Autowired
	PostServiceImpl postService;

	@Autowired
	PostRepository postRepository;

	/**
	 * Creates new post, positive
	 */
	@Test
	@Order(1)
	public void createCommentTestPositive() {
		long postId = 2; // see data.sql
		Post responsePostDto = postRepository.findById(postId).get();
		assertThat(responsePostDto).isNotNull();

		CreateCommentDto createCommentDto = new CreateCommentDto();
		createCommentDto.setName("CommentName-5");
		createCommentDto.setEmail("email-5@email.com");
		createCommentDto.setBody("CommentBody-5");

		CommentDto commentDto = commentService.createComment(postId, createCommentDto);
		long commentId = 5; // see data.sql
		assertThat(commentDto.getId()).isEqualTo(commentId);
		assertThat(commentDto.getName()).isEqualTo("CommentName-5");
		assertThat(commentDto.getEmail()).isEqualTo("email-5@email.com");
		assertThat(commentDto.getBody()).isEqualTo("CommentBody-5");
	}

	/**
	 * Creates new post. Negative, no post with given id
	 */
	@Test
	@Order(2)
	public void createCommentTestNegativePostNotFound() {
		long wrongPostId = 100;
		CreateCommentDto createCommentDto = new CreateCommentDto();
		createCommentDto.setName("Random");
		createCommentDto.setEmail("Random");
		createCommentDto.setBody("Random");

		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongPostId);

		ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
			commentService.createComment(wrongPostId, createCommentDto);
		});
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
	}

	/**
	 * Retrieves comments by post id, positive
	 */
	@Test
	@Order(3)
	public void getCommentsByPostIdTestPositive() {
		long postId = 1; // see data.sql
		List<CommentDto> commentDtoList = commentService.getCommentsByPostId(postId);
		CommentDto comment1 = commentDtoList.get(0);
		CommentDto comment2 = commentDtoList.get(1);
		assertThat(commentDtoList.size()).isEqualTo(2);
		assertThat(comment1.getName()).isEqualTo("CommentName-1");
		assertThat(comment1.getEmail()).isEqualTo("email-1@email.com");
		assertThat(comment1.getBody()).isEqualTo("CommentBody-1");
		assertThat(comment2.getName()).isEqualTo("CommentName-2");
		assertThat(comment2.getEmail()).isEqualTo("email-2@email.com");
		assertThat(comment2.getBody()).isEqualTo("CommentBody-2");
	}

	/**
	 * Retrieves comments by post id. Positive, no comments with given post id
	 * (empty list)
	 */
	@Test
	@Order(4)
	public void getCommentsByPostIdTestPositiveEmpty() {
		long postId = 3; // see data.sql
		List<CommentDto> commentDtoList = commentService.getCommentsByPostId(postId);
		assertThat(commentDtoList).isEqualTo(Collections.EMPTY_LIST);
	}

	/**
	 * Retrieves comments by post id. Negative, no post with given id
	 */
	@Test
	@Order(5)
	public void getCommentsByPostIdTestNegativePostNotFound() {
		long wrongPostId = 100L;
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongPostId);

		ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
			commentService.getCommentsByPostId(wrongPostId);
		});
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
	}

	/**
	 * Retrieves comment by id, positive
	 */
	@Test
	@Order(6)
	public void getCommentByIdTestPositive() {
		long postId = 1;
		long commentId = 1;
		CommentDto commentDto = commentService.getCommentById(postId, commentId);
		assertThat(commentDto.getId()).isEqualTo(commentId);
		assertThat(commentDto.getName()).isEqualTo("CommentName-1");
		assertThat(commentDto.getEmail()).isEqualTo("email-1@email.com");
		assertThat(commentDto.getBody()).isEqualTo("CommentBody-1");
	}

	/**
	 * Retrieves comment by id. Negative, no post with given id
	 */
	@Test
	@Order(7)
	public void getCommentByIdTestNegativePostNotFound() {
		long wrongPostId = 100;
		long randomCommentId = 100;
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongPostId);

		ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
			commentService.getCommentById(wrongPostId, randomCommentId);
		});
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
	}

	/**
	 * Retrieves comment by id. Negative, valid post id, but no comment with given
	 * id
	 */
	@Test
	@Order(8)
	public void getCommentByIdTestNegativeCommentNotFound() {
		long postId = 1;
		long wrongCommentId = 100;
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Comment", "id", wrongCommentId);

		ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
			commentService.getCommentById(postId, wrongCommentId);
		});
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
	}

	/**
	 * Retrieves comment by comment id. Negative, valid post id and comment id, but
	 * comment does not belong to the post
	 */
	@Test
	@Order(9)
	public void getCommentByIdTestNegativePostCommentMismatch() {
		long postId = 1;
		long commentId = 3;
		PostCommentMismatchException expectedException = new PostCommentMismatchException(postId, commentId);

		PostCommentMismatchException actualException = assertThrows(PostCommentMismatchException.class, () -> {
			commentService.getCommentById(postId, commentId);
		});
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
	}

	/**
	 * Updates comment by id, positive
	 */
	@Test
	@Order(10)
	public void updateCommentByIdPositive() {
		// see data.sql
		long postId = 2;
		long commentId = 3;
		CommentDto updateBodyDto = new CommentDto();
		updateBodyDto.setName("CommentName-3 Updated");
		updateBodyDto.setEmail("email-3-updated@email.com");
		updateBodyDto.setBody("CommentBody-3 Updated");

		CommentDto updatedComment = commentService.updateCommentById(postId, commentId, updateBodyDto);
		assertThat(updatedComment.getId()).isEqualTo(commentId);
		assertThat(updatedComment.getName()).isEqualTo(updateBodyDto.getName());
		assertThat(updatedComment.getEmail()).isEqualTo(updateBodyDto.getEmail());
		assertThat(updatedComment.getBody()).isEqualTo(updateBodyDto.getBody());
	}

	/**
	 * Updates comment. Negative, no post with given id
	 */
	@Test
	@Order(11)
	public void updateCommentByIdTestNegativePostNotFound() {
		int wrongPostId = 100;
		int randomCommentId = 100;
		CommentDto updateBodyDto = new CommentDto();
		updateBodyDto.setName("CommentName-4 Updated");
		updateBodyDto.setEmail("email-4-updated@email.com");
		updateBodyDto.setBody("CommentBody-4 Updated");
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Post", "id", wrongPostId);

		ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
			commentService.updateCommentById(wrongPostId, randomCommentId, updateBodyDto);
		});
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
	}

	/**
	 * Updates comment. Negative, valid post id, but no comment with given id
	 */
	@Test
	@Order(12)
	public void updateCommentByIdTestNegativeCommentNotFound() {
		int postId = 1;
		int wrongCommentId = 100;
		CommentDto updateBodyDto = new CommentDto();
		updateBodyDto.setName("CommentName-4 Updated");
		updateBodyDto.setEmail("email-4-updated@email.com");
		updateBodyDto.setBody("CommentBody-4 Updated");
		ResourceNotFoundException expectedException = new ResourceNotFoundException("Comment", "id", wrongCommentId);

		ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
			commentService.updateCommentById(postId, wrongCommentId, updateBodyDto);
		});
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
	}

	/**
	 * Updates comment. Negative, valid post id and comment id, but comment does not
	 * belong to the post
	 */
	@Test
	@Order(13)
	public void updateCommentByIdTestNegativePostCommentMismatch() {
		long postId = 1;
		long commentId = 3;
		PostCommentMismatchException expectedException = new PostCommentMismatchException(postId, commentId);

		PostCommentMismatchException actualException = assertThrows(PostCommentMismatchException.class, () -> {
			commentService.getCommentById(postId, commentId);
		});
		assertThat(actualException.getStatus()).isEqualTo(expectedException.getStatus());
		assertThat(actualException.getMessage()).isEqualTo(expectedException.getMessage());
	}
}
