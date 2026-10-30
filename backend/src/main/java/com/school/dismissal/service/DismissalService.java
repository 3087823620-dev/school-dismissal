package com.school.dismissal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.dismissal.entity.DismissalRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DismissalService extends IService<DismissalRecord> {
    List<Map<String, Object>> records(String date);
    Map<String, Object> todayStatus();
    Integer publish(Integer classId, Integer studentId, String gate, String remark, Integer operatorId);
    void batchPublish(List<Integer> classIds, String gate, String remark, Integer operatorId);
    Integer addDetention(Integer studentId, String reason, LocalDate date, Integer operatorId);
    List<Map<String, Object>> listDetentions();
}
