package com.hello.spring.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Spring事务是否会失效的判断标准：某个加了@Transactional注解的方法被调用时，要判断到底是不是直接被代理对象调用的，如果是则事务会生效，如果不是则失效。
@Component
public class UserService {

    @Transactional
    public void test(){
        // 执行 sql 业务
        System.out.println("外部调用事务不会失效");
        test2();
    }

    @Transactional
    public void test2(){
        // 执行 sql 业务
        System.out.println("事务失效，调用的对象不是代理对象，而是this普通对象");
    }
}
