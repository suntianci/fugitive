package com.stc.fugitive.controller.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author suntianci on 2022/6/4
 */
@RestController
@RequestMapping("/log")
public class Logcontroller {

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 测试日志输出
     * SLF4J 日志级别从小到大trace>debug>info>warn>error
     */
    @GetMapping("/")
    public void logTest() {
        //日志级别 由低到高
        logger.trace("trace 级别日志");
        logger.debug("debug 级别日志");
        logger.info("info 级别日志");
        logger.warn("warn 级别日志");
        logger.error("error 级别日志");
    }

    @GetMapping("/path/{group:[a-zA-Z0-9_]+}/{userid}")
    public String path(@PathVariable("group") String group, @PathVariable("userid") Integer userid) {
        return group + ":" + userid;
    }

}
