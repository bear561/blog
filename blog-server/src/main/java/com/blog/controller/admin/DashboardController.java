package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.service.DashboardService;
import com.blog.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public Result<DashboardVO> getStats() {
        return Result.success(dashboardService.getStats());
    }
}
