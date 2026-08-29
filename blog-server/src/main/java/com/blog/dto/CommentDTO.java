package com.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CommentDTO {

    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    private Long parentId;

    private Long replyToId;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private Boolean isAnonymous;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
