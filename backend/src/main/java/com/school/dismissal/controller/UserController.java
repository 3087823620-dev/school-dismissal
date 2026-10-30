package com.school.dismissal.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.dismissal.common.BusinessException;
import com.school.dismissal.common.Result;
import com.school.dismissal.common.ResultCode;
import com.school.dismissal.dto.UserSaveRequest;
import com.school.dismissal.entity.User;
import com.school.dismissal.security.RequireRole;
import com.school.dismissal.service.UserService;
import com.school.dismissal.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserService userService;

    @Operation(summary = "用户列表")
    @GetMapping
    public Result<List<UserVO>> list() {
        List<User> users = userService.list(new LambdaQueryWrapper<User>()
                .select(User::getId, User::getUsername, User::getName, User::getRole,
                        User::getPhone, User::getEmail, User::getCreatedAt)
                .orderByAsc(User::getId));
        List<UserVO> vos = users.stream().map(u -> {
            UserVO v = new UserVO();
            BeanUtils.copyProperties(u, v);
            return v;
        }).toList();
        return Result.ok("获取成功", vos);
    }

    @Operation(summary = "统计数据")
    @GetMapping("/stats/summary")
    public Result<Map<String, Object>> stats() {
        return Result.ok("获取成功", userService.stats());
    }

    @Operation(summary = "获取单个用户")
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Integer id) {
        User u = userService.getOne(new LambdaQueryWrapper<User>()
                .select(User::getId, User::getUsername, User::getName, User::getRole,
                        User::getPhone, User::getEmail, User::getCreatedAt)
                .eq(User::getId, id));
        if (u == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        UserVO v = new UserVO();
        BeanUtils.copyProperties(u, v);
        return Result.ok("获取成功", v);
    }

    @Operation(summary = "新增用户")
    @RequireRole("admin")
    @PostMapping
    public Result<Map<String, Object>> create(@Valid @RequestBody UserSaveRequest req) {
        Integer id = userService.create(req);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.ok("创建成功", data);
    }

    @Operation(summary = "更新用户")
    @RequireRole("admin")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @Valid @RequestBody UserSaveRequest req) {
        userService.update(id, req);
        return Result.ok("更新成功");
    }

    @Operation(summary = "删除用户")
    @RequireRole("admin")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        userService.removeById(id);
        return Result.ok("删除成功");
    }
}
