package com.school.dismissal.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Integer id;
    private String username;
    private String name;
    private String role;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
}
