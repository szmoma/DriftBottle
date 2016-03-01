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

public class BuyLwDialog extends Dialog {
	Context context;
	ImageView iv_head;
	TextView tv_ts;
	TextView tv_lwname;
	String submitText;
	Button bt_01;
	Button bt_02;
	
	private static ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	PropsModel djModel;

	public BuyLwDialog(Context context, String submitText) {
		super(context);
		this.context = context;
		this.submitText = submitText;
	}

	public BuyLwDialog(Context context, int theme, String submitText) {
		super(context, theme);
		this.context = context;
		this.submitText = submitText;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dialog_buylw);
		this.setCancelable(true);
		this.setCanceledOnTouchOutside(false);
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.transparent)         //加载开始默认的图片      
        .showImageForEmptyUri(R.drawable.transparent)     //url爲空會显示该图片，自己放在drawable里面的
        .showImageOnFail(R.drawable.transparent)                //加载图片出现问题，会显示该图片
        .cacheInMemory(true)                                               //缓存用
        .cacheOnDisc(true)                                                    //缓存用
        .considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
        .build();
		
		iv_head = (ImageView) findViewById(R.id.iv_head);
		tv_ts = (TextView) findViewById(R.id.tv_ts);
		tv_lwname = (TextView) findViewById(R.id.tv_lwname);
		
		findViewById(R.id.bt_01).setOnClickListener(new Button.OnClickListener(){
			public void onClick(View view) {
				BuyLwDialog.this.cancel();
			}
		});
		
		bt_02 = (Button) findViewById(R.id.bt_02);
		bt_02.setText(submitText);
	}
	
	public void setOnSubmitClick(Button.OnClickListener clc){
		bt_02.setOnClickListener(clc);
	}

	/**
	 * 名字，头像,价格,备注,魅力
	 * @param msgs
	 */
	public void setContent(String [] msgs) {
		imageLoader.displayImage(msgs[1], iv_head, options);
		tv_lwname.setText(msgs[0]);
//		tv_author.setText(Html.fromHtml("<font color=#BCBCBC>作者：</font>" + "<font color=#25698B>" + cartoonModel.getAuthor() + "</font>"));
		String remark = msgs[3];
		remark = TextUtils.isEmpty(remark) ?"":remark;
		tv_ts.setText("价格:"+msgs[2]+"扇贝\n"+remark+"\n魅力:+"+msgs[4]);
	}
}