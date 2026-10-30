package com.school.dismissal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.dismissal.common.BusinessException;
import com.school.dismissal.common.ResultCode;
import com.school.dismissal.dto.StudentSaveRequest;
import com.school.dismissal.entity.Student;
import com.school.dismissal.mapper.StudentMapper;
import com.school.dismissal.security.UserContext;
import com.school.dismissal.service.StudentService;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Override
    public List<Map<String, Object>> list(Integer classId, String keyword) {
        // 老师只能看自己当班主任的班的学生；管理员看全部
        Integer teacherId = UserContext.isAdmin() ? null : UserContext.currentUserId();
        return baseMapper.listByCondition(classId, keyword, teacherId);
    }

    @Override
    public Map<String, Object> getDetail(Integer id) {
        Map<String, Object> s = baseMapper.getDetailById(id);
        if (s == null) throw new BusinessException(ResultCode.STUDENT_NOT_FOUND);
        return s;
    }

    @Override
    public Integer create(StudentSaveRequest req) {
        Student s = new Student();
        BeanUtils.copyProperties(req, s);
        if (req.getStudentNo() != null && req.getStudentNo().isBlank()) s.setStudentNo(null);
        try {
            this.save(s);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.STUDENT_NO_DUPLICATE);
        }
        return s.getId();
    }

    @Override
    public void update(Integer id, StudentSaveRequest req) {
        Student s = new Student();
        BeanUtils.copyProperties(req, s);
        s.setId(id);
        if (req.getStudentNo() != null && req.getStudentNo().isBlank()) s.setStudentNo(null);
        if (s.getStatus() == null || s.getStatus().isBlank()) s.setStatus("active");
        try {
            this.updateById(s);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.STUDENT_NO_DUPLICATE);
        }
    }
}
