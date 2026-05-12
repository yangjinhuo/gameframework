package com.scriptplatform.service;

import com.scriptplatform.common.PageResult;
import com.scriptplatform.dto.AuditQuery;
import com.scriptplatform.entity.AuditLog;

import java.util.List;

public interface AuditLogService {

    void record(AuditLog log);

    PageResult<AuditLog> page(AuditQuery query);

    List<AuditLog> list(AuditQuery query);
}
