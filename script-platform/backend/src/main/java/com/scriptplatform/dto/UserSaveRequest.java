package com.scriptplatform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UserSaveRequest {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{3,32}$", message = "用户名必须为 3~32 位字母数字下划线")
    private String username;

    /**
     * optional on update; required on create (validated in service).
     */
    @Size(max = 64)
    private String password;

    @NotBlank(message = "角色不能为空")
    private String role;

    private Integer status = 1;

    @Size(max = 256)
    private String remark;
}
