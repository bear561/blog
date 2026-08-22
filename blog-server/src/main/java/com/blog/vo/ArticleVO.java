package com.blog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleVO implements Serializable {
    private static final long serialVersionUID = 1L;

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
    /** 预计阅读分钟数，由服务端按 content_html 统一计算，保证列表与详情一致 */
    private Integer readMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
