package com.scriptplatform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ScriptSaveRequest {
    private Long id;

    @NotBlank(message = "脚本名称不能为空")
    @Size(max = 128)
    private String scriptName;

    @NotBlank(message = "脚本路径不能为空")
    @Size(max = 512)
    private String scriptPath;

    @NotBlank(message = "归属模块不能为空")
    @Size(max = 64)
    private String module;

    @NotBlank(message = "功能说明不能为空")
    private String description;

    @NotBlank(message = "负责人不能为空")
    @Size(max = 64)
    private String owner;

    private String alertDingtalk;
    private String alertPhone;
    private String scheduleCron;
    private String scheduleDesc;

    @NotBlank(message = "脚本内容不能为空")
    private String scriptContent;

    private Integer status = 1;

    /**
     * optimistic lock — expected current version when updating.
     */
    private Integer version;
}
