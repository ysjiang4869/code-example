package org.jys.common.utils;

import org.jooq.impl.DSL;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YueSong Jiang
 * @date 2017/12/5
 * @description <p> </p>
 */
public class SQLUtils {

    /**
     * get properties map,key is field name and value is object field value
     *
     * @param object the object need to be resolved
     * @return object <fieldName.value>  map
     * @throws IllegalAccessException can't access field value
     */
    public static Map<String, Object> resolveObject(Object object) throws IllegalAccessException {
        Map<String, Object> ret = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            String key = field.getName();
            field.setAccessible(true);
            Object value = field.get(object);
            if (value instanceof Instant) {
                value = getDateStr((Instant) value);
            }
            ret.put(key, value);
        }
        return ret;
    }

    /**
     * get properties map,key is field name and value is object field value
     *
     * @param object the object need to be resolved
     * @param mapper the fieldName contains in mapper need using mapper value replace, and null value mains don't resolve this field
     * @return object <fieldName.value>  map
     * @throws IllegalAccessException can't access field value
     */
    public static Map<String, Object> resolveObject(Object object, Map<String, String> mapper) throws IllegalAccessException {
        Map<String, Object> ret = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            String key = field.getName();
            field.setAccessible(true);
            if (mapper.containsKey(key)) {
                key = mapper.get(key);
            }
            if (key != null) {
                Object value = field.get(object);
                if (value instanceof Instant) {
                    value = getDateStr((Instant) value);
                }
                ret.put(key, value);
            }
        }
        return ret;
    }


    /**
     * assemble object using result set from database query
     *
     * @param rs     Database query ResultSet
     * @param object The object need to assemble
     * @param mapper specified fieldName<->ColumnName mapper,key is field name and value is column name；
     *               if value is null, do nothing
     * @throws IllegalAccessException can access field
     * @throws SQLException           rs exception
     */
    public static void assembleObject(ResultSet rs, Object object, Map<String, String> mapper) throws IllegalAccessException, SQLException {
        if (object == null) {
            throw new NullPointerException("param object can not be null");
        }
        if (mapper == null) {
            mapper = Collections.emptyMap();
        }
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            field.setAccessible(true);
            if (mapper.containsKey(fieldName)) {
                fieldName = mapper.get(fieldName);
            }
            Class type = field.getType();
            Object value;
            if (fieldName != null) {
                switch (type.getSimpleName()) {
                    case "short":
                        value = rs.getShort(fieldName);
                        break;
                    case "Instant":
                        value = toInstant(rs.getTimestamp(fieldName));
                        break;
                    case "double":
                        value = rs.getDouble(fieldName);
                        break;
                    default:
                        value = rs.getObject(fieldName);
                        break;
                }
                field.set(object, value);
            }
        }
    }

    public static String getDateStr(Instant date) {
        return (date == null) ? null : date.toString();
    }

    public static Instant toInstant(Timestamp ts) {
        return ts != null ? ts.toInstant() : null;
    }

    /**
     * check if the result set has the column name
     *
     * @param rs         database query result set
     * @param columnName the column name need to check
     * @return true: has column with name ,reverse
     * @throws SQLException sql exception
     */
    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columns = metaData.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(metaData.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }

    public static Map<org.jooq.Field<Object>, org.jooq.Field<Object>> getFields(Map<String, Object> params) {
        Map<org.jooq.Field<Object>, org.jooq.Field<Object>> sets = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            if (val == null) {
                continue;
            }
            sets.put(DSL.field(key), DSL.inline(val));
        }
        return sets;
    }
}
