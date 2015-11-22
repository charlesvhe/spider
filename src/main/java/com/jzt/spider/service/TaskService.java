package com.jzt.spider.service;

import com.jzt.spider.dao.BaseDao;
import com.jzt.spider.dao.TaskDao;
import com.jzt.spider.entity.Robot;
import com.jzt.spider.entity.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Charles on 2015/11/19.
 */
@Service
public class TaskService extends BaseService<Task> {
    // 全局锁 Scheduled 和 Controller 有可能同时调用 refresh()
    private static final Lock REFRESH_LOCK = new ReentrantLock();
    private static final Logger LOG = LogManager.getLogger(TaskService.class);

    @Autowired
    private TaskDao taskDao;

    @Override
    public BaseDao<Task> getDao() {
        return taskDao;
    }

    @Scheduled(cron = "* */15 9-21 * * *")  // 活跃时间刷新, 防止被发现
    public void refresh() {
        if (TaskService.REFRESH_LOCK.tryLock()) {
            try {
                LOG.info("refresh task.");

            } finally {
                TaskService.REFRESH_LOCK.unlock();
            }
        } else {
            LOG.warn("refresh task is running.");
        }
    }

    public void runTask(Task task) throws ScriptException {
        // 获得执行该任务的机器人
        Robot robot = new Robot();
        robot.setScriptEngine("groovy");
        robot.setScript("return context+\"-\"+input;");

        // 传入数据 运行脚本
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(robot.getScriptEngine());
        Bindings bindings = engine.createBindings();
        bindings.put("context",task.getContext());
        bindings.put("input",task.getInput());

        String output = (String) engine.eval(robot.getScript(), bindings);

        // 记录脚本运行结果 更新任务状态
        task.setOutput(output);
        task.setEndTime(new Date(System.currentTimeMillis()));
        task.setStatus(Task.STATUS_DONE);

        // 解析output 是否下载资源并开启子任务
    }

    public static void main(String[] args) {
        TaskService ts = new TaskService();
        Task task = new Task();
        task.setContext("ctx");
        task.setInput("ipt");

        try {
            ts.runTask(task);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
