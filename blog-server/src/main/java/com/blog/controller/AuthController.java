package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.LoginDTO;
import com.blog.service.AuthService;
import com.blog.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            authService.logout(token.substring(7));
        }
        return Result.success();
    }
}
