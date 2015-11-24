package com.jzt.spider.service;

import com.jzt.spider.BaseTest;
import com.jzt.spider.entity.Task;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Charles on 2015/11/23.
 */
public class TaskServiceTest extends BaseTest {
    @Autowired
    TaskService taskService;

    @Test
    public void test_runTask() throws ScriptException {
        // 运行挂号网起始任务, 产生列表页任务
//        taskService.runTask(taskService.find(1L));
        // 刷新, 下载列表页
//        taskService.refresh();
        // 运行列表页任务
//        taskService.runTask(taskService.find(2L));
        // 刷新, 下载详情页
//        taskService.refresh();
        // 运行详情页
        List<Task> taskList = taskService.query(0, 25, "status=?", Arrays.asList(Task.STATUS_RESPONSE), null);
        for (Task task : taskList) {
            taskService.runTask(task);
        }
    }
}
