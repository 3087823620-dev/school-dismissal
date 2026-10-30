package com.school.dismissal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.dismissal.dto.ClassSaveRequest;
import com.school.dismissal.entity.Classes;

import java.util.List;
import java.util.Map;

public interface ClassesService extends IService<Classes> {
    List<Map<String, Object>> listWithHeadTeacher();
    List<String> listGrades();
    Map<String, Object> getDetail(Integer id);
    Integer create(ClassSaveRequest req);
    void update(Integer id, ClassSaveRequest req);
    void delete(Integer id);
}
