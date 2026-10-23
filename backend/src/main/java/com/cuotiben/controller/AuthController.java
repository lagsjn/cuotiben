package com.cuotiben.controller;

import com.cuotiben.common.ApiResponse;
import com.cuotiben.common.UserContext;
import com.cuotiben.dto.AuthDtos;
import com.cuotiben.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能 1：注册与登录
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/register */
    @PostMapping("/register")
    public ApiResponse<AuthDtos.LoginResult> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    /** POST /api/auth/login，account 支持用户名或邮箱 */
    @PostMapping("/login")
    public ApiResponse<AuthDtos.LoginResult> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    /** POST /api/auth/logout（注销当前令牌） */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            authService.logout(header.substring(7));
        }
        return ApiResponse.ok();
    }

    /** GET /api/auth/me */
    @GetMapping("/me")
    public ApiResponse<AuthDtos.UserVO> me() {
        return ApiResponse.ok(authService.currentUser(UserContext.require()));
    }
}
