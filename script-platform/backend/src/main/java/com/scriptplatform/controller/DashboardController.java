package com.scriptplatform.controller;

import com.scriptplatform.common.R;
import com.scriptplatform.dto.AuditQuery;
import com.scriptplatform.dto.ScriptQuery;
import com.scriptplatform.dto.UserQuery;
import com.scriptplatform.security.RequireRole;
import com.scriptplatform.service.AuditLogService;
import com.scriptplatform.service.ScriptInfoService;
import com.scriptplatform.service.SysUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequireRole({"super_admin", "admin", "readonly"})
public class DashboardController {

    @Resource
    private SysUserService sysUserService;
    @Resource
    private ScriptInfoService scriptInfoService;
    @Resource
    private AuditLogService auditLogService;

    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        UserQuery uq = new UserQuery();
        uq.setPageSize(1);
        data.put("userCount", sysUserService.page(uq).getTotal());
        ScriptQuery sq = new ScriptQuery();
        sq.setPageSize(1);
        data.put("scriptCount", scriptInfoService.page(sq).getTotal());
        AuditQuery aq = new AuditQuery();
        aq.setPageSize(1);
        data.put("auditCount", auditLogService.page(aq).getTotal());
        return R.ok(data);
    }
}
