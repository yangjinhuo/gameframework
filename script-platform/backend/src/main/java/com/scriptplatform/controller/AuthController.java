package com.scriptplatform.controller;

import com.scriptplatform.aspect.AuditEvent;
import com.scriptplatform.common.R;
import com.scriptplatform.dto.LoginRequest;
import com.scriptplatform.dto.LoginResponse;
import com.scriptplatform.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login")
    @AuditEvent(type = "USER_LOGIN", remark = "用户登录")
    public R<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public R<LoginResponse> refresh() {
        return R.ok(authService.refresh());
    }

    @PostMapping("/logout")
    @AuditEvent(type = "USER_LOGOUT", remark = "用户登出")
    public R<Void> logout() {
        // stateless JWT: client just discards the token
        return R.ok();
    }
}
