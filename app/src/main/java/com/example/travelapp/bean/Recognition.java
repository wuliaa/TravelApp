package com.example.travelapp.bean;

import java.util.Date;

public class Recognition {
    private Long id;

    private String userId;

    private String photo;

    private String landmarkName;

    public Recognition(Long id, String userId, String photo, String landmarkName, String landmarkDescribe, Date createTime) {
        this.id = id;
        this.userId = userId;
        this.photo = photo;
        this.landmarkName = landmarkName;
        this.landmarkDescribe = landmarkDescribe;
        this.createTime = createTime;
    }

    public Recognition() {

    }

    private String landmarkDescribe;

    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getLandmarkName() {
        return landmarkName;
    }

    public void setLandmarkName(String landmarkName) {
        this.landmarkName = landmarkName;
    }

    public String getLandmarkDescribe() {
        return landmarkDescribe;
    }

    public void setLandmarkDescribe(String landmarkDescribe) {
        this.landmarkDescribe = landmarkDescribe;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}