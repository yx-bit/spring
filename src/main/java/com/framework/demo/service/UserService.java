package com.framework.demo.service;

import com.framework.spring.*;

@Component("userService")
/*@Lazy
@Scope("prototype")*/
public class UserService implements BeanNameAware,InitializingBean {

    @Autowired
    private User user;
    private String beanName;
    private String userName;
    public void test() {
        System.out.println(user);
        System.out.println(beanName);
        System.out.println(userName);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        this.userName = "xxx";
    }


}
