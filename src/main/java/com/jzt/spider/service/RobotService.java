package com.jzt.spider.service;

import com.jzt.spider.dao.BaseDao;
import com.jzt.spider.dao.RobotDao;
import com.jzt.spider.entity.Robot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Charles on 2015/11/19.
 */
@Service
public class RobotService extends BaseService<Robot> {
    @Autowired
    private RobotDao robotDao;

    @Override
    public BaseDao<Robot> getDao() {
        return robotDao;
    }

}
