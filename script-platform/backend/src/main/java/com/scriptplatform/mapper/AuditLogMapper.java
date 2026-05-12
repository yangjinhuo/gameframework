package com.scriptplatform.mapper;

import com.scriptplatform.dto.AuditQuery;
import com.scriptplatform.entity.AuditLog;

import java.util.List;

public interface AuditLogMapper {

    int insert(AuditLog log);

    List<AuditLog> selectPage(AuditQuery query);

    List<AuditLog> selectList(AuditQuery query);
}
