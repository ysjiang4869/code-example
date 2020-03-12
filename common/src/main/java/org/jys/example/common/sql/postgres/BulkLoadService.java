package org.jys.example.common.sql.postgres;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author jiangys
 */
public interface BulkLoadService<T> {

    /**
     * load bulk data to database
     *
     * @param data            origin data list
     * @param processSupplier object need to process same with copy
     * @return the data save success
     */
    long bulkLoad(List<T> data, Supplier<Object> processSupplier);
}
