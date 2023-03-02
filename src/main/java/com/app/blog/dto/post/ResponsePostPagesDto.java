package com.app.blog.dto.post;

import com.app.blog.dto.post.PostDto;
import lombok.Data;

import java.util.List;

@Data
public class ResponsePostPagesDto {
	private int pageNo;
	private int pageSize;
	private long totalElements;
	private long totalPages;
	private boolean last;
	private List<PostDto> posts;
}
