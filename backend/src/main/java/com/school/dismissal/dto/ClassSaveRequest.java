package com.school.dismissal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClassSaveRequest {
    @NotBlank(message = "班级名称不能为空")
    private String name;
    @NotBlank(message = "年级不能为空")
    private String grade;
    private Integer headTeacherId;
    private String dismissalTime;
    private String schoolGate;
    private String status;
}
