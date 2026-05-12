package com.scriptplatform.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.scriptplatform.common.PageResult;
import com.scriptplatform.dto.AuditQuery;
import com.scriptplatform.entity.AuditLog;
import com.scriptplatform.mapper.AuditLogMapper;
import com.scriptplatform.service.AuditLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Resource
    private AuditLogMapper auditLogMapper;

    @Override
    public void record(AuditLog log) {
        auditLogMapper.insert(log);
    }

    @Override
    public PageResult<AuditLog> page(AuditQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<AuditLog> list = auditLogMapper.selectPage(query);
        return PageResult.of(new PageInfo<>(list));
    }

    @Override
    public List<AuditLog> list(AuditQuery query) {
        return auditLogMapper.selectList(query);
    }
}
