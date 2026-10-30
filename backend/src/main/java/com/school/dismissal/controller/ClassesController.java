package com.school.dismissal.controller;

import com.school.dismissal.annotation.OperationLog;
import com.school.dismissal.common.Result;
import com.school.dismissal.dto.ClassSaveRequest;
import com.school.dismissal.service.ClassesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "班级管理")
@RestController
@RequestMapping("/api/classes")
public class ClassesController {

    @Autowired private ClassesService classesService;

    @Operation(summary = "班级列表")
    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok("获取成功", classesService.listWithHeadTeacher());
    }

    @Operation(summary = "年级列表")
    @GetMapping("/grades/list")
    public Result<List<String>> grades() {
        return Result.ok("获取成功", classesService.listGrades());
    }

    @Operation(summary = "班级详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Integer id) {
        return Result.ok("获取成功", classesService.getDetail(id));
    }

    @Operation(summary = "新增班级")
    @PostMapping
    @OperationLog(module = "班级管理", action = "新增班级")
    public Result<Map<String, Object>> create(@Valid @RequestBody ClassSaveRequest req) {
        Integer id = classesService.create(req);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.ok("创建成功", data);
    }

    @Operation(summary = "更新班级")
    @PutMapping("/{id}")
    @OperationLog(module = "班级管理", action = "修改班级", targetId = "#id")
    public Result<Void> update(@PathVariable Integer id, @Valid @RequestBody ClassSaveRequest req) {
        classesService.update(id, req);
        return Result.ok("更新成功");
    }

    @Operation(summary = "删除班级")
    @DeleteMapping("/{id}")
    @OperationLog(module = "班级管理", action = "删除班级", targetId = "#id")
    public Result<Void> delete(@PathVariable Integer id) {
        classesService.delete(id);
        return Result.ok("删除成功");
    }
}
