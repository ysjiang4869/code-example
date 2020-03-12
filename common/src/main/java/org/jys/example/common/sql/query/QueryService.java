package org.jys.example.common.sql.query;

import java.util.List;

/**
 * @author YueSong Jiang
 * @date 2020/3/12
 */
public interface QueryService<T> {


    /**
     * search structured table and get result
     *
     * @param param query param from
     * @return standard result
     */
    StructureDataResponse<T> searchStructuredData(StructuredParam param);

    /**
     * @param param
     * @return
     */
    long getTotal(StructuredCountParam param);

    /**
     * @param recordIds
     * @return
     */
    List<T> searchStructuredById(String[] recordIds);

    /**
     * @param param
     */
    void validateStructuredParam(StructuredParam param);

    /**
     * @param param
     */
    void validateCountParam(StructuredCountParam param);
}
