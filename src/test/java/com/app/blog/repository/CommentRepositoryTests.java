package com.app.blog.repository;

import com.app.blog.entity.Comment;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class contains tests of the repository methods for Post resource. It
 * utilizes H2 in-memory database and data.sql initialization script.
 */
@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentRepositoryTests {
	@Autowired
	CommentRepository commentRepository;

	/**
	 * Retrieves comments by post id, positive
	 */
	@Test
	@Order(1)
	public void getCommentsByPostIdTestPositive() {
		long postId = 1; // see data.sql
		List<Comment> comments = commentRepository.findByPostId(postId);
		Comment comment1 = comments.get(0);
		Comment comment2 = comments.get(1);
		assertThat(comments.size()).isEqualTo(2);
		// comment_id=1
		assertThat(comment1.getName()).isEqualTo("CommentName-1");
		assertThat(comment1.getEmail()).isEqualTo("email-1@email.com");
		assertThat(comment1.getBody()).isEqualTo("CommentBody-1");
		// comment_id=2
		assertThat(comment2.getName()).isEqualTo("CommentName-2");
		assertThat(comment2.getEmail()).isEqualTo("email-2@email.com");
		assertThat(comment2.getBody()).isEqualTo("CommentBody-2");
	}

	/**
	 * Retrieves comments by post id. Negative, no post with given id (empty list)
	 */
	@Test
	@Order(2)
	public void getCommentsByPostIdTestNegativePostNotFound() {
		long wrongPostId = 100;
		List<Comment> comments = commentRepository.findByPostId(wrongPostId);
		assertThat(comments.size()).isEqualTo(0);
	}
}
