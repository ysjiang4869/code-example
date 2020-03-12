package org.jys.example.common.sql.vertica;

/**
 * @author jiangys
 */

public enum VsqlErrorStateEnum {

    /**
     * column not exist
     */
    COLUMN_UNDEFINED_QUERY("22023", "COLUMN_NOT_EXIST"),

    /**
     * convert string to int failed
     */
    INVALID_TEXT_REPRESENTATION("22P02", "ERROR_CONVERT_STRING_TO_COLUMN_TYPE"),

    /**
     * violate unique constraint
     */
    ERROR_UNIQUE_VIOLATION("23505", "DUPLICATE UNIQUE/PRIMARY KEY DETECTED"),

    /**
     * number out of range
     */
    NUMERIC_VALUE_OUT_OF_RANGE("22003", "NUMERIC_VALUE_OUT_OF_RANGE"),

    /**
     * copy format error
     */
    BAD_COPY_FILE_FORMAT("22V04", "COPY_FILE_FORMAT_ERROR"),

    /**
     * table not exist
     */
    TABLE_NOT_FOUND("42V01", "TABLE_NOT_FOUND_ERROR"),

    /**
     * user canceled query or system, or timeout
     */
    QUERY_CANCELED("57014", "QUERY_CANCELED_UNEXPECTED"),

    /**
     * syntax error
     */
    SQL_SYNTAX_ERROR("42601", "SQL_SYNTAX_ERROR"),

    /**
     * column not exist
     */
    COLUMN_UNDEFINED("42703", "COLUMN_NOT_EXIST"),

    /**
     * connection database failed
     */
    CONNECTION_FAILURE("08006", "CONNECTION_FAILURE"),

    /**
     * server error
     */
    CONNECTION_NOT_EXIST("08003", "CONNECTION_CLOSED_UNEXPECTEDLY"),

    /**
     * such as too many ros container
     */
    INSUFFICIENT_RESOURCE("53000", "INSUFFICIENT_RESOURCE"),

    /**
     * unknown
     */
    OTHER_RESULT("-99", "UNKNOWN_ERROR");

    private String sqlState;
    private String message;

    VsqlErrorStateEnum(String sqlState, String message) {
        this.sqlState = sqlState;
        this.message = message;
    }

    public String getSqlState() {
        return sqlState;
    }

    public String getMessage() {
        return message;
    }

    public static VsqlErrorStateEnum getErrorEnumBySqlState(String state) {
        for (VsqlErrorStateEnum errorStateEnum : VsqlErrorStateEnum.values()) {
            if (errorStateEnum.getSqlState().equals(state)) {
                return errorStateEnum;
            }
        }
        return OTHER_RESULT;
    }
}
