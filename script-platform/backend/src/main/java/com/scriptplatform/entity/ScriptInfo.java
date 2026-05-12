package com.scriptplatform.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScriptInfo {
    private Long id;
    private String scriptName;
    private String scriptPath;
    private String module;
    private String description;
    private String owner;
    private String alertDingtalk;
    private String alertPhone;
    private String scheduleCron;
    private String scheduleDesc;
    private String scriptContent;
    private Integer version;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
