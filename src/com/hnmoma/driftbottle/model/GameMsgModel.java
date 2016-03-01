package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.Date;

public class GameMsgModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String bottleId; //瓶子id
	private String userId; //对手id
	private String nickName; //昵称
	private String headImg; //头像地址
	private String content;  //双方游戏结果内容
	private String result; //游戏结果(0表示输，1表示平，2表示赢，3拒绝)
	private Date createTime; //创建时间
	private String bUId; //关系id
	private int money; //赌注
	private int state; //状态(0表示处理中 1表示完成未查看，2表示已查看，3表示拒绝,4表示超时)
	private int isHost; //是否为发起者，此针对于对手而言，1表示发起者，0表示接受者
	
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getHeadImg() {
		return headImg;
	}
	public void setHeadImg(String headImg) {
		this.headImg = headImg;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getbUId() {
		return bUId;
	}
	public void setbUId(String bUId) {
		this.bUId = bUId;
	}
	public int getMoney() {
		return money;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getIsHost() {
		return isHost;
	}
	public void setIsHost(int isHost) {
		this.isHost = isHost;
	}
	public String getBottleId() {
		return bottleId;
	}
	public void setBottleId(String bottleId) {
		this.bottleId = bottleId;
	}
	
}