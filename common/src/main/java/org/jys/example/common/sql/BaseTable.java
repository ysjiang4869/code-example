package org.jys.example.common.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author YueSong Jiang
 * @date 2020/3/7
 * <p>
 * get table name based on split table rule
 */
public interface BaseTable {

    Logger logger = LoggerFactory.getLogger(BaseTable.class);

    DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * the time field for table split rule
     *
     * @return time filed name
     */
    String getTime();

    String getSchema();

    String getBaseTableName();

    default String getFullTableName() {
        return getSchema() + "_" + getBaseTableName();
    }

    default String getTableFormatWithAlgorithm() {
        return getFullTableName() + "_a%06d";
    }

    default String getTableFormatWithDateAndAlgorithm() {
        return getFullTableName() + "_a%06d_%s";
    }

    default DateTimeFormatter dateFormatter() {
        return DEFAULT_DATE_FORMATTER;
    }

    default String getTableName(long algorithmId) {
        return String.format(getTableFormatWithAlgorithm(), algorithmId);
    }

    default String getTableName(long algorithmId, long time) {
        Instant instant = Instant.ofEpochMilli(time);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDate date = LocalDate.from(localDateTime);
        return getTableName(algorithmId, date);
    }

    default String getTableName(long algorithmId, LocalDate date) {
        return String.format(getTableFormatWithDateAndAlgorithm(), algorithmId, dateFormatter().format(date));
    }

    default LocalDate getDateFromTableName(String tableName) {
        String[] list = tableName.split("_");
        String dateString = list[list.length - 1];
        try {
            return LocalDate.parse(dateString, dateFormatter());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
