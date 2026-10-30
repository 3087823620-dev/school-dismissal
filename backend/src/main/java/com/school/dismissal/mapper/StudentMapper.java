package com.school.dismissal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.dismissal.entity.Student;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface StudentMapper extends BaseMapper<Student> {

    @Select("""
            <script>
            SELECT s.id, s.name, s.student_no, s.gender, s.parent_name, s.parent_phone, s.status, s.class_id,
                   s.face_image_url,
                   c.name AS class_name, c.grade
            FROM students s
            LEFT JOIN classes c ON s.class_id = c.id
            WHERE 1=1
            <if test="classId != null"> AND s.class_id = #{classId} </if>
            <if test="teacherId != null"> AND c.head_teacher_id = #{teacherId} </if>
            <if test="keyword != null and keyword != ''">
                AND (s.name LIKE CONCAT('%',#{keyword},'%')
                  OR s.student_no LIKE CONCAT('%',#{keyword},'%')
                  OR s.parent_name LIKE CONCAT('%',#{keyword},'%'))
            </if>
            ORDER BY c.grade, c.name, s.student_no
            </script>
            """)
    List<Map<String, Object>> listByCondition(@Param("classId") Integer classId,
                                               @Param("keyword") String keyword,
                                               @Param("teacherId") Integer teacherId);

    @Select("""
            SELECT s.id, s.name, s.student_no, s.gender, s.parent_name, s.parent_phone, s.status, s.class_id,
                   s.face_image_url,
                   c.name AS class_name, c.grade
            FROM students s
            LEFT JOIN classes c ON s.class_id = c.id
            WHERE s.id = #{id}
            """)
    Map<String, Object> getDetailById(Integer id);

    @Select("SELECT id, name, student_no, gender, parent_name, parent_phone, status FROM students WHERE class_id = #{classId} ORDER BY student_no")
    List<Map<String, Object>> listByClassId(Integer classId);
}
