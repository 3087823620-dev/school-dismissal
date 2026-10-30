package com.school.dismissal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.dismissal.common.BusinessException;
import com.school.dismissal.common.ResultCode;
import com.school.dismissal.config.DismissalWebSocketHandler;
import com.school.dismissal.entity.DetentionRecord;
import com.school.dismissal.entity.DismissalRecord;
import com.school.dismissal.mapper.ClassesMapper;
import com.school.dismissal.mapper.DetentionRecordMapper;
import com.school.dismissal.mapper.DismissalRecordMapper;
import com.school.dismissal.mapper.StudentMapper;
import com.school.dismissal.security.UserContext;
import com.school.dismissal.service.DismissalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DismissalServiceImpl extends ServiceImpl<DismissalRecordMapper, DismissalRecord> implements DismissalService {

    private static final Logger log = LoggerFactory.getLogger(DismissalServiceImpl.class);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired private ClassesMapper classesMapper;
    @Autowired private DetentionRecordMapper detentionMapper;
    @Autowired private StudentMapper studentMapper;
    @Autowired private DismissalWebSocketHandler wsHandler;
    @Autowired private ObjectMapper objectMapper;

    /**
     * 当前老师能看的班级过滤 ID
     * - 管理员返回 null（看全部）
     * - 老师返回自己的 userId（SQL 里 WHERE head_teacher_id = ?）
     */
    private Integer currentScopeTeacherId() {
        return UserContext.isAdmin() ? null : UserContext.currentUserId();
    }

    @Override
    public List<Map<String, Object>> records(String date) {
        return baseMapper.listRecords(date, currentScopeTeacherId());
    }

    @Override
    public Map<String, Object> todayStatus() {
        List<Map<String, Object>> classes = classesMapper.listTodayStatus(currentScopeTeacherId());
        long totalStudents = classes.stream().mapToLong(c -> ((Number) c.get("student_count")).longValue()).sum();
        long dismissed = classes.stream().filter(c -> ((Number) c.get("dismissed")).longValue() > 0).count();
        long pending = classes.stream().filter(c -> ((Number) c.get("dismissed")).longValue() == 0).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClasses", classes.size());
        stats.put("totalStudents", totalStudents);
        stats.put("dismissedClasses", dismissed);
        stats.put("pendingClasses", pending);

        Map<String, Object> data = new HashMap<>();
        data.put("classes", classes);
        data.put("stats", stats);
        return data;
    }

    @Override
    public Integer publish(Integer classId, Integer studentId, String gate, String remark, Integer operatorId) {
        if (classId == null) throw new BusinessException(ResultCode.CLASS_ID_REQUIRED);
        DismissalRecord r = new DismissalRecord();
        r.setClassId(classId);
        r.setStudentId(studentId);
        r.setDismissalTime(LocalDateTime.now());
        r.setGate(gate == null ? "正门" : gate);
        r.setStatus("completed");
        r.setOperatorId(operatorId);
        r.setRemark(remark == null ? "" : remark);
        this.save(r);

        String studentName = resolveStudentName(classId, studentId);
        pushFacePass(studentName, r.getGate(), r.getDismissalTime());
        return r.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchPublish(List<Integer> classIds, String gate, String remark, Integer operatorId) {
        if (classIds == null || classIds.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请选择要放学的班级");
        }
        LocalDateTime now = LocalDateTime.now();
        String g = gate == null ? "正门" : gate;
        String rmk = remark == null ? "" : remark;
        for (Integer cid : classIds) {
            DismissalRecord r = new DismissalRecord();
            r.setClassId(cid);
            r.setDismissalTime(now);
            r.setGate(g);
            r.setStatus("completed");
            r.setOperatorId(operatorId);
            r.setRemark(rmk);
            this.save(r);
            pushFacePass(resolveStudentName(cid, null), g, now);
        }
    }

    private String resolveStudentName(Integer classId, Integer studentId) {
        if (studentId != null) {
            Map<String, Object> s = studentMapper.getDetailById(studentId);
            if (s != null && s.get("name") != null) {
                return s.get("name").toString();
            }
            return "未知学生";
        }
        Map<String, Object> c = classesMapper.getDetailById(classId);
        String className = (c != null && c.get("name") != null) ? c.get("name").toString() : "未知班级";
        return className + " 全体学生";
    }

    private void pushFacePass(String studentName, String gate, LocalDateTime time) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "face_pass");
            msg.put("studentName", studentName);
            msg.put("gate", gate);
            msg.put("time", time.format(TIME_FMT));
            wsHandler.broadcast(objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            log.warn("WebSocket 推送消息序列化失败: {}", e.getMessage());
        }
    }

    @Override
    public Integer addDetention(Integer studentId, String reason, LocalDate date, Integer operatorId) {
        if (studentId == null || reason == null || reason.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "学生ID和原因不能为空");
        }
        DetentionRecord d = new DetentionRecord();
        d.setStudentId(studentId);
        d.setReason(reason);
        d.setDetentionDate(date == null ? LocalDate.now() : date);
        d.setOperatorId(operatorId);
        detentionMapper.insert(d);
        return d.getId();
    }

    @Override
    public List<Map<String, Object>> listDetentions() {
        return detentionMapper.listAll();
    }
}
