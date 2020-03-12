package org.jys.example.common.sql.query;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author YueSong Jiang
 * @date 2020/3/12
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StructuredCountResponse {

    private long total;

    public StructuredCountResponse() {
    }

    public StructuredCountResponse(long total) {
        this.total = total;
    }

    public long getTotal() {
        return this.total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
