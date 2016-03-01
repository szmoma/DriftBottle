package com.hnmoma.driftbottle;

import java.io.File;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class AttachImageActivity extends BaseActivity {
	
	TextView tv_title;
	
	protected GifImageView gifview;
	
	private PhotoViewAttacher mAttacher;
	
	String imagePath;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("imagePath", imagePath);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null){
			imagePath = savedInstanceState.getString("imagePath");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			imagePath = intent.getStringExtra("imagePath");
		}
		
		if(TextUtils.isEmpty(imagePath)){
			this.finish();
			return;
		}
		
		initView();
		
		setGifFile(new File(imagePath));
	}
	
	private void initView(){
		setContentView(R.layout.activity_attach_image);
		tv_title = (TextView) findViewById(R.id.tv_title);
		
		//gif
        gifview = (GifImageView) findViewById(R.id.gifview);
        mAttacher = new PhotoViewAttacher(gifview);
	}
	
	private void setGifFile(File file){
		if(gifview!=null && file!=null){
			try {
				GifDrawable gifDrawable=new GifDrawable(file);
				gifview.setImageDrawable(gifDrawable);
			} catch (Exception e) {
				gifview.setImageURI(Uri.parse(file.getPath()));
				
				if(!TextUtils.isEmpty(imagePath)){
					BitmapFactory.Options opts = new BitmapFactory.Options();
	    			opts = new BitmapFactory.Options();
	    			opts.inJustDecodeBounds = true;
	    			BitmapFactory.decodeFile(imagePath, opts);
					
					if(opts.outHeight>opts.outWidth){
						mAttacher.setScaleType(ScaleType.CENTER_CROP);
					}else{
						mAttacher.setScaleType(ScaleType.FIT_CENTER);
					}
				}
				
				mAttacher.update();
			}
		}
	}
	
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.bt_back:
				onBackPressed();
				break;
			case R.id.bt_del:
				setResult(RESULT_FIRST_USER);
				this.finish();
				break;
		}
	}
}