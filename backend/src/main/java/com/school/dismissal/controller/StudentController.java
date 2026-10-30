package com.school.dismissal.controller;

import com.school.dismissal.annotation.OperationLog;
import com.school.dismissal.common.BusinessException;
import com.school.dismissal.common.Result;
import com.school.dismissal.common.ResultCode;
import com.school.dismissal.dto.StudentSaveRequest;
import com.school.dismissal.entity.Student;
import com.school.dismissal.service.StorageService;
import com.school.dismissal.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Tag(name = "学生管理")
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired private StudentService studentService;
    @Autowired private StorageService storageService;

    @Operation(summary = "学生列表")
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(name = "class_id", required = false) Integer classId,
                                                  @RequestParam(required = false) String keyword) {
        return Result.ok("获取成功", studentService.list(classId, keyword));
    }

    @Operation(summary = "学生详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Integer id) {
        return Result.ok("获取成功", studentService.getDetail(id));
    }

    @Operation(summary = "新增学生")
    @PostMapping
    @OperationLog(module = "学生管理", action = "新增学生")
    public Result<Map<String, Object>> create(@Valid @RequestBody StudentSaveRequest req) {
        Integer id = studentService.create(req);
        return Result.ok("创建成功", Map.of("id", id));
    }

    @Operation(summary = "更新学生")
    @PutMapping("/{id}")
    @OperationLog(module = "学生管理", action = "修改学生", targetId = "#id")
    public Result<Void> update(@PathVariable Integer id, @Valid @RequestBody StudentSaveRequest req) {
        studentService.update(id, req);
        return Result.ok("更新成功");
    }

    @Operation(summary = "删除学生")
    @DeleteMapping("/{id}")
    @OperationLog(module = "学生管理", action = "删除学生", targetId = "#id")
    public Result<Void> delete(@PathVariable Integer id) {
        studentService.removeById(id);
        return Result.ok("删除成功");
    }

    @Operation(summary = "上传学生人脸照片")
    @PostMapping("/{id}/face")
    public Result<Map<String, Object>> uploadFace(@PathVariable Integer id,
                                                  @RequestParam("file") MultipartFile file) {
        Student student = studentService.getById(id);
        if (student == null) throw new BusinessException(ResultCode.STUDENT_NOT_FOUND);

        String oldUrl = student.getFaceImageUrl();
        String url = storageService.uploadImage(file, "student-face");

        Student update = new Student();
        update.setId(id);
        update.setFaceImageUrl(url);
        studentService.updateById(update);

        if (oldUrl != null && !oldUrl.equals(url)) {
            String toDelete = oldUrl;
            CompletableFuture.runAsync(() -> storageService.deleteByUrl(toDelete));
        }

        return Result.ok("上传成功", Map.of("url", url));
    }
}
