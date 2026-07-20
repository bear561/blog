package com.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class FriendLinkDTO {

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "链接不能为空")
    private String url;

    private String avatar;

    private String description;

    private Integer sortOrder;

    private Integer isVisible;
}
