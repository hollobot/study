package com.hello.springevent;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.hello.springevent.mapper")
@SpringBootApplication
@Slf4j
public class SpringEventApplication {

    /**
     * Spring Boot 应用启动入口，启动后打印 Swagger 和 Knife4j 访问地址。
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringEventApplication.class, args);
        log.info("http://127.0.0.1:8080/swagger-ui/index.html");
        log.info("http://127.0.0.1:8080/doc.html");
    }

}
