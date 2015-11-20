package com.jzt.spider.controller;

import com.jzt.spider.service.BaseService;
import com.jzt.spider.vo.RestfulResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by Charles on 2015/11/19.
 */
@Controller
@ResponseBody
public abstract class BaseAdminController<T> {
    public abstract BaseService<T> getBaseAdminService();

    @RequestMapping(method = RequestMethod.GET)
    public RestfulResult<T> index() {
        //TODO add paging
        List<T> list = this.getBaseAdminService().query(0, 25, null, null, null);

        return new RestfulResult<>(list);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public RestfulResult<T> get(@PathVariable Long id) {
        T entity = this.getBaseAdminService().find(id);

        return new RestfulResult<>(entity);
    }

    @RequestMapping(method = RequestMethod.POST)
    public RestfulResult<T> post(T entity) {
        this.getBaseAdminService().persist(entity);

        return new RestfulResult<>(entity);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public RestfulResult<T> put(@PathVariable Long id, T entity) {
        this.getBaseAdminService().merge(entity);

        return new RestfulResult<>(entity);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public RestfulResult<T> delete(@PathVariable Long id) {
        this.getBaseAdminService().remove(id);

        return new RestfulResult<>();
    }
}
