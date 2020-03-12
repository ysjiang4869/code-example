package org.jys.example.common.sql.dao;

import java.util.List;

/**
 * @author YueSong Jiang
 * @date 2020/2/3
 */
public interface DataAccessService<T> {
    List<T> queryForList(QueryParam param);

    T queryForObject(QueryParam param);

    long queryCount(QueryParam param);

    List<T> queryForList(String sql);

    T queryForObject(String sql);

    long queryCount(String sql);

    <E> List<E> queryForList(String sql, Class<E> elementType);

    <E> E queryForObject(String sql, Class<E> elementType);
}
