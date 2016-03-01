package com.hnmoma.driftbottle.model;

import java.util.List;
/**
 * 查询游戏消息通知
 */
public class QueryGameMsgBModel{
	private String code;
	private String msg;
	private int isMore;
	private List<GameMsgModel> gameResultList;
	private String gameRecord;//战绩结果---(胜_平_负)
	
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
	public int getIsMore() {
		return isMore;
	}
	public void setIsMore(int isMore) {
		this.isMore = isMore;
	}
	public List<GameMsgModel> getGameResultList() {
		return gameResultList;
	}
	public void setGameResultList(List<GameMsgModel> gameResultList) {
		this.gameResultList = gameResultList;
	}
	
	public String getGameRecord() {
		return gameRecord;
	}
	public void setGameRecord(String gameRecord) {
		this.gameRecord = gameRecord;
	}
}