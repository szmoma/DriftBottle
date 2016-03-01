package com.hnmoma.driftbottle.business;

/**
 * 事件监听器
 * @author MOMA
 *
 */
public abstract interface EventListener {
	
	/**paramBmobMsg为空时是离线消息
	 * 
	 * @param paramBmobMsg
	 */
	
	/**
	 * 是否要处理给定事件
	 * @param paramBmobMsg
	 */
  public abstract boolean supportEventType(String type);
	
  public abstract void onMessage(BmobMsg paramBmobMsg);

  public abstract void onReaded(String paramString1, String paramString2);

  public abstract void onNetChange(boolean paramBoolean);

//  public abstract void onAddUser(BmobInvitation paramBmobInvitation);

  public abstract void onOffline();
}