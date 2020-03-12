package org.jys.example.common.sql.postgres;

import org.postgresql.util.PSQLException;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author YueSong Jiang
 * @date 2020/3/10
 */
public class PsqlErrorState {

    private Integer vendorCode;

    private SQLException exception;

    private PsqlErrorStateEnum sqlError;

    private static final Pattern VENDOR_CODE_PATTERN = Pattern.compile("\\[Vertica]\\[VJDBC]\\((\\d+)\\)");

    public PsqlErrorState(PSQLException exception) {
        this.exception = exception;
        this.sqlError = PsqlErrorStateEnum.getErrorEnumBySqlState(exception.getSQLState());
        Matcher m = VENDOR_CODE_PATTERN.matcher(exception.getMessage());
        vendorCode = exception.getErrorCode();
    }

    public Integer getVendorCode() {
        return vendorCode;
    }

    public SQLException getException() {
        return exception;
    }

    public PsqlErrorStateEnum getSqlError() {
        return sqlError;
    }
}
