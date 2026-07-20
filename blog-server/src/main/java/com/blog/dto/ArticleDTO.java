package com.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class ArticleDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverImage;

    private Long categoryId;

    private Integer isPublished;

    private Integer isTop;

    private List<Long> tagIds;
}
