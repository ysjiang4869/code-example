package org.jys.example.common.sql.query;



import org.jys.example.common.sql.dao.DataAccessService;

import java.util.List;

/**
 * @author YueSong Jiang
 * @date 2020/3/12
 */
public class AbstractQueryService<T> implements QueryService<T> {

    protected DataAccessService<T> dao;
    protected QueryParamInterpreter paramInterpreter;

    public AbstractQueryService(DataAccessService<T> dao, QueryParamInterpreter paramInterpreter) {
        this.dao = dao;
        this.paramInterpreter = paramInterpreter;
    }


    @Override
    public StructureDataResponse<T> searchStructuredData(StructuredParam param) {
        return null;
    }

    @Override
    public long getTotal(StructuredCountParam param) {
        return 0;
    }

    @Override
    public List<T> searchStructuredById(String[] recordIds) {
        return null;
    }

    @Override
    public void validateStructuredParam(StructuredParam param) {

    }

    @Override
    public void validateCountParam(StructuredCountParam param) {

    }
}
