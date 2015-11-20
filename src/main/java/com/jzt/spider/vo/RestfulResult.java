package com.jzt.spider.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Charles on 2015/11/19.
 */
public class RestfulResult<T> {
    private boolean success = true;
    private int errorCode = 0;
    private String message = "";
    private List<T> data = new ArrayList<>();

    public RestfulResult() {
    }

    public RestfulResult(T t) {
        this();
        this.addData(t);
    }

    public RestfulResult(List<T> data) {
        this();
        this.addData(data);
    }

    public void addData(T t) {
        this.data.add(t);
    }

    public void addData(List<T> data) {
        this.data.addAll(data);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<T> getData() {
        return data;
    }
}
