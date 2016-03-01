package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.GameOpponentModel;
import com.hnmoma.driftbottle.model.QueryUserInfoModel;
import com.hnmoma.driftbottle.model.Stranger;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class Game_Cq_Activity extends BaseActivity implements OnClickListener{

	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	CircularImage iv_t; //其他人
	CircularImage iv_b;//自己的头像
	ImageView iv_l, iv_r, iv_c;
	/**
	 * 1维是第几局，2维是哪方
	 */
	TextView[][] textView = new TextView[3][2];
	
	//hand
	Animation count_ani,shake_ani,shake_ani2;
	int index;
	
	//传过来的数据
	int [] array01,array02, flag;
	int money;
	
	Drawable stone_s,scissors_s,cloth_s,stone_y,scissors_y,cloth_y,stone_p,scissors_p,cloth_p;
	/**
	 * 1维是输赢平，2维是石头剪刀布
	 */
	Drawable[][] drawable = new Drawable[3][3];
	int[] id_count = new int[]{R.drawable.pic_round1,R.drawable.pic_round2,R.drawable.pic_round3};
	int[][] id_hand = new int[][]{
			{R.drawable.pic_stone_up,R.drawable.pic_scissors_up,R.drawable.pic_cloth_up},
			{R.drawable.pic_stone_down,R.drawable.pic_scissors_down,R.drawable.pic_cloth_down}};
	
	ImageView[] stars = new ImageView[3];
	
	ImageView iv_state;
	
	Handler handler = new Handler();
	
	GameOpponentModel model;
	
	private AlertDialog dlg;
	
	private boolean isFromChat;
	private String bUId;	//瓶子的标识
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)         //加载开始默认的图片      
        .showImageForEmptyUri(R.drawable.defalutimg)     //url爲空會显示该图片，自己放在drawable里面的
        .showImageOnFail(R.drawable.defalutimg)                //加载图片出现问题，会显示该图片
        .cacheInMemory(true)                                               //缓存用
        .cacheOnDisc(true)                                                    //缓存用
        .considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
        .build();
		
		Intent intent = getIntent();
		array01 = intent.getIntArrayExtra("array01");
		array02 = intent.getIntArrayExtra("array02");
		flag = intent.getIntArrayExtra("flag");
		money = intent.getIntExtra("money", 100);
		isFromChat = intent.getBooleanExtra("fromChat", false);
		
		model = (GameOpponentModel) intent.getSerializableExtra("GameOpponentModel");
		
		setContentView(R.layout.activity_game_cq);
		initView();
		
		initDate(model);
	}
	
	private void initDate(GameOpponentModel model){
		if(model==null)
			return ;
		String t_headimg = model.getHeadImg();
		String b_headimg = UserManager.getInstance(this).getCurrentUser().getHeadImg();
		if(TextUtils.isEmpty(t_headimg))
			MoMaLog.e("debug", "好友头像是空，显示默认头像");
		if(TextUtils.isEmpty(b_headimg))
			MoMaLog.e("debug", "自己的头像是空，显示默认头像");
		
		imageLoader.displayImage(t_headimg, iv_t, options); //设置其他人的头像
		imageLoader.displayImage(b_headimg, iv_b, options);	//设置自己的头像
		
		if(model.getContent()==null)
			return ;
		if(model.getMoney()!=0)
			money = model.getMoney();
		String[] ary = model.getContent().split("_"); //发起方_接收方
		if(ary==null||ary.length!=2)
			return ;
		array01 = MoMaUtil.cq_change2Array(ary[1]);
		array02 = MoMaUtil.cq_change2Array(ary[0]);
		flag = new int[3];
		flag[0] = compare(array01[0], array02[0]);
		flag[1] = compare(array01[1], array02[1]);
		flag[2] = compare(array01[2], array02[2]);
		bUId = getIntent().getStringExtra("bUId");
	}
	
	public void initView() {
		iv_c = (ImageView) findViewById(R.id.iv_c);
		iv_t = (CircularImage) findViewById(R.id.iv_t);
		iv_b = (CircularImage) findViewById(R.id.iv_b);
		iv_l = (ImageView) findViewById(R.id.iv_l);
		iv_r = (ImageView) findViewById(R.id.iv_r);
		
		textView[0][0] = (TextView) findViewById(R.id.tv_t_01);
		textView[1][0] = (TextView) findViewById(R.id.tv_t_02);
		textView[2][0] = (TextView) findViewById(R.id.tv_t_03);
		textView[0][1] = (TextView) findViewById(R.id.tv_b_01);
		textView[1][1] = (TextView) findViewById(R.id.tv_b_02);
		textView[2][1] = (TextView) findViewById(R.id.tv_b_03);
		
		//round count
		count_ani = AnimationUtils.loadAnimation(this, R.anim.yx_cq_count);
		count_ani.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				iv_c.setVisibility(View.GONE);
				
				iv_l.startAnimation(shake_ani);
				iv_r.startAnimation(shake_ani2);
			}
		});
		
		//hand
		shake_ani = AnimationUtils.loadAnimation(this, R.anim.yx_cq_shake);
		shake_ani2 = AnimationUtils.loadAnimation(this, R.anim.yx_cq_shake2);
//		shake_ani.setFillAfter(true);
//		shake_ani2.setFillAfter(true);
		
		shake_ani2.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
//				iv_r.clearAnimation();
//				iv_l.clearAnimation();
				
				handler.postDelayed(mt3, 100);
			}
		});
		
		iv_c.setVisibility(View.VISIBLE);
		iv_c.startAnimation(count_ani);
		
		//view 输赢平
		drawable[0][0] = getResources().getDrawable(R.drawable.icon_stone_red_76);
		int w = drawable[0][0].getMinimumWidth();
		int h = drawable[0][0].getMinimumHeight();
		drawable[0][0].setBounds(0, 0, w, h); //设置边界
		drawable[0][1] = getResources().getDrawable(R.drawable.icon_scissors_red_76);
		drawable[0][1].setBounds(0, 0, w, h); //设置边界
		drawable[0][2] = getResources().getDrawable(R.drawable.icon_cloth_red_76);
		drawable[0][2].setBounds(0, 0, w, h); //设置边界
		
		drawable[1][0] = getResources().getDrawable(R.drawable.icon_stone_green_76);
		drawable[1][0].setBounds(0, 0, w, h); //设置边界
		drawable[1][1] = getResources().getDrawable(R.drawable.icon_scissors_green_76);
		drawable[1][1].setBounds(0, 0, w, h); //设置边界
		drawable[1][2] = getResources().getDrawable(R.drawable.icon_cloth_green_76);
		drawable[1][2].setBounds(0, 0, w, h); //设置边界
		
		drawable[2][0] = getResources().getDrawable(R.drawable.icon_stone_blue_76);
		drawable[2][0].setBounds(0, 0, w, h); //设置边界
		drawable[2][1] = getResources().getDrawable(R.drawable.icon_scissors_blue_76);
		drawable[2][1].setBounds(0, 0, w, h); //设置边界
		drawable[2][2] = getResources().getDrawable(R.drawable.icon_cloth_blue_76);
		drawable[2][2].setBounds(0, 0, w, h); //设置边界
	}
	
	MyThread mt = new MyThread();
	class MyThread implements Runnable{
		public void run() {
			iv_l.setImageResource(id_hand[0][0]);
			iv_r.setImageResource(id_hand[1][0]);
			
			//初始化数据后开始下一轮
			iv_c.setVisibility(View.VISIBLE);
			iv_c.startAnimation(count_ani);
		}
	}
	
	MyThread2 mt2 = new MyThread2();
	class MyThread2 implements Runnable{
		public void run() {
			showOverDialog();
		}
	}
	
	
	MyThread3 mt3 = new MyThread3();
	class MyThread3 implements Runnable{
		public void run() {
			if(index<3){
				//显示手势结果
				iv_l.setImageResource(id_hand[0][array01[index]]);
				iv_r.setImageResource(id_hand[1][array02[index]]);
				
				if(flag[index] == 0){
					textView[index][0].setText("赢");
					textView[index][1].setText("输");
					
					Drawable dw = drawable[1][array01[index]];
					textView[index][0].setCompoundDrawables(null, dw, null, null);//画在右边
					
					Drawable dw2 = drawable[0][array02[index]];
					textView[index][1].setCompoundDrawables(null, dw2, null, null);//画在右边
				}else if(flag[index] == 1){
					textView[index][0].setText("平");
					textView[index][1].setText("平");
					
					Drawable dw = drawable[2][array01[index]];
					textView[index][0].setCompoundDrawables(null, dw, null, null);//画在右边
					
					Drawable dw2 = drawable[2][array02[index]];
					textView[index][1].setCompoundDrawables(null, dw2, null, null);//画在右边
				}else{
					textView[index][0].setText("输");
					textView[index][1].setText("赢");
					
					Drawable dw = drawable[0][array01[index]];
					textView[index][0].setCompoundDrawables(null, dw, null, null);//画在右边
					
					Drawable dw2 = drawable[1][array02[index]];
					textView[index][1].setCompoundDrawables(null, dw2, null, null);//画在右边
				}
				
				if(index<2){
					iv_c.setImageResource(id_count[index+1]);
					handler.postDelayed(mt, 1000);
				}else{
					handler.postDelayed(mt2, 500);	//打开对话框的消息
				}
				index++;
			}
		}
	}
	
	MyThread4 mt4 = new MyThread4();
	/**
	 * 关闭对话框
	 * @author Administrator
	 *
	 */
	class MyThread4 implements Runnable{
		public void run() {
			if(dlg!=null){
				dlg.dismiss();
				dlg=null;
				if(isFromChat){
					handler.postDelayed(mt5, 500);
				}
			}
		}
	}
	
	MyThread5 mt5 = new MyThread5();
	private int total;
	/**
	 * 关闭当前窗体
	 * @author Administrator
	 *
	 */
	class MyThread5 implements Runnable{
		public void run() {
			 Intent intent = new Intent();
			 int result =0;
			 if(total > 3){//赢
				 result = 2;
			 }else if(total < 3){//shu
				result = 0;
			 }else{
				 result = 1;
			 }
			 
			intent.putExtra("result", result);
			intent.putExtra("bUId", bUId);
			intent.putExtra("GameOpponentModel", model);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	}
	
	private void showOverDialog() {
		 dlg = new AlertDialog.Builder(this).create();
		 dlg.setCancelable(false);
		 dlg.show();
		 Window window = dlg.getWindow();
		 window.setContentView(R.layout.view_game_cq_dialog);
		 ImageView ib_close = (ImageView) window.findViewById(R.id.ib_close);
		 if(isFromChat){
			 window.findViewById(R.id.ll_bottom).setVisibility(View.GONE);
		 }
		 
		 ib_close.setOnClickListener(this);
		 
		 stars[0] = (ImageView) window.findViewById(R.id.iv_s1);
		 stars[1] = (ImageView) window.findViewById(R.id.iv_s2);
		 stars[2] = (ImageView) window.findViewById(R.id.iv_s3);
		 iv_state = (ImageView) window.findViewById(R.id.iv_state);
		 TextView tv_sb = (TextView) window.findViewById(R.id.tv_sb);
		 
		 this.total = flag[0] + flag[1] + flag[2];
		 if(total > 3){//赢
			 int yCount = 0;
			 for(int i : flag){
				 if(i==2){
					 stars[yCount].setImageResource(R.drawable.pic_smallstar_had);
					 yCount++; 
				 }
			 }
			 
			 if(yCount==3){
				 iv_state.setImageResource(R.drawable.pic_winmore);
			 }else if(yCount==2){
				 iv_state.setImageResource(R.drawable.pic_win);
			 }else{
				 iv_state.setImageResource(R.drawable.pic_winless);
			 }
			 
			 int m = (int) (money*0.9);
			 tv_sb.setText("+"+m);
		 }else if(total < 3){//shu
			 int sCount = 0;
			 for(int i : flag){
				 if(i==0){
					 sCount++; 
				 }
			 }
			 
			 if(sCount==3){
				 iv_state.setImageResource(R.drawable.pic_losemore);
			 }else if(sCount==2){
				 iv_state.setImageResource(R.drawable.pic_lose);
			 }else{
				 iv_state.setImageResource(R.drawable.pic_loseless);
			 }
			 
			 tv_sb.setText("+"+0);
		 }else{
			 iv_state.setImageResource(R.drawable.pic_same);
			 tv_sb.setText("+"+0);
		 }
		 
		 CircularImage dl_iv_head = (CircularImage) window.findViewById(R.id.dl_iv_head);
		 TextView dl_tv_name = (TextView) window.findViewById(R.id.dl_tv_name);
		 Button dl_bt_ll = (Button) window.findViewById(R.id.dl_bt_ll);
		 Button dl_bt_tz = (Button) window.findViewById(R.id.dl_bt_tz);
		 dl_bt_ll.setOnClickListener(this);
		 dl_bt_tz.setOnClickListener(this);
		 dl_iv_head.setOnClickListener(this);
		 
		 String headImg = model.getHeadImg();
		 String nickName = model.getNickName();
		 imageLoader.displayImage(headImg, dl_iv_head, options);
		 dl_tv_name.setText(nickName);
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.ib_close:
				if(isFromChat)
					 handler.post(mt5);
				else{
					setResult(Activity.RESULT_OK);
					this.finish();
				}
				break;
			case R.id.dl_bt_ll:
				// 跟ta聊聊
				
				Stranger  stranger= MyApplication.getApp().getDaoSession().getStrangerDao().load(model.getUserId());
				if(stranger!=null){
					if(stranger.getState()!=1){
						stranger.setState(1);
						MyApplication.getApp().getDaoSession().getStrangerDao().update(stranger);
						lanuch();
					}else{
						lanuch();
					}
				}else{
					queryUserInfo(model.getUserId());
				}
				break;
			case R.id.dl_bt_tz:
				Intent intent = new Intent(this, Game_cq_tz.class);
				intent.putExtra("GameOpponentModel", model);
				startActivity(intent);
				setResult(Activity.RESULT_OK);
				this.finish();
				break;
			case R.id.dl_iv_head:
				Intent mVzone = new Intent(this, VzoneActivity.class);
				mVzone.putExtra("userId", model.getUserId());
				mVzone.putExtra("visitUserId",  UserManager.getInstance(this).getCurrentUser().getUserId());
				mVzone.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(mVzone);
				break;
		}
	}
	
	
	/**
	 * 启动到聊天页面
	 */
	private void lanuch() {
		// TODO Auto-generated method stub
		setResult(Activity.RESULT_OK);
		
		Intent mChatIntent = new Intent(this, ChatActivity.class);
		mChatIntent.putExtra("userId", model.getUserId());
		mChatIntent.putExtra("nickName", model.getNickName());
		mChatIntent.putExtra("isConnOfGame", true);
		mChatIntent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivity(mChatIntent);
		
		this.finish();
	}

	@Override
	public void onBackPressed() {
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(dlg!=null&&dlg.isShowing()){
			dlg.dismiss();
			dlg= null;
		}
	}
	
	/**
	 * t ta
	 * w wo
	 * 
	 * 0,1,2 石头，剪刀，布
	 * 
	 * @return 相对自己而言，012,输平赢
	 */
	private int compare(int t, int w){
		int result = 0;
		
		switch(t){
			case 0:
				if(w==0){
					result = 1;
				}else if(w==1){
					result = 0;
				}else{
					result = 2;
				}
				break;
			case 1:
				if(w==0){
					result = 2;
				}else if(w==1){
					result = 1;
				}else{
					result = 0;
				}
				break;
			case 2:
				if(w==0){
					result = 0;
				}else if(w==1){
					result = 2;
				}else{
					result = 1;
				}
				break;
		}
		return result;
	}
	
	private void queryUserInfo(String userId) {
		// TODO Auto-generated method stub
		JsonObject jo = new JsonObject();
		jo.addProperty("id", userId);
		BottleRestClient.post("queryUserInfo", this, jo, new AsyncHttpResponseHandler (){
			@Override
			public void onStart() {
				super.onStart();
				showDialog("正在初始化", "初始化失败", 20*1000);
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				closeDialog(mpDialog);
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
							 Stranger stranger = new Stranger();
							stranger.setUserId(userObj.getUserId());
							stranger.setCity(userObj.getCity());
							stranger.setDescript(userObj.getDescript());
							stranger.setHeadImg(userObj.getHeadImg());
							try {
								stranger.setIdentityType(userObj.getIdentityType());
							} catch (Exception e) {
								// TODO: handle exception
							}
							stranger.setNickName(userObj.getNickName());
							stranger.setProvince(userObj.getProvince());
							stranger.setState(1);
							stranger.setIsVIP(userObj.getIsVIP());
							MyApplication.getApp().getDaoSession().insertOrReplace(stranger); 
							
							lanuch();
						 }
					 }
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				closeDialog(mpDialog);
			}
        });
	}
}