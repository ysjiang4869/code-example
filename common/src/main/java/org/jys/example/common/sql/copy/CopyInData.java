package org.jys.example.common.sql.copy;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author YueSong Jiang
 * @date 2019/12/28
 * interface for copy in data object
 */
public interface CopyInData {

    /**
     * generate copy in string ,format is [data+line delimiter+data+line delimiter] and so on
     * @return the copy in string
     */
    String generateCopyString();

    /**
     * get field data delimiter
     * @return the delimiter
     */
    default char getFieldDelimiter(){return '\033';}

    /**
     * get record line delimiter
     * @return line delimiter
     */
    default char getLineDelimiter(){return '\036';}

    /**
     * get null string means null in copy data
     * @return null string
     */
    default String getNullCharacter(){return "\\N";}

    /**
     * for null value or empty string, return null character
     * else return the value
     * @param value value, ATTENTION: basic value like int/long must use boxed object
     * @return replaced string
     */
    default String transferNullOrEmptyData(Object value){
        if (value instanceof String && StringUtils.isEmpty((String)value)) {
            return this.getNullCharacter();
        } else {
            return Objects.isNull(value) ? this.getNullCharacter() : value.toString();
        }
    }

    long getRecordTime();

    long getAlgorithmId();
}
