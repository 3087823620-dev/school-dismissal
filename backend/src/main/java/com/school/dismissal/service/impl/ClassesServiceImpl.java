package com.school.dismissal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.dismissal.common.BusinessException;
import com.school.dismissal.common.ResultCode;
import com.school.dismissal.dto.ClassSaveRequest;
import com.school.dismissal.entity.Classes;
import com.school.dismissal.entity.Student;
import com.school.dismissal.mapper.ClassesMapper;
import com.school.dismissal.mapper.StudentMapper;
import com.school.dismissal.security.UserContext;
import com.school.dismissal.service.ClassesService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ClassesServiceImpl extends ServiceImpl<ClassesMapper, Classes> implements ClassesService {

    @Autowired private StudentMapper studentMapper;

    @Override
    public List<Map<String, Object>> listWithHeadTeacher() {
        // 老师只能看自己当班主任的班；管理员看全部
        Integer teacherId = UserContext.isAdmin() ? null : UserContext.currentUserId();
        return baseMapper.listWithHeadTeacher(teacherId);
    }

    @Override
    public List<String> listGrades() {
        return baseMapper.listGrades();
    }

    @Override
    public Map<String, Object> getDetail(Integer id) {
        Map<String, Object> c = baseMapper.getDetailById(id);
        if (c == null) throw new BusinessException(ResultCode.CLASS_NOT_FOUND);
        c.put("students", studentMapper.listByClassId(id));
        return c;
    }

    @Override
    public Integer create(ClassSaveRequest req) {
        Classes c = new Classes();
        BeanUtils.copyProperties(req, c);
        if (c.getSchoolGate() == null || c.getSchoolGate().isBlank()) c.setSchoolGate("正门");
        try {
            this.save(c);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ResultCode.HEAD_TEACHER_NOT_FOUND);
        }
        return c.getId();
    }

    @Override
    public void update(Integer id, ClassSaveRequest req) {
        Classes c = new Classes();
        BeanUtils.copyProperties(req, c);
        c.setId(id);
        if (c.getSchoolGate() == null || c.getSchoolGate().isBlank()) c.setSchoolGate("正门");
        if (c.getStatus() == null || c.getStatus().isBlank()) c.setStatus("active");
        try {
            this.updateById(c);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ResultCode.HEAD_TEACHER_NOT_FOUND);
        }
    }

    @Override
    public void delete(Integer id) {
        Long cnt = studentMapper.selectCount(new LambdaQueryWrapper<Student>().eq(Student::getClassId, id));
        if (cnt != null && cnt > 0) throw new BusinessException(ResultCode.CLASS_HAS_STUDENT);
        this.removeById(id);
    }
}
