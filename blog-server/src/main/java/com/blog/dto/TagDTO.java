package com.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class TagDTO {

    @NotBlank(message = "标签名称不能为空")
    private String name;

    @NotBlank(message = "标签别名不能为空")
    private String slug;
}
