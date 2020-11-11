package org.jys.example.common.spring;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jys.example.common.plugin.mvc.EnumConvertMethod;
import org.jys.example.common.plugin.swagger.SwaggerDisplayEnum;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangys
 */
@Getter
@AllArgsConstructor
@Slf4j
@SwaggerDisplayEnum
public enum PreSaleStateEnum {
    /**
     *
     */
    NOT_OPEN(1,"待开启"),
    OPEN(2,"已开启"),
    SUSPEND(3,"已暂停"),
    CLOSED(9,"已结束")
    ;

    @JsonValue
    private final int index;

    private final String name;

    private static final Map<Integer, PreSaleStateEnum> MAPPINGS;

    static {
        Map<Integer, PreSaleStateEnum> temp = new HashMap<>();
        for (PreSaleStateEnum mode : values()) {
            temp.put(mode.index, mode);
        }
        MAPPINGS = Collections.unmodifiableMap(temp);
    }

    @EnumConvertMethod
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    @Nullable
    public static PreSaleStateEnum resolve(int index) {
        return MAPPINGS.get(index);
    }


}
