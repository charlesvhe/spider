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
        // 刷新
        taskService.refresh();

        try {
            // 异步操作, 需要主线程存活
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
