package com.cuotiben;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 错题本系统后端启动类。
 */
@SpringBootApplication
@MapperScan("com.cuotiben.mapper")
public class CuoTiBenApplication {

    public static void main(String[] args) {
        SpringApplication.run(CuoTiBenApplication.class, args);
    }
}
