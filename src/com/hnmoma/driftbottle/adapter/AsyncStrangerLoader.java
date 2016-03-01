package com.hnmoma.driftbottle.adapter;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.BottleMsgManager;
import com.hnmoma.driftbottle.model.QueryUserInfoModel;
import com.hnmoma.driftbottle.model.Stranger;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class AsyncStrangerLoader {

	private HashMap<String, SoftReference<Stranger>> imageCache; // 内存图片软引用缓冲 
	Context context;

	public AsyncStrangerLoader(Context context) {
		imageCache = new HashMap<String, SoftReference<Stranger>>();
		this.context = context;
	}

	public Stranger loadStranger(final String userId, final StrangerCallback strangerCallback) {
		if (imageCache.containsKey(userId)) {
			SoftReference<Stranger> softReference = imageCache.get(userId);
			Stranger stranger = softReference.get();
			if (stranger != null) {
				return stranger;
			}
		}
		//从数据库中查询
		Stranger stranger = BottleMsgManager.getInstance(context).getStrangerById(userId);
		if(stranger!=null&&!TextUtils.isEmpty(stranger.getHeadImg())){
			imageCache.put(userId, new SoftReference<Stranger>(stranger));
			return stranger;
		}
		
		JsonObject jo = new JsonObject();
		jo.addProperty("id", userId);
		BottleRestClient.post("queryUserInfo", context, jo, new AsyncHttpResponseHandler (){
			@Override
			public void onStart() {
				super.onStart();
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryUserInfoModel userModel = gson.fromJson(str, QueryUserInfoModel.class);
					 
					 if(userModel!=null&&"0".equals(userModel.getCode())){
						 UserInfoModel userObj = userModel.getUserInfo();
						 if(userObj!=null){
								Stranger stranger = BottleMsgManager.getInstance(context).insertOrReplaceStranger(userObj);
								imageCache.put(userId, new SoftReference<Stranger>(stranger));
								strangerCallback.strangerLoaded(stranger);
						 }
					 }
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
			}
        });
		
		return null;
	}
	
	public interface StrangerCallback {
		public void strangerLoaded(Stranger stranger);
	}
}
