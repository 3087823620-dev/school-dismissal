package com.school.dismissal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.dismissal.entity.Notice;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface NoticeMapper extends BaseMapper<Notice> {

    @Select("""
            <script>
            SELECT n.id, n.title, n.content, n.type, n.target_audience,
                   n.is_published, n.read_count, n.created_at,
                   u.name AS author_name
            FROM notices n
            LEFT JOIN users u ON n.author_id = u.id
            WHERE 1=1
            <if test="type != null and type != ''"> AND n.type = #{type} </if>
            <if test="published != null"> AND n.is_published = #{published} </if>
            ORDER BY n.created_at DESC
            LIMIT 50
            </script>
            """)
    List<Map<String, Object>> listByCondition(@Param("type") String type,
                                              @Param("published") Integer published);

    @Select("""
            SELECT n.id, n.title, n.content, n.type, n.target_audience,
                   n.is_published, n.read_count, n.created_at,
                   u.name AS author_name
            FROM notices n
            LEFT JOIN users u ON n.author_id = u.id
            WHERE n.id = #{id}
            """)
    Map<String, Object> getDetailById(Integer id);

    @Update("UPDATE notices SET read_count = read_count + 1 WHERE id = #{id}")
    int incrReadCount(Integer id);
}
