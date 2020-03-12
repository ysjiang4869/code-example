package org.jys.example.common.sql.dao;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author YueSong Jiang
 * @date 2020/2/3
 */
public abstract class AbstractJdbcAccessService<T> implements DataAccessService<T> {
    protected JdbcTemplate jdbcTemplate;
    protected static final String AND = " and ";
    private static final Logger logger = LoggerFactory.getLogger(AbstractJdbcAccessService.class);

    public AbstractJdbcAccessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<T> queryForList(QueryParam param) {
        String sql = getSqlFromParam(param);
        return queryForList(sql);
    }

    @Override
    public T queryForObject(QueryParam param) {
        String sql = getSqlFromParam(param);
        return queryForObject(sql);
    }

    @Override
    public long queryCount(QueryParam param) {
        String sql = getSqlFromParam(param);
        return queryCount(sql);
    }

    @Override
    public List<T> queryForList(String sql) {
        logger.trace("queryForList sql : {}", sql);
        return jdbcTemplate.query(sql, rowMapper());
    }

    @Override
    public T queryForObject(String sql) {
        logger.trace("queryForObject sql : {}", sql);
        return jdbcTemplate.queryForObject(sql, rowMapper());
    }

    @Override
    public long queryCount(String sql) {
        logger.trace("queryCount sql : {}", sql);
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return Optional.ofNullable(count).orElse(0L);
    }

    @Override
    public <E> List<E> queryForList(String sql, Class<E> elementType) {
        logger.trace("queryForList sql : {}", sql);
        return jdbcTemplate.queryForList(sql, elementType);
    }

    @Override
    public <E> E queryForObject(String sql, Class<E> elementType) {
        logger.trace("queryForObject sql : {}", sql);
        return jdbcTemplate.queryForObject(sql, elementType);
    }

    protected String getSqlFromParam(QueryParam param) {
        StringBuilder sqlBuilder = new StringBuilder();
        String selectFields = "*";
        if (param.getFields() != null && !param.getFields().isEmpty()) {
            selectFields = String.join(",", param.getFields());
        }

        if (param.isQueryCount()) {
            selectFields = "count(*)";
        }

        sqlBuilder.append("select ").append(selectFields).append(" from ").append(param.getTable());
        String querySql = constructConditionSql(param.getConditions());
        if (StringUtils.isNotEmpty(querySql)) {
            sqlBuilder.append(" where 1=1 ");
        }

        sqlBuilder.append(querySql);
        String sql;
        if (!param.isQueryCount()) {
            sqlBuilder.append(constructOrderSql(param.getOrderInfo()));
            sql = constructPageSql(param);
            sqlBuilder.append(sql);
        }

        sql = sqlBuilder.toString();
        logger.debug("query sql is[{}]", sql);
        return sql;
    }

    /**
     * construct condition sql (after where clause)
     *
     * @param conditionsList conditions
     * @return sql
     */
    protected String constructConditionSql(List<Condition> conditionsList) {
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isEmpty(conditionsList)) {
            return sb.toString();
        }

        for (Condition condition : conditionsList) {
            if (null == condition.getKey()) {
                throw new RuntimeException("condition key is null");
            }

            if (null == condition.getOperator()) {
                throw new RuntimeException("condition operator is null");
            }

            String columnName = getQueryColumn(condition.getKey());

            if (null == columnName) {
                logger.error("column for condition key {} not exist", condition.getKey());
                throw new RuntimeException("column for query not exist");
            }
            String likeConditionValue;
            switch (condition.getOperator()) {
                case ConditionOperator.EQ:
                    processValueCondition(sb, condition, columnName, "=");
                    break;
                case ConditionOperator.NE:
                    processValueCondition(sb, condition, columnName, "!=");
                    break;
                case ConditionOperator.LT:
                    processValueCondition(sb, condition, columnName, "<");
                    break;
                case ConditionOperator.LTE:
                    processValueCondition(sb, condition, columnName, "<=");
                    break;
                case ConditionOperator.GT:
                    processValueCondition(sb, condition, columnName, ">");
                    break;
                case ConditionOperator.GTE:
                    processValueCondition(sb, condition, columnName, ">=");
                    break;
                case ConditionOperator.IS:
                    processIsNullCondition(sb, condition, columnName);
                    break;
                case ConditionOperator.LIKE:
                    if (Objects.nonNull(condition.getValue())) {
                        likeConditionValue = condition.getValue().toString().replace("?", "_").replace("*", "%");
                        condition.setValue(likeConditionValue);
                    }
                    processLikeCondition(sb, condition, columnName);
                    break;
                case ConditionOperator.LLIKE:
                    if (Objects.nonNull(condition.getValue())) {
                        likeConditionValue = condition.getValue().toString().replace("?", "_").replace("*", "%");
                        condition.setValue(likeConditionValue);
                    }
                    processLLikeCondition(sb, condition, columnName);
                    break;
                case ConditionOperator.RLIKE:
                    if (Objects.nonNull(condition.getValue())) {
                        likeConditionValue = condition.getValue().toString().replace("?", "_").replace("*", "%");
                        condition.setValue(likeConditionValue);
                    }
                    processRLikeCondition(sb, condition, columnName);
                    break;
                case ConditionOperator.IN:
                    processInCondition(sb, condition, columnName);
                    break;
                default:
                    logger.warn("unknown condition key [{}]", condition.getKey());
                    break;
            }
        }

        return sb.toString();
    }

    protected String constructOrderSql(List<OrderInfo> orderInfoList) {
        if (CollectionUtils.isEmpty(orderInfoList)) {
            return "";
        }

        StringBuilder sb = null;
        for (OrderInfo order : orderInfoList) {
            String orderKey = null;
            if (order.getKey() != null) {
                orderKey = getQueryColumn(order.getKey());
            }
            boolean validOrderInfo = StringUtils.isNotEmpty(orderKey)
                    && StringUtils.isNotEmpty(order.getOrder()) && StringUtils.equalsIgnoreCase(order.getOrder(), "asc")
                    || StringUtils.equalsIgnoreCase(order.getOrder(), "desc");

            if (!validOrderInfo) {
                throw new RuntimeException("order info is not valid");
            }
            if (sb == null) {
                sb = new StringBuilder(" order by ");
            } else {
                sb.append(",");
            }
            logger.trace("order key is [{}] and order value is [{}]", orderKey, order.getOrder());
            sb.append(orderKey).append(" ").append(order.getOrder());
        }

        String sql = sb == null ? "" : sb.toString();
        logger.debug("order sql is:[{}]", sql);
        return sql;
    }

    protected void processInCondition(StringBuilder sb, Condition condition, String columnName) {
        Object value = condition.getValue();
        if (!(value instanceof Object[]) && !(value instanceof List)) {
            throw new RuntimeException("condition value is null");
        }

        Class<?> type = null;

        String[] valueArray;

        if (value instanceof Object[]) {
            if (((Object[]) value).length == 0) {
                throw new RuntimeException("condition value is empty");
            }

            if (value instanceof String[]) {
                type = String.class;
                valueArray = (String[]) value;
            } else {
                Object[] array = (Object[]) value;
                valueArray = new String[array.length];

                for (int i = 0; i < array.length; ++i) {
                    valueArray[i] = array[i].toString();
                }
            }
        } else {
            List array = (List) condition.getValue();
            if (array.isEmpty()) {
                throw new RuntimeException("condition value is empty");
            }

            type = array.get(0).getClass();
            boolean allSameClass = true;
            for (Object item : array) {
                allSameClass = item.getClass().equals(type);
                if (!allSameClass) {
                    break;
                }
            }

            if (!allSameClass) {
                throw new RuntimeException("list condition value are not same type");
            }

            List<String> stringArray = new ArrayList<>(array.size());
            for (Object item : array) {
                stringArray.add(item.toString());
            }
            valueArray = stringArray.toArray(new String[0]);
        }

        String in;
        if (type == String.class) {
            in = String.join("','", valueArray);
            sb.append(AND).append(columnName).append(" in ('").append(in).append("')");
        } else {
            in = String.join(",", valueArray);
            sb.append(AND).append(columnName).append(" in(").append(in).append(")");
        }
    }

    protected void processLikeCondition(StringBuilder sb, Condition propCondition, String column) {
        sb.append(AND).append(column).append(" like '%").append(propCondition.getValue()).append("%'");
    }

    protected void processLLikeCondition(StringBuilder sb, Condition propCondition, String column) {
        sb.append(AND).append(column).append(" like '%").append(propCondition.getValue()).append("'");
    }

    protected void processRLikeCondition(StringBuilder sb, Condition propCondition, String column) {
        sb.append(AND).append(column).append(" like '").append(propCondition.getValue()).append("%'");
    }

    /**
     * process value basic operation
     *
     * @param sb          string builder
     * @param condition   condition
     * @param columnName  table column name
     * @param sqlOperator sql operator, include "=", "<", "<=", ">" ">="
     */
    protected void processValueCondition(StringBuilder sb, Condition condition, String columnName, String sqlOperator) {
        Class<?> type = condition.getValue().getClass();
        if (!type.equals(String.class) && !type.equals(Date.class)) {
            sb.append(AND).append(columnName).append(sqlOperator).append(condition.getValue());
        } else {
            sb.append(AND).append(columnName).append(sqlOperator).append("'").append(condition.getValue()).append("'");
        }

    }

    private static void processIsNullCondition(StringBuilder sb, Condition propCondition, String column) {
        if ("0".equals(propCondition.getValue())) {
            sb.append(AND).append(column).append(" is null ");
        } else {
            sb.append(AND).append(column).append(" is not null ");
        }

    }

    protected String constructPageSql(QueryParam param) {
        StringBuilder sb = new StringBuilder();
        if (param.getPageSize() != null) {
            sb.append(" limit ").append(param.getPageSize());
            if (param.getPageNo() != null) {
                sb.append(" offset ").append((param.getPageNo() - 1) * param.getPageSize());
            }
        }

        String sql = sb.toString();
        logger.debug("page sql is:[{}]", sql);
        return sql;
    }

    /**
     * sometimes, one query field equal to multi database fields, and just judge is all null or has non-null
     *
     * @param columns the equal fields list
     * @param isNull  true: all is null; false: hasi non null
     * @return sql string, such as (a is null and b is null) or (a is not null or b is not null)
     */
    protected String getSubSqlOfMultiColumnsNullJudge(String[] columns, boolean isNull) {
        String judgeString;
        String relation;
        if (isNull) {
            judgeString = " is null ";
            relation = AND;
        } else {
            judgeString = " is not null ";
            relation = " or ";
        }

        StringBuilder builder = new StringBuilder(" (");

        for (int i = 0; i < columns.length; ++i) {
            builder.append(columns[0]).append(judgeString);
            if (i < columns.length - 1) {
                builder.append(relation);
            }
        }

        builder.append(" )");
        return builder.toString();
    }

    public abstract RowMapper<T> rowMapper();

    public String getQueryColumn(String queryKey) {
        return queryKey.replace("_", "");
    }

    protected Double getMayNullDouble(ResultSet rs, String item) throws SQLException {
        double v = rs.getDouble(item);
        return rs.wasNull() ? null : v;
    }

    protected Long getMayNullLong(ResultSet rs, String item) throws SQLException {
        long v = rs.getLong(item);
        return rs.wasNull() ? null : v;
    }

    protected Integer getMayNullInt(ResultSet rs, String item) throws SQLException {
        int v = rs.getInt(item);
        return rs.wasNull() ? null : v;
    }
}

