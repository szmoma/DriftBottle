package com.hnmoma.driftbottle;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.chat.EMContactManager;
import com.easemob.exceptions.EaseMobException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CustomDialog;
import com.hnmoma.driftbottle.custom.CustomDialog.CustomDialogItemClickListener;
import com.hnmoma.driftbottle.custom.ImageCountTextView;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.PhotoWallModel;
import com.hnmoma.driftbottle.model.PicModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
/**
 * 查看图片---图片墙
 * @author Administrator
 *
 */
public class PhotoWallShowActivity extends BaseFrameShowActivity implements OnClickListener{
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	private ImageCountTextView image_browser_text;
	List<PhotoWallModel> list;
	
	
	private int currentIndex = 0;
	int identityflag;
	private int total;
	private String userId;
	String url;
	
	private PhotoViewAttacher mAttacher;
	ProgressBar pb_loading;	//记载图片的进度
	PhotoView mImage;
	
	RelativeLayout rl_bottom;
	Button btn_gn;
	TextView tv_desc;
	TextView tv_mylove;
	/**
	 * false没删图片，true为删除过图片
	 */
	boolean delPhotoFlag;
	private TextView tvTitle;	//标题
	private boolean isPaging;	//分页处理
	
	private boolean[] state;//图片点赞的状态
	Map<String, WeakReference<Bitmap>> imagesCache = new HashMap<String, WeakReference<Bitmap>>();//软引用，防止OOM
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("identityflag", identityflag);
		outState.putSerializable("imageUrls", (Serializable)list);
		outState.putInt("position", currentIndex);
		outState.putString("userId", userId);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null){
			identityflag = savedInstanceState.getInt("identityflag");
			list = (List<PhotoWallModel>) savedInstanceState.getSerializable("imageUrls");
			currentIndex = savedInstanceState.getInt("position");
			userId = savedInstanceState.getString("userId");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			identityflag = intent.getIntExtra("identityflag", 0);
			list = (List<PhotoWallModel>) intent.getSerializableExtra("imageUrls");
			currentIndex = intent.getIntExtra("position", 0);
			userId = intent.getStringExtra("userId");
			isPaging = intent.getBooleanExtra("page", false);
		}
		
		if(list == null || list.size()==0){
			this.finish();
			return;
		}
		state = new boolean[list.size()];
		
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
		if(isPaging){
			loadMorePhoto(list.get(0).getPicId());
		}else
			initData();
	}
	
	private void loadMorePhoto(String picId) {
		// TODO Auto-generated method stub
		String uid = UserManager.getInstance(this).getCurrentUserId();
		JsonObject jo = new JsonObject();
		jo.addProperty("picId", "0");
		if(uid.equals(userId)){	//自己看自己
			jo.addProperty("userId", userId);
			jo.addProperty("visitUserId", "");
		}else{
			jo.addProperty("userId", userId);
			jo.addProperty("visitUserId", uid);
		}
		int num = MyApplication.getApp().getSpUtil().getPicNumLimit();
		if(num<=0){
			num = 20;
		}
		jo.addProperty("pageNum", num);
		BottleRestClient.post("queryPic", this, jo, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				showDialog("努力加载...","加载失败...",15*1000);
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					PicModel	 model = gson.fromJson(str, PicModel.class);
					if (model != null && !TextUtils.isEmpty(model.getCode())) {
						if ("0".equals(model.getCode())) {
							list = model.getPicList();
							if(state.length!=list.size())
								state = new boolean[list.size()];
							initData();
						} else {
							showMsg(model.getMsg());
						}
					} else {
					showMsg("服务器繁忙");
					}
				}
			}
	
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("sorry，出错了···");
				closeDialog(mpDialog);
			}
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				super.onFinish();
				closeDialog(mpDialog);
			}
		});
	}
	
	private void initView(){
		setContentView(R.layout.activity_photowallshow);
		image_browser_text = (ImageCountTextView) findViewById(R.id.image_browser_text);
		findViewById(R.id.bt_pre).setOnClickListener(this);
		findViewById(R.id.bt_next).setOnClickListener(this);
		findViewById(R.id.ib_show_gride).setOnClickListener(this);
		findViewById(R.id.bt_back).setOnClickListener(this);
		rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		pb_loading = (ProgressBar)findViewById(R.id.loading);
		
		btn_gn = (Button) findViewById(R.id.btn_gn);
		tv_desc = (TextView) findViewById(R.id.tv_desc);
		tv_mylove = (TextView) findViewById(R.id.tv_mylove);
		mImage = (PhotoView) findViewById(R.id.image);
		
		if(identityflag == 1) {//1表示看别人
			tvTitle.setText(getResources().getString(R.string.tv_album));
			tv_mylove.setVisibility(View.GONE);
		} else {
			tvTitle.setText(getResources().getString(R.string.tv_myAlbum));
			btn_gn.setBackgroundResource(R.drawable.selector_btn_pw_del);
			tv_mylove.setVisibility(View.VISIBLE);
			btn_gn.setVisibility(View.GONE);
		}
		
		//imagezoom
		mAttacher = new PhotoViewAttacher(mImage);
		
	
		mAttacher.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				//TODO
				
				if(identityflag==1)
					return true;
				
				String[] strarray = new String[]{getResources().getString(R.string.save_photo),getResources().getString(R.string.delete_photo), "取消"};
				new AlertDialog.Builder(PhotoWallShowActivity.this)
					.setTitle("图片操作")
					.setItems(strarray, new DialogInterface.OnClickListener() {
						     @Override
						     public void onClick(DialogInterface dialog, int which) {
						    	 if(which == 0){
						    		dialog.dismiss();
						    		savetoLocal(url, imageLoader, false);
						    	 }else if(which == 1){
						    		dialog.dismiss();
						    		delPhoto(list.get(currentIndex).getPicId());
						    	 }else if(which == 2){
						    		 dialog.dismiss();
						    	 }
						     }
					    })
					.show();
				
//				CustomDialog dlg = new CustomDialog();
//				final String[] items = {getResources().getString(R.string.save_photo),getResources().getString(R.string.delete_photo)};
//				dlg.showListDialog(PhotoWallShowActivity.this, "图片操作", items, new CustomDialogItemClickListener() {
//					
//					@Override
//					public void confirm(String result) {
//						// TODO Auto-generated method stub
//						if(getResources().getString(R.string.save_photo).equals(result)){
//							savetoLocal(url, imageLoader, false);
//						}else if(getResources().getString(R.string.delete_photo).equals(result)){
//							delPhoto(list.get(currentIndex).getPicId());
//						}
//					}
//				});
				
				return true;
			}
		});
	}
	
	private void initData(){
		total = list.size();
		if(total<=1){
			rl_bottom.setVisibility(View.GONE);
		}
		updateChange();
	}
	
	/* 图片地址 */
    File imageUri;
	private void setFile(final String url){
		WeakReference<Bitmap> imageCache = imagesCache.get(url);
		if(imageCache!=null){
			Bitmap bitmap = imageCache.get();
			if(bitmap!=null){
				mImage.setImageBitmap(bitmap);
				mAttacher.update();
				
				mImage.setVisibility(View.VISIBLE);
				pb_loading.setVisibility(View.GONE);
				return ;
			}
		}
		imageLoader.loadImage(url, null, options, new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				pb_loading.setVisibility(View.VISIBLE);
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
			public void onLoadingComplete(String uri, View view, Bitmap loadedImage) {
				if(loadedImage != null){
					if(loadedImage.getHeight()>loadedImage.getWidth()){
						mAttacher.setScaleType(ScaleType.CENTER_CROP);
					}else{
						mAttacher.setScaleType(ScaleType.FIT_CENTER);
					}
				}
				WeakReference<Bitmap> ref = new WeakReference<Bitmap>(loadedImage);
				imagesCache.put(url, ref);
				mImage.setImageBitmap(loadedImage);
				mAttacher.update();
				
				mImage.setVisibility(View.VISIBLE);
				pb_loading.setVisibility(View.GONE);
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
			case R.id.ib_show_gride:
				Intent intent = new Intent(this,AlbumActivity.class);
				intent.putExtra("resultList", (Serializable)list);
				intent.putExtra("load", true);
				intent.putExtra("identityflag", identityflag);
				intent.putExtra("userId", userId);
	        	startActivity(intent);
	        	
	        	if(delPhotoFlag){
	        		Intent result = new Intent();
	        		intent.putExtra("resultList", (Serializable)list);
	        		setResult(Activity.RESULT_OK,result);
	    		}
	        	
	        	finish();
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
			case R.id.btn_gn:
				if(identityflag == 0){//删除
					delPhoto(list.get(currentIndex).getPicId());
				}else{//点赞
					btn_gn.setClickable(false);
					if(!state[currentIndex]){
						lovePhoto(list.get(currentIndex).getPicId());
					}else
						showMsg("已赞");
				}
				break;
			
		}
	}
	
	private void delPhoto(String picId){
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("picId", picId);
		
		BottleRestClient.post("delPic", this, jo, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				super.onStart();
				showDialog("正在删除...","删除图片失败...",15*1000);
			}
	
			@Override
			public void onFinish() {
				super.onFinish();
				closeDialog(mpDialog);
			}
	
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				Gson gson = new Gson();
				BaseModel bm = gson.fromJson(str, BaseModel.class);
				if(bm.getCode().equals("0")){
					delPhotoFlag = true;
					
					list.remove(currentIndex);
					total = list.size();
					
					if(list.size()<=0){
						Intent intent = new Intent();
						intent.putExtra("resultList", (Serializable)list);
			        	setResult(RESULT_OK, intent);
						PhotoWallShowActivity.this.finish();
					}else{
						if(currentIndex >= list.size()-1){
							currentIndex--;
							if(currentIndex<0){
								currentIndex=0;
							}
						}
						
						updateChange();
					}
					showMsg("删除成功");
				}else{
					showMsg(bm.getMsg());	
				}
			}
	
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("删除失败");
				closeDialog(mpDialog);
			}
		});
	}
	
	private void lovePhoto(String picId){
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("picId", picId);
		
		BottleRestClient.post("lovePic", this, jo, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				super.onStart();
				showDialog("爱心送达中...","送达失败...",15*1000);
			}
	
			@Override
			public void onFinish() {
				super.onFinish();
				closeDialog(mpDialog);
			}
	
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				Gson gson = new Gson();
				BaseModel bm = gson.fromJson(str, BaseModel.class);
				if(bm.getCode().equals("0")){
					delPhotoFlag = true;
					PhotoWallModel pwm = list.get(currentIndex);
					pwm.setIsLove(1);
					pwm.setLoveNum(pwm.getLoveNum()+1);
					updateChange();
					showMsg("点赞成功");
					state[currentIndex] = true;
				}else{
					showMsg(bm.getMsg());	
				}
			}
	
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("点赞失败");
				closeDialog(mpDialog);
			}
		});
	}
	
	public void updateChange(){
		if(currentIndex>=list.size()||currentIndex<0){
			return ;
		}
		
		if(identityflag==1){	//别人的
			tv_mylove.setVisibility(View.GONE);
			if(list.get(currentIndex).getIsLove()==1){
				btn_gn.setBackgroundResource(R.drawable.pw_love_p);
			}else{
				btn_gn.setBackgroundResource(R.drawable.pw_love);
			}
			btn_gn.setText(list.get(currentIndex).getLoveNum()+"");
			
			if(state[currentIndex]){
				btn_gn.setClickable(false);
			}else
				btn_gn.setClickable(true);
			
		}else{	//自己的
			tv_mylove.setText(list.get(currentIndex).getLoveNum()+"");
		}
		
		if(list.get(currentIndex).getState()==2){
			tv_desc.setVisibility(View.VISIBLE);
		}else{
			tv_desc.setVisibility(View.GONE);
		}
		
		image_browser_text.setText((currentIndex+1)+"/"+total);
		url = list.get(currentIndex).getPicUrl();
		
		setFile(url);
	}
	
	@Override
	public void onBackPressed() {
		if(delPhotoFlag){
			Intent intent = new Intent();
			intent.putExtra("resultList", (Serializable)list);
			setResult(RESULT_OK, intent);
		}else{
			setResult(RESULT_CANCELED);
		}
		this.finish();
	}
	
	
}