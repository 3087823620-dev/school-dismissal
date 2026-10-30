package com.school.dismissal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.dismissal.common.BusinessException;
import com.school.dismissal.common.ResultCode;
import com.school.dismissal.dto.NoticeSaveRequest;
import com.school.dismissal.entity.Notice;
import com.school.dismissal.mapper.NoticeMapper;
import com.school.dismissal.service.NoticeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public List<Map<String, Object>> list(String type, Integer published) {
        return baseMapper.listByCondition(type, published);
    }

    @Override
    public Map<String, Object> getDetail(Integer id) {
        baseMapper.incrReadCount(id);
        Map<String, Object> n = baseMapper.getDetailById(id);
        if (n == null) throw new BusinessException(ResultCode.NOTICE_NOT_FOUND);
        return n;
    }

    @Override
    public Integer create(NoticeSaveRequest req, Integer operatorId) {
        Notice n = new Notice();
        BeanUtils.copyProperties(req, n);
        if (n.getType() == null) n.setType("general");
        if (n.getTargetAudience() == null) n.setTargetAudience("all");
        n.setAuthorId(operatorId);
        n.setIsPublished(req.getIsPublished() == null || req.getIsPublished() ? 1 : 0);
        this.save(n);
        return n.getId();
    }

    @Override
    public void update(Integer id, NoticeSaveRequest req) {
        Notice n = new Notice();
        BeanUtils.copyProperties(req, n);
        n.setId(id);
        n.setIsPublished(req.getIsPublished() != null && req.getIsPublished() ? 1 : 0);
        this.updateById(n);
    }
}
