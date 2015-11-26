package com.jzt.spider.service;

import com.jzt.spider.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.script.ScriptException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Charles on 2015/11/23.
 */
public class TaskServiceTest extends BaseTest {
    @Autowired
    TaskService taskService;

    @Test
    public void test_refresh() throws ScriptException {
        // 刷新
        taskService.refresh();

        try {
            // 异步操作, 需要主线程存活
            for (ThreadPoolExecutor threadPoolExecutor : TaskPoolService.THREAD_POOL_MAP.values()) {
                while (threadPoolExecutor.getTaskCount() != threadPoolExecutor.getCompletedTaskCount()){// 所以任务已完成
                    Thread.sleep(1000);
                }
            }
            System.out.println("THREAD_POOL_MAP all isTerminated DONE");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
