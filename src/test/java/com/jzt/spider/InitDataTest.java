package com.jzt.spider;

import com.alibaba.fastjson.JSONObject;
import com.jzt.spider.entity.Robot;
import com.jzt.spider.entity.Task;
import com.jzt.spider.service.RobotService;
import com.jzt.spider.service.TaskService;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Charles on 2015/11/23.
 */
public class InitDataTest extends BaseTest {

    @Autowired
    RobotService robotService;
    @Autowired
    TaskService taskService;

    @Test
    public void initData() throws IOException {
        initRobot();
        initTask();

    }

    private void initTask() {
        // 初始化 挂号网 入口任务
        Task task = new Task();
        task.setName("挂号网起始任务");
        task.setRobotId(1L);
        task.setContext("");
        task.setRequest("");

        JSONObject response = new JSONObject();
        List<Task> subTask = new ArrayList<>();
        subTask.add(new Task(
                "http://www.guahao.com/hospital/areahospitals?q=&pi=1&p=%E5%8C%97%E4%BA%AC",
                2L, // 处理列表页面 robot
                "",
                "[{\"url\" : \"http://www.guahao.com/hospital/areahospitals?q=&pi=1&p=%E5%8C%97%E4%BA%AC\"}]",
                "",
                "",
                Task.STATUS_NOT_START
        ));

        response.put("subTask", subTask);

        task.setResponse(response.toJSONString());
        task.setOutput("");
        task.setRefreshTime(new Date(System.currentTimeMillis()));
        task.setStatus(Task.STATUS_DONE);

        taskService.persist(task);
    }

    private void initRobot() throws IOException {
        Robot robot = null;
        // 统一起始入口robot
        robot = new Robot(0,
                "起始入口，启动后续任务，直接返回response",
                1,
                "groovy",
                FileUtils.readFileToString(new File(Class.class.getResource("/RobotStart.groovy").getPath())),
                Robot.STATUS_ENABLE);
        robotService.persist(robot);
        // 列表页
        robot = new Robot(0,
                "挂号网医院列表页面",
                3,
                "groovy",
                FileUtils.readFileToString(new File(Class.class.getResource("/RobotList.groovy").getPath())),
                Robot.STATUS_ENABLE);
        robotService.persist(robot);
        // 详情页
        robot = new Robot(0,
                "挂号网医院详情页面",
                5,
                "groovy",
                FileUtils.readFileToString(new File(Class.class.getResource("/RobotDetail.groovy").getPath())),
                Robot.STATUS_ENABLE);
        robotService.persist(robot);
    }
}
