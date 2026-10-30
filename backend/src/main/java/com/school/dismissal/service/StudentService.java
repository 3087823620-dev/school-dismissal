package com.school.dismissal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.dismissal.dto.StudentSaveRequest;
import com.school.dismissal.entity.Student;

import java.util.List;
import java.util.Map;

public interface StudentService extends IService<Student> {
    List<Map<String, Object>> list(Integer classId, String keyword);
    Map<String, Object> getDetail(Integer id);
    Integer create(StudentSaveRequest req);
    void update(Integer id, StudentSaveRequest req);
}
