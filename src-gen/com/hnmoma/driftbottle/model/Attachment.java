package com.hnmoma.driftbottle.model;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table ATTACHMENT.
 */
public class Attachment implements java.io.Serializable {

	
	
    private Long id;
    private Integer attachmentType;
    private String attachmentUrl;
    private long bottleIdPk;

    public Attachment() {
    }

    public Attachment(Long id) {
        this.id = id;
    }

    public Attachment(Long id, Integer attachmentType, String attachmentUrl, long bottleIdPk) {
        this.id = id;
        this.attachmentType = attachmentType;
        this.attachmentUrl = attachmentUrl;
        this.bottleIdPk = bottleIdPk;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(Integer attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public long getBottleIdPk() {
        return bottleIdPk;
    }

    public void setBottleIdPk(long bottleIdPk) {
        this.bottleIdPk = bottleIdPk;
    }

}
