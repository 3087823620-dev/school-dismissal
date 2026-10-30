package com.school.dismissal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.dismissal.common.BusinessException;
import com.school.dismissal.common.ResultCode;
import com.school.dismissal.dto.ChangePasswordRequest;
import com.school.dismissal.dto.LoginRequest;
import com.school.dismissal.dto.UserSaveRequest;
import com.school.dismissal.entity.User;
import com.school.dismissal.mapper.UserMapper;
import com.school.dismissal.security.JwtUtil;
import com.school.dismissal.service.UserService;
import com.school.dismissal.vo.LoginUserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired private BCryptPasswordEncoder encoder;
    @Autowired private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginRequest req) {
        User u = this.lambdaQuery().eq(User::getUsername, req.getUsername()).one();
        if (u == null || !encoder.matches(req.getPassword(), u.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_WRONG);
        }
        String token = jwtUtil.generateToken(u.getId(), u.getUsername(), u.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", new LoginUserVO(u.getId(), u.getUsername(), u.getName(),
                u.getRole(), u.getPhone(), u.getEmail(), u.getAvatar()));
        return data;
    }

    @Override
    public LoginUserVO me(Integer userId) {
        User u = this.getById(userId);
        if (u == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        return new LoginUserVO(u.getId(), u.getUsername(), u.getName(),
                u.getRole(), u.getPhone(), u.getEmail(), u.getAvatar());
    }

    @Override
    public void changePassword(ChangePasswordRequest req) {
        User u = this.lambdaQuery().eq(User::getUsername, req.getUsername()).one();
        if (u == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        if (!encoder.matches(req.getOldPassword(), u.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_WRONG);
        }
        u.setPassword(encoder.encode(req.getNewPassword()));
        this.updateById(u);
    }

    @Override
    public Integer create(UserSaveRequest req) {
        User u = new User();
        BeanUtils.copyProperties(req, u);
        u.setPassword(encoder.encode(req.getPassword()));
        if (u.getRole() == null || u.getRole().isBlank()) u.setRole("teacher");
        try {
            this.save(u);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.USERNAME_DUPLICATE);
        }
        return u.getId();
    }

    @Override
    public void update(Integer id, UserSaveRequest req) {
        User u = new User();
        BeanUtils.copyProperties(req, u);
        u.setId(id);
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPassword(encoder.encode(req.getPassword()));
        } else {
            u.setPassword(null);
        }
        try {
            this.updateById(u);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.USERNAME_DUPLICATE);
        }
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", ((Number) baseMapper.countAll().get("count")).longValue());
        data.put("classCount", ((Number) baseMapper.countClasses().get("count")).longValue());
        data.put("studentCount", ((Number) baseMapper.countStudents().get("count")).longValue());
        data.put("noticeCount", ((Number) baseMapper.countNotices().get("count")).longValue());
        data.put("dismissalToday", ((Number) baseMapper.countDismissalToday().get("count")).longValue());
        return data;
    }
}
