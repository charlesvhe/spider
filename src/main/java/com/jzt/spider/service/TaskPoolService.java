package com.jzt.spider.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jzt.spider.entity.Robot;
import com.jzt.spider.entity.Task;
import com.sun.istack.internal.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by charles on 11/25/15.
 */
@Service
public class TaskPoolService {
    private static final Logger LOG = LogManager.getLogger(TaskPoolService.class);

    private static final ConcurrentHashMap<Long, ThreadPoolExecutor> THREAD_POOL_MAP = new ConcurrentHashMap();

    @Autowired
    private RobotService robotService;

    @Autowired
    private TaskService taskService;

    public void addRequestTask(Long taskId, String request) {
        Task task = taskService.find(taskId);
        TaskPoolService.addTask(task.getRobotId(), robotService, new RequestTask(taskId, request));
    }

    public void addResponseTask(Long taskId, String response) {
        Task task = taskService.find(taskId);
        TaskPoolService.addTask(task.getRobotId(), robotService, new ResponseTask(taskId, response));
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

    private class ResponseTask implements Runnable {
        private Long taskId;
        private String response;

        public ResponseTask(@NotNull Long taskId, @NotNull String response) {
            this.taskId = taskId;
            this.response = response;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ResponseTask responseTask = (ResponseTask) o;

            return taskId.equals(responseTask.taskId);

        }

        @Override
        public int hashCode() {
            return taskId.hashCode();
        }

        @Override
        public void run() {
            try {
                Task task = taskService.find(taskId);
                task.setResponse(response);

                taskService.runTask(task);
            } catch (ScriptException e) {
                LOG.warn(e);
            }
        }
    }

    private class RequestTask implements Runnable {
        private Long taskId;
        private String request;

        public RequestTask(@NotNull Long taskId, @NotNull String request) {
            this.taskId = taskId;
            this.request = request;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RequestTask that = (RequestTask) o;

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
                addResponseTask(taskId, responseArray.toJSONString());
            } catch (IOException e) {
                LOG.warn(e);
            }
        }
    }
}
