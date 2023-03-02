package com.app.blog.repository;

import com.app.blog.entity.Post;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This class contains tests of the repository methods for Post resource. It
 * utilizes H2 in-memory database and data.sql initialization script.
 */
@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostRepositoryTests {
	private static Post mockPost;
	private static Long mockPostId;
	private static Long lastDbPostId;	

	@Autowired
	PostRepository postRepository;

	/**
	 * Generate mock data to create a new post only. Other tests use H2 with data
	 * from data.sql initialization script
	 */
	@BeforeAll
	static void beforeAll() {
		mockPost = new Post();
		mockPost.setTitle("PostTitle-New");
		mockPost.setDescription("PostDescription");
		mockPost.setContent("PostContent");
		mockPostId = 6L; // see data.sql
		
		lastDbPostId = 5L;
	}

	/**
	 * Creates new post, positive. New post will have id=6 as 5 posts had been
	 * created with data.sql before it, however this new post wouldn't be saved in
	 * H2 for other tests.
	 */
	@Test
	@Order(1)
	public void savePostTest() {
		Post savedPost = postRepository.save(mockPost);

		assertThat(savedPost).isNotNull();
		assertThat(savedPost.getId()).isEqualTo(mockPostId);
		assertThat(savedPost.getTitle()).isEqualTo(mockPost.getTitle());
		assertThat(savedPost.getDescription()).isEqualTo(mockPost.getDescription());
		assertThat(savedPost.getContent()).isEqualTo(mockPost.getContent());
	}

	/**
	 * Creates new post. Negative, null reference
	 */
	@Test
	@Order(2)
	public void savePostTestNullable() {
		Post postToSave = new Post();

		assertThrows(DataIntegrityViolationException.class, () -> {
			postRepository.save(postToSave);
		});
	}

	/**
	 * Retrieves post by id, positive
	 */
	@Test
	@Order(3)
	public void getPostByIdTest() {
		Optional<Post> retrievedPost = postRepository.findById(lastDbPostId);
		assertThat(retrievedPost.isPresent()).isTrue();
	}

	/**
	 * Retrieves post by id, positive (Optional empty)
	 */
	@Test
	@Order(4)
	public void getPostByIdTestEmpty() {
		long wrongPostId = 100L;
		Optional<Post> retrievedPost = postRepository.findById(wrongPostId);
		assertThat(retrievedPost.isPresent()).isFalse();
	}

	/**
	 * Retrieves all posts, positive
	 */
	@Test
	@Order(5)
	public void getAllPostsTest() {
		List<Post> retrievedPosts = postRepository.findAll();
		int totalPosts = 5; // see data.sql

		assertThat(retrievedPosts.size()).isEqualTo(totalPosts);

		for (int i = 0; i < totalPosts; i++) {
			assertThat(retrievedPosts.get(i).getId()).isNotNull();
			assertThat(retrievedPosts.get(i).getId()).isBetween(1L, lastDbPostId);
			assertThat(retrievedPosts.get(i).getTitle()).isNotNull();
			assertThat(retrievedPosts.get(i).getDescription()).isNotNull();
			assertThat(retrievedPosts.get(i).getContent()).isNotNull();
		}
	}

	/**
	 * Updates post by id, positive
	 */
	@Test
	@Order(6)
	public void updatePostByIdTest() {
		Optional<Post> retrievedPostOptional = postRepository.findById(lastDbPostId);
		Post retrievedPost = retrievedPostOptional.get();
		
		final String updatedTitle = "PostTitle-Updated";
		final String updatedDescription = "PostDescription-Updated";
		final String updatedContent = "PostContent-Updated";
		retrievedPost.setTitle(updatedTitle);
		retrievedPost.setDescription(updatedDescription);
		retrievedPost.setContent(updatedContent);

		Post updatedPost = postRepository.save(retrievedPost);

		assertThat(updatedPost.getId()).isEqualTo(retrievedPost.getId());
		assertThat(updatedPost.getId()).isEqualTo(lastDbPostId);
		assertThat(updatedPost.getTitle()).isEqualTo(updatedTitle);
		assertThat(updatedPost.getDescription()).isEqualTo(updatedDescription);
		assertThat(updatedPost.getContent()).isEqualTo(updatedContent);
	}

	/**
	 * Removes post by id, positive
	 */
	@Test
	@Order(7)
	public void deletePostByIdTest() {
		Optional<Post> retrievedPostOptional = postRepository.findById(lastDbPostId);
		Post retrievedPost = retrievedPostOptional.get();
		postRepository.delete(retrievedPost);

		Optional<Post> retrievedAgainPostOptional = postRepository.findById(mockPostId);
		assertThat(retrievedAgainPostOptional.isPresent()).isFalse();
	}
}
