package com.school.dismissal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentSaveRequest {
    @NotBlank(message = "学生姓名不能为空")
    private String name;
    private String studentNo;
    @NotNull(message = "班级不能为空")
    private Integer classId;
    private String gender;
    private String parentName;
    private String parentPhone;
    private String status;
}
