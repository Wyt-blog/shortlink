package com.nageoffer.shortlink.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.nageoffer.shortlink.project.dao.mapper")
@EnableTransactionManagement
public class ShortLinkProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkProjectApplication.class, args);
    }

}
