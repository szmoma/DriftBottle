package com.hnmoma.driftbottle.net;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * 使用单利模式（线程安全），更新访客、相册、关注、捞网的上限值缓存
 * @author Administrator
 *
 */
public class UpdateCacheOfUpperLimit {

	private static UpdateCacheOfUpperLimit instance;
	private final String PIC= "pic";
	private final String VISITOR= "visitor";
	private final String ATTENTION= "attention";
	private final String NET = "net";
	

	public static synchronized UpdateCacheOfUpperLimit getInstance() {
		if (instance == null) {
			instance = new UpdateCacheOfUpperLimit();
		}
		return instance;
	}
	
	
	public void cache(int isVip,Context context){
		try {
			queryData(isVip,PIC,context);
//			queryData(isVip,ATTENTION,context);
			queryData(isVip,VISITOR,context);
//			queryData(isVip,NET,context); 
			MyApplication.getApp().getSpUtil().setTimeOfUpperLimit(System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 查询相关数据
	 * @param isVip 是否是VIP
	 * @param dataName 需要查询的数据名称，支持4种:pic(相册)、attention(关注度)、visitor(访客)、net(捞网)
	 * @throws Exception 
	 */
	public void queryData(int isVip,final String dataName,Context context) throws Exception{
		JsonObject params = new JsonObject();
		if(PIC.equals(dataName)||VISITOR.equals(dataName)||ATTENTION.equals(dataName)||NET.equals(dataName)){
			params.addProperty("dataName", dataName);
		}else
			throw new Exception("不支持'"+dataName+"'查询，请认真检查") ; 
		params.addProperty("isVIP", String.valueOf(isVip));
		
		BottleRestClient.post("queryData",context, params, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				// TODO Auto-generated method stub
				try {
					JSONObject respon = new JSONObject(new String(responseBody));
					if("0".equals(respon.getString("code"))){
						String dataValue = respon.getString("dataValue");
						int num = 0;
						if(TextUtils.isEmpty(dataValue)){
							num = Integer.MAX_VALUE;
						}else{
							if(MoMaUtil.isDigist(dataValue))
								num = Integer.valueOf(dataValue);
							else
								num = Integer.MAX_VALUE;
						}
						
						if(PIC.equals(dataName)){
							MyApplication.getApp().getSpUtil().setPicNumLimit(num);
						}else if(VISITOR.equals(dataName)){
							MyApplication.getApp().getSpUtil().setVisitorNumLimit(num);
						}else if(ATTENTION.equals(dataName)){
							MyApplication.getApp().getSpUtil().setAttentionNumLimit(num);
						}else if(NET.equals(dataName)){
							MyApplication.getApp().getSpUtil().setCatchNumLimit(num);
						}
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				// TODO Auto-generated method stub
				
			}
		});
	}

}
