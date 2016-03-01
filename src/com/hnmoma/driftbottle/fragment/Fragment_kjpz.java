package com.hnmoma.driftbottle.fragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hnmoma.driftbottle.LoginActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.VzoneActivity;
import com.hnmoma.driftbottle.VzoneTousuActivity;
import com.hnmoma.driftbottle.adapter.KjpzPhotoAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.AnimRelativeLayout;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.ActionNumModel;
import com.hnmoma.driftbottle.model.Attachment;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.CommentModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
import com.hnmoma.driftbottle.model.TousuModel;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 空间瓶子
 * @author Administrator
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class Fragment_kjpz extends BaseFragment implements OnClickListener{
	
	BottleModel bottleModel;
	ActionNumModel actionNumModel;
	List<CommentModel>  commentModels;
	
	Button bt_left;
	Button bt_right;
	//TextView tv_complain;
	
	 GridView gv_photo;
	 KjpzPhotoAdapter photoAdapter;
	 protected ImageLoader imageLoader = ImageLoader.getInstance();
	 DisplayImageOptions options;
	 
	 CircularImage iv_userhead;
	 TextView tv_name;
	 TextView tv_nl;
	 TextView tv_xz;
	 TextView tv_qm;
	 TextView jg;
	 
//	 private BlurHelper mBlurHelper;
	 
	 // 瓶子扔掉动画
	 LinearLayout ll_first;
	 RelativeLayout rl_second;
	 AnimRelativeLayout chuck_bottle_layout;
	 AnimationSet set;
	 private ImageView chuck_spray1;
	 
	 long waitTime = 5000L;  
		long touchTime = 0L;

	public static BaseFragment newInstance(PickBottleModel model) {
		Fragment_kjpz fragment = new Fragment_kjpz();
		Bundle bundle = new Bundle();
		bundle.putSerializable("PickBottleModel", model);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.default_zone_head)
		.showImageForEmptyUri(R.drawable.default_zone_head)
		.showImageOnFail(R.drawable.default_zone_head)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fishing_kjpz, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle bundle = getArguments();
		PickBottleModel model = (PickBottleModel) bundle.getSerializable("PickBottleModel");
		
		bt_left = (Button) findViewById(R.id.bt_left);
		bt_right = (Button) findViewById(R.id.bt_right);
		
		bt_left.setOnClickListener(this);
		bt_right.setOnClickListener(this);
//		tv_complain = (TextView) findViewById(R.id.tv_complain);
//		tv_complain.setOnClickListener(this);
		
		gv_photo = (GridView) findViewById(R.id.gv_photo);
		
		iv_userhead = (CircularImage) findViewById(R.id.iv_userhead);
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_nl = (TextView) findViewById(R.id.tv_nl);
		tv_xz = (TextView) findViewById(R.id.tv_xz);
		tv_qm = (TextView) findViewById(R.id.tv_qm);
		jg = (TextView) findViewById(R.id.jg);
		
		ll_first = (LinearLayout) findViewById(R.id.ll_first);
		rl_second = (RelativeLayout) findViewById(R.id.rl_second);
		chuck_bottle_layout = (AnimRelativeLayout) findViewById(R.id.chuck_bottle_layout);
		chuck_spray1=(ImageView) findViewById(R.id.chuck_spray1);
		initAnimation();
		
		initDate(model);
	}
	
	@SuppressLint("NewApi")
	private void initDate(PickBottleModel model){
		bottleModel = model.getBottleInfo();
		UserInfoModel userInfo = bottleModel.getUserInfo();
		final String headImg = userInfo.getHeadImg();
		
		imageLoader.displayImage(headImg, iv_userhead, options);
		tv_name.setText(userInfo.getNickName());
		
		Drawable vipPic;
		if(bottleModel.getUserInfo().getIsVIP() == 0) {
			tv_name.setTextColor(getResources().getColor(R.color.white));
			vipPic = getResources().getDrawable(R.drawable.ic_vip_not);
		} else {
			tv_name.setTextColor(getResources().getColor(R.color.kjpz_username));
			vipPic = getResources().getDrawable(R.drawable.ic_vip);
		}
		vipPic.setBounds(0, 0, vipPic.getMinimumWidth(), vipPic.getMinimumHeight());
		tv_name.setCompoundDrawables(null, null, vipPic, null);
		//tv_name.setPadding(MoMaUtil.dip2px(act, 5), 0, MoMaUtil.dip2px(act, 5), 0);
		
		String xz = userInfo.getConstell();
		xz = TextUtils.isEmpty(xz)?"":xz;
		tv_xz.setText(xz);
		tv_nl.setText(userInfo.getAge()+"");
		
		String[] identity = new String[2];
		String idtype = userInfo.getIdentityType();
		if (TextUtils.isEmpty(idtype)) {
			identity[0] = "男";
			identity[1] = "m";
		} else {
			identity = getIdentityByCode(idtype);
		}
		
		Drawable drawable;
		if (identity[1].equals("m")) {
			tv_nl.setBackgroundResource(R.drawable.bg_sex_man);
			drawable = getResources().getDrawable(R.drawable.icon_male_32);
		} else {
			tv_nl.setBackgroundResource(R.drawable.bg_sex_female);
			drawable = getResources().getDrawable(R.drawable.icon_female_32);
		}
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv_nl.setCompoundDrawables(drawable, null, null, null);
		tv_nl.setPadding(MoMaUtil.dip2px(act, 5), 0, MoMaUtil.dip2px(act, 5), 0);
		
		tv_qm.setText(userInfo.getDescript());
		
		String province = userInfo.getProvince();
		province = TextUtils.isEmpty(province) ? "" : province;
		String city = userInfo.getCity();
		city = TextUtils.isEmpty(city) ? "" : city;
		if(TextUtils.isEmpty(province) && TextUtils.isEmpty(city)){
			jg.setVisibility(View.GONE);
		}else{
			jg.setText(province + " " + city);
		}
		
		initMyAdapter();
	}
	
	private String[] getIdentityByCode(String code) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("2000", "小萝莉");
		map.put("2001", "花样少女");
		map.put("2002", "知性熟女");
		map.put("2003", "小正太");
		map.put("2004", "魅力少年");
		map.put("2005", "成熟男生");
		map.put("2006", "男");
		map.put("2007", "女");
		String flag = map.get(code);
		map = null;

		String[] strs = new String[2];
		strs[0] = flag;
		// m-lan f-nv
		if (code.equals("2000") || code.equals("2001") || code.equals("2002") || code.equals("2007")) {
			strs[1] = "f";
		} else {
			strs[1] = "m";
		}

		return strs;
	}
	
	public void initMyAdapter(){
        ViewTreeObserver vto2 = gv_photo.getViewTreeObserver(); 
        vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
	        public void onGlobalLayout() { 
	        	gv_photo.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	        	int width = (gv_photo.getWidth() - MoMaUtil.dip2px(act, 32)) / 4;
	        	photoAdapter = new KjpzPhotoAdapter(imageLoader, width);
	    		gv_photo.setAdapter(photoAdapter);	
	    		
	    		List<Attachment> subList = bottleModel.getSubList();
	    		if(subList ==null||subList.isEmpty()){
	    			subList = new ArrayList<Attachment>();
	    		}
	    		int c = 4 - subList.size();
    			if(c>0){
    				if(c==4){
    					gv_photo.setVisibility(View.GONE);
    				}else{
    					for(int i = 0; i<c; i++){
        					Attachment at = new Attachment();
        					at.setAttachmentUrl("");
        					subList.add(at);
        				}
    					photoAdapter.addItemTop(subList);
    		    		photoAdapter.notifyDataSetChanged();
    				}
    				
    			}
	        } 
        }); 
	}
	
	@Override
	public boolean onBackPressed() {
//		act.finish();
		long currentTime = System.currentTimeMillis();
		 if((currentTime-touchTime)>=waitTime) {  
	           touchTime = currentTime;  
	           ready2Send();
		    }
		return true;
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.bt_left:
				long currentTime = System.currentTimeMillis();
				 if((currentTime-touchTime)>=waitTime) {  
			           touchTime = currentTime;  
			           ready2Send();
				    }
				break;
			case R.id.bt_right:
				String visitUserId = UserManager.getInstance(act).getCurrentUserId();
				Intent intent = new Intent(act, VzoneActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("identityflag", 1);
				intent.putExtra("userId", bottleModel.getUserInfo().getUserId());
				intent.putExtra("visitUserId", visitUserId);
				startActivity(intent);
//				act.finish();
				break;
			case R.id.tv_complain:
				if(UserManager.getInstance(act).getCurrentUser() == null) {
					Intent complain = new Intent(act, LoginActivity.class);
					complain.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivity(complain);
				} else {
					Intent jubao = new Intent(act, VzoneTousuActivity.class);
					jubao.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					TousuModel tm = new TousuModel();
					tm.setId(bottleModel.getBottleId());
					tm.setUserId(UserManager.getInstance(act).getCurrentUserId());
					tm.setbUserId(bottleModel.getUserInfo().getUserId());
					tm.setReportType(3);
					tm.setContent(bottleModel.getContent());
					jubao.putExtra("tousuModel", tm);
					startActivity(jubao);
				}
				break;
		}
	}
	
	public void ready2Send(){
        ll_first.setVisibility(View.GONE);
        showDialog("正在密封瓶子...", "密封失败...", 15*1000);
        rl_second.setVisibility(View.VISIBLE);
        chuck_bottle_layout.startAnimation(set);
        if(getActivity()!=null)
        getActivity().getSupportFragmentManager().popBackStackImmediate();//退栈
}

	private void initAnimation() {
		
		//适配
		DisplayMetrics dm = new DisplayMetrics();   
		act.getWindowManager().getDefaultDisplay().getMetrics(dm);   
		final int width = dm.widthPixels;   
		final int height = dm.heightPixels;  
		
		Animation animationR = AnimationUtils.loadAnimation(act, R.anim.bottle_throw1);
		Animation animationT = AnimationUtils.loadAnimation(act, R.anim.bottle_throw2);
		
		set = new AnimationSet(false);
		set.addAnimation(animationR);
		set.addAnimation(animationT);
		
		set.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if (mpDialog != null && mpDialog.isShowing()) {
					mpDialog.cancel();
				}
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				
				chuck_bottle_layout.setVisibility(View.GONE);
				
				int [] location = new int [2];  
				chuck_bottle_layout.getLocationOnScreen(location);  
				
				float x = location[0];  
				float y = location[1];
	//			//231*180
				x = (float) (x - width * 0.3);
				y = (float) (y - height * 0.5);
				
	//			chuck_spray1.setX(x);
	//			chuck_spray1.setY(y);
				
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) chuck_spray1.getLayoutParams();
				lp.leftMargin = (int) x; //Your X coordinate
				lp.topMargin = (int) y; //Your Y coordinate
				
				AnimationDrawable ad1 = (AnimationDrawable) chuck_spray1.getDrawable();
				chuck_spray1.setVisibility(View.VISIBLE);
				ad1.start();
				
				myHandler.postDelayed(mt, 450);
			}
		});
	}

	Handler myHandler = new Handler();
	MyThread mt = new MyThread();
	class MyThread implements Runnable{
		public void run() {
			chuck_spray1.setVisibility(View.GONE);
			if(mpDialog!=null){
				mpDialog.dismiss();
				mpDialog = null;
			}
			act.finish();
		}
	}
}
