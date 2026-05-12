package com.scriptplatform.dto;

import com.scriptplatform.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {
    private String username;
    private Integer status;
    private String startTime;
    private String endTime;
}
