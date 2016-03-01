package com.hnmoma.driftbottle.model;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table STRANGER.
 */
public class Stranger implements java.io.Serializable {

    private String userId;
    private String province;
    private String city;
    private String nickName;
    private String identityType;
    private String descript;
    private String headImg;
    private java.util.Date updateTime;
    private int state; //状态：0表示未连接；1表示连接；2表示删除连接
    private int isVIP;

    public Stranger() {
    }

    public Stranger(String userId) {
        this.userId = userId;
    }

    public Stranger(String userId, String province, String city, String nickName, String identityType, String descript, String headImg, java.util.Date updateTime, int state, int isVIP) {
        this.userId = userId;
        this.province = province;
        this.city = city;
        this.nickName = nickName;
        this.identityType = identityType;
        this.descript = descript;
        this.headImg = headImg;
        this.updateTime = updateTime;
        this.state = state;
        this.isVIP = isVIP;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public String getDescript() {
        return descript;
    }

    public void setDescript(String descript) {
        this.descript = descript;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public java.util.Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(java.util.Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getIsVIP() {
        return isVIP;
    }

    public void setIsVIP(int isVIP) {
        this.isVIP = isVIP;
    }

}
