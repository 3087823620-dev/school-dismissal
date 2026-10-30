package com.school.dismissal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.dismissal.entity.DismissalRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface DismissalRecordMapper extends BaseMapper<DismissalRecord> {

    /**
     * 查询放学记录（带老师数据权限过滤）
     * @param date      日期，yyyy-MM-dd，null 查全部
     * @param teacherId 老师ID，只看自己当班主任的班；null 表示管理员看全部
     */
    @Select("""
            <script>
            SELECT dr.id, dr.class_id, dr.student_id, dr.dismissal_time, dr.gate,
                   dr.status, dr.remark, dr.created_at,
                   c.name AS class_name,
                   s.name AS student_name,
                   u.name AS operator_name
            FROM dismissal_records dr
            LEFT JOIN classes c ON dr.class_id = c.id
            LEFT JOIN students s ON dr.student_id = s.id
            LEFT JOIN users u ON dr.operator_id = u.id
            <where>
                <if test="date != null and date != ''"> AND DATE(dr.created_at) = #{date} </if>
                <if test="teacherId != null"> AND c.head_teacher_id = #{teacherId} </if>
            </where>
            ORDER BY dr.created_at DESC
            LIMIT 100
            </script>
            """)
    List<Map<String, Object>> listRecords(@Param("date") String date,
                                          @Param("teacherId") Integer teacherId);
}
