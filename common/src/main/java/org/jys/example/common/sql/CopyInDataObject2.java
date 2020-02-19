package org.jys.example.common.sql;

/**
 * @author YueSong Jiang
 * @date 2019/12/28
 */
public class CopyInDataObject2 extends CopyInDataObject {

    static {
        System.out.println("class CopyInDataObject loaded");
    }

    @CopyOrder()
    protected Long fileId;

    @CopyOrder(beforeField = "fileId")
    protected Long recordId;


    @Override
    public Long getRecordId() {
        return recordId;
    }

    @Override
    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getFileId() {


        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }
}
