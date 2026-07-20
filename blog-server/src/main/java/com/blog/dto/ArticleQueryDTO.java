package com.blog.dto;

import lombok.Data;

@Data
public class ArticleQueryDTO {

    private Integer page = 1;

    private Integer size = 10;

    private Long categoryId;

    private Long tagId;

    private String keyword;

    private Integer year;

    private Integer month;
}
