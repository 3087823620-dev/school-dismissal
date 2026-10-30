package com.school.dismissal.controller;

import com.school.dismissal.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "ok");
        data.put("message", "可视化放学系统服务运行中");
        data.put("version", "1.0.0");
        data.put("timestamp", LocalDateTime.now().toString());
        return new Result<>(200, "ok", data);
    }
}
