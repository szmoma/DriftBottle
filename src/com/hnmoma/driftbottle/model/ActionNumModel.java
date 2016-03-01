package com.hnmoma.driftbottle.model;

import java.io.Serializable;


public class ActionNumModel implements Serializable{
	
	private int supportNum;
	private int disSupportNum;
	private int reviewNum;
	private int doNum;
	
	public int getSupportNum() {
		return supportNum;
	}
	public void setSupportNum(int supportNum) {
		this.supportNum = supportNum;
	}
	public int getDisSupportNum() {
		return disSupportNum;
	}
	public void setDisSupportNum(int disSupportNum) {
		this.disSupportNum = disSupportNum;
	}
	public int getReviewNum() {
		return reviewNum;
	}
	public void setReviewNum(int reviewNum) {
		this.reviewNum = reviewNum;
	}
	public int getDoNum() {
		return doNum;
	}
	public void setDoNum(int doNum) {
		this.doNum = doNum;
	}
}