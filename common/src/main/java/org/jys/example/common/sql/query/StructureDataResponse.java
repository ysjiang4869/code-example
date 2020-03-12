package org.jys.example.common.sql.query;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YueSong Jiang
 * @date 2020/3/12
 * <p>
 * structured data query standard response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StructureDataResponse<T> {
    private Long searchTime;
    private Long postProcessTime;
    private Integer count;
    private List<T> data;

    public static <T> StructureDataResponse<T> returnEmpty(long searchTime, long endTime) {
        StructureDataResponse<T> dataResponse = new StructureDataResponse<T>(new ArrayList<T>());
        dataResponse.setSearchTime(searchTime);
        dataResponse.setPostProcessTime(System.currentTimeMillis() - endTime);
        dataResponse.setCount(0);
        return dataResponse;
    }

    public static <T> StructureDataResponse<T> of(long searchTime, long endTime, List<T> resultList) {
        StructureDataResponse<T> dataResponse = new StructureDataResponse<T>(resultList);
        dataResponse.setSearchTime(searchTime);
        dataResponse.setPostProcessTime(System.currentTimeMillis() - endTime);
        dataResponse.setCount(resultList.size());
        return dataResponse;
    }

    public StructureDataResponse(List<T> results) {
        this.data = results;
    }

    public Long getSearchTime() {
        return this.searchTime;
    }

    public void setSearchTime(Long searchTime) {
        this.searchTime = searchTime;
    }

    public Long getPostProcessTime() {
        return this.postProcessTime;
    }

    public void setPostProcessTime(Long postProcessTime) {
        this.postProcessTime = postProcessTime;
    }

    public List<T> getData() {
        return this.data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
