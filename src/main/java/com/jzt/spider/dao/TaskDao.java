package com.jzt.spider.dao;

import com.jzt.spider.entity.Task;
import org.springframework.stereotype.Repository;

/**
 * Created by Charles on 2015/11/19.
 */
@Repository
public class TaskDao extends BaseDao<Task> {
    public TaskDao() {
        super(Task.class);
    }
}
