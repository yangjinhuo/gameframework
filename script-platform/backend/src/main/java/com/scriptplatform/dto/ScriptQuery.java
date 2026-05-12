package com.scriptplatform.dto;

import com.scriptplatform.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScriptQuery extends PageQuery {
    private String scriptName;
    private String module;
    private String owner;
    private Integer status;
}
