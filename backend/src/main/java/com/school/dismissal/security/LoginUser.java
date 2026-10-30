package com.school.dismissal.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginUser {
    private Integer id;
    private String username;
    private String role;
}
