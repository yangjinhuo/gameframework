package com.scriptplatform.service.impl;

import com.scriptplatform.dto.LoginRequest;
import com.scriptplatform.dto.LoginResponse;
import com.scriptplatform.entity.SysUser;
import com.scriptplatform.exception.BizException;
import com.scriptplatform.mapper.SysUserMapper;
import com.scriptplatform.security.JwtUtil;
import com.scriptplatform.security.LoginUser;
import com.scriptplatform.security.UserContext;
import com.scriptplatform.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BizException(400, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(403, "账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(400, "用户名或密码错误");
        }
        return buildResponse(user);
    }

    @Override
    public LoginResponse refresh() {
        LoginUser current = UserContext.get();
        if (current == null) throw new BizException(401, "未登录");
        SysUser user = sysUserMapper.selectById(current.getId());
        if (user == null) throw new BizException(401, "用户不存在");
        return buildResponse(user);
    }

    private LoginResponse buildResponse(SysUser user) {
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole());
        return resp;
    }
}
