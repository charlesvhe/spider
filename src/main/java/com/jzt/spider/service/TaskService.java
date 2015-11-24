package com.jzt.spider.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jzt.spider.dao.BaseDao;
import com.jzt.spider.dao.TaskDao;
import com.jzt.spider.entity.Robot;
import com.jzt.spider.entity.Task;
import com.sun.istack.internal.NotNull;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    // robotId线程池
    private static final ConcurrentHashMap<Long, ThreadPoolExecutor> THREAD_POOL_MAP = new ConcurrentHashMap();
    public static final int MAX_QUEUE = 10000;
    public static final int TIME_OUT = 1000 * 60;


    @Autowired
    private TaskDao taskDao;
    @Autowired
    private RobotService robotService;

    @Override
    public BaseDao<Task> getDao() {
        return taskDao;
    }

    public Task findByName(String name) {
        List<Task> list = this.query(0, 1, "name = ?", Arrays.asList(name), null);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    @Scheduled(cron = "* */15 9-21 * * *")  // 用户活跃时段刷新, 防止被发现
    @Transactional
    public void refresh() {
        if (TaskService.REFRESH_LOCK.tryLock()) {
            try {
                // 获取需要刷新的任务列表, 未开始任务/超时任务/刷新任务
                List<Task> taskList = this.query(0, MAX_QUEUE, "status=? or (status=? and startTime>?) or (status=? and refreshTime<?)",
                        Arrays.asList(Task.STATUS_NOT_START, Task.STATUS_START, new Date(System.currentTimeMillis() + TIME_OUT), Task.STATUS_DONE, new Date()), null);
                for (Task task : taskList) {
                    // 异常不影响后续任务处理
                    try {
                        DownloadTask command = new DownloadTask(this, task.getId(), task.getRequest());
                        TaskService.addTask(task.getRobotId(), robotService, command);

                        // 变更任务状态
                        task.setStartTime(new Date());
                        task.setStatus(Task.STATUS_START);
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

    public void addParseTask(Long taskId, String response) {
        Task task = this.find(taskId);
        TaskService.addTask(task.getRobotId(), robotService, new ParseTask(this, taskId, response));
    }

    private static void addTask(Long robotId, RobotService robotService, Runnable task) {
        ThreadPoolExecutor tpe = THREAD_POOL_MAP.get(robotId);
        if (null == tpe) {
            // 线程生命周期大于刷新间隔
            Robot robot = robotService.find(robotId);
            tpe = new ThreadPoolExecutor(1, robot.getCount(), 20, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            THREAD_POOL_MAP.put(robot.getId(), tpe);
        }
        if (!tpe.getQueue().contains(task)) {
            tpe.execute(task);
        }
    }

    @Transactional
    private void parseResponse(Long taskId, String response) throws ScriptException {
        Task task = this.find(taskId);
        task.setResponse(response);
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
        task.setEndTime(new Date());
        task.setStatus(Task.STATUS_DONE);
        task.setRefreshTime(jsonObject.getDate("refreshTime"));
        this.merge(task);

        // 解析output 是否开启子任务
        JSONArray jsonArray = jsonObject.getJSONArray("subTask");
        if (!CollectionUtils.isEmpty(jsonArray)) for (int i = 0; i < jsonArray.size(); i++) {
            Task subTask = jsonArray.getObject(i, Task.class);
            // 保存子任务
            subTask.setParentId(task.getId());
            // 鉴别任务唯一性
            Task persistTask = this.findByName(subTask.getName());
            if (null != persistTask) {
                subTask.setId(persistTask.getId());
                this.merge(subTask);
            } else {
                this.persist(subTask);
            }
        }
    }

    private class ParseTask implements Runnable {
        private TaskService taskService;
        private Long taskId;
        private String response;

        public ParseTask(@NotNull TaskService taskService, @NotNull Long taskId, @NotNull String response) {
            this.taskService = taskService;
            this.taskId = taskId;
            this.response = response;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ParseTask parseTask = (ParseTask) o;

            return taskId.equals(parseTask.taskId);

        }

        @Override
        public int hashCode() {
            return taskId.hashCode();
        }

        @Override
        public void run() {
            try {
                taskService.parseResponse(taskId, response);
            } catch (ScriptException e) {
                LOG.warn(e);
            }
        }
    }

    private class DownloadTask implements Runnable {
        private TaskService taskService;
        private Long taskId;
        private String request;

        public DownloadTask(@NotNull TaskService taskService, @NotNull Long taskId, @NotNull String request) {
            this.taskService = taskService;
            this.taskId = taskId;
            this.request = request;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DownloadTask that = (DownloadTask) o;

            return taskId.equals(that.taskId);

        }

        @Override
        public int hashCode() {
            return taskId.hashCode();
        }

        @Override
        public void run() {
            try {
                // 异步下载
                JSONArray requestArray = JSONObject.parseArray(request);
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
                responseArray.toJSONString();
                // 异步下载完成
                taskService.addParseTask(taskId, responseArray.toJSONString());
            } catch (IOException e) {
                LOG.warn(e);
            }
        }
    }
}
