package org.jys.example.common.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * @author YueSong Jiang
 * @date 2019/3/27
 * gave one time A, gave one interval B, return the nearest time to N*B for A
 */
public class TimeIntervalUtils {

    public static long getNearestTime(long originTime, int interval) {
        LocalDate today = Instant.ofEpochSecond(originTime).atZone(ZoneOffset.UTC).toLocalDate();
        long todayStartSeconds = today.atStartOfDay(ZoneOffset.UTC).toInstant().getEpochSecond();
        long sub = originTime - todayStartSeconds;
        long add = sub / interval;
        return todayStartSeconds + add * interval;
    }
}
