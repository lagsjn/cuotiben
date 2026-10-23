package com.cuotiben.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册/登录相关请求体。
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "请填写用户名")
            @Size(min = 3, max = 20, message = "用户名长度需在 3-20 位之间")
            String username,

            @NotBlank(message = "请填写邮箱")
            @Email(message = "邮箱格式不正确")
            String email,

            @NotBlank(message = "请填写密码")
            @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
            String password,

            @Size(max = 20, message = "昵称过长")
            String nickname
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "请填写用户名或邮箱") String account,
            @NotBlank(message = "请填写密码") String password
    ) {
    }

    /** 登录成功后返回给前端的内容 */
    public record LoginResult(String token, UserVO user) {
    }

    public record UserVO(Long id, String username, String email, String nickname, String identity, String goal) {
    }
}
