package com.hnmoma.driftbottle;

import java.util.HashMap;

import org.apache.http.Header;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.custom.ProgressWebView;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
import com.hnmoma.driftbottle.model.SinaTopicModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class WebFrameWithCacheActivity extends BaseActivity implements OnClickListener, PlatformActionListener, ShareContentCustomizeCallback, Callback{
	
	ProgressWebView wv;
	String webUrl;
	
	PickBottleModel model;
	BottleModel bottleModel;
	
	Button bt_fx;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("webUrl", webUrl);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webframe);
		bt_fx = (Button) findViewById(R.id.bt_fx);
		
		if (savedInstanceState != null){
			webUrl = savedInstanceState.getString("webUrl");
		}
		
		Intent intent = getIntent();
		if(intent!=null){
			webUrl = intent.getStringExtra("webUrl");
			model = (PickBottleModel) intent.getSerializableExtra("PickBottleModel");
			if(model!=null){
				bottleModel = model.getBottleInfo();
				bt_fx.setVisibility(View.VISIBLE);
			}
		}
		
		
		
		wv = (ProgressWebView) findViewById(R.id.wv);
		
		WebSettings settings = wv.getSettings(); 
//		settings.setJavaScriptEnabled(true); 
//		settings.setDefaultTextEncodingName("utf-8");
//		settings.setAppCacheEnabled(true);// 设置启动缓存
//		settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		
		settings.setJavaScriptEnabled(true);
		settings.setPluginState(PluginState.ON);
//		settings.setPluginsEnabled(true);//可以使用插件
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setAllowFileAccess(true);
		settings.setDefaultTextEncodingName("UTF-8");
		settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
			wv.getSettings().setLoadsImagesAutomatically(true);
		}else{
			wv.getSettings().setLoadsImagesAutomatically(false);
		}
		wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		
		wv.setWebViewClient(new WebViewClient(){
			//如果不需要其他对点击链接事件的处理返回true，否则返回false 
		      @Override
		      public boolean shouldOverrideUrlLoading(WebView view, String url){
		    	  view.loadUrl(url);  
		          return true;  
		      }
		      @Override
			    public void onPageFinished(WebView view, String url) {
			    	// TODO Auto-generated method stub
			    	super.onPageFinished(view, url);
			    	if(!wv.getSettings().getLoadsImagesAutomatically()){
			    		wv.getSettings().setLoadsImagesAutomatically(true);
			    	}
			    }
		});
		
		wv.setDownloadListener(new DownloadListener(){
			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
//				Uri uri = Uri.parse(url);  
//	            Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
//	            startActivity(intent);    
				Intent intent = new Intent();        
		         intent.setAction("android.intent.action.VIEW");    
		         Uri content_url = Uri.parse(url);   
		         intent.setData(content_url);           
		         //指定android自带的浏览器访问（com.android.browser.BrowserActivity），如果被禁用，可能出现问题；
//		         intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");   
		         startActivity(intent);
			}
		});  
		
		wv.loadUrl(webUrl);  
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				wv.loadUrl("about:blank");
				this.finish();
				break;
			case R.id.bt_fx:
				getSinaTopic();
				showShare(true, null);
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		wv.loadUrl("about:blank");
		this.finish();
	}
	
	private void showShare(boolean silent, String platform) {
//		if (bottleModel.getContentType().equals("5000")) {
//			textFrameShare(silent, platform);
//		} else if (bottleModel.getContentType().equals("5001")) {
//		} else if (bottleModel.getContentType().equals("5002")) {
//			voiceFrameShare(silent, platform);
//		} else if (bottleModel.getContentType().equals("5003")) {
//		}
		OnekeyShare oks = new OnekeyShare();
		oks.disableSSOWhenAuthorize();
		oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
		oks.setAddress("12345678901");
		oks.setTitle(MyConstants.SHARE_TITLE);
		oks.setTitleUrl(MyConstants.SHARE_URL + bottleModel.getBottleId());
		oks.setText(bottleModel.getContent());
		oks.setUrl(MyConstants.SHARE_URL + bottleModel.getBottleId());
		oks.setComment("分享");
		oks.setSilent(silent);
		oks.setSite(getString(R.string.app_name));
		oks.setSiteUrl(MyConstants.MYWEB_URL);
		oks.setCallback(this);
		oks.setShareContentCustomizeCallback(this);
		oks.show(this);
	}
	
	String sinaTopic;
	String sinaImageUrl;
	final String WebSite = " http://www.52plp.com ";
	private void getSinaTopic() {
		//获取新浪话题
		JsonObject obj = new JsonObject();
		obj.addProperty("bottleId", bottleModel.getBottleId());
		BottleRestClient.post("querySinaShare", this, obj,  new AsyncHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String response = new String(responseBody);
				Gson gson = new Gson();
				SinaTopicModel baseModel = gson.fromJson(response, SinaTopicModel.class);
				if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode()) ) {
					if("0".equals(baseModel.getCode())) {
						sinaTopic = baseModel.getTopicContent();
						sinaImageUrl = baseModel.getUrl();
//						showMsg(sinaImageUrl);
//						showMsg(sinaTopic);
						if(TextUtils.isEmpty(sinaImageUrl)) {
							sinaTopic = "";
						} else {
							sinaTopic = "#" +sinaTopic + "#";
						}
					}else{
						showMsg(baseModel.getMsg());
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				sinaTopic = "";
				sinaImageUrl = "http://52plp.com/share/sina.jpg";
				//shareToWeibo(paramsToShare);
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
			}
		});
}
	// 分享到微博
	public void shareToWeibo(ShareParams paramsToShare) {
		//贺卡瓶子
		if(bottleModel.getContentType().equals("5002")) {
			if(bottleModel.getIsRedirect() == 1 ) {
				paramsToShare.setText(bottleModel.getContent()+ bottleModel.getRedirectUrl() +" "+" #漂流瓶子# " + sinaTopic);
			} else {
				paramsToShare.setText(bottleModel.getContent()+ MyConstants.SHARE_URL + bottleModel.getBottleId() +" "+" #漂流瓶子# " + sinaTopic);
			}
			paramsToShare.setImageUrl(bottleModel.getShortPic());
			return;
		}
		
		String sysType = bottleModel.getSysType();
		if("13".equals(sysType)) {
			paramsToShare.setText("女神出没，请注意！" + WebSite +" "+" #漂流瓶子# " + sinaTopic);
		}  else if("11".equals(sysType)) {
			      if (bottleModel.getContent().length() > 100 || TextUtils.isEmpty(bottleModel.getContent()))  {
			    	  paramsToShare.setText("哈哈哈我也是微醺~ " + WebSite +" "+"#漂流瓶子#" + sinaTopic);
			      } else {
			    	  paramsToShare.setText(bottleModel.getContent() + WebSite +" "+"#漂流瓶子#" + sinaTopic);
			      }
			} else if("2".equals(sysType) || "7".equals(sysType)) {
				paramsToShare.setText("一语中的 " + WebSite +" "+"#漂流瓶子#" + sinaTopic);
			} else if("9".equals(sysType)) {
				
				if("5000".equals(bottleModel.getContentType())) {
					paramsToShare.setText("朕已笑死，有事烧纸 " + WebSite +" "+"#漂流瓶子#" + sinaTopic);
				} else if("5001".equals(bottleModel.getContentType())) {
					if (bottleModel.getContent().length() >100 || TextUtils.isEmpty(bottleModel.getContent()))  {
				    	  paramsToShare.setText("啥也不说了，戳图！ " + WebSite +" "+"#漂流瓶子#" + sinaTopic);
				      } else {
				    	  paramsToShare.setText(bottleModel.getContent() + WebSite +" "+"#漂流瓶子#" + sinaTopic);
				      }
				}
			} else if("10".equals(sysType)) {
				paramsToShare.setText("这题不会，帮我看看？ " + WebSite +" "+"#漂流瓶子#" + sinaTopic);
			} 
		paramsToShare.setImageUrl((sinaImageUrl));
		
	}
	
	// 分享到微信朋友圈
	public void shareToWeChatFriend(ShareParams paramsToShare) {
		// TODO Auto-generated method stub
		String sysType = bottleModel.getSysType();
		paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
		if("13".equals(sysType)) {
			paramsToShare.setTitle("这个妹纸是我的菜");
			paramsToShare.setImageUrl(bottleModel.getShortPic());
		}  else if("11".equals(sysType)) {
			      if (bottleModel.getContent().length() > 25 || TextUtils.isEmpty(bottleModel.getContent()))  {
			    	  paramsToShare.setTitle("朕已笑死，有事烧纸 ");
			    	  paramsToShare.setImageUrl(bottleModel.getShortPic());
			      } else {
			    	  paramsToShare.setTitle(bottleModel.getContent());
			    	  paramsToShare.setImageUrl(bottleModel.getShortPic());
			      }
			} else if("2".equals(sysType) || "7".equals(sysType)) {
				paramsToShare.setTitle("说得真好， " + bottleModel.getContent());
				paramsToShare.setImageUrl("http://52plp.com/share/share"+sysType+".jpg");
			} else if("9".equals(sysType)) {
				if("5000".equals(bottleModel.getContentType())) {
					paramsToShare.setTitle("笑到飙泪，" + bottleModel.getContent());
					paramsToShare.setImageUrl("http://52plp.com/share/share9.jpg");
				} else if("5001".equals(bottleModel.getContentType())) {
					paramsToShare.setTitle("哈哈哈，真是醉了… " + bottleModel.getContent());
					paramsToShare.setImageUrl(bottleModel.getShortPic());
				}
			} else if("10".equals(sysType)) {
				paramsToShare.setTitle("你知道答案吗？" );
				paramsToShare.setImageUrl("http://52plp.com/share/share10.jpg");
			} else if("12".equals(sysType)) {// 贺卡
				if (bottleModel.getContent().length() > 25 || TextUtils.isEmpty(bottleModel.getContent()))  {
					paramsToShare.setTitle(bottleModel.getContent().substring(0, 25)+ "...");
				}  else {
					paramsToShare.setTitle(bottleModel.getContent());
				}
				paramsToShare.setImageUrl(bottleModel.getShortPic());
				if(bottleModel.getIsRedirect() == 1 ) {
					paramsToShare.setUrl(bottleModel.getRedirectUrl());
					paramsToShare.setTitleUrl(bottleModel.getRedirectUrl());
				}
			}
	}
	
	// 分享到其他平台
	public void shareToOthers(ShareParams paramsToShare) {
		// TODO Auto-generated method stub
		String sysType = bottleModel.getSysType();
		paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
		if("13".equals(sysType)) {
			paramsToShare.setTitle("女神出没!");
			paramsToShare.setImageUrl(bottleModel.getShortPic());
		}  else if("11".equals(sysType)) {
			    	  paramsToShare.setTitle("画面太美");
			    	  paramsToShare.setImageUrl(bottleModel.getShortPic());
			} else if("2".equals(sysType) || "7".equals(sysType)) {
				paramsToShare.setTitle("你同意不？ ");
				paramsToShare.setImageUrl("http://52plp.com/share/share"+sysType+".jpg");
			} else if("9".equals(sysType)) {
				if("5000".equals(bottleModel.getContentType())) {
					paramsToShare.setTitle("转发测笑点");
					paramsToShare.setImageUrl("http://52plp.com/share/share9.jpg");
				} else if("5001".equals(bottleModel.getContentType())) {
					paramsToShare.setTitle("笑shi我了");
					paramsToShare.setImageUrl(bottleModel.getShortPic());
				}
			} else if("10".equals(sysType)) {
				paramsToShare.setTitle("脑洞大开" );
				paramsToShare.setImageUrl("http://52plp.com/share/share10.jpg");
			} else if("12".equals(sysType)) {
				paramsToShare.setTitle(bottleModel.getContent());
				paramsToShare.setImageUrl(bottleModel.getShortPic());
				if(bottleModel.getIsRedirect() == 1 ) {
					paramsToShare.setTitleUrl(bottleModel.getRedirectUrl());
					paramsToShare.setUrl(bottleModel.getRedirectUrl());
				}
			}
	}
	
	Handler handler = new Handler();

	@Override
	public void onCancel(Platform platform, int action) {
		Message msg = new Message();
		msg.arg1 = 3;
		msg.arg2 = action;
		msg.obj = platform;
		handler.sendMessage(msg);

	}

	@Override
	public void onComplete(Platform platform, int action, HashMap<String, Object> arg2) {
		Message msg = new Message();
		msg.arg1 = 1;
		msg.arg2 = action;
		msg.obj = platform;
		handler.sendMessage(msg);
	}

	@Override
	public void onError(Platform platform, int action, Throwable arg2) {
		Message msg = new Message();
		msg.arg1 = 2;
		msg.arg2 = action;
		msg.obj = platform;
		handler.sendMessage(msg);
	}

	@Override
	public void onShare(Platform platform, ShareParams paramsToShare) {
		//TODO  根据不同的平台
		if(SinaWeibo.NAME.equals(platform.getName())){
			// 分享到新浪微博
			shareToWeibo(paramsToShare);
		}  else if(WechatMoments.NAME.equals(platform.getName())) {
			// 分享到微信朋友圈
				shareToWeChatFriend(paramsToShare);
//			}
		}  else {
			// 分享到其他平台
				shareToOthers(paramsToShare);
//			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.arg1) {
		case 1:
			// closeDialog();
			showMsg("分享成功");
			break;
		case 2:
			// closeDialog();
			showMsg("分享失败");
			break;
		case 3:
			// closeDialog();
			showMsg("用户取消");
			break;
		}
		return false;
	}
	
}