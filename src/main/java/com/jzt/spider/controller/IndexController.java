package com.jzt.spider.controller;

import com.jzt.spider.vo.RestfulResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Charles on 2015/11/19.
 */
@Controller
@RequestMapping("/")
public class IndexController {
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public RestfulResult<String> get() {
        return new RestfulResult<>("Hello World");
    }
}
