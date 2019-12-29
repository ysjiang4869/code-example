package org.jys.example.common.sql;

import org.jys.example.common.sql.CopyInData;
import org.jys.example.common.sql.CopyOrder;

/**
 * @author YueSong Jiang
 * @date 2019/12/28
 */
public class CopyInDataObject implements CopyInData {

    static {
        System.out.println("class CopyInDataObject loaded");
    }

    @CopyOrder(0)
    private long recordId;

    @CopyOrder(1)
    private String personId;

    @CopyOrder(2)
    private int gender;

    @CopyOrder(3)
    private int ageLowerLimit;

    @CopyOrder(4)
    private int ageUpLimit;

    @CopyOrder(5)
    private boolean wearGlasses;

    @CopyOrder(6)
    private String imageUrl;

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getAgeLowerLimit() {
        return ageLowerLimit;
    }

    public void setAgeLowerLimit(int ageLowerLimit) {
        this.ageLowerLimit = ageLowerLimit;
    }

    public int getAgeUpLimit() {
        return ageUpLimit;
    }

    public void setAgeUpLimit(int ageUpLimit) {
        this.ageUpLimit = ageUpLimit;
    }

    public boolean isWearGlasses() {
        return wearGlasses;
    }

    public void setWearGlasses(boolean wearGlasses) {
        this.wearGlasses = wearGlasses;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String generateCopyString() {
        return null;
    }

    @Override
    public char getDelimiter() {
        return '\030';
    }
}
