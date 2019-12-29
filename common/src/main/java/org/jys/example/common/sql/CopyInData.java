package org.jys.example.common.sql;

/**
 * @author YueSong Jiang
 * @date 2019/12/28
 */
public interface CopyInData {

    /**
     * generate copy in string ,format is data+delimiter+data+delimiter and so on
     * @return the copy in string
     */
    String generateCopyString();

    /**
     * get field data delimiter
     * @return the delimiter
     */
    char getDelimiter();
}
