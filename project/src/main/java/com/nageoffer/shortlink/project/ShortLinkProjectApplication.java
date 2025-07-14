package com.nageoffer.shortlink.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.nageoffer.shortlink.project.dao.mapper")
@EnableTransactionManagement
@EnableDiscoveryClient
public class ShortLinkProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkProjectApplication.class, args);
    }

}
