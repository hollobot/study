package com.hello.scene;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
@MapperScan("com.hello.scene.mapper") //强制扫描mapper包
public class SceneApplication {

    public static void main(String[] args) {
        SpringApplication.run(SceneApplication.class, args);
        log.info("http://127.0.0.1:8080/swagger-ui/index.html");
        log.info("http://127.0.0.1:8080/doc.html");
    }

}
