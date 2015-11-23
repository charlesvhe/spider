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
        taskService.runTask(taskService.find(2L));
    }

//    @Test
//    public void test_refresh() {
//        taskService.refresh();
//    }
}
