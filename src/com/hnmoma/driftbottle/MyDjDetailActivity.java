package com.hnmoma.driftbottle;

import java.text.SimpleDateFormat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hnmoma.driftbottle.custom.BuyDialog;
import com.hnmoma.driftbottle.model.PropsModel;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MyDjDetailActivity extends BaseActivity{
	
	BuyDialog dialog;
	PropsModel propsModel;
	
	ImageView iv_dj;
	TextView tv_djName;
	TextView tv_djCount;
	TextView tv_djDesc;
	
	Button bt_dj;
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");  
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("propsModel", propsModel);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)
		.showImageForEmptyUri(R.drawable.defalutimg)
		.showImageOnFail(R.drawable.defalutimg)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
//		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565)
//		.displayer(new RoundedBitmapDisplayer(5))
		.build();
		
		if (savedInstanceState != null){
			propsModel = (PropsModel) savedInstanceState.getSerializable("propsModel");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			propsModel = (PropsModel) intent.getSerializableExtra("propsModel");
		}
		
		if(propsModel==null){
			this.finish();
		}
		
		MoMaLog.d("道具信息", propsModel.getProName()+" ,"+propsModel.getType()+" ,"+propsModel.getUseType());
		
		initView();
		initData();
	}
	
	private void initView(){
		setContentView(R.layout.activity_mydjdetail);
		iv_dj = (ImageView) findViewById(R.id.iv_dj);
		tv_djName = (TextView) findViewById(R.id.tv_djName);
		tv_djCount = (TextView) findViewById(R.id.tv_djCount);
		tv_djDesc = (TextView) findViewById(R.id.tv_djDesc);
		bt_dj = (Button) findViewById(R.id.bt_dj);
		
	}
	
	private void initData(){
		imageLoader.displayImage(propsModel.getShortPic(), iv_dj, options);
		tv_djName.setText(propsModel.getProName());
		tv_djDesc.setText(propsModel.getDescript());
		
		if(propsModel.getUseType() == 0){
			String date = format.format(propsModel.getEndTime());
			tv_djCount.setText(date+"到期");
		}else{
			tv_djCount.setText("剩余："+propsModel.getNum()+"个");
		}
		
		//道具实际类型
		//如果为1000是定向瓶子
//		if(PropUtil.canUsedInMyprops(propsModel.getType())){
//			bt_dj.setVisibility(View.VISIBLE);
//		}else{
//			bt_dj.setVisibility(View.GONE);
//		}
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				this.finish();
				break;
			case R.id.bt_dj:
				long temp = MyApplication.getApp().getSpUtil().getSpqOkTime();
				if(temp < 1000*60*5){
					showMsg("雅灭蝶，让刷瓶器休息"+Math.ceil(((1000*60*5-temp)/60000))+"分钟吧~");
				}else{
					doDj(v);
				}
				break;
		}
	}
	
	private void doDj(View v){
		if(propsModel.getType().equals("1001")){
			v.setEnabled(false);
			//领取奖励,更新入库，刷新
			MyApplication.isPending = true;
			MyApplication.getApp().getSpUtil().setOldBottle(10);
			showMsg("瓶子已经刷新");
			MyApplication.getApp().getSpUtil().updateSpqRefreshTime();
			
			//跳到主页面
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("flag", 1);
			startActivity(intent);
		}else{
			showMsg("版本过低，无法使用道具，请升级");
		}
	}
	
}