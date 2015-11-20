package com.jzt.spider.dao;

import com.jzt.spider.entity.Robot;
import org.springframework.stereotype.Repository;

/**
 * Created by Charles on 2015/11/19.
 */
@Repository
public class RobotDao extends BaseDao<Robot> {
    public RobotDao() {
        super(Robot.class);
    }
}
