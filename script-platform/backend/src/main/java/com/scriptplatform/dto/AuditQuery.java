package com.scriptplatform.dto;

import com.scriptplatform.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuditQuery extends PageQuery {
    private String startTime;
    private String endTime;
    private List<String> eventTypes;
    private String operator;
    private Integer status;
}
