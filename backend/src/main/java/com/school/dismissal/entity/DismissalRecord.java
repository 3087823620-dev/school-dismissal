package com.school.dismissal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dismissal_records")
public class DismissalRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer classId;
    private Integer studentId;
    private LocalDateTime dismissalTime;
    private String gate;
    private String status;
    private Integer operatorId;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
