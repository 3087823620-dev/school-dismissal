package com.school.dismissal.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * 加在 Controller 方法上，切面会自动记录这条操作到 operation_logs 表。
 * 用法：@OperationLog(module = "学生", action = "删除学生", targetId = "#id")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 模块名，如：学生管理 / 班级管理 / 通知公告 */
    String module();

    /** 动作描述，如：新增学生 / 修改班级 / 发布通知 */
    String action();

    /** 操作对象ID，支持 SpEL，如 "#id"、"#req.name"；不填就不记 */
    String targetId() default "";
}
