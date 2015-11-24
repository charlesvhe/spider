package com.jzt.spider.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jzt.spider.dao.BaseDao;
import com.jzt.spider.dao.TaskDao;
import com.jzt.spider.entity.Robot;
import com.jzt.spider.entity.Task;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
    @Autowired
    private RobotService robotService;

    @Override
    public BaseDao<Task> getDao() {
        return taskDao;
    }

    @Scheduled(cron = "* */15 9-21 * * *")  // 用户活跃时段刷新, 防止被发现
    @Transactional
    public void refresh() {
        if (TaskService.REFRESH_LOCK.tryLock()) {
            try {
                // 获取需要刷新的任务列表
                // TODO 请求超时刷新 "1=1"
                List<Task> taskList = this.query(0, 25, "status=? or (status=? and 1=1)", Arrays.asList(Task.STATUS_NOT_START, Task.STATUS_REQUEST), null);
                for (Task task : taskList) {
                    try {
                        JSONArray requestArray = JSONObject.parseArray(task.getRequest());

                        // TODO 调用分布式下载
                        task.setStatus(Task.STATUS_REQUEST);
                        this.merge(task);

                        // TODO 模拟下载完成
                        JSONArray responseArray = new JSONArray();
                        for (int i = 0; i < requestArray.size(); i++) {
                            JSONObject requestJson = requestArray.getJSONObject(i);
                            Connection connection = Jsoup.connect(requestJson.getString("url"));

                            Connection.Response response = connection.execute();
                            JSONObject responseJson = new JSONObject();
                            responseJson.put("header", response.headers());
                            responseJson.put("body", response.body());
                            responseArray.add(responseJson);
                        }

                        task.setResponse(responseArray.toJSONString());
                        task.setStatus(Task.STATUS_RESPONSE);

                        this.merge(task);
                    } catch (Exception e) {
                        LOG.warn(e);
                    }
                }
                LOG.info("refresh task.");
            } finally {
                TaskService.REFRESH_LOCK.unlock();
            }
        } else {
            LOG.warn("refresh task is running.");
        }
    }

    @Transactional
    public void runTask(Task task) throws ScriptException {
        // 获得执行该任务的机器人
        Robot robot = robotService.find(task.getRobotId());
        // 传入数据 运行脚本
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(robot.getScriptEngine());
        // 如果传入对象，为引用类型，可在脚本中改变对象属性值
        Bindings bindings = engine.createBindings();
        String jsonString = JSONObject.toJSONString(task);
        bindings.put("task", jsonString);

        String output = (String) engine.eval(robot.getScript(), bindings);
        JSONObject jsonObject = JSON.parseObject(output);
        // 记录脚本运行结果 更新任务状态
        task.setOutput(output);
        task.setEndTime(new Date(System.currentTimeMillis()));
        task.setStatus(Task.STATUS_DONE);
        task.setRefreshTime(jsonObject.getDate("refreshTime"));
        this.merge(task);

        // 解析output 是否开启子任务
        JSONArray jsonArray = jsonObject.getJSONArray("subTask");
        if (!CollectionUtils.isEmpty(jsonArray)) {
            for (int i = 0; i < jsonArray.size(); i++) {
                Task subTask = jsonArray.getObject(i, Task.class);
                // 保存子任务
                subTask.setParentId(task.getId());
                // TODO 鉴别任务 name 唯一性
                this.persist(subTask);
            }
        }
    }
}
