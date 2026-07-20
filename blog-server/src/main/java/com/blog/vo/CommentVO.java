package com.blog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {

    private Long id;
    private Long articleId;
    private Long parentId;
    private Long replyToId;
    private String nickname;
    private String email;
    private String website;
    private String content;
    private Integer isReviewed;
    private String createdAtFmt;
    private String formattedDate;
    private List<CommentVO> replies;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
