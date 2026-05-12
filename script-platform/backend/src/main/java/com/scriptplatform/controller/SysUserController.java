package com.scriptplatform.controller;

import com.scriptplatform.aspect.AuditEvent;
import com.scriptplatform.common.PageResult;
import com.scriptplatform.common.R;
import com.scriptplatform.dto.UserQuery;
import com.scriptplatform.dto.UserSaveRequest;
import com.scriptplatform.entity.SysUser;
import com.scriptplatform.security.RequireRole;
import com.scriptplatform.security.UserContext;
import com.scriptplatform.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequireRole({"super_admin"})
public class SysUserController {

    @Resource
    private SysUserService sysUserService;

    @GetMapping
    public R<PageResult<SysUser>> page(UserQuery query) {
        return R.ok(sysUserService.page(query));
    }

    @GetMapping("/{id}")
    public R<SysUser> detail(@PathVariable Long id) {
        return R.ok(sysUserService.getById(id));
    }

    @GetMapping("/me")
    @RequireRole({"super_admin", "admin", "readonly"})
    public R<Map<String, Object>> me() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", UserContext.get() == null ? null : UserContext.get().getId());
        map.put("username", UserContext.username());
        map.put("role", UserContext.role());
        return R.ok(map);
    }

    @PostMapping
    @AuditEvent(type = "USER_CREATE", remark = "新增用户")
    public R<SysUser> create(@RequestBody @Valid UserSaveRequest req) {
        return R.ok(sysUserService.create(req));
    }

    @PutMapping("/{id}")
    @AuditEvent(type = "USER_UPDATE", remark = "修改用户")
    public R<SysUser> update(@PathVariable Long id, @RequestBody @Valid UserSaveRequest req) {
        req.setId(id);
        return R.ok(sysUserService.update(req));
    }

    @PatchMapping("/{id}/status")
    @AuditEvent(type = "USER_UPDATE", remark = "切换用户状态")
    public R<Void> status(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        sysUserService.updateStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @AuditEvent(type = "USER_DELETE", remark = "删除用户")
    public R<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return R.ok();
    }
}
