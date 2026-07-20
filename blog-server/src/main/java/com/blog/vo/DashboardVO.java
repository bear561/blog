package com.blog.vo;

import lombok.Data;

@Data
public class DashboardVO {

    private Long articleCount;
    private Long commentCount;
    private Long pendingCommentCount;
    private Long categoryCount;
    private Long tagCount;
}
