package com.scriptplatform.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.scriptplatform.common.PageResult;
import com.scriptplatform.dto.UserQuery;
import com.scriptplatform.dto.UserSaveRequest;
import com.scriptplatform.entity.SysUser;
import com.scriptplatform.exception.BizException;
import com.scriptplatform.mapper.SysUserMapper;
import com.scriptplatform.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SysUserServiceImpl implements SysUserService {

    private static final Pattern PWD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,32}$");

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public PageResult<SysUser> page(UserQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<SysUser> list = sysUserMapper.selectPage(query);
        return PageResult.of(new PageInfo<>(list));
    }

    @Override
    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    public SysUser create(UserSaveRequest req) {
        if (sysUserMapper.selectByUsername(req.getUsername()) != null) {
            throw new BizException("用户名已存在");
        }
        if (req.getPassword() == null || req.getPassword().isEmpty()) {
            throw new BizException("密码不能为空");
        }
        validatePassword(req.getPassword());
        validateRole(req.getRole());

        SysUser entity = new SysUser();
        BeanUtils.copyProperties(req, entity);
        entity.setPassword(passwordEncoder.encode(req.getPassword()));
        if (entity.getStatus() == null) entity.setStatus(1);
        sysUserMapper.insert(entity);
        return sysUserMapper.selectById(entity.getId());
    }

    @Override
    public SysUser update(UserSaveRequest req) {
        if (req.getId() == null) throw new BizException("用户 ID 不能为空");
        SysUser existing = sysUserMapper.selectById(req.getId());
        if (existing == null) throw new BizException("用户不存在");
        validateRole(req.getRole());

        SysUser entity = new SysUser();
        entity.setId(req.getId());
        entity.setRole(req.getRole());
        entity.setStatus(req.getStatus());
        entity.setRemark(req.getRemark());

        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            validatePassword(req.getPassword());
            entity.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        sysUserMapper.update(entity);
        return sysUserMapper.selectById(req.getId());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser u = sysUserMapper.selectById(id);
        if (u == null) throw new BizException("用户不存在");
        if ("super_admin".equals(u.getRole()) && status != null && status == 0) {
            throw new BizException("超级管理员不可禁用");
        }
        sysUserMapper.updateStatus(id, status);
    }

    @Override
    public void delete(Long id) {
        SysUser u = sysUserMapper.selectById(id);
        if (u == null) throw new BizException("用户不存在");
        if ("super_admin".equals(u.getRole())) {
            throw new BizException("超级管理员账号不可删除");
        }
        sysUserMapper.deleteById(id);
    }

    private void validatePassword(String pwd) {
        if (!PWD_PATTERN.matcher(pwd).matches()) {
            throw new BizException("密码需 8~32 位，且包含大写字母、小写字母、数字");
        }
    }

    private void validateRole(String role) {
        if (!"super_admin".equals(role) && !"admin".equals(role) && !"readonly".equals(role)) {
            throw new BizException("角色非法");
        }
    }
}
