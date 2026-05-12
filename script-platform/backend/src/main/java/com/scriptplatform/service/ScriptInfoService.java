package com.scriptplatform.service;

import com.scriptplatform.common.PageResult;
import com.scriptplatform.dto.ScriptQuery;
import com.scriptplatform.dto.ScriptSaveRequest;
import com.scriptplatform.entity.ScriptInfo;

import java.util.List;

public interface ScriptInfoService {

    PageResult<ScriptInfo> page(ScriptQuery query);

    ScriptInfo getById(Long id);

    ScriptInfo create(ScriptSaveRequest req);

    ScriptInfo update(ScriptSaveRequest req);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    List<String> listModules();
}
