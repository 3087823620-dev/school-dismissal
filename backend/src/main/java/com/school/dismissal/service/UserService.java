package com.school.dismissal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.dismissal.common.Result;
import com.school.dismissal.dto.ChangePasswordRequest;
import com.school.dismissal.dto.LoginRequest;
import com.school.dismissal.dto.UserSaveRequest;
import com.school.dismissal.entity.User;
import com.school.dismissal.vo.LoginUserVO;

import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(LoginRequest req);
    LoginUserVO me(Integer userId);
    void changePassword(ChangePasswordRequest req);
    Integer create(UserSaveRequest req);
    void update(Integer id, UserSaveRequest req);
    Map<String, Object> stats();
}
