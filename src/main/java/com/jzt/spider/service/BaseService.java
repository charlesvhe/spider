package com.jzt.spider.service;

import com.jzt.spider.dao.BaseDao;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Charles on 2015/11/19.
 */
public abstract class BaseService<T> {
    public abstract BaseDao<T> getDao();

    @Transactional
    public void persist(T entity) {
        getDao().persist(entity);
    }

    @Transactional
    public void remove(Serializable... entityIds) {
        getDao().remove(entityIds);
    }

    @Transactional
    public void merge(T entity) {
        getDao().merge(entity);
    }

    public T find(Serializable entityId) {
        return getDao().find(entityId);
    }

    public List<T> query(int fistResult, int maxResult, String whereSql,
                         List<Object> params, LinkedHashMap<String, BaseDao.Order> orderBy) {
        return getDao().query(fistResult, maxResult, whereSql, params, orderBy);
    }

    public Long count(String whereSql, List<Object> params) {
        return getDao().count(whereSql, params);
    }
}
