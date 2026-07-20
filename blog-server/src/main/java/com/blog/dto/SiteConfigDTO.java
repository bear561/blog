package com.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class SiteConfigDTO {

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    private String configValue;

    private String description;
}
