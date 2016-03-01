package com.hnmoma.driftbottle;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.PropsTextProgressBar;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
/**
 * 等级详情
 * 
 *
 */
public class GradeActivity extends BaseActivity{
	private TextView tvVipState;	//vip状态
	private TextView tvCharm;//魅力值
	private TextView tvWealth;	//财富值
	private TextView tvLove;	//爱心值
	private TextView tvGame;	//猜拳游戏，格式是：胜/负/平
	private TextView tvLevel;	//等级
	PropsTextProgressBar pbGrade;
	private String userId;	//查询用户等级的id
	private WebView web;//等级详情说明
	private ImageView ivShop;	//魅力商城
	
	private int vipState;
	private int charm;
	private int fortune; //财富值
	private int love;
	private int pointNum;//积分
	private String gameResut;
	
	final String mimeType = "text/html";  
	final String encoding = "utf-8";  
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("userId", userId);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grade);
		if (savedInstanceState != null){
			userId = savedInstanceState.getString("userId");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			userId = intent.getStringExtra("userId");
		}
		setupView();
		queryLevelDoc(userId);
		setListener();
		
	}
	
	private void setupView() {
		// TODO Auto-generated method stub
		tvVipState = (TextView) findViewById(R.id.tv_vip_state);
		tvWealth = (TextView) findViewById(R.id.tv_wealth); 
		tvCharm =  (TextView) findViewById(R.id.tv_charm); 
		tvLove = (TextView) findViewById(R.id.tv_love); 
		tvGame = (TextView) findViewById(R.id.tv_game); 
		tvLevel = (TextView) findViewById(R.id.tv_level); 
		pbGrade = (PropsTextProgressBar) findViewById(R.id.pb_grade);
		web = (WebView) findViewById(R.id.web_level_detail);
		ivShop = (ImageView) findViewById(R.id.iv_shop);
		
		if(!TextUtils.isEmpty(userId)&&userId.equals(UserManager.getInstance(this).getCurrentUserId())){
			ivShop.setVisibility(View.VISIBLE);
		}else{
			ivShop.setVisibility(View.GONE);
		}
		
		//不需要js交换
		web.getSettings().setJavaScriptEnabled(false);
		web.getSettings().setDefaultTextEncodingName(encoding);
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
			web.getSettings().setLoadsImagesAutomatically(true);
		}else{
			web.getSettings().setLoadsImagesAutomatically(false);
		}
		
		web.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				if(!web.getSettings().getLoadsImagesAutomatically())
					web.getSettings().setLoadsImagesAutomatically(true);
			}
		});
	}

	private void init() {
		// TODO Auto-generated method stub
		((TextView)findViewById(R.id.tv_title)).setText(getResources().getString(R.string.level_detail));
		//vip状态
		if(vipState ==1)
			tvVipState.setText(getResources().getString(R.string.vip_state_on));
		else
			tvVipState.setText(getResources().getString(R.string.vip_state_off));
		
		tvWealth.setText(String.valueOf(fortune));
		tvCharm.setText(String.valueOf(charm));
		tvLove.setText(String.valueOf(love));
		String score = gameResut;
		
		if(TextUtils.isEmpty(score))
		tvGame.setText("0/0/0");
		else
		tvGame.setText("("+score.replace("_", "/")+")");
		
		String [] level = MoMaUtil.getLevel(pointNum);
		tvLevel.setText("LV."+level[0]);
		pbGrade.setMax(Integer.parseInt(level[2]));
		pbGrade.setProgress(pointNum);
		
	}

	private void setListener() {
		// TODO Auto-generated method stub
		findViewById(R.id.btn_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
		
		ivShop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(GradeActivity.this,MlscActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent);
			}
		});
	}
	/**
	 * 查询等级详情的接口
	 */
	private void queryLevelDoc(String userId){
		JsonObject params = new JsonObject();
		params.addProperty("userId", userId);
		BottleRestClient.post("queryLevel", this,params, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				showDialog("努力加载...", "加载失败...", 15*1000);
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				// TODO Auto-generated method stub
				try {
					
					JSONObject obj = new JSONObject(new String(responseBody,encoding));
					if("0".equals(obj.getString("code"))){
						JSONObject info = obj.getJSONObject("levelInfo");
						pointNum = info.getInt("pointNum");
//						fansNum = info.getInt("fansNum");
						love = info.getInt("loveNum");
						charm = info.getInt("charm");
						fortune = info.getInt("fortune");
						gameResut = info.getString("score");
						vipState = info.getInt("isVIP");
						init();
						web.loadDataWithBaseURL("", obj.getString("levelDoc"), mimeType, encoding, "");
//						web.loadData(obj.getString("levelDoc"),mimeType, encoding); //显示中文内容，出现乱码，不知道是webView的bug，还是其他的
					}else{
						web.loadDataWithBaseURL("",obj.getString("msg"),mimeType, encoding,"");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					web.loadDataWithBaseURL("","加载失败",mimeType, encoding,"");
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				super.onFinish();
				closeDialog(mpDialog);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				// TODO Auto-generated method stub
				web.loadDataWithBaseURL("","加载失败",mimeType, encoding,"");
			}
		});
	}
	

	
}
