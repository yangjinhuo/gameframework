package com.scriptplatform.mapper;

import com.scriptplatform.dto.UserQuery;
import com.scriptplatform.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserMapper {

    List<SysUser> selectPage(UserQuery query);

    SysUser selectById(@Param("id") Long id);

    SysUser selectByUsername(@Param("username") String username);

    int insert(SysUser user);

    int update(SysUser user);

    int deleteById(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
