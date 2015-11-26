package com.jzt.spider.controller;

import com.alibaba.fastjson.JSONObject;
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

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Date;
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

    @RequestMapping(value = "/{id}/run", method = RequestMethod.PUT)
    public RestfulResult<Task> run(@PathVariable Long id) throws ScriptException {
        taskService.runTask(taskService.find(id));
        return new RestfulResult<>();
    }

    @RequestMapping(value = "/startTask", method = RequestMethod.POST)
    public RestfulResult<Task> startTask(String name, List<String> urlList, Long robotId) {
        JSONObject response = new JSONObject();
        List<Task> subTask = new ArrayList<>();

        for (String url : urlList) {
            subTask.add(new Task(
                    url,
                    robotId, // 处理列表页面 robot
                    "",
                    "[{\"url\" : \""+url+"\"}]",
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
