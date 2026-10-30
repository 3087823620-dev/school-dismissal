package com.school.dismissal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.dismissal.entity.DetentionRecord;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface DetentionRecordMapper extends BaseMapper<DetentionRecord> {

    @Select("""
            SELECT dr.id, dr.student_id, dr.reason, dr.detention_date, dr.notified, dr.created_at,
                   s.name AS student_name, s.student_no,
                   c.name AS class_name,
                   u.name AS operator_name
            FROM detention_records dr
            LEFT JOIN students s ON dr.student_id = s.id
            LEFT JOIN classes c ON s.class_id = c.id
            LEFT JOIN users u ON dr.operator_id = u.id
            ORDER BY dr.created_at DESC
            LIMIT 100
            """)
    List<Map<String, Object>> listAll();
}
