package com.hnmoma.driftbottle.model;

/**
 * 查询单条游戏消息
 * @author yangsy
 *
 */
public class QueryGameInfoModel{
	private String code;
	private String msg; //处理消息
	private GameMsgModel gameResultInfo; //游戏结果
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public GameMsgModel getGameResultInfo() {
		return gameResultInfo;
	}
	public void setGameResultInfo(GameMsgModel gameResultInfo) {
		this.gameResultInfo = gameResultInfo;
	}

}