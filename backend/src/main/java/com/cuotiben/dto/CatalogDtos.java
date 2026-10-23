package com.cuotiben.dto;

import java.util.List;

/**
 * 学科/知识点/标签相关返回结构。
 */
public final class CatalogDtos {

    private CatalogDtos() {
    }

    /** 学科（附带该学科下的错题数量，用于列表和筛选栏） */
    public record SubjectVO(Long id, String name, String color, long count) {
    }

    public record SubjectRequest(String name, String color) {
    }

    public record KnowledgePointVO(Long id, Long subjectId, Long parentId, String name, long count) {
    }

    public record KnowledgePointRequest(Long subjectId, Long parentId, String name) {
    }

    public record TagVO(Long id, String name, long count) {
    }

    public record TagRequest(String name) {
    }

    /** 一次性返回三个维度的分类数据，减少前端请求次数 */
    public record CatalogVO(List<SubjectVO> subjects, List<KnowledgePointVO> knowledgePoints, List<TagVO> tags) {
    }
}
