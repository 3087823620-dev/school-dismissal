package com.school.dismissal.controller;

import com.school.dismissal.annotation.OperationLog;
import com.school.dismissal.common.Result;
import com.school.dismissal.dto.NoticeSaveRequest;
import com.school.dismissal.security.UserContext;
import com.school.dismissal.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "通知公告")
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    @Autowired private NoticeService noticeService;

    @Operation(summary = "通知列表")
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String type,
                                                  @RequestParam(required = false) String published) {
        Integer pub = null;
        if (published != null && !published.isEmpty()) {
            pub = "true".equalsIgnoreCase(published) ? 1 : 0;
        }
        return Result.ok("获取成功", noticeService.list(type, pub));
    }

    @Operation(summary = "通知详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Integer id) {
        return Result.ok("获取成功", noticeService.getDetail(id));
    }

    @Operation(summary = "发布通知")
    @PostMapping
    @OperationLog(module = "通知公告", action = "发布通知")
    public Result<Map<String, Object>> create(@Valid @RequestBody NoticeSaveRequest req) {
        Integer id = noticeService.create(req, UserContext.currentUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.ok("发布成功", data);
    }

    @Operation(summary = "更新通知")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @Valid @RequestBody NoticeSaveRequest req) {
        noticeService.update(id, req);
        return Result.ok("更新成功");
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    @OperationLog(module = "通知公告", action = "删除通知", targetId = "#id")
    public Result<Void> delete(@PathVariable Integer id) {
        noticeService.removeById(id);
        return Result.ok("删除成功");
    }
}
