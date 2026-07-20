package com.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class CategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotBlank(message = "分类别名不能为空")
    private String slug;

    private String description;

    private Integer sortOrder;
}
