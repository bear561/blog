package com.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ArchiveVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer year;
    private Integer month;
    private List<ArticleListVO> articles;
}
