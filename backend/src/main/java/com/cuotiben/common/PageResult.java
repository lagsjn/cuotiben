package com.cuotiben.common;

import java.util.List;

/**
 * 分页结果，与前端 fetchMistakes 的约定一致。
 */
public record PageResult<T>(List<T> list, long total, int page, int size) {

    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        return new PageResult<>(list, total, page, size);
    }
}
