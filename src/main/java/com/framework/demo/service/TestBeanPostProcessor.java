package com.framework.demo.service;

import com.framework.spring.BeanPostProcessor;
import com.framework.spring.Component;

@Component
public class TestBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前"+beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后"+beanName);
        return bean;
    }

}
