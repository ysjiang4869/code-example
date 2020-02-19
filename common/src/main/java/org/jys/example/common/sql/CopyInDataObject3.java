package org.jys.example.common.sql;

/**
 * @author YueSong Jiang
 * @date 2019/12/28
 */
public class CopyInDataObject3 extends CopyInDataObject2 {

    static {
        System.out.println("class CopyInDataObject loaded");
    }

    @CopyOrder(beforeField = "imageUrl")
    protected Long  imageSize;

    public Long getImageSize() {
        return imageSize;
    }

    public void setImageSize(Long imageSize) {
        this.imageSize = imageSize;
    }
}
