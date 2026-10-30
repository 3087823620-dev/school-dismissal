package com.school.dismissal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.dismissal.entity.Classes;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ClassesMapper extends BaseMapper<Classes> {

    @Select("""
            <script>
            SELECT c.id, c.name, c.grade, c.dismissal_time, c.school_gate, c.status,
                   c.head_teacher_id,
                   u.name AS head_teacher_name,
                   (SELECT COUNT(*) FROM students s WHERE s.class_id = c.id) AS student_count
            FROM classes c
            LEFT JOIN users u ON c.head_teacher_id = u.id
            WHERE 1=1
            <if test="teacherId != null"> AND c.head_teacher_id = #{teacherId} </if>
            ORDER BY c.grade, c.name
            </script>
            """)
    List<Map<String, Object>> listWithHeadTeacher(@Param("teacherId") Integer teacherId);

    @Select("SELECT DISTINCT grade FROM classes ORDER BY grade")
    List<String> listGrades();

    @Select("""
            SELECT c.id, c.name, c.grade, c.dismissal_time, c.school_gate, c.status,
                   u.name AS head_teacher_name, u.id AS head_teacher_id
            FROM classes c
            LEFT JOIN users u ON c.head_teacher_id = u.id
            WHERE c.id = #{id}
            """)
    Map<String, Object> getDetailById(Integer id);

    @Select("""
            <script>
            SELECT c.id, c.name, c.grade, c.dismissal_time, c.school_gate,
                   c.head_teacher_id,
                   u.name AS head_teacher_name,
                   (SELECT COUNT(*) FROM students s WHERE s.class_id = c.id) AS student_count,
                   (SELECT COUNT(*) FROM dismissal_records dr
                    WHERE dr.class_id = c.id AND DATE(dr.created_at) = CURDATE()) AS dismissed
            FROM classes c
            LEFT JOIN users u ON c.head_teacher_id = u.id
            WHERE c.status = 'active'
            <if test="teacherId != null"> AND c.head_teacher_id = #{teacherId} </if>
            ORDER BY c.grade, c.name
            </script>
            """)
    List<Map<String, Object>> listTodayStatus(@Param("teacherId") Integer teacherId);
}
