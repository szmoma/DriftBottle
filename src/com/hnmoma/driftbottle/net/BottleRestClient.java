package com.hnmoma.driftbottle.net;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;

import android.content.Context;

import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class BottleRestClient {
//	public static final String BASE_URL_1 = "http://192.168.0.108:6666/";
//	public static final String BASE_URL_1 = "http://121.199.58.137:6666/";
	public static final String BASE_URL_1 = "http://android.52plp.com:6666/";
//	public static final String BASE_URL_1 = "http://murener.vicp.cc:6666/";
	
//	public static final String BASE_URL_ATTTH = "http://192.168.0.108:2880/BottleOss/";
//	public static final String BASE_URL_ATTTH = "http://121.199.58.137:28040/BottleOss/";
	public static final String BASE_URL_ATTTH = "http://android.52plp.com/BottleOss/";
//	public static final String BASE_URL_ATTTH = "http://murener.vicp.cc:2880/BottleOss/";
	
	private static AsyncHttpClient client = new AsyncHttpClient();
	
	public static void get(String url, BinaryHttpResponseHandler responseHandler) {
		client.get(url, responseHandler);
	}
	
	public static void get(String url, FileAsyncHttpResponseHandler async) {
		client.get(url, async);
	}
	
	public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}
	
	public static void post(String url, Context ctx, JsonObject params, AsyncHttpResponseHandler responseHandler) {
		try {
			String postParam = params.toString();
			StringEntity entityst = new StringEntity(postParam, "UTF-8");
//			client.setTimeout(15 * 1000);
			client.post(ctx, getAbsoluteUrl(url), entityst, null, responseHandler);
		} catch (UnsupportedEncodingException e) {
		}
	}
	
	/**
	 * 对象转字符串传参
	 * @param url
	 * @param ctx
	 * @param params
	 * @param responseHandler
	 */
	public static void post(String url, Context ctx, String params, AsyncHttpResponseHandler responseHandler) {
		try {
			StringEntity entityst = new StringEntity(params, "UTF-8");
//			client.setTimeout(15 * 1000);
			client.post(ctx, getAbsoluteUrl(url), entityst, null, responseHandler);
		} catch (UnsupportedEncodingException e) {
			MoMaLog.e("对象转字符串传参post请求", e.getMessage());
		}
	}
	
	//上传附件带参数
	public static void postWithAttath(String uri, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.setTimeout(30 * 1000);
		client.post(getAttathUrl(uri), params, responseHandler);
	}
	
	public static void cancelRequests(Context ctx, boolean mayInterruptIfRunning) {
		client.cancelRequests(ctx, mayInterruptIfRunning);
	}

	private static String getAbsoluteUrl(String relativeUrl) {
		return BASE_URL_1 + relativeUrl;
	}
	
	private static String getAttathUrl(String uri) {
		return BASE_URL_ATTTH + uri;
	}
}