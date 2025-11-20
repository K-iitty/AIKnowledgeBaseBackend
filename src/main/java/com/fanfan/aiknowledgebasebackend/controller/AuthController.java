package com.fanfan.aiknowledgebasebackend.controller;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.fanfan.aiknowledgebasebackend.domain.dto.LoginRequest;
import com.fanfan.aiknowledgebasebackend.domain.dto.RegisterRequest;
import com.fanfan.aiknowledgebasebackend.domain.dto.TokenResponse;
import com.fanfan.aiknowledgebasebackend.domain.entity.User;
import com.fanfan.aiknowledgebasebackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "用户注册、登录等认证相关接口")
public class AuthController {

    private final UserService userService;
    private final CaptchaService captchaService;

    public AuthController(UserService userService, CaptchaService captchaService) {
        this.userService = userService;
        this.captchaService = captchaService;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册新账号")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest req) {
        User u = userService.register(req.getUsername(), req.getPassword(), req.getNickname(), req.getEmail(), req.getPhone());
        return ResponseEntity.ok(u);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取JWT令牌")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = userService.login(req.getUsername(), req.getPassword(), req.getCaptchaVerification());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/captcha/get")
    @Operation(summary = "获取验证码", description = "获取滑动验证码")
    public ResponseModel getCaptcha(@RequestBody CaptchaVO captchaVO) {
        return captchaService.get(captchaVO);
    }

    @PostMapping("/captcha/check")
    @Operation(summary = "校验验证码", description = "校验滑动验证码")
    public ResponseModel checkCaptcha(@RequestBody CaptchaVO captchaVO) {
        return captchaService.check(captchaVO);
    }
}
