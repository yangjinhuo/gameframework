package com.scriptplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.scriptplatform.mapper")
public class ScriptPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScriptPlatformApplication.class, args);
    }
}
