package com.school.dismissal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.dismissal.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT COUNT(*) AS count FROM users")
    Map<String, Object> countAll();

    @Select("SELECT COUNT(*) AS count FROM classes")
    Map<String, Object> countClasses();

    @Select("SELECT COUNT(*) AS count FROM students")
    Map<String, Object> countStudents();

    @Select("SELECT COUNT(*) AS count FROM notices")
    Map<String, Object> countNotices();

    @Select("SELECT COUNT(*) AS count FROM dismissal_records WHERE DATE(created_at) = CURDATE()")
    Map<String, Object> countDismissalToday();
}
