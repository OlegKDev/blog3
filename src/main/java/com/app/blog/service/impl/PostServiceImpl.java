package com.app.blog.service.impl;

import com.app.blog.dto.post.*;
import com.app.blog.entity.Post;
import com.app.blog.exception.AlreadyExistsException;
import com.app.blog.exception.BlogApiException;
import com.app.blog.exception.ResourceNotFoundException;
import com.app.blog.repository.PostRepository;
import com.app.blog.service.PostService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
	private PostRepository postRepository;
	private ModelMapper mapper;

	public PostServiceImpl(PostRepository postRepository, ModelMapper mapper) {
		this.postRepository = postRepository;
		this.mapper = mapper;
	}

	/**
	 * Creates a new post (without the comments)
	 *
	 * @param createPostDto data for a new post
	 * @return PostDto object
	 * @throws AlreadyExistsException if the post with the same title already exists
	 */
	@Override
	public ResponsePostDto createPost(CreatePostDto createPostDto) {
		Optional<Post> retrievedPostOptional = postRepository.findByTitle(createPostDto.getTitle());

		if (retrievedPostOptional.isPresent()) {
			throw new AlreadyExistsException("Post", "title", createPostDto.getTitle());
		}

		Post postToSave = mapper.map(createPostDto, Post.class);
		Post newPost = postRepository.save(postToSave);
		ResponsePostDto responsePostDto = mapper.map(newPost, ResponsePostDto.class);

		return responsePostDto;
	}

	/**
	 * Retrieves all the posts
	 *
	 * @param pageNo   page number
	 * @param pageSize amount of items per page
	 * @param sortBy   the field name used to sort by
	 * @param sortDir  the value for sorting direction: "asc" for ascending and
	 *                 "desc" for descending
	 * @return ResponsePostPagesDto object, possibly empty
	 */
	@Override
	public ResponsePostPagesDto getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
				: Sort.by(sortBy).descending();

		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
		Page<Post> page = postRepository.findAll(pageable);

		List<Post> posts = page.getContent();
		List<PostDto> postsDto = posts.stream().map((post) -> {
			return mapper.map(post, PostDto.class);
		}).collect(Collectors.toList());

		ResponsePostPagesDto responsePostPagesDto = new ResponsePostPagesDto();
		responsePostPagesDto.setPageNo(page.getNumber());
		responsePostPagesDto.setPageSize(page.getSize());
		responsePostPagesDto.setTotalElements(page.getTotalElements());
		responsePostPagesDto.setTotalPages(page.getTotalPages());
		responsePostPagesDto.setLast(page.isLast());
		responsePostPagesDto.setPosts(postsDto);

		return responsePostPagesDto;
	}

	/**
	 * Retrieves a post by id
	 *
	 * @param id post's id
	 * @return ResponsePostDto object
	 * @throws ResourceNotFoundException if no post found with given id
	 */
	@Override
	public ResponsePostDto getPostById(long id) {
		Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
		ResponsePostDto responsePostDto = mapper.map(post, ResponsePostDto.class);
		return responsePostDto;
	}

	/**
	 * Updates a post by id
	 *
	 * @param id            post's id
	 * @param updatePostDto data for updates
	 * @return ResponsePostDto object
	 * @throws ResourceNotFoundException if no post found with provided id
	 * @throws AlreadyExistsException    if the post with same title already exists
	 */
	@Override
	public ResponsePostDto updatePost(UpdatePostDto updatePostDto, long id) {
		Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

		if (updatePostDto.getTitle() != null) {
			Optional<Post> checkPostOptional = postRepository.findByTitle(updatePostDto.getTitle());
			if (checkPostOptional.isPresent()) {
				throw new AlreadyExistsException("Post", "title", updatePostDto.getTitle());
			} else {
				post.setTitle(updatePostDto.getTitle());
			}
		}
		if (updatePostDto.getDescription() != null)
			post.setDescription(updatePostDto.getDescription());
		if (updatePostDto.getContent() != null)
			post.setContent(updatePostDto.getContent());

		Post updatedPost = postRepository.save(post);
		ResponsePostDto responsePostDto = mapper.map(updatedPost, ResponsePostDto.class);
		return responsePostDto;
	}

	/**
	 * Partially updates a post by id
	 * 
	 * @param id           post's id
	 * @param patchPostDto data for updates (the method name and the value to
	 *                     update)
	 * @return ResponsePostDto object
	 * @throws NoSuchMethodException     if invalid method name
	 * @throws ResourceNotFoundException if no post with given id
	 * @throws AlreadyExistsException    if the post with same title already exists
	 */
	@Override
	public ResponsePostDto partialUpdatePost(long id, PatchPostDto patchPostDto) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (patchPostDto.getFieldName().equals("setTitle")) {
			Optional<Post> checkTitlePost = postRepository.findByTitle(patchPostDto.getFieldValue());
			if (checkTitlePost.isPresent()) {
				throw new AlreadyExistsException("Post", "title", patchPostDto.getFieldValue());
			}
		}
		Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
		Method method = null;
		try {
			method = post.getClass().getMethod(patchPostDto.getFieldName(), String.class);
			method.invoke(post, patchPostDto.getFieldValue());
		} catch (Exception ex) {
			throw new BlogApiException(ex.getMessage(), HttpStatus.BAD_REQUEST);
		}
		Post updatedPost = postRepository.save(post);
		ResponsePostDto responsePostDto = mapper.map(updatedPost, ResponsePostDto.class);
		return responsePostDto;
	}

	/**
	 * Removes a post by id
	 *
	 * @param id post's id
	 * @return DeletePostDto object
	 * @throws ResourceNotFoundException if no post found with provided id
	 */
	@Override
	public DeletePostDto deletePost(long id) {
		Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
		postRepository.delete(post);
		DeletePostDto deletePostDto = new DeletePostDto(id);
		return deletePostDto;
	}
}
