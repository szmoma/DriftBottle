package com.hnmoma.driftbottle.custom;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.model.PropsModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 兑换对话框
 * @author Administrator
 *
 */
public class BuyDialog extends Dialog {
	Context context;
	ImageView iv_head;
	TextView tv_ts;
	Button bt_01;
	Button bt_02;
	String text;
	
	private static ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	PropsModel djModel;

	public BuyDialog(Context context, String text) {
		super(context);
		this.context = context;
		this.text = text;
	}

	public BuyDialog(Context context, int theme, String text) {
		super(context, theme);
		this.context = context;
		this.text = text;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dialog_buy);
		this.setCancelable(true);
		this.setCanceledOnTouchOutside(false);
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)         //加载开始默认的图片      
        .showImageForEmptyUri(R.drawable.defalutimg)     //url爲空會显示该图片，自己放在drawable里面的
        .showImageOnFail(R.drawable.defalutimg)                //加载图片出现问题，会显示该图片
        .cacheInMemory(true)                                               //缓存用
        .cacheOnDisc(true)                                                    //缓存用
        .considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
        .build();
		
		iv_head = (ImageView) findViewById(R.id.iv_head);
		tv_ts = (TextView) findViewById(R.id.tv_ts);
		
		findViewById(R.id.bt_01).setOnClickListener(new Button.OnClickListener(){
			public void onClick(View view) {
				BuyDialog.this.cancel();
			}
		});
		
		bt_02 = (Button) findViewById(R.id.bt_02);
		bt_02.setText(text);
	}
	
	public void setOnSubmitClick(Button.OnClickListener clc){
		bt_02.setOnClickListener(clc);
	}

	public void setContent(String icon, String msg) {
		tv_ts.setText(msg);
		if(!TextUtils.isEmpty(icon)){
			imageLoader.displayImage(icon, iv_head, options);
		} else {
			iv_head.setVisibility(View.GONE);
		}
	}
}