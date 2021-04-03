package com.framework.demo;

import com.framework.demo.service.UserService;
import com.framework.spring.ComponentScan;
import com.framework.spring.SpringApplication;

@ComponentScan("com.framework.demo.service")
public class Test {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(Test.class);
        UserService bean = (UserService)springApplication.getBean("userService");
        bean.test();
        System.out.println(bean);
    }
}
