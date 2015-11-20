package com.jzt.spider.controller;

import com.jzt.spider.entity.Task;
import com.jzt.spider.service.BaseService;
import com.jzt.spider.service.TaskService;
import com.jzt.spider.vo.RestfulResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Charles on 2015/11/19.
 */
@Controller
@RequestMapping("/task")
public class TaskController extends BaseAdminController<Task> {
    @Autowired
    private TaskService taskService;

    @Override
    public BaseService<Task> getBaseAdminService() {
        return taskService;
    }

    @RequestMapping(value = "/{id}/refresh", method = RequestMethod.PUT)
    public RestfulResult<Task> refresh(@PathVariable Long id) {
        taskService.refresh();
        return new RestfulResult<>();
    }
}
