package com.jzt.spider.controller;

import com.jzt.spider.entity.Robot;
import com.jzt.spider.entity.Task;
import com.jzt.spider.service.BaseService;
import com.jzt.spider.service.RobotService;
import com.jzt.spider.service.TaskService;
import com.jzt.spider.vo.RestfulResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by Charles on 2015/11/19.
 */
@Controller
@RequestMapping("/robot")
public class RobotController extends BaseAdminController<Robot> {
    @Autowired
    private RobotService robotService;

    @Override
    public BaseService<Robot> getBaseAdminService() {
        return robotService;
    }
}
