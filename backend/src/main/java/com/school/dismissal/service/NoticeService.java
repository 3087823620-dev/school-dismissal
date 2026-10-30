package com.school.dismissal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.dismissal.dto.NoticeSaveRequest;
import com.school.dismissal.entity.Notice;

import java.util.List;
import java.util.Map;

public interface NoticeService extends IService<Notice> {
    List<Map<String, Object>> list(String type, Integer published);
    Map<String, Object> getDetail(Integer id);
    Integer create(NoticeSaveRequest req, Integer operatorId);
    void update(Integer id, NoticeSaveRequest req);
}
