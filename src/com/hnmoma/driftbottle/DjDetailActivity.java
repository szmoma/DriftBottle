package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.BuyDialog;
import com.hnmoma.driftbottle.model.BuyPropertyBModel;
import com.hnmoma.driftbottle.model.PropsModel;
import com.hnmoma.driftbottle.model.QueryShanbeiBModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 道具详情
 *
 */
public class DjDetailActivity extends BaseActivity {

	private static final int REQ_CODE_ONE = 100;//12或20个月的服务

	private static final int REQ_CODE_TWO = 101;//6或10个月的服务

	private static final int REQ_CODE_THREE = 103;	//3或5个月的服务

	private static final int REQ_CODE_FOUR = 104;	//1个月的服务
	
	BuyDialog dialog;
	PropsModel propsModel;

	ImageView iv_dj;
	TextView tv_djName;
	TextView tv_djCount;
	TextView tv_djDesc;

	// 道具价格表
	ImageView iv_01;
	TextView tv_jg01;
	TextView tv_jgdesc01;
	ImageView iv_02;
	TextView tv_jg02;
	TextView tv_jgdesc02;
	ImageView iv_03;
	TextView tv_jg03;
	TextView tv_jgdesc03;
	ImageView iv_04;
	TextView tv_jg04;
	TextView tv_jgdesc04;

	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;

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
				.showImageOnFail(R.drawable.defalutimg).cacheInMemory(true)
				.cacheOnDisc(true).considerExifParams(true)
				// .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.bitmapConfig(Bitmap.Config.RGB_565)
				// .displayer(new RoundedBitmapDisplayer(5))
				.build();

		if (savedInstanceState != null) {
			propsModel = (PropsModel) savedInstanceState
					.getSerializable("propsModel");
		}

		Intent intent = getIntent();
		if (intent != null) {
			propsModel = (PropsModel) intent.getSerializableExtra("propsModel");
		}

		if (propsModel == null) {
			this.finish();
		}

		initView();
		initData();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, responseCode, data);
		if(responseCode==Activity.RESULT_OK){
			switch (requestCode) {
			case REQ_CODE_ONE:
				if(propsModel.getUseType() == 0){
					showBuyDialog(propsModel, jiage01, 12);
				}else{
					showBuyDialog(propsModel, jiage01, 20);
				}
				break;
			case REQ_CODE_TWO:
				if(propsModel.getUseType() == 0){
					showBuyDialog(propsModel, jiage02, 6);
				}else{
					showBuyDialog(propsModel, jiage02, 10);
				}
				break;
			case REQ_CODE_THREE:
				if(propsModel.getUseType() == 0){
					showBuyDialog(propsModel, jiage02, 3);
				}else{
					showBuyDialog(propsModel, jiage02, 5);
				}
				break;	
				
			case REQ_CODE_FOUR:
				showBuyDialog(propsModel, jiage04, 1);
				break;	
			default:
				break;
			}
		}
	}

	private void initView() {
		setContentView(R.layout.activity_djdetail);
		iv_dj = (ImageView) findViewById(R.id.iv_dj);
		tv_djName = (TextView) findViewById(R.id.tv_djName);
		tv_djCount = (TextView) findViewById(R.id.tv_djCount);
		tv_djDesc = (TextView) findViewById(R.id.tv_djDesc);

		// 道具价格表
		iv_01 = (ImageView) findViewById(R.id.iv_01);
		tv_jg01 = (TextView) findViewById(R.id.tv_jg01);
		tv_jgdesc01 = (TextView) findViewById(R.id.tv_jgdesc01);
		iv_02 = (ImageView) findViewById(R.id.iv_02);
		tv_jg02 = (TextView) findViewById(R.id.tv_jg02);
		tv_jgdesc02 = (TextView) findViewById(R.id.tv_jgdesc02);
		iv_03 = (ImageView) findViewById(R.id.iv_03);
		tv_jg03 = (TextView) findViewById(R.id.tv_jg03);
		tv_jgdesc03 = (TextView) findViewById(R.id.tv_jgdesc03);
		iv_04 = (ImageView) findViewById(R.id.iv_04);
		tv_jg04 = (TextView) findViewById(R.id.tv_jg04);
		tv_jgdesc04 = (TextView) findViewById(R.id.tv_jgdesc04);
	}

	int jiage01;
	int jiage02;
	int jiage03;
	int jiage04;

	private void initData() {
		imageLoader.displayImage(propsModel.getShortPic(), iv_dj, options);
		tv_djName.setText(propsModel.getProName());
		tv_djCount.setText(Html.fromHtml("已有<font color=#3ac5ff>"
				+ propsModel.getBuyNum() + "</font>人购买"));
		tv_djDesc.setText(propsModel.getDescript());

		// 道具价格表
		if (propsModel.getUseType() == 0) {
			// 12
			jiage01 = (int) ((propsModel.getPrice() - propsModel.getPrice() * 0.3) * 12);
			iv_01.setImageResource(R.drawable.djxq08);
			tv_jg01.setText(jiage01 + "扇贝");
			tv_jgdesc01.setText("平均"+ (propsModel.getPrice() - propsModel.getPrice() * 0.3)	+ "扇贝/月 省30%");
			// 6
			jiage02 = (int) ((propsModel.getPrice() - propsModel.getPrice() * 0.2) * 6);
			iv_02.setImageResource(R.drawable.djxq07);
			tv_jg02.setText(jiage02 + "扇贝");
			tv_jgdesc02.setText("平均"
					+ (propsModel.getPrice() - propsModel.getPrice() * 0.2)+ "扇贝/月 省20%");
			// 3
			jiage03 = (int) ((propsModel.getPrice() - propsModel.getPrice() * 0.1) * 3);
			iv_03.setImageResource(R.drawable.djxq06);
			tv_jg03.setText(jiage03 + "扇贝");
			tv_jgdesc03.setText("平均"
					+ (propsModel.getPrice() - propsModel.getPrice() * 0.1)
					+ "扇贝/月 省10%");
			// 1
			jiage04 = propsModel.getPrice();
			iv_04.setImageResource(R.drawable.djxq05);
			tv_jg04.setText(jiage04 + "扇贝");
			tv_jgdesc04.setText("平均" + propsModel.getPrice() + "扇贝/月");
		} else {
			// 20
			jiage01 = (int) ((propsModel.getPrice() - propsModel.getPrice() * 0.3) * 20);
			iv_01.setImageResource(R.drawable.djxq04);
			tv_jg01.setText(jiage01 + "扇贝");
			tv_jgdesc01.setText("平均"
					+ (propsModel.getPrice() - propsModel.getPrice() * 0.3)+ "扇贝/个 省30%");
			// 10
			jiage02 = (int) ((propsModel.getPrice() - propsModel.getPrice() * 0.2) * 10);
			iv_02.setImageResource(R.drawable.djxq03);
			tv_jg02.setText(jiage02 + "扇贝");
			tv_jgdesc02.setText("平均"
					+ (propsModel.getPrice() - propsModel.getPrice() * 0.2)
					+ "扇贝/个 省20%");
			// 5
			jiage03 = (int) ((propsModel.getPrice() - propsModel.getPrice() * 0.1) * 5);
			iv_03.setImageResource(R.drawable.djxq02);
			tv_jg03.setText(jiage03 + "扇贝");
			tv_jgdesc03.setText("平均"
					+ (propsModel.getPrice() - propsModel.getPrice() * 0.1)
					+ "扇贝/个 省10%");
			// 1
			jiage04 = propsModel.getPrice();
			iv_04.setImageResource(R.drawable.djxq01);
			tv_jg04.setText(jiage04 + "扇贝");
			tv_jgdesc04.setText(propsModel.getPrice() + "扇贝/个");
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				this.finish();
				break;
			case R.id.bt_01:
				if(UserManager.getInstance(this).getCurrentUser()==null){
					launch(LoginActivity.class,REQ_CODE_ONE);
					return ;
				}
				if(propsModel.getUseType() == 0){
					showBuyDialog(propsModel, jiage01, 12);
				}else{
					showBuyDialog(propsModel, jiage01, 20);
				}
				break;
			case R.id.bt_02:
				if(UserManager.getInstance(this).getCurrentUser()==null){
					launch(LoginActivity.class,REQ_CODE_TWO);
					return ;
				}
				if(propsModel.getUseType() == 0){
					showBuyDialog(propsModel, jiage02, 6);
				}else{
					showBuyDialog(propsModel, jiage02, 10);
				}
				break;
			case R.id.bt_03:
				if(UserManager.getInstance(this).getCurrentUser()==null){
					launch(LoginActivity.class,REQ_CODE_THREE);
					return ;
				}
				if(propsModel.getUseType() == 0){
					showBuyDialog(propsModel, jiage03, 3);
				}else{
					showBuyDialog(propsModel, jiage03, 5);
				}
				break;
			case R.id.bt_04:
				if(UserManager.getInstance(this).getCurrentUser()==null){
					launch(LoginActivity.class,REQ_CODE_FOUR);
					return ;
				}
				showBuyDialog(propsModel, jiage04, 1);
				break;
		}
	}

	// TODO
	private void toRechargeDialog() {
		final BuyDialog dialogToBuy = new BuyDialog(this,
				R.style.style_dialog_ballon, "确定");
		dialogToBuy.show();
		dialogToBuy.setContent(propsModel.getShortPic(), "哇哦，扇贝不足，请先充值");
		dialogToBuy.setOnSubmitClick(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialogToBuy.dismiss();
				;
				Intent intent = new Intent(DjDetailActivity.this,RechargeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent);
			}
		});
	}

	private void buyProperty(String proId, int num) {
		// buy道具
		JsonObject jso = new JsonObject();
		jso.addProperty("userId", UserManager.getInstance(this)	.getCurrentUserId());
		jso.addProperty("proId", proId);
		jso.addProperty("num", num);
		BottleRestClient.post("buyProperty", this, jso,new AsyncHttpResponseHandler() {
					@Override
					public void onStart() {
						super.onStart();
						showDialog("购买中...","购买失败...",15*1000);
					}

					@Override
					public void onFinish() {
						super.onFinish();
						closeDialog(mpDialog);
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							BuyPropertyBModel baseModel = gson.fromJson(str,	BuyPropertyBModel.class);
							if (baseModel != null
									&& !TextUtils.isEmpty(baseModel.getCode())) {
								if ("0".equals(baseModel.getCode())) {
									showMsg("购买成功");
									MyApplication.getApp().getSpUtil()	.setMyMoney(baseModel.getShanbei());
									MyApplication.getApp().getSpUtil().setBrush(1);

									Intent intent = new Intent();
									intent.putExtra("resultMoney",
											baseModel.getShanbei());
									setResult(RESULT_OK, intent);

									DjDetailActivity.this.finish();
								} else {
									showMsg(baseModel.getMsg());
									MyApplication.getApp().getSpUtil()
											.setMyMoney(baseModel.getShanbei());
								}
							} else {
								showMsg("购买失败");
							}
						} else {
							showMsg("购买失败");
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						showMsg("服务器繁忙，提交失败");
					}
				});
	}
	/**
	 * 
	 * @param props 购买的道具
	 * @param price 道具价格
	 * @param num 道具数量
	 */
	private void showBuyDialog(final PropsModel props,final int price,final int num){
		if(props==null&&UserManager.getInstance(this).getCurrentUser()==null){
			showMsg("提交失败");
			return ;
		}
		if(dialog==null){
			dialog = new BuyDialog(DjDetailActivity.this, R.style.style_dialog_ballon, "确定"); 
			dialog.show();  
			dialog.setOnSubmitClick(new Button.OnClickListener(){
				public void onClick(View arg0) {
					if(MyApplication.getApp().getSpUtil().getMyMoney()>=price){
						buyProperty(propsModel.getProId(), num);
					}else{
						queryMoney(propsModel.getProId(),price,num);
					}
					dialog.cancel();
				}
			});
		}else{
			dialog.show();  
		}
		dialog.setContent(props.getShortPic(), "本次消费："+price+"扇贝");
	}
	
	private void launch(Class clazz,int requestCode){
		Intent intent = new Intent(this,clazz);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivityForResult(intent, requestCode);
	}
	
	//查询余额
	private void queryMoney(final String id,final int price,final int num){
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		BottleRestClient.post("queryShanbei", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryShanbeiBModel baseModel = gson.fromJson(str, QueryShanbeiBModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							MyApplication.getApp().getSpUtil().setMyMoney(baseModel.getShanbei());
							if(baseModel.getShanbei()>=price){
								buyProperty(propsModel.getProId(), num);
							}else{
								toRechargeDialog();
							}
						}
					}else{
//							showMsg("查询余额失败");
					}
				}else{
//						showMsg("查询余额失败");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//					showMsg("查询余额失败");
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
			}
        });
	}

}