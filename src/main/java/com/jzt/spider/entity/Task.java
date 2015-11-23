package com.jzt.spider.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Charles on 2015/11/19.
 */
@Entity
@Table(name = "tb_task")
public class Task {
    public static final int STATUS_NOT_START = 0;
    public static final int STATUS_REQUEST = 1;
    public static final int STATUS_RESPONSE = 2;
    public static final int STATUS_DONE = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;
    private String name;
    @Column(name = "robot_id")
    private Long robotId;
    private String context;
    /**
     * request 格式
     * {
     *      url: "",    // 必填
     *      method: ""  // 选填，默认为get
     * }
     */
    @Column(length = Integer.MAX_VALUE)
    private String request;
    /**
     * response 格式
     * {
     *      header: {}, // 响应头
     *      body: ""   // 响应体
     * }
     */
    @Column(length = Integer.MAX_VALUE)
    private String response;
    /**
     * output 格式，如果为中间过程，需要进一步下载解析：
     * {
     *      refreshTime: 1234567890, // 下次任务运行时间点, null为不运行
     *      // 分裂出来的子任务列表
     *      subTask: [
     *          {
     *              // 子任务属性，返回后新建子任务插入数据库
     *          }
     *      ]
     * }
     * 如果为最终结果，为任意约定好的json串
     */
    @Column(length = Integer.MAX_VALUE)
    private String output;
    @Column(name = "start_time")
    private Date startTime;
    @Column(name = "end_time")
    private Date endTime;
    @Column(name = "refresh_time")
    private Date refreshTime;
    private Integer status;

    public Task() {
    }

    public Task(String name, Long robotId, String context, String request, String response, String output, Integer status) {
        this();
        this.name = name;
        this.robotId = robotId;
        this.context = context;
        this.request = request;
        this.response = response;
        this.output = output;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRobotId() {
        return robotId;
    }

    public void setRobotId(Long robotId) {
        this.robotId = robotId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(Date refreshTime) {
        this.refreshTime = refreshTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
