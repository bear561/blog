package com.blog.vo;

import lombok.Data;

import java.util.List;

@Data
public class ArchiveVO {

    private Integer year;
    private Integer month;
    private List<ArticleListVO> articles;
}
