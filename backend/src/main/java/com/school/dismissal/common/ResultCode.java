package com.school.dismissal.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USERNAME_OR_PASSWORD_WRONG(10001, "用户名或密码错误"),
    USERNAME_DUPLICATE(10002, "用户名已存在"),
    USER_NOT_FOUND(10003, "用户不存在"),
    OLD_PASSWORD_WRONG(10004, "原密码错误"),

    CLASS_NOT_FOUND(20001, "班级不存在"),
    CLASS_HAS_STUDENT(20002, "该班级还有学生，无法删除"),
    HEAD_TEACHER_NOT_FOUND(20003, "班主任ID不存在"),

    STUDENT_NOT_FOUND(30001, "学生不存在"),
    STUDENT_NO_DUPLICATE(30002, "学号已存在"),

    NOTICE_NOT_FOUND(40001, "通知不存在"),
    CLASS_ID_REQUIRED(50001, "班级ID不能为空"),
    UPLOAD_FAIL(50002, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(50003, "不支持的文件类型"),
    FILE_TOO_LARGE(50004, "文件大小超过限制");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
