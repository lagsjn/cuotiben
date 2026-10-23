package com.cuotiben.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cuotiben.common.BusinessException;
import com.cuotiben.dto.CatalogDtos;
import com.cuotiben.entity.KnowledgePoint;
import com.cuotiben.entity.Mistake;
import com.cuotiben.entity.Subject;
import com.cuotiben.entity.Tag;
import com.cuotiben.mapper.KnowledgePointMapper;
import com.cuotiben.mapper.MistakeMapper;
import com.cuotiben.mapper.MistakeTagMapper;
import com.cuotiben.mapper.SubjectMapper;
import com.cuotiben.mapper.TagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 分类服务：学科、知识点、标签。
 */
@Service
public class CatalogService {

    private final SubjectMapper subjectMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final TagMapper tagMapper;
    private final MistakeMapper mistakeMapper;
    private final MistakeTagMapper mistakeTagMapper;

    public CatalogService(SubjectMapper subjectMapper,
                          KnowledgePointMapper knowledgePointMapper,
                          TagMapper tagMapper,
                          MistakeMapper mistakeMapper,
                          MistakeTagMapper mistakeTagMapper) {
        this.subjectMapper = subjectMapper;
        this.knowledgePointMapper = knowledgePointMapper;
        this.tagMapper = tagMapper;
        this.mistakeMapper = mistakeMapper;
        this.mistakeTagMapper = mistakeTagMapper;
    }

    public List<CatalogDtos.SubjectVO> subjects(Long userId) {
        List<Subject> subjects = subjectMapper.selectList(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getUserId, userId)
                .orderByAsc(Subject::getSortNo)
                .orderByAsc(Subject::getId));
        List<CatalogDtos.SubjectVO> result = new ArrayList<>(subjects.size());
        for (Subject subject : subjects) {
            long count = mistakeMapper.selectCount(new LambdaQueryWrapper<Mistake>()
                    .eq(Mistake::getUserId, userId)
                    .eq(Mistake::getSubjectId, subject.getId()));
            result.add(new CatalogDtos.SubjectVO(subject.getId(), subject.getName(), subject.getColor(), count));
        }
        return result;
    }

    public Long createSubject(Long userId, CatalogDtos.SubjectRequest request) {
        String name = request.name() == null ? "" : request.name().trim();
        if (name.isEmpty()) {
            throw new BusinessException(40020, "请填写学科名称");
        }
        Long exists = subjectMapper.selectCount(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getUserId, userId)
                .eq(Subject::getName, name));
        if (exists != null && exists > 0) {
            throw new BusinessException(40021, "该学科已存在");
        }
        Subject subject = new Subject();
        subject.setUserId(userId);
        subject.setName(name);
        subject.setColor(isNotBlank(request.color()) ? request.color() : "#2c4bd6");
        subject.setSortNo((int) (subjectMapper.selectCount(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getUserId, userId)) + 1));
        subject.setCreatedAt(LocalDateTime.now());
        subjectMapper.insert(subject);
        return subject.getId();
    }

    public void updateSubject(Long userId, Long id, CatalogDtos.SubjectRequest request) {
        Subject subject = requireSubject(userId, id);
        if (isNotBlank(request.name())) {
            subject.setName(request.name().trim());
        }
        if (isNotBlank(request.color())) {
            subject.setColor(request.color());
        }
        subjectMapper.updateById(subject);
    }

    public void deleteSubject(Long userId, Long id) {
        requireSubject(userId, id);
        Long used = mistakeMapper.selectCount(new LambdaQueryWrapper<Mistake>()
                .eq(Mistake::getUserId, userId)
                .eq(Mistake::getSubjectId, id));
        if (used != null && used > 0) {
            throw new BusinessException(40022, "该学科下还有错题，无法删除");
        }
        subjectMapper.deleteById(id);
    }

    public List<CatalogDtos.KnowledgePointVO> knowledgePoints(Long userId, Long subjectId) {
        List<KnowledgePoint> points = knowledgePointMapper.selectList(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getUserId, userId)
                .eq(subjectId != null, KnowledgePoint::getSubjectId, subjectId)
                .orderByAsc(KnowledgePoint::getSortNo)
                .orderByAsc(KnowledgePoint::getId));
        List<CatalogDtos.KnowledgePointVO> result = new ArrayList<>(points.size());
        for (KnowledgePoint point : points) {
            long count = mistakeMapper.selectCount(new LambdaQueryWrapper<Mistake>()
                    .eq(Mistake::getUserId, userId)
                    .eq(Mistake::getKnowledgePointId, point.getId()));
            result.add(new CatalogDtos.KnowledgePointVO(
                    point.getId(), point.getSubjectId(), point.getParentId(), point.getName(), count));
        }
        return result;
    }

    public Long createKnowledgePoint(Long userId, CatalogDtos.KnowledgePointRequest request) {
        if (request.subjectId() == null) {
            throw new BusinessException(40023, "请选择学科");
        }
        requireSubject(userId, request.subjectId());
        String name = request.name() == null ? "" : request.name().trim();
        if (name.isEmpty()) {
            throw new BusinessException(40024, "请填写知识点名称");
        }
        KnowledgePoint point = new KnowledgePoint();
        point.setUserId(userId);
        point.setSubjectId(request.subjectId());
        point.setParentId(request.parentId());
        point.setName(name);
        point.setSortNo((int) (knowledgePointMapper.selectCount(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getUserId, userId)) + 1));
        point.setCreatedAt(LocalDateTime.now());
        knowledgePointMapper.insert(point);
        return point.getId();
    }

    public List<CatalogDtos.TagVO> tags(Long userId) {
        List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getUserId, userId)
                .orderByAsc(Tag::getId));
        List<CatalogDtos.TagVO> result = new ArrayList<>(tags.size());
        for (Tag tag : tags) {
            result.add(new CatalogDtos.TagVO(tag.getId(), tag.getName(), mistakeTagMapper.selectMistakeIds(tag.getId()).size()));
        }
        return result;
    }

    public Long createTag(Long userId, String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) {
            throw new BusinessException(40025, "请填写标签名称");
        }
        Tag exists = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getUserId, userId)
                .eq(Tag::getName, value));
        if (exists != null) {
            return exists.getId();
        }
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(value);
        tag.setCreatedAt(LocalDateTime.now());
        tagMapper.insert(tag);
        return tag.getId();
    }

    @Transactional
    public void deleteTag(Long userId, Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null || !Objects.equals(tag.getUserId(), userId)) {
            throw new BusinessException(40400, "标签不存在或无权访问");
        }
        mistakeTagMapper.deleteByTagId(id);
        tagMapper.deleteById(id);
    }

    /** 分类总览：一次返回学科 + 知识点 + 标签 */
    public CatalogDtos.CatalogVO catalog(Long userId) {
        return new CatalogDtos.CatalogVO(subjects(userId), knowledgePoints(userId, null), tags(userId));
    }

    private Subject requireSubject(Long userId, Long id) {
        Subject subject = subjectMapper.selectById(id);
        if (subject == null || !Objects.equals(subject.getUserId(), userId)) {
            throw new BusinessException(40400, "学科不存在或无权访问");
        }
        return subject;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
