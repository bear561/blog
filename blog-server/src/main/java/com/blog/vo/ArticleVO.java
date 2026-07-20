package com.blog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleVO {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String contentHtml;
    private String coverImage;
    private Long categoryId;
    private String categoryName;
    private Integer isPublished;
    private Integer isTop;
    private Long viewCount;
    private List<TagVO> tags;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
