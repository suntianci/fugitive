package com.stc.fugitive.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    public static String SERVICE_NAME;

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static String getServiceName() {
        if (SERVICE_NAME == null && applicationContext != null) {
            SERVICE_NAME = applicationContext.getEnvironment().getProperty("spring.application.name");
        }
        return SERVICE_NAME;
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String bean, Class<T> clazz) {
        return applicationContext.getBean(bean, clazz);
    }

    public static <T> T getBean(Class<T> clazz, Object... args) {
        return applicationContext.getBean(clazz, args);
    }


    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    public static boolean isSingleton(String name) {
        return applicationContext.isSingleton(name);
    }

    public static Class<? extends Object> getType(String name) {
        return applicationContext.getType(name);
    }

}
