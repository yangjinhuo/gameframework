package com.scriptplatform.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.scriptplatform.common.PageResult;
import com.scriptplatform.dto.ScriptQuery;
import com.scriptplatform.dto.ScriptSaveRequest;
import com.scriptplatform.entity.ScriptInfo;
import com.scriptplatform.exception.BizException;
import com.scriptplatform.mapper.ScriptInfoMapper;
import com.scriptplatform.service.ScriptInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ScriptInfoServiceImpl implements ScriptInfoService {

    @Resource
    private ScriptInfoMapper scriptInfoMapper;

    @Override
    public PageResult<ScriptInfo> page(ScriptQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<ScriptInfo> list = scriptInfoMapper.selectPageBrief(query);
        return PageResult.of(new PageInfo<>(list));
    }

    @Override
    public ScriptInfo getById(Long id) {
        ScriptInfo info = scriptInfoMapper.selectById(id);
        if (info == null) throw new BizException("脚本不存在");
        return info;
    }

    @Override
    public ScriptInfo create(ScriptSaveRequest req) {
        ScriptInfo info = new ScriptInfo();
        BeanUtils.copyProperties(req, info);
        info.setVersion(1);
        if (info.getStatus() == null) info.setStatus(1);
        scriptInfoMapper.insert(info);
        return scriptInfoMapper.selectById(info.getId());
    }

    @Override
    public ScriptInfo update(ScriptSaveRequest req) {
        if (req.getId() == null) throw new BizException("脚本 ID 不能为空");
        ScriptInfo existing = scriptInfoMapper.selectById(req.getId());
        if (existing == null) throw new BizException("脚本不存在");
        if (req.getVersion() != null && !req.getVersion().equals(existing.getVersion())) {
            throw new BizException("脚本已被他人修改，请刷新后重试");
        }
        ScriptInfo entity = new ScriptInfo();
        BeanUtils.copyProperties(req, entity);
        // use the version from existing record as optimistic lock
        entity.setVersion(existing.getVersion());
        int rows = scriptInfoMapper.updateWithVersion(entity);
        if (rows == 0) {
            throw new BizException("脚本已被他人修改，请刷新后重试");
        }
        return scriptInfoMapper.selectById(req.getId());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        ScriptInfo info = scriptInfoMapper.selectById(id);
        if (info == null) throw new BizException("脚本不存在");
        scriptInfoMapper.updateStatus(id, status);
    }

    @Override
    public void delete(Long id) {
        ScriptInfo info = scriptInfoMapper.selectById(id);
        if (info == null) throw new BizException("脚本不存在");
        scriptInfoMapper.deleteById(id);
    }

    @Override
    public List<String> listModules() {
        return scriptInfoMapper.distinctModules();
    }
}
