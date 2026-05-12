package com.scriptplatform.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLog {
    private Long id;
    private LocalDateTime eventTime;
    private String eventType;
    private String operator;
    private String operatorIp;
    private Long targetId;
    private String targetName;
    private String requestParams;
    private String responseResult;
    private Integer status;
    private String remark;
}
