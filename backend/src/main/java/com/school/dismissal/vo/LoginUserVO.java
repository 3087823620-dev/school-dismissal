package com.school.dismissal.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginUserVO {
    private Integer id;
    private String username;
    private String name;
    private String role;
    private String phone;
    private String email;
    private String avatar;
}
