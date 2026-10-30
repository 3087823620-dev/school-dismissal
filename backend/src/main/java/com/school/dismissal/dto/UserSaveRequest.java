package com.school.dismissal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserSaveRequest {
    private String username;
    private String password;
    @NotBlank(message = "姓名不能为空")
    private String name;
    private String role;
    private String phone;
    private String email;
}
