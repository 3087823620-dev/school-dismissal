package com.school.dismissal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("students")
public class Student {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String studentNo;
    private Integer classId;
    private String gender;
    private String parentName;
    private String parentPhone;
    private String status;
    private String faceImageUrl;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
