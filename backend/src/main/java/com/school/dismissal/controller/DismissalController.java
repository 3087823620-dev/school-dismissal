package com.school.dismissal.controller;

import com.school.dismissal.annotation.OperationLog;
import com.school.dismissal.common.Result;
import com.school.dismissal.security.UserContext;
import com.school.dismissal.service.DismissalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "放学管理")
@RestController
@RequestMapping("/api/dismissal")
public class DismissalController {

    @Autowired private DismissalService dismissalService;

    @Operation(summary = "放学记录列表")
    @GetMapping("/records")
    public Result<List<Map<String, Object>>> records(@RequestParam(required = false) String date) {
        return Result.ok("获取成功", dismissalService.records(date));
    }

    @Operation(summary = "今日放学状态")
    @GetMapping("/today-status")
    public Result<Map<String, Object>> todayStatus() {
        return Result.ok("获取成功", dismissalService.todayStatus());
    }

    @Operation(summary = "发布放学")
    @PostMapping("/publish")
    @OperationLog(module = "放学管理", action = "发布放学")
    public Result<Map<String, Object>> publish(@RequestBody Map<String, Object> body) {
        Integer classId = Integer.valueOf(body.get("class_id").toString());
        Object studentId = body.get("student_id");
        Integer id = dismissalService.publish(
                classId,
                studentId == null ? null : Integer.valueOf(studentId.toString()),
                (String) body.get("gate"),
                (String) body.get("remark"),
                UserContext.currentUserId()
        );
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("time", LocalDateTime.now());
        return Result.ok("放学信息发布成功", data);
    }

    @Operation(summary = "批量发布放学")
    @PostMapping("/batch-publish")
    public Result<Map<String, Object>> batchPublish(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> classIds = ((List<?>) body.get("class_ids")).stream()
                .map(o -> Integer.valueOf(o.toString())).toList();
        dismissalService.batchPublish(classIds,
                (String) body.get("gate"),
                (String) body.get("remark"),
                UserContext.currentUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("time", LocalDateTime.now());
        return Result.ok("成功发布 " + classIds.size() + " 个班级的放学信息", data);
    }

    @Operation(summary = "添加留堂记录")
    @PostMapping("/detention")
    public Result<Map<String, Object>> addDetention(@RequestBody Map<String, Object> body) {
        Integer studentId = Integer.valueOf(body.get("student_id").toString());
        String reason = (String) body.get("reason");
        Object date = body.get("detention_date");
        Integer id = dismissalService.addDetention(studentId, reason,
                date == null ? LocalDate.now() : LocalDate.parse(date.toString()),
                UserContext.currentUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.ok("留堂信息已记录", data);
    }

    @Operation(summary = "留堂记录列表")
    @GetMapping("/detentions")
    public Result<List<Map<String, Object>>> listDetentions() {
        return Result.ok("获取成功", dismissalService.listDetentions());
    }
}
