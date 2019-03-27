package org.jys.common.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * @author YueSong Jiang
 * @date 2019/3/27
 * @description <p> </p>
 */
public class TimeIntervalUtils {

    public static long getClosedTime(long originTime, int interval) {
        LocalDate today = Instant.ofEpochSecond(originTime).atZone(ZoneOffset.UTC).toLocalDate();
        long todayStartSeconds = today.atStartOfDay(ZoneOffset.UTC).toInstant().getEpochSecond();
        long sub = originTime - todayStartSeconds;
        long add = sub / interval;
        return todayStartSeconds + add * interval;
    }
}
