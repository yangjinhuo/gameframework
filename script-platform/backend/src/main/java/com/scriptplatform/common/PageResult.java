package com.scriptplatform.common;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private long total;
    private List<T> list;

    public static <T> PageResult<T> of(PageInfo<T> pageInfo) {
        PageResult<T> r = new PageResult<>();
        r.setTotal(pageInfo.getTotal());
        r.setList(pageInfo.getList());
        return r;
    }
}
