package com.app.blog.service;

import com.app.blog.dto.comment.CommentDto;
import com.app.blog.dto.comment.CreateCommentDto;

import java.sql.SQLException;
import java.util.List;

public interface CommentService {
    CommentDto createComment(long postId, CreateCommentDto createCommentDto);

    List<CommentDto> getCommentsByPostId(long postId);

    CommentDto getCommentById(long postId, long commentId);

    CommentDto updateCommentById(long postId, long commentId, CommentDto commentDto);

    void deleteComment(long postId, long commentId);
}
