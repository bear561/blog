package com.blog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleListVO {

    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private String categoryName;
    private List<TagVO> tags;
    private Integer isPublished;
    private Integer isTop;
    private Long viewCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
