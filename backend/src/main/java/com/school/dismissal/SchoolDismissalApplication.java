package com.school.dismissal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.school.dismissal.mapper")
public class SchoolDismissalApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchoolDismissalApplication.class, args);
    }
}
