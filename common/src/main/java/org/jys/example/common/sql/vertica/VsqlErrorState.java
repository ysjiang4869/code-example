package org.jys.example.common.sql.vertica;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author YueSong Jiang
 * @date 2020/3/10
 */
public class VsqlErrorState {

    private Integer vendorCode;

    private SQLException exception;

    private VsqlErrorStateEnum sqlError;

    private static final Pattern VENDOR_CODE_PATTERN = Pattern.compile("\\[Vertica]\\[VJDBC]\\((\\d+)\\)");

    public VsqlErrorState(SQLException exception) {
        this.exception = exception;
        this.sqlError = VsqlErrorStateEnum.getErrorEnumBySqlState(exception.getSQLState());
        Matcher m = VENDOR_CODE_PATTERN.matcher(exception.getMessage());
        if (m.matches()) {
            this.vendorCode = Integer.valueOf(m.group(1));
        }
    }

    public Integer getVendorCode() {
        return vendorCode;
    }

    public SQLException getException() {
        return exception;
    }

    public VsqlErrorStateEnum getSqlError() {
        return sqlError;
    }
}
