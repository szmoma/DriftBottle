package com.hnmoma.driftbottle.model;
/**
 * 该类扩展BaseModel，增添了一个字段bottleId,主要用于扔游戏瓶时，把字符串解析为对象使用
 * @author yangsy
 *
 */
public class BaseModelEx extends BaseModel {
	private String bottleId; //游戏瓶子的id

	public String getBottleId() {
		return bottleId;
	}

	public void setBottleId(String bottleId) {
		this.bottleId = bottleId;
	}
	
}
