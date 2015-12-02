package com.jzt.spider.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jzt.spider.dao.BaseDao;
import com.jzt.spider.dao.TaskDao;
import com.jzt.spider.entity.Robot;
import com.jzt.spider.entity.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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

    public static final int MAX_QUEUE = 10000;
    public static final int TIME_OUT = 1000 * 1;


    @Autowired
    private TaskDao taskDao;
    @Autowired
    private RobotService robotService;
    @Autowired
    private TaskPoolService taskPoolService;

    @Override
    public BaseDao<Task> getDao() {
        return taskDao;
    }

    public Task findByName(String name) {
        List<Task> list = this.query(0, 1, "name = ?", Arrays.asList(name), null);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    /**
     * 刷新任务, 启动 未开始 或已开始但超时 或已完成但到达刷新时间点 的任务
     */
    @Scheduled(cron = "0 */15 9-21 * * *")  // 用户活跃时段刷新, 防止被发现
    @Transactional
    public void refresh() {
        if (TaskService.REFRESH_LOCK.tryLock()) {
            try {
                // 获取需要刷新的任务列表, 未开始任务/超时任务/刷新任务
                List<Task> taskList = this.query(0, MAX_QUEUE, "status=? or (status=? and startTime<?) or (status=? and refreshTime<?)",
                        Arrays.asList(Task.STATUS_NOT_START, Task.STATUS_START, new Date(System.currentTimeMillis() - TIME_OUT), Task.STATUS_DONE, new Date()), null);
                for (Task task : taskList) {
                    refresh(task);
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
    public void refresh(Task task) {
        // 异常不影响后续任务处理
        try {
            boolean isAdded;    // 已存任务不会重复添加
            if(StringUtils.isEmpty(task.getRequest())){ // 起始任务没有request, 直接执行
                isAdded = taskPoolService.addResponseTask(task.getId(), task.getResponse());
            }else{
                isAdded = taskPoolService.addRequestTask(task.getId(), task.getRequest());
            }

            // 变更任务状态
            if(isAdded){
                task.setStartTime(new Date());
                task.setStatus(Task.STATUS_START);
                this.merge(task);
            }
        } catch (Exception e) {
            LOG.warn(e);
        }
    }

    /**
     * 所有条件准备好, 执行脚本
     * @param task
     * @throws ScriptException
     */
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
}
