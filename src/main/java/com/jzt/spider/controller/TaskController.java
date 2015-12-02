package com.jzt.spider.controller;

import com.alibaba.fastjson.JSONObject;
import com.jzt.spider.entity.Task;
import com.jzt.spider.service.BaseService;
import com.jzt.spider.service.TaskPoolService;
import com.jzt.spider.service.TaskService;
import com.jzt.spider.vo.RestfulResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Charles on 2015/11/19.
 */
@Controller
@RequestMapping("/task")
public class TaskController extends BaseAdminController<Task> {
    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskPoolService taskPoolService;

    @Override
    public BaseService<Task> getBaseAdminService() {
        return taskService;
    }

    @RequestMapping(value = "/{id}/refresh", method = RequestMethod.PUT)
    public RestfulResult<Task> refresh(@PathVariable Long id) {
        taskService.refresh(taskService.find(id));
        return new RestfulResult<>();
    }

    @RequestMapping(value = "/{id}/response", method = RequestMethod.PUT)
    public RestfulResult<Task> response(@PathVariable Long id, String data) throws ScriptException {
        taskPoolService.addResponseTask(id, data);
        return new RestfulResult<>();
    }

    @RequestMapping(value = "/{id}/run", method = RequestMethod.PUT)
    public RestfulResult<Task> run(@PathVariable Long id) throws ScriptException {
        taskService.runTask(taskService.find(id));
        return new RestfulResult<>();
    }

    @RequestMapping(value = "/startTask", method = RequestMethod.POST)
    public RestfulResult<Task> startTask(String name, String[] url, Long robotId) {
        JSONObject response = new JSONObject();
        List<Task> subTask = new ArrayList<>();

        for (String oneUrl : url) {
            subTask.add(new Task(
                    oneUrl,
                    robotId, // 处理列表页面 robot
                    "",
                    "[{\"url\" : \""+oneUrl+"\"}]",
                    "",
                    "",
                    Task.STATUS_NOT_START
            ));
        }

        response.put("subTask", subTask);

        Task task = new Task(name, 1L, "", "", response.toJSONString(), "", Task.STATUS_DONE);
        task.setStartTime(new Date());
        task.setRefreshTime(new Date());

        return this.post(task);
    }
}
