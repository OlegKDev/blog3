package com.app.blog.service.impl;

import com.app.blog.dto.comment.CommentDto;
import com.app.blog.dto.comment.CreateCommentDto;
import com.app.blog.entity.Comment;
import com.app.blog.entity.Post;
import com.app.blog.exception.BlogApiException;
import com.app.blog.exception.PostCommentMismatchException;
import com.app.blog.exception.ResourceNotFoundException;
import com.app.blog.repository.CommentRepository;
import com.app.blog.repository.PostRepository;
import com.app.blog.service.CommentService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
	private CommentRepository commentRepository;
	private PostRepository postRepository;
	private ModelMapper mapper;

	public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository, ModelMapper mapper) {
		this.commentRepository = commentRepository;
		this.postRepository = postRepository;
		this.mapper = mapper;
	}

	/**
	 * Creates a new comment
	 * 
	 * @param postId           post's id
	 * @param createCommentDto data for a new comment
	 * @return CommentDto object
	 * @throws ResourceNotFoundException if no post with the given id
	 */
	@Override
	public CommentDto createComment(long postId, CreateCommentDto createCommentDto) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		Comment comment = mapper.map(createCommentDto, Comment.class);
		comment.setPost(post); // comments_table FK comment.post_id := posts_table post.id
		Comment newComment = commentRepository.save(comment);
		CommentDto commentDto = mapper.map(newComment, CommentDto.class);
		return commentDto;
	}

	/**
	 * Retrieve the comments by the post id
	 *
	 * @param postId post's id
	 * @return List<CommentDto> or an empty list if no comments
	 * @throws ResourceNotFoundException if no post with provided id
	 */
	@Override
	public List<CommentDto> getCommentsByPostId(long postId) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		List<Comment> comments = commentRepository.findByPostId(postId);
		List<CommentDto> commentsDto = comments.stream().map((comment) -> {
			return mapper.map(comment, CommentDto.class);
		}).collect(Collectors.toList());

		return commentsDto;
	}

	/**
	 * Retrieve a comment by id
	 *
	 * @param postId    post's id
	 * @param commentId comment's id
	 * @return CommentDto object if the post id and comment id are valid, and match
	 *         each other
	 * @throws ResourceNotFoundException    if no post or comment with provided id
	 * @throws PostCommentMismatchException if the comment does not belong to the
	 *                                      post
	 */
	@Override
	public CommentDto getCommentById(long postId, long commentId) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

		if (!comment.getPost().getId().equals(post.getId())) {
			String message = String.format("Comment with id=%s does not belong to the post with id=%s", comment.getId(),
					post.getId());
			throw new PostCommentMismatchException(message);
		}

		CommentDto commentDto = mapper.map(comment, CommentDto.class);
		return commentDto;
	}

	/**
	 * Updates a comment by id
	 * 
	 * @param postId     post's id
	 * @param commentId  comment's id
	 * @param commentDto data for updating
	 * @return CommentDto object
	 * @throws ResourceNotFoundException    if no post or comment with provided id
	 * @throws PostCommentMismatchException if the comment does not belong to the
	 *                                      post
	 */
	@Override
	public CommentDto updateCommentById(long postId, long commentId, CommentDto commentDto)
			throws BlogApiException, ResourceNotFoundException, PostCommentMismatchException {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

		if (!comment.getPost().getId().equals(post.getId())) {
			throw new PostCommentMismatchException(comment.getId(), post.getId());
		}

		if (commentDto.getName() != null)
			comment.setName(commentDto.getName());
		if (commentDto.getEmail() != null)
			comment.setEmail(commentDto.getEmail());
		if (commentDto.getBody() != null)
			comment.setBody(commentDto.getBody());

		Comment updatedComment = commentRepository.save(comment);
		CommentDto updatedCommentDto = mapper.map(updatedComment, CommentDto.class);
		return updatedCommentDto;
	}

	/**
	 * Deletes a comment by id
	 * 
	 * @param postId    post's id
	 * @param commentId comment's id
	 * @return void
	 * @throws ResourceNotFoundException    if no post or comment with provided id
	 * @throws PostCommentMismatchException if the comment does not belong to the
	 *                                      post
	 */
	@Override
	public void deleteComment(long postId, long commentId) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

		if (!comment.getPost().getId().equals(post.getId())) {
			throw new PostCommentMismatchException(comment.getId(), post.getId());
		}

		commentRepository.delete(comment);
	}
}
