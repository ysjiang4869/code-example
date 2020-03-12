package org.jys.example.common.sql.query;



import org.jys.example.common.sql.BaseTable;
import org.jys.example.common.sql.dao.QueryParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YueSong Jiang
 * @date 2020/3/12
 * <p>
 * transfer query param from web to dao query param
 * <p>
 * why use this? in web query api, we need to limit some conditions
 * such as pass_time is needed, we can use an object to limit this
 * but in dao, we need common query parameter
 */
public interface QueryParamInterpreter {

    /**
     * @param param
     * @return
     */
    List<QueryParam> transferToQueryParam(StructuredCountParam param);

    /**
     * @param param
     * @return
     */
    List<QueryParam> transferToQueryParam(StructuredParam param);

    /**
     * @param idList
     * @param fieldName
     * @return
     */
    List<QueryParam> transferIdListToQueryParam(String[] idList, String fieldName);


    /**
     * @return
     */
    List<String> getAllTables();

    /**
     * @return
     */
    BaseTable getBaseTable();

    /**
     * @return
     */
    default List<String> getExistTables() {
        return this.getAllTables().stream().filter((t) -> t.contains(this.getBaseTable().getFullTableName()))
                .collect(Collectors.toList());
    }
}
