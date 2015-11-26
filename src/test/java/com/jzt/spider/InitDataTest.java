package com.jzt.spider;

import com.jzt.spider.controller.RobotController;
import com.jzt.spider.controller.TaskController;
import com.jzt.spider.entity.Robot;
import com.jzt.spider.service.RobotService;
import com.jzt.spider.service.TaskService;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Charles on 2015/11/23.
 */
public class InitDataTest extends BaseTest {

    @Autowired
    RobotController robotController;
    @Autowired
    TaskController taskController;
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
        if (taskService.find(1L) == null) {
            taskController.startTask("挂号网起始任务",
                    Arrays.asList("http://www.guahao.com/hospital/areahospitals?q=&pi=1&p=%E5%8C%97%E4%BA%AC",
                            "http://www.guahao.com/hospital/areahospitals?q=&pi=19&p=%E6%B9%96%E5%8C%97&ci=229&c=%E6%AD%A6%E6%B1%89"),
                    2L  // 列表页robotId
            );
        }
    }

    private void initRobot() throws IOException {
        if (robotService.find(1L) == null) {
            Robot robot = null;
            // 统一起始入口robot
            robot = new Robot(0,
                    "起始入口，启动后续任务，直接返回response",
                    1,
                    "groovy",
                    FileUtils.readFileToString(new File(Class.class.getResource("/RobotStart.groovy").getPath())),
                    Robot.STATUS_ENABLE);
            robotController.post(robot);
            // 列表页
            robot = new Robot(0,
                    "挂号网医院列表页面",
                    1,
                    "groovy",
                    FileUtils.readFileToString(new File(Class.class.getResource("/RobotList.groovy").getPath())),
                    Robot.STATUS_ENABLE);
            robotController.post(robot);
            // 详情页
            robot = new Robot(0,
                    "挂号网医院详情页面",
                    3,
                    "groovy",
                    FileUtils.readFileToString(new File(Class.class.getResource("/RobotDetail.groovy").getPath())),
                    Robot.STATUS_ENABLE);
            robotController.post(robot);
        }
    }
}
