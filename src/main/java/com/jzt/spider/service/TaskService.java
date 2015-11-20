package com.jzt.spider.service;

import com.jzt.spider.dao.BaseDao;
import com.jzt.spider.dao.TaskDao;
import com.jzt.spider.entity.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    @Override
    public BaseDao<Task> getDao() {
        return taskDao;
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void refresh() {
        if (TaskService.REFRESH_LOCK.tryLock()) {
            try {
                LOG.info("refresh");


            } finally {
                TaskService.REFRESH_LOCK.unlock();
            }
        } else {
            LOG.info("skip");
        }
    }
}
