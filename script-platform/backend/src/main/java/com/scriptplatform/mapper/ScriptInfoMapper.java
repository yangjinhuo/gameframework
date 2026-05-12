package com.scriptplatform.mapper;

import com.scriptplatform.dto.ScriptQuery;
import com.scriptplatform.entity.ScriptInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScriptInfoMapper {

    List<ScriptInfo> selectPage(ScriptQuery query);

    /**
     * list projection (no script_content) for list view.
     */
    List<ScriptInfo> selectPageBrief(ScriptQuery query);

    ScriptInfo selectById(@Param("id") Long id);

    int insert(ScriptInfo info);

    int updateWithVersion(ScriptInfo info);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(@Param("id") Long id);

    List<String> distinctModules();
}
