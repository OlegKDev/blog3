package com.app.blog.service;

import com.app.blog.dto.post.*;
import com.app.blog.exception.ResourceNotFoundException;

import java.lang.reflect.InvocationTargetException;

public interface PostService {
	ResponsePostDto createPost(CreatePostDto createPostDto);

	ResponsePostPagesDto getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir);

	ResponsePostDto getPostById(long id) throws ResourceNotFoundException;

	ResponsePostDto updatePost(UpdatePostDto postDto, long id) throws ResourceNotFoundException;

	DeletePostDto deletePost(long id) throws ResourceNotFoundException;

	ResponsePostDto partialUpdatePost(long id, PatchPostDto patchPostDto) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
}
