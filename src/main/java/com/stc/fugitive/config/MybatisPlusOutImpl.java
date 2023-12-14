package com.stc.fugitive.config;

import org.apache.ibatis.logging.Log;

/**
 * @author suntianci on 2022/6/4
 * @Description MybatisPlusOutImpl，直接使用控制台输出日志
 */

public class MybatisPlusOutImpl implements Log {

    public MybatisPlusOutImpl(String clazz) {
        System.out.println("MybatisPlusOutImpl:clazz:" + clazz);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void error(String s, Throwable e) {
        System.err.println(s);
        e.printStackTrace(System.err);
    }

    @Override
    public void error(String s) {
        System.err.println("MybatisPlusOutImpl:error:" + s);
    }

    @Override
    public void debug(String s) {
        System.out.println("MybatisPlusOutImpl:debug:" + s);
    }

    @Override
    public void trace(String s) {
        System.out.println("MybatisPlusOutImpl:trace:" + s);
    }

    @Override
    public void warn(String s) {
        System.out.println("MybatisPlusOutImpl:warn:" + s);
    }
}

