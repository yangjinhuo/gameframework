package com.scriptplatform.controller;

import com.scriptplatform.common.PageResult;
import com.scriptplatform.common.R;
import com.scriptplatform.dto.AuditQuery;
import com.scriptplatform.entity.AuditLog;
import com.scriptplatform.security.RequireRole;
import com.scriptplatform.service.AuditLogService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private AuditLogService auditLogService;

    @GetMapping
    @RequireRole({"super_admin", "admin", "readonly"})
    public R<PageResult<AuditLog>> page(AuditQuery query) {
        return R.ok(auditLogService.page(query));
    }

    @GetMapping("/export")
    @RequireRole({"super_admin", "admin"})
    public void export(AuditQuery query, HttpServletResponse response) throws Exception {
        List<AuditLog> list = auditLogService.list(query);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("audit_logs");
            String[] headers = {"ID", "操作时间", "事件类型", "操作人", "IP",
                    "操作对象ID", "操作对象名称", "请求参数", "响应结果", "结果", "备注"};
            Row head = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) head.createCell(i).setCellValue(headers[i]);

            int rowIdx = 1;
            for (AuditLog log : list) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(log.getId() == null ? "" : log.getId().toString());
                r.createCell(1).setCellValue(log.getEventTime() == null ? "" : log.getEventTime().format(FMT));
                r.createCell(2).setCellValue(nv(log.getEventType()));
                r.createCell(3).setCellValue(nv(log.getOperator()));
                r.createCell(4).setCellValue(nv(log.getOperatorIp()));
                r.createCell(5).setCellValue(log.getTargetId() == null ? "" : log.getTargetId().toString());
                r.createCell(6).setCellValue(nv(log.getTargetName()));
                r.createCell(7).setCellValue(nv(log.getRequestParams()));
                r.createCell(8).setCellValue(nv(log.getResponseResult()));
                r.createCell(9).setCellValue(log.getStatus() != null && log.getStatus() == 1 ? "成功" : "失败");
                r.createCell(10).setCellValue(nv(log.getRemark()));
            }

            String filename = URLEncoder.encode("audit_logs.xlsx", StandardCharsets.UTF_8.name());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            try (OutputStream os = response.getOutputStream()) {
                wb.write(os);
            }
        }
    }

    private static String nv(String s) {
        return s == null ? "" : s;
    }
}
