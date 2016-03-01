package com.hnmoma.driftbottle.model;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table CHAT.
 */
public class Chat implements java.io.Serializable {

    private Long id;
    /** Not-null value. */
    private String userId;
    private String content;
    /** Not-null value. */
    private String contentType;
    /** Not-null value. */
    private String nickName;
    private String identityType;
    private String headImg;
    private String province;
    private String city;
    private Boolean isContent;
    private boolean isComMsg;
    private java.util.Date chatTime;
    private long bottleIdPk;

    public Chat() {
    }

    public Chat(Long id) {
        this.id = id;
    }

    public Chat(Long id, String userId, String content, String contentType, String nickName, String identityType, String headImg, String province, String city, Boolean isContent, boolean isComMsg, java.util.Date chatTime, long bottleIdPk) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.contentType = contentType;
        this.nickName = nickName;
        this.identityType = identityType;
        this.headImg = headImg;
        this.province = province;
        this.city = city;
        this.isContent = isContent;
        this.isComMsg = isComMsg;
        this.chatTime = chatTime;
        this.bottleIdPk = bottleIdPk;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getUserId() {
        return userId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /** Not-null value. */
    public String getContentType() {
        return contentType;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /** Not-null value. */
    public String getNickName() {
        return nickName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
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

    public Boolean getIsContent() {
        return isContent;
    }

    public void setIsContent(Boolean isContent) {
        this.isContent = isContent;
    }

    public boolean getIsComMsg() {
        return isComMsg;
    }

    public void setIsComMsg(boolean isComMsg) {
        this.isComMsg = isComMsg;
    }

    public java.util.Date getChatTime() {
        return chatTime;
    }

    public void setChatTime(java.util.Date chatTime) {
        this.chatTime = chatTime;
    }

    public long getBottleIdPk() {
        return bottleIdPk;
    }

    public void setBottleIdPk(long bottleIdPk) {
        this.bottleIdPk = bottleIdPk;
    }

}