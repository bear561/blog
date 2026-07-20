package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private Long parentId;
    private Long replyToId;
    private String nickname;
    private String email;
    private String website;
    private String content;
    private Integer isReviewed;
    private String ip;
    private String userAgent;
    private LocalDateTime createdAt;
}
