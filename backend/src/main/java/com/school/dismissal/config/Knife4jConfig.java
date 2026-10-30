package com.school.dismissal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("可视化放学系统 API")
                .version("1.0")
                .description("Spring Boot 3 + MyBatis-Plus 版后端接口文档"));
    }
}
