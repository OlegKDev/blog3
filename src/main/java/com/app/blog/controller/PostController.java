package com.app.blog.controller;

import com.app.blog.dto.post.*;
import com.app.blog.exception.AlreadyExistsException;
import com.app.blog.exception.ResourceNotFoundException;
import com.app.blog.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.lang.reflect.InvocationTargetException;

class PostDefaults {
	public static final String PAGE_NUMBER = "0";
	public static final String PAGE_SIZE = "10";
	public static final String SORT_BY = "id";
	public static final String SORT_DIR = "asc";
}

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
	private PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}

	/**
	 * Creates a new post
	 * 
	 * @param createPostDto data for a new post
	 * @return ResponseEntity<ResponsePostDto> object (201 Created)
	 * @exception AlreadyExistsException if post with same title already exists (400
	 *                                   Bad request)
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@PostMapping
	public ResponseEntity<ResponsePostDto> createPost(@Valid @RequestBody CreatePostDto createPostDto) {
		ResponsePostDto responsePostDto = postService.createPost(createPostDto);
		HttpStatus status = HttpStatus.CREATED;
		return new ResponseEntity<>(responsePostDto, status);
	}

	/**
	 * Retrieves all the posts
	 *
	 * @param pageNo   the page number
	 * @param pageSize the amount of the items per page
	 * @param sortBy   the field name used to sort by
	 * @param sortDir  the value for sorting direction: "asc" for ascending and
	 *                 "desc" for descending
	 * @return ResponseEntity<ResponsePostPagesDto> object (200 OK), possibly empty
	 *         list
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@GetMapping
	public ResponseEntity<ResponsePostPagesDto> getAllPosts(
			@RequestParam(value = "pageNo", defaultValue = PostDefaults.PAGE_NUMBER, required = false) int pageNo,
			@RequestParam(value = "pageSize", defaultValue = PostDefaults.PAGE_SIZE, required = false) int pageSize,
			@RequestParam(value = "sortBy", defaultValue = PostDefaults.SORT_BY, required = false) String sortBy,
			@RequestParam(value = "sortDir", defaultValue = PostDefaults.SORT_DIR, required = false) String sortDir) {
		ResponsePostPagesDto postsDto = postService.getAllPosts(pageNo, pageSize, sortBy, sortDir);
		return ResponseEntity.ok(postsDto);
	}

	/**
	 * Retrieves a post by id
	 *
	 * @param id post's id
	 * @return ResponseEntity<ResponsePostDto> object (200 OK)
	 * @exception ResourceNotFoundException if no post found with given id (404 Not
	 *                                      found)
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@GetMapping("/{id}")
	public ResponseEntity<ResponsePostDto> getPostById(@PathVariable("id") long id) throws ResourceNotFoundException {
		ResponsePostDto responsePostDto = postService.getPostById(id);
		return ResponseEntity.ok(responsePostDto);
	}

	/**
	 * Updates a post by id
	 *
	 * @param id            post's id
	 * @param updatePostDto data for updates
	 * @return ResponseEntity<ResponsePostDto> object (200 OK)
	 * @exception ResourceNotFoundException if no post found with provided id (404
	 *                                      Not found)
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@PutMapping("/{id}")
	public ResponseEntity<ResponsePostDto> updatePost(@Valid @RequestBody UpdatePostDto updatePostDto,
			@PathVariable("id") long id) throws ResourceNotFoundException {
		ResponsePostDto updatedPostDto = postService.updatePost(updatePostDto, id);
		return ResponseEntity.ok(updatedPostDto);
	}

	/**
	 * Partially updates a post by id
	 *
	 * @param id           post's id
	 * @param patchPostDto data for updates (the method name and the new value)
	 * @return ResponseEntity<ResponsePostDto> object (200 OK)
	 * @exception NoSuchMethodException     if invalid method name
	 * @exception ResourceNotFoundException if no post with given id
	 * @exception AlreadyExistsException    if the post with same title already
	 *                                      exists
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@PatchMapping("/{id}")
	public ResponseEntity<ResponsePostDto> patchPost(@PathVariable("id") long id,
			@Valid @RequestBody PatchPostDto patchPostDto)
			throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		ResponsePostDto responsePostDto = postService.partialUpdatePost(id, patchPostDto);
		return ResponseEntity.ok(responsePostDto);
	}

	/**
	 * Removes a post by id
	 *
	 * @param id post's id
	 * @return ResponseEntity<DeletePostDto> object (200 OK)
	 * @exception ResourceNotFoundException if no post found with provided id (404
	 *                                      Not found)
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@DeleteMapping("/{id}")
	public ResponseEntity<DeletePostDto> deletePost(@PathVariable("id") long id) throws ResourceNotFoundException {
		DeletePostDto deletePostDto = postService.deletePost(id);
		return ResponseEntity.ok(deletePostDto);
	}
}
