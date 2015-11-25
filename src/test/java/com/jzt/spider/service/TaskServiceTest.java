package com.jzt.spider.service;

import com.jzt.spider.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.script.ScriptException;

/**
 * Created by Charles on 2015/11/23.
 */
public class TaskServiceTest extends BaseTest {
    @Autowired
    TaskService taskService;

    @Test
    public void test_runTask() throws ScriptException {
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
