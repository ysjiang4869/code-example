package org.jys.example.common.sql;

/**
 * @author YueSong Jiang
 * @date 2019/12/28
 */
public class CopyInDataObject implements CopyInData {

    static {
        System.out.println("class CopyInDataObject loaded");
    }

    @CopyOrder()
    private Long recordId;

    @CopyOrder(beforeField = "recordId")
    private String personId;

    @CopyOrder(beforeField = "personId")
    private Integer gender;

    @CopyOrder(beforeField = "gender")
    private Integer ageLowerLimit;

    @CopyOrder(beforeField = "ageLowerLimit")
    private Integer ageUpLimit;

    @CopyOrder(beforeField = "ageUpLimit")
    private Boolean wearGlasses;

    @CopyOrder(beforeField = "wearGlasses")
    private String imageUrl;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getAgeLowerLimit() {
        return ageLowerLimit;
    }

    public void setAgeLowerLimit(Integer ageLowerLimit) {
        this.ageLowerLimit = ageLowerLimit;
    }

    public Integer getAgeUpLimit() {
        return ageUpLimit;
    }

    public void setAgeUpLimit(Integer ageUpLimit) {
        this.ageUpLimit = ageUpLimit;
    }

    public Boolean isWearGlasses() {
        return wearGlasses;
    }

    public void setWearGlasses(Boolean wearGlasses) {
        this.wearGlasses = wearGlasses;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * this will be modified by {@link CopyMethodGenerator}
     * @return copy string
     */
    @Override
    public String generateCopyString() {
        return null;
    }
}
