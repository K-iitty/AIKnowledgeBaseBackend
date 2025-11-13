package com.fanfan.aiknowledgebasebackend.controller;

import com.fanfan.aiknowledgebasebackend.entity.User;
import com.fanfan.aiknowledgebasebackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "用户注册、登录等认证相关接口")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册新账号")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterReq req) {
        User u = userService.register(req.getUsername(), req.getPassword(), req.getNickname(), req.getEmail(), req.getPhone());
        return ResponseEntity.ok(u);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取JWT令牌")
    public ResponseEntity<TokenResp> login(@Valid @RequestBody LoginReq req) {
        String token = userService.login(req.getUsername(), req.getPassword());
        TokenResp r = new TokenResp();
        r.setToken(token);
        return ResponseEntity.ok(r);
    }

    @Data
    public static class RegisterReq {
        @NotBlank
        private String username;
        @NotBlank
        @Size(min = 6, message = "密码至少6位")
        private String password;
        @NotBlank
        private String nickname;
        private String email;
        private String phone;
    }

    @Data
    public static class LoginReq {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    @Data
    public static class TokenResp {
        private String token;
    }
}
