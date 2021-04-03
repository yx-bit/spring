package com.framework.spring;

public interface BeanPostProcessor {

    default Object postProcessBeforeInitialization(Object bean, String beanName){
        return bean;
    }

    Object postProcessAfterInitialization(Object bean, String beanName);
}
