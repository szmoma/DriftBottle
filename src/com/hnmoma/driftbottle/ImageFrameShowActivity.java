package com.hnmoma.driftbottle;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hnmoma.driftbottle.custom.BuyDialog;
import com.hnmoma.driftbottle.custom.ImageCountTextView;
import com.hnmoma.driftbottle.model.Attachment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
/**
 * 图片墙:多张图片
 * @author Administrator
 *
 */
public class ImageFrameShowActivity extends BaseFrameShowActivity implements OnClickListener{
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	private ImageCountTextView image_browser_text;
	List<Attachment> list;
	
	private int currentIndex = 0;
	private int total;
	
	private PhotoViewAttacher mAttacher;
	ProgressBar pb_loading;	//加载图片的进度
	PhotoView mImage;
	
	LinearLayout ll_bt;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("imageUrls", (Serializable)list);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null){
			list = (List<Attachment>) savedInstanceState.getSerializable("imageUrls");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			list = (List<Attachment>) intent.getSerializableExtra("imageUrls");
		}
		
		if(list == null || list.size()==0){
			this.finish();
			return;
		}
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.transparent)         //加载开始默认的图片      
        .showImageForEmptyUri(R.drawable.transparent)     //url爲空會显示该图片，自己放在drawable里面的
        .showImageOnFail(R.drawable.transparent)                //加载图片出现问题，会显示该图片
        .cacheInMemory(false)                                               //缓存用
        .cacheOnDisc(true)                                                    //缓存用
        .considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
        .build();
		
		initView();
		initData();
	}
	
	private void initView(){
		setContentView(R.layout.activity_imageframeshow);
		image_browser_text = (ImageCountTextView) findViewById(R.id.image_browser_text);
		findViewById(R.id.bt_pre).setOnClickListener(this);
		findViewById(R.id.bt_next).setOnClickListener(this);
		
		ll_bt = (LinearLayout) findViewById(R.id.ll_btn);
		
		//imagezoom
		mImage = (PhotoView) findViewById(R.id.image);
		mAttacher = new PhotoViewAttacher(mImage);
		mAttacher.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				//TODO
				final BuyDialog dialog = new BuyDialog(ImageFrameShowActivity.this, R.style.style_dialog_ballon, "确定");
				dialog.show();
				
				WindowManager windowManager = getWindowManager();
				Display display = windowManager.getDefaultDisplay();
				WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
				lp.width = (int)(display.getWidth()); //设置宽度
				dialog.getWindow().setAttributes(lp);
				
				dialog.setContent(null, "保存图片到手机");
				dialog.setOnSubmitClick(new Button.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						showMsg("正在保存");
						Uri filename = Uri.parse(list.get(currentIndex).getAttachmentUrl());
						savetoLocal(filename.toString(), imageLoader, false);
						dialog.dismiss();
					}
				});
				
				return true;
			}
		});
		pb_loading = (ProgressBar)findViewById(R.id.loading);
	}
	
	private void initData(){
		total = list.size();
		
		if(total<=1){
			ll_bt.setVisibility(View.GONE);
		}
		
		updateChange();
	}
	
	/* 图片地址 */
    File imageUri;
	private void setFile(final String url){
		imageLoader.loadImage(url, null, options, new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				pb_loading.setVisibility(View.VISIBLE);
				pb_loading.setProgress(0);
				mImage.setVisibility(View.GONE);
			}
			
			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				String message = null;
				switch (failReason.getType()) {
					case IO_ERROR:
						message = "服务器繁忙";
						break;
					case DECODING_ERROR:
						message = "Image can't be decoded";
						break;
					case NETWORK_DENIED:
						message = "Downloads are denied";
						break;
					case OUT_OF_MEMORY:
						message = "Out Of Memory error";
						break;
					case UNKNOWN:
						message = "Unknown error";
						break;
				}
				showMsg(message);
				pb_loading.setVisibility(View.GONE);
			}
			
			@Override
			public void onLoadingComplete(final String uri, View view, final Bitmap loadedImage) {
				if(loadedImage != null){
					if(loadedImage.getHeight()>loadedImage.getWidth()){
						mAttacher.setScaleType(ScaleType.CENTER_CROP);
					}else{
						mAttacher.setScaleType(ScaleType.FIT_CENTER);
					}        
				}
				
				File file = DiscCacheUtil.findInCache(uri, imageLoader.getDiscCache());
				if(file != null && file.exists()){
					imageUri = file;
					
					mImage.setImageBitmap(loadedImage);
					mAttacher.update();
					
					mImage.setVisibility(View.VISIBLE);
					pb_loading.setVisibility(View.GONE);
				}else{
					showMsg("服务器繁忙");
				}
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				imageLoader.loadImage(url, options, this);
			}
		}, new ImageLoadingProgressListener() {
			@Override
			public void onProgressUpdate(String imageUri, View view, int current, int total) {
				pb_loading.setProgress(Math.round(100.0f * current / total));
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.bt_back:
				onBackPressed();
				break;
			case R.id.bt_pre:
				if(currentIndex>0){
					currentIndex--;
					updateChange();
				}
				break;
			case R.id.bt_next:
				if(currentIndex<total-1){
					currentIndex++;
					updateChange();
				}
				break;
			
		}
	}
	
	public void updateChange(){
		image_browser_text.setText((currentIndex+1)+"/"+total);
		String url = list.get(currentIndex).getAttachmentUrl();
		
		setFile(url);
	}
}