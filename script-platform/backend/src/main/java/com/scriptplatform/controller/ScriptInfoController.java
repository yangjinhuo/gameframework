package com.scriptplatform.controller;

import com.scriptplatform.aspect.AuditEvent;
import com.scriptplatform.common.PageResult;
import com.scriptplatform.common.R;
import com.scriptplatform.dto.ScriptQuery;
import com.scriptplatform.dto.ScriptSaveRequest;
import com.scriptplatform.entity.ScriptInfo;
import com.scriptplatform.security.RequireRole;
import com.scriptplatform.service.ScriptInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scripts")
public class ScriptInfoController {

    @Resource
    private ScriptInfoService scriptInfoService;

    @GetMapping
    @RequireRole({"super_admin", "admin", "readonly"})
    public R<PageResult<ScriptInfo>> page(ScriptQuery query) {
        return R.ok(scriptInfoService.page(query));
    }

    @GetMapping("/modules")
    @RequireRole({"super_admin", "admin", "readonly"})
    public R<List<String>> modules() {
        return R.ok(scriptInfoService.listModules());
    }

    @GetMapping("/{id}")
    @RequireRole({"super_admin", "admin", "readonly"})
    public R<ScriptInfo> detail(@PathVariable Long id) {
        return R.ok(scriptInfoService.getById(id));
    }

    @PostMapping
    @RequireRole({"super_admin", "admin"})
    @AuditEvent(type = "SCRIPT_CREATE", remark = "新增脚本")
    public R<ScriptInfo> create(@RequestBody @Valid ScriptSaveRequest req) {
        return R.ok(scriptInfoService.create(req));
    }

    @PutMapping("/{id}")
    @RequireRole({"super_admin", "admin"})
    @AuditEvent(type = "SCRIPT_EDIT", remark = "编辑脚本")
    public R<ScriptInfo> update(@PathVariable Long id, @RequestBody @Valid ScriptSaveRequest req) {
        req.setId(id);
        return R.ok(scriptInfoService.update(req));
    }

    @PatchMapping("/{id}/status")
    @RequireRole({"super_admin", "admin"})
    @AuditEvent(type = "SCRIPT_EDIT", remark = "切换脚本状态")
    public R<Void> status(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        scriptInfoService.updateStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({"super_admin"})
    @AuditEvent(type = "SCRIPT_DELETE", remark = "删除脚本")
    public R<Void> delete(@PathVariable Long id) {
        scriptInfoService.delete(id);
        return R.ok();
    }
}
