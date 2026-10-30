package com.school.dismissal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("detention_records")
public class DetentionRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer studentId;
    private String reason;
    private LocalDate detentionDate;
    private Integer operatorId;
    private Integer notified;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
