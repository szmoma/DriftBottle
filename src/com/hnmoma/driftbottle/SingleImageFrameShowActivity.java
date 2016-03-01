package com.hnmoma.driftbottle;

import java.io.File;
import java.lang.ref.SoftReference;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.R.anim;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hnmoma.driftbottle.custom.BuyDialog;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
/**
 * 图片框--一张图片
 * @author Administrator
 *
 */
public class SingleImageFrameShowActivity extends BaseFrameShowActivity implements OnLongClickListener {
	String imageUrl;
	boolean isGif = false;
	
	TextView tv_title;
	
	protected GifImageView gifview;
	
	private PhotoViewAttacher mAttacher;
	private ProgressBar pbLoading;
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("imageUrl", imageUrl);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null){
			imageUrl = savedInstanceState.getString("imageUrl");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			imageUrl = intent.getStringExtra("imageUrl");
		}
		
		if(TextUtils.isEmpty(imageUrl)){
			this.finish();
			return;
		}
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.transparent)         //加载开始默认的图片      
        .showImageForEmptyUri(R.drawable.transparent)     //url爲空會显示该图片，自己放在drawable里面的
        .showImageOnFail(R.drawable.transparent)                //加载图片出现问题，会显示该图片
        .cacheInMemory(true)                                               //缓存用
        .cacheOnDisc(true)                                                    //缓存用
        .considerExifParams(true)
        .imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
        .build();
		
		initView();
		setGifFile(imageUrl);
	}
	
	private void initView(){
		setContentView(R.layout.activity_singleimageframeshow);
		tv_title = (TextView) findViewById(R.id.tv_title);
		pbLoading = (ProgressBar)findViewById(R.id.loading);
		pbLoading .setVisibility(View.GONE);
		//gif
        gifview = (GifImageView) findViewById(R.id.gifview);
        gifview.setOnLongClickListener(this);
        mAttacher = new PhotoViewAttacher(gifview);
        mAttacher.setOnLongClickListener(this);
	}

	private void setGifFile(final String url){
		imageLoader.loadImage(url, null, options, new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				pbLoading.setVisibility(View.VISIBLE);
				pbLoading.setProgress(10);
				gifview.setVisibility(View.GONE);
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
				pbLoading.setVisibility(View.GONE);
			}
			
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				File file = DiscCacheUtil.findInCache(imageUri, imageLoader.getDiscCache());
				if(file != null && file.exists()){
					setGifFile(file);
				}
				
				gifview.setVisibility(View.VISIBLE);
				pbLoading.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				imageLoader.loadImage(url, options, this);
			}
		}, new ImageLoadingProgressListener() {
			@Override
			public void onProgressUpdate(String imageUri, View view, int current, int total) {
				pbLoading.setProgress(Math.round(100.0f * current / total));
			}
		});
	}
	/**
	 * 设置显示gift图片
	 * @param file
	 */
	private void setGifFile(File file){
		if(gifview!=null&&file!=null){
			try {
				GifDrawable gifDrawable=new GifDrawable(file);
				gifview.setImageDrawable(gifDrawable);
				isGif = true;
			} catch (Exception e) {
				//mAttacher = new PhotoViewAttacher(gifview);
				if(android.os.Build.VERSION.SDK_INT<=android.os.Build.VERSION_CODES.KITKAT)
					System.gc();            
				showGeneralPicture(file);
			}

		}
	}
	/**
	 * 显示普通照片
	 * @param file 图片文件
	 */
	private void showGeneralPicture(File file) {
		// TODO Auto-generated method stub
		isGif = false;
		if(!TextUtils.isEmpty(file.getPath())){
//			imageLoader.displayImage(("file://"+file.getPath()), gifview,options);
			
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(file.getPath(), opts);
			opts.inSampleSize = MoMaUtil.computeSampleSize(opts, -1, 1024*1024);
			opts.inJustDecodeBounds = false;
			Bitmap bm = null;
			try{
				if(opts.outHeight>opts.outWidth){
					mAttacher.setScaleType(ScaleType.CENTER_CROP);
				}else{
					mAttacher.setScaleType(ScaleType.FIT_CENTER);
				}
				bm = BitmapFactory.decodeFile(file.getPath(), opts);
				gifview.setImageBitmap(bm);
				mAttacher.update();
			}catch(OutOfMemoryError er){
				bm.recycle();
				bm = null;
				showMsg("图片加载失败");
			}
		}
	}

	public void onClick(View v) {
 		switch(v.getId()){
			case R.id.bt_back:
				onBackPressed();
				break;
			case R.id.bt_share:
				break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onLongClick(View v) {
		//TODO  保存图片到本地
		final BuyDialog dialog = new BuyDialog(this, R.style.style_dialog_ballon, "保存");
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
				savetoLocal(imageUrl, imageLoader, isGif);
				dialog.dismiss();
			}
		});
		
		return true;
	}
}