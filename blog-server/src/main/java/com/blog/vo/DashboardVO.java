package com.blog.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DashboardVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long articleCount;
    private Long commentCount;
    private Long pendingCommentCount;
    private Long categoryCount;
    private Long tagCount;
}
