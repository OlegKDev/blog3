package com.app.blog.controller;

import com.app.blog.dto.comment.CommentDto;
import com.app.blog.dto.comment.CreateCommentDto;
import com.app.blog.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CommentController {
	private CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	/**
	 * Creates a new comment
	 *
	 * @param postId           post's id, the comment belongs to
	 * @param createCommentDto data for a new comment creation
	 * @return ResponseEntity<CommentDto> instance (201 Created)
	 * @exception ResourceNotFoundException if no post with given id (400 Bad
	 *                                      request)
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@PostMapping("/posts/{postId}/comments")
	public ResponseEntity<CommentDto> createComment(@PathVariable("postId") long postId,
			@Valid @RequestBody CreateCommentDto createCommentDto) {
		CommentDto newCommentDto = commentService.createComment(postId, createCommentDto);
		HttpStatus status = HttpStatus.CREATED;
		return new ResponseEntity<>(newCommentDto, status);
	}

	/**
	 * Retrieves the comments by post id
	 *
	 * @param postId post's id, the comments belong to
	 * @return List<CommentDto> (200 OK) or an empty list if no comments
	 * @exception ResourceNotFoundException if no post with given id (404 Not found)
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@GetMapping("/posts/{postId}/comments")
	public ResponseEntity<List<CommentDto>> getCommentsByPostId(@PathVariable("postId") Long postId) {
		List<CommentDto> commentsDto = commentService.getCommentsByPostId(postId);
		return ResponseEntity.ok(commentsDto);
	}

	/**
	 * Retrieves a comment by id
	 *
	 * @param postId    post's id
	 * @param commentId comment's id
	 * @return CommentDto object (200 OK)
	 * @exception ResourceNotFoundException    if no post or comment with given id
	 *                                         (404 Not found)
	 * @exception PostCommentMismatchException if the post id and the comment id are
	 *                                         valid, but don't match each other
	 *                                         (400 Bad request)
	 */
	@GetMapping("/posts/{postId}/comments/{commentId}")
	public ResponseEntity<CommentDto> getCommentById(@PathVariable("postId") Long postId,
			@PathVariable("commentId") Long commentId) {
		CommentDto commentDto = commentService.getCommentById(postId, commentId);
		return ResponseEntity.ok(commentDto);
	}

	/**
	 * Updates a comment by id
	 * 
	 * @param postId     post's id
	 * @param commentId  comment's id
	 * @param commentDto data for updates
	 * @return CommentDto object (200 OK)
	 * @exception ResourceNotFoundException    if no post or comment with given id
	 *                                         (404 Not found)
	 * @exception PostCommentMismatchException if the post id and the comment id are
	 *                                         valid, but don't match each other
	 *                                         (400 Bad request)
	 */
	@PutMapping("/posts/{postId}/comments/{id}")
	public ResponseEntity<CommentDto> updateCommentById(@PathVariable("postId") Long postId,
			@PathVariable("id") Long commentId, @Valid @RequestBody CommentDto commentDto) {
		CommentDto updatedCommentDto = commentService.updateCommentById(postId, commentId, commentDto);
		return ResponseEntity.ok(updatedCommentDto);
	}

	/**
	 * Deletes a comment by id
	 * 
	 * @param postId    post's id
	 * @param commentId comment's id
	 * @return ResponseEntity<String> message
	 * @exception ResourceNotFoundException    if no post or comment with given id
	 *                                         (404 Not found)
	 * @exception PostCommentMismatchException if the post id and the comment id are
	 *                                         valid, but don't match each other
	 *                                         (400 Bad request)
	 */
	@DeleteMapping("/posts/{postId}/comments/{id}")
	public ResponseEntity<String> deleteComment(@PathVariable("postId") Long postId,
			@PathVariable("id") Long commentId) {
		commentService.deleteComment(postId, commentId);
		String message = "Comment deleted";
		return ResponseEntity.ok(message);
	}
}
