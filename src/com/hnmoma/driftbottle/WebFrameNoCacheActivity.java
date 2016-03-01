package com.hnmoma.driftbottle;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hnmoma.driftbottle.custom.ProgressWebView;

public class WebFrameNoCacheActivity extends BaseActivity implements OnClickListener{
	
	ProgressWebView wv;
	String webUrl;
	private boolean isShowTitle;//是否显示标题，如果网页存在标题，则不需要添加标题
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("webUrl", webUrl);
		outState.putBoolean("isShowTitle", isShowTitle);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webframe);
		
		if (savedInstanceState != null){
			webUrl = savedInstanceState.getString("webUrl");
			isShowTitle = savedInstanceState.getBoolean("isShowTitle");
		}
		
		Intent intent = getIntent();
		if(intent!=null){
			webUrl = intent.getStringExtra("webUrl");
			isShowTitle = intent.getBooleanExtra("isShowTitle", true);
		}
		if(isShowTitle){
			findViewById(R.id.top_bar).setVisibility(View.VISIBLE);
		}else{
			findViewById(R.id.top_bar).setVisibility(View.GONE);
		}
		wv = (ProgressWebView) findViewById(R.id.wv);
		
		WebSettings settings = wv.getSettings(); 
		settings.setJavaScriptEnabled(true); 
		settings.setDefaultTextEncodingName("utf-8");
		settings.setAppCacheEnabled(false);// 设置禁用缓存
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
			settings.setLoadsImagesAutomatically(true);
		}else{
			settings.setLoadsImagesAutomatically(false);
		}
		
		wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		
		wv.setWebViewClient(new WebViewClient(){
			//如果不需要其他对点击链接事件的处理返回true，否则返回false 
		      @Override
		      public boolean shouldOverrideUrlLoading(WebView view, String url){
		    	  wv.loadUrl(url);  
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
		wv.loadUrl(webUrl);  
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				wv.loadUrl("about:blank");
				this.finish();
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		wv.loadUrl("about:blank");
		this.finish();
	}
}
