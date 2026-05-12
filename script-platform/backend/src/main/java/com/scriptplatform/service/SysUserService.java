package com.scriptplatform.service;

import com.scriptplatform.common.PageResult;
import com.scriptplatform.dto.UserQuery;
import com.scriptplatform.dto.UserSaveRequest;
import com.scriptplatform.entity.SysUser;

public interface SysUserService {

    PageResult<SysUser> page(UserQuery query);

    SysUser getById(Long id);

    SysUser create(UserSaveRequest req);

    SysUser update(UserSaveRequest req);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
