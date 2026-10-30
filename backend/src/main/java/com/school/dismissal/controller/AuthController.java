package com.school.dismissal.controller;

import com.school.dismissal.common.BusinessException;
import com.school.dismissal.common.Result;
import com.school.dismissal.common.ResultCode;
import com.school.dismissal.dto.ChangePasswordRequest;
import com.school.dismissal.dto.LoginRequest;
import com.school.dismissal.entity.User;
import com.school.dismissal.security.UserContext;
import com.school.dismissal.service.StorageService;
import com.school.dismissal.service.UserService;
import com.school.dismissal.vo.LoginUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private StorageService storageService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok("登录成功", userService.login(req));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok("退出成功");
    }

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/me")
    public Result<LoginUserVO> me() {
        return Result.ok("获取成功", userService.me(UserContext.currentUserId()));
    }

    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(req);
        return Result.ok("密码修改成功");
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public Result<LoginUserVO> updateProfile(@RequestBody Map<String, String> body) {
        Integer uid = UserContext.currentUserId();
        User u = userService.getById(uid);
        if (u == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        if (body.get("name") != null) u.setName(body.get("name"));
        if (body.get("phone") != null) u.setPhone(body.get("phone"));
        if (body.get("email") != null) u.setEmail(body.get("email"));
        userService.updateById(u);
        return Result.ok("更新成功", userService.me(uid));
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Integer uid = UserContext.currentUserId();
        User user = userService.getById(uid);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);

        String oldAvatar = user.getAvatar();
        String url = storageService.uploadImage(file, "avatar");

        User update = new User();
        update.setId(uid);
        update.setAvatar(url);
        userService.updateById(update);

        // 异步清理旧头像，不阻塞响应
        if (oldAvatar != null && !oldAvatar.equals(url)) {
            String toDelete = oldAvatar;
            CompletableFuture.runAsync(() -> storageService.deleteByUrl(toDelete));
        }

        return Result.ok("上传成功", Map.of("url", url));
    }
}
