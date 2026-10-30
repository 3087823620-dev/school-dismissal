package com.school.dismissal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("classes")
public class Classes {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String grade;
    private Integer headTeacherId;
    private String dismissalTime;
    private String schoolGate;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
