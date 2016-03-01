package com.hnmoma.driftbottle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.hnmoma.driftbottle.adapter.AlbumPhotoAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.AddPhotoBModel;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.PhotoWallModel;
import com.hnmoma.driftbottle.model.PicModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.BitmapUtil;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.UtilScanDICM;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 相册图片,此处activity的启动模式是不能是singleInstance，否则获取照片以及相册内容
 * @author Administrator
 *
 */
public class AlbumActivity extends BaseFrameShowActivity {
	private final int MORE = 1;	//加载更多的标志
	private final int FINISH = 0; //没有更多的内容标志
	private  final int SUBMITPHOTO = 2; //上传图片
	private  final int CLOSEDIALOG = 3; //取消进度条
	private  final int REFRESH = 4;//刷新adapter
	
	
	private GridView mGridView;
	private PullToRefreshGridView mPullToRefreshGridView;
	private TextView tvTitle;	//标题
	private TextView tvVip;	//VIP 字体
	private ImageView ivVip;  //VIP图像
	private ImageButton ibAddPhoto; //添加照片
	
	private AlbumPhotoAdapter photoAdapter;
	
	private List<PhotoWallModel>  list; 
	
	private String userId;
	private String picId;
	private  int picNUm;	//照片总数
	
	/*用来标识请求照相功能的activity*/        
    private static final int CAMERA_WITH_DATA = 1001;        
    /*用来标识请求gallery的activity*/        
    private static final int PHOTO_PICKED_WITH_DATA = 1002;  //不高于api leve 19以下使用 
    private static final int KITKAT_PHOTO_PICKED_WITH_DATA = 1003;  //不低于api level 4.4使用
	private static final int SCANPHOTO = 1004; //浏览照片
	
    String uploadimgPath;
    private Uri outputFileUri;  
    /**
	 * 选择提示对话框
	 */
	final String[] strarray = new String[] {"拍照", "从图库选择"};
	String cacheDir = MyApplication.mAppPath + "/.temp/upload/";
	File head;
	
	int identityflag = 0;// 身份标识，0表示自己看自己,1表示看别人
	ImageLoader imageLoader = ImageLoader.getInstance();
	private boolean isLast;//是否是最后一条
	private boolean isUpdatePhoto;//照片列表是否需要更新或者已经更新了
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album);
		setupView();
		init();
		setLinstener()	;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(isUpdatePhoto){
			Intent intent = new Intent();
			intent.putExtra("picNUm", picNUm);
			intent.putExtra("resultList",(Serializable) list);
			setResult(Activity.RESULT_OK, intent);
		}
		finish();
	}
	
	private void setupView() {
		// TODO Auto-generated method stub
		mPullToRefreshGridView = (PullToRefreshGridView) findViewById(R.id.wgv_photots);
		mPullToRefreshGridView.setMode(Mode.PULL_FROM_END);	//底部刷新
		mGridView = mPullToRefreshGridView.getRefreshableView();
		registerForContextMenu(mPullToRefreshGridView);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvVip = (TextView) findViewById(R.id.tv_vip);
		ivVip = (ImageView) findViewById(R.id.iv_vip);
		ibAddPhoto = (ImageButton) findViewById(R.id.ib_add);
		
		TextView tvTip = (TextView)findViewById(R.id.tv_no_message);
		tvTip.setText(getResources().getString(R.string.tip_no_album));
		tvTip.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.defalut_empt_albumy), null, null);
	}

	private void init() {
		// TODO Auto-generated method stub
		list = new LinkedList<PhotoWallModel>();
		isLast = false;//默认情况，不是最后一条
		userId = getIntent().getStringExtra("userId");
		picId = "0";
		
		identityflag = getIntent().getIntExtra("identityflag", 1);
		if(identityflag == 1) {//1表示看别人
			tvTitle.setText(getResources().getString(R.string.tv_album));
			findViewById(R.id.rl_bottom).setVisibility(View.GONE);
		} else {
			tvTitle.setText(getResources().getString(R.string.tv_myAlbum));
			findViewById(R.id.rl_bottom).setVisibility(View.VISIBLE);
			Integer flag = UserManager.getInstance(this).getCurrentUser().getIsVIP();
			if(flag!=null&&flag.intValue()==1){
				ivVip.setImageDrawable(getResources().getDrawable(R.drawable.ic_vip));
			}else{
				ivVip.setImageDrawable(getResources().getDrawable(R.drawable.ic_vip_not));
			}
		}
		
		photoAdapter = new AlbumPhotoAdapter(imageLoader, identityflag);
		mGridView.setAdapter(photoAdapter);
		 
		if(getIntent()!=null){
			boolean isLoad = getIntent().getBooleanExtra("load", false);
			isUpdatePhoto = getIntent().getBooleanExtra("addPhoto", false);
			if(isLoad){
				List<PhotoWallModel> list = (List<PhotoWallModel>) getIntent().getSerializableExtra("resultList");
				updatePhotoWall(list);
				return ;
			}
		}
		//如果需要上传相片，说明相册是空的
		if(isUpdatePhoto){
			if(identityflag==0){
				Integer flag = UserManager.getInstance(AlbumActivity.this).getCurrentUser().getIsVIP();
				int sum = MyApplication.getApp().getSpUtil().getPicNumLimit();
				if(flag!=null&&flag==1){
					if(sum==Integer.MAX_VALUE){
						tvVip.setText("VIP相册");
					}else{
						tvVip.setText("VIP相册容量:0/"+sum);
					}
				}else{
					tvVip.setText("普通相册容量:"+picNUm+"/"+sum);
				}
			}
			if(isCanAddPhoto()) {
				showPickDialog();
			} else {
				showMsg(getResources().getString(R.string.tip_album));
			}
		}else
			pickupData(picId,false);
		
	}
	
	private Handler handler = new Handler()	{
		public void handleMessage(android.os.Message msg) {
			switch (msg.arg1) {
			case FINISH://
				showMsg("没有更多");
				mPullToRefreshGridView.getLoadingLayoutProxy().setPullLabel("没有更多");
				mPullToRefreshGridView.onRefreshComplete();
				break;
			case MORE:	//加载更多
				mPullToRefreshGridView.getLoadingLayoutProxy().setRefreshingLabel("正在加载");
				mPullToRefreshGridView.getLoadingLayoutProxy().setPullLabel("上拉加载更多");
				mPullToRefreshGridView.getLoadingLayoutProxy().setReleaseLabel("释放开始加载");
				pickupData(picId, true);
				break;
			case SUBMITPHOTO://上传图片
				submitPhoto();
				break;
			case CLOSEDIALOG:
				closeDialog(mpDialog);
				break;
			case REFRESH:
				photoAdapter.notifyDataSetChanged();
				if(photoAdapter.getCount()>0){
					mPullToRefreshGridView.setVisibility(View.VISIBLE);
					findViewById(R.id.no_message).setVisibility(View.GONE);
				}else{
					mPullToRefreshGridView.setVisibility(View.GONE);
					findViewById(R.id.no_message).setVisibility(View.VISIBLE);
				}
				break;
			
			default:
				break;
			}
		};
	};
	/**
	 * 注册点击事件
	 */
	private void setLinstener() {
		// TODO Auto-generated method stub
		mPullToRefreshGridView.setOnRefreshListener(new OnRefreshListener2<GridView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				Message msg = Message.obtain();
				if(isLast){
					msg.arg1 = FINISH;
				}else{
					msg.arg1 = MORE;
				}
				handler.sendMessage(msg);
			}

		});
		
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> av, View view, int position, long id) {
				PhotoWallModel vpm = (PhotoWallModel) av.getItemAtPosition(position);
				if(vpm.getPicId().equals("0000")){//添加图片
						if(isCanAddPhoto()) {
							showPickDialog();
						} else {
							showMsg(getResources().getString(R.string.tip_album));
						}
				}else{//查看图片
					Intent intent = new Intent(AlbumActivity.this, PhotoWallShowActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("imageUrls", (Serializable)list);
					intent.putExtra("position", position);
					intent.putExtra("identityflag", identityflag);
					intent.putExtra("userId", userId);
					startActivityForResult(intent,SCANPHOTO);
				}
			}
		});
		findViewById(R.id.bt_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
		
		ibAddPhoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isCanAddPhoto()){
					showPickDialog();
				}else{
					showMsg(getResources().getString(R.string.tip_album));
				}
			}
		});

		mGridView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
				// TODO Auto-generated method stub
				if(identityflag==0){
					MenuInflater inflater = getMenuInflater();
					menu.setHeaderTitle("温馨提示");
				    inflater.inflate(R.menu.menu_photo, menu);
				}
			}
		});
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		PhotoWallModel model = photoAdapter.getItem(info.position);
		switch (item.getItemId()) {
		case R.id.save_photo:
			savetoLocal(model.getPicUrl(), imageLoader, false);
			break;
		case R.id.delete_photo:
			delPhoto(model.getPicId(),info.position);
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	
	/**
	 * 
	 * @param picId 最后一张图片的ID
	 * @param isLoadMore 如果isLoadMore是true，则表示加载更多，否则不需要加载
	 */
	private void pickupData(@NonNull String picId,final boolean isLoadMore) {
		String uid = UserManager.getInstance(this).getCurrentUserId();
		JsonObject jo = new JsonObject();
		jo.addProperty("picId", picId);
		if(uid.equals(userId)){	//自己看自己
			jo.addProperty("userId", userId);
			jo.addProperty("visitUserId", "");
		}else{
			jo.addProperty("userId", userId);
			jo.addProperty("visitUserId", uid);
		}
		
		jo.addProperty("pageNum", "12");
		BottleRestClient.post("queryPic", this, jo, new AsyncHttpResponseHandler() {
	
			@Override
			public void onStart() {
				super.onStart();
				if(!isLoadMore)
					showDialog("努力加载...","sorry,加载失败...",15 * 1000);
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				closeDialog(mpDialog);
				Integer flag = UserManager.getInstance(AlbumActivity.this).getCurrentUser().getIsVIP();
				int sum = MyApplication.getApp().getSpUtil().getPicNumLimit();
				if(flag!=null&&flag==1){
					if(sum==Integer.MAX_VALUE){
						tvVip.setText("VIP相册");
					}else{
						tvVip.setText("VIP相册容量:"+picNUm+"/"+sum);
					}
				}else{
					tvVip.setText("普通相册容量:"+picNUm+"/"+sum);
				}
			}
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				closeDialog(mpDialog);
				String str = new String(responseBody);
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					PicModel	 model = gson.fromJson(str, PicModel.class);
					if (model != null && !TextUtils.isEmpty(model.getCode())) {
						if ("0".equals(model.getCode())) {
							if(!isLoadMore){
								list = model.getPicList();
								photoAdapter.reset(list);
							}else{
								for (PhotoWallModel info:model.getPicList()) {
									list.add(0, info);
								}
								photoAdapter.addItemTop(model.getPicList());
							}
							if(model.getPicList()!=null&&!model.getPicList().isEmpty())
								AlbumActivity.this.picId = model.getPicList().get(model.getPicList().size()-1).getPicId();
							picNUm = model.getPicNum();
							if("0".equalsIgnoreCase(model.getIsMore())){
								isLast = true;
								if(isLoadMore)
									mPullToRefreshGridView.getLoadingLayoutProxy().setPullLabel("没有更多");
							}else
								isLast = false;
							if(!isLoadMore&&(model==null||model.getPicList().isEmpty())){
								mPullToRefreshGridView.setVisibility(View.GONE);
							}
							
							if(isLoadMore)
								mPullToRefreshGridView.onRefreshComplete();
							Message msg = Message.obtain();
							msg.arg1  =REFRESH;
							handler.sendMessage(msg);
						} else if("1000".equals(model.getCode())){
							setResult(RESULT_CANCELED);
							isLast = true;//最后一条
							if(isLoadMore){
								mPullToRefreshGridView.getLoadingLayoutProxy().setPullLabel("没有更多");
								mPullToRefreshGridView.onRefreshComplete();
							}
							Message msg = Message.obtain();
							msg.arg1  =REFRESH;
							handler.sendMessage(msg);
						}else{
							isLast = true;//最后一条
							if(isLoadMore){
								mPullToRefreshGridView.getLoadingLayoutProxy().setPullLabel("没有更多");
								mPullToRefreshGridView.onRefreshComplete();
							}
							Message msg = Message.obtain();
							msg.arg1  =REFRESH;
							handler.sendMessage(msg);
						}
					} else {
						showMsg("服务器繁忙");
						setResult(RESULT_CANCELED);
					}
				} else {
					showMsg("服务器繁忙");
					setResult(RESULT_CANCELED);
				}
			}
	
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("sorry，出错了···");
				closeDialog(mpDialog);
			}
		});
	}
    private void showPickDialog() {
		new AlertDialog.Builder(this)
			.setTitle(getResources().getString(R.string.upload_photo))
			.setItems(strarray, new DialogInterface.OnClickListener() {
				     @Override
				     public void onClick(DialogInterface dialog, int which) {
				    	 if(which == 0){
				    		dialog.dismiss();
				    		
				    		File file = new File(cacheDir);
				    		 if (!file.exists()) {
								file.mkdirs();
				    		 }
	                        outputFileUri = Uri.fromFile(new File(cacheDir, "uploadimg.jpg"));  
	  
	                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
	                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);  
	                        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
	                        startActivityForResult(cameraIntent, CAMERA_WITH_DATA);  
				    	 }else if(which == 1){
				    		dialog.dismiss();
				    		//google官方建议：在api level不高于4.3时候，url使用ACTION_GET_CONTENT或ACTION_PICK；不低于4.4时候，url使用ACTION_OPEN_DOCUMENT
				    		Intent intent=new Intent();
				    		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				    		intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			    			 intent.setType("image/*");
//				    		if(android.os.Build.VERSION.SDK_INT >=android.os.Build.VERSION_CODES.KITKAT){
//				    			intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//				    			intent.addCategory(Intent.CATEGORY_OPENABLE);
//				    			 startActivityForResult(intent, KITKAT_PHOTO_PICKED_WITH_DATA);  
//				    		}else{
				    			intent.setAction(Intent.ACTION_PICK); 
				    			 startActivityForResult(intent, PHOTO_PICKED_WITH_DATA); 
//				    		}
				    		 
				    	 }
				     }
			    })
			.show();
	}
	
    // 上传图片
   	private void submitPhoto(){
   		Bitmap bitmap = BitmapUtil.getSmallBitmap(uploadimgPath,40);
   		String fileName = uploadimgPath.substring(uploadimgPath.lastIndexOf("/"));//图片名称和格式
   		final File uploadFile =new File(MyApplication.mAppPath+"pictures",fileName);
   		BitmapUtil.writeBitmap(uploadFile, bitmap,true);
		try {
			RequestParams rp = new RequestParams();
			rp.put("userId", userId);
			rp.put("attathUrl", uploadFile);
			rp.put("addType", "9001");//个人相册
			
			BottleRestClient.postWithAttath("bottlefile", rp, new AsyncHttpResponseHandler(){
				@Override
				public void onStart() {
					super.onStart();
					mPullToRefreshGridView.setMode(Mode.PULL_FROM_END);
				}
				
				@Override
				public void onFinish() {
					super.onFinish();
					closeDialog(mpDialog);
					if(uploadFile.exists()&&uploadFile.isFile())
						uploadFile.delete();
				}

				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					//删除临时文件夹
					if(uploadFile.exists()&&uploadFile.isFile())
						uploadFile.delete();
					closeDialog(mpDialog);
					String str = new String(responseBody);
					if (!TextUtils.isEmpty(str)) {
						Gson gson = new Gson();
						AddPhotoBModel model = gson.fromJson(str, AddPhotoBModel.class);
						if (model != null && !TextUtils.isEmpty(model.getCode())) {
							if ("0".equals(model.getCode())) {
								PhotoWallModel pwm = model.getPicInfo();
								
								if(list == null){
									list = new ArrayList<PhotoWallModel>();
									list.add(pwm);
								}else{
									list.add(0, pwm);
								}
								picId = pwm.getPicId();
								photoAdapter.addItem2Top(pwm);
								Message msg = Message.obtain();
								msg.arg1  =REFRESH;
								handler.sendMessage(msg);
								
								picNUm +=1;
								if(identityflag==0){
									Integer flag = UserManager.getInstance(AlbumActivity.this).getCurrentUser().getIsVIP();
									int sum = MyApplication.getApp().getSpUtil().getPicNumLimit();
									if(flag!=null&&flag==1){
										if(sum==Integer.MAX_VALUE){
											tvVip.setText("VIP相册");
										}else{
											tvVip.setText("VIP相册容量:0/"+sum);
										}
									}else{
										tvVip.setText("普通相册容量:"+picNUm+"/"+sum);
									}
								}
								showMsg("上传成功");
								isLast = false;	//上传成功后，把isLast置为false，不是最后一项，可以从后台刷新最新的数据
								isUpdatePhoto = true;
							} else {
								showMsg("上传失败");
							}
						} else {
							showMsg("上传失败");
						}
					} else {
						showMsg("上传失败");
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					showMsg("上传失败");
					closeDialog(mpDialog);
					if(uploadFile.exists()&&uploadFile.isFile())
						uploadFile.delete();
				}
			});
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	}
    
   	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
    	if(requestCode == PHOTO_PICKED_WITH_DATA){	//android 4.3
    		if(resultCode==Activity.RESULT_OK){
    			if(data == null){  
                    return;  
                }  
    			showDialog("提交中...","提交失败...",50 * 1000);
    			//启动线程，加载图片
    			runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Uri uri = data.getData();  //格式：content://media/external/images/media/3952
		                String[] proj = { MediaStore.Images.Media.DATA }; 
		                if(getContentResolver()==null){
		                	closeDialog(mpDialog);
			                showMsg("图片上传失败");
		                	return ;
		                }
		                Cursor cursor = getContentResolver().query(uri, proj, null, null, null); // Order-by clause (ascending by name)  
		                if(cursor!=null){
		                	 int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  
		                     cursor.moveToFirst(); 
		                     
		                     uploadimgPath = cursor.getString(column_index); 
		                     
		                     if(!TextUtils.isEmpty(uploadimgPath)){
		                     	Message msg = Message.obtain();
		                     	msg.arg1 = SUBMITPHOTO;
		                     	handler.sendMessage(msg);
		                     }else{
		                    	closeDialog(mpDialog);
				                showMsg("图片上传失败");
		                     }
		                     cursor.close();
		                }else{
		                	closeDialog(mpDialog);
		                	showMsg("图片上传失败");
		                }
					}
				});
                  
                
    		}
    	}else if(requestCode==KITKAT_PHOTO_PICKED_WITH_DATA&&resultCode==Activity.RESULT_OK){//android 4.4
    		if(data == null){  
                return;  
            } 
    		showDialog("提交中...","提交失败...",50 * 1000);
    		//启动线程加载图片
    		runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Uri contentUri = data.getData();  //格式：content://com.android.providers.media.documents/document/image:3952
		    		if(contentUri==null){
		    			closeDialog(mpDialog);
	                	showMsg("图片上传失败");
		    		}else{
		    			uploadimgPath = UtilScanDICM.getPath(AlbumActivity.this, contentUri);
			    		 if(!TextUtils.isEmpty(uploadimgPath)){
			    				Message msg = Message.obtain();
			                 	msg.arg1 = SUBMITPHOTO;
			                 	handler.sendMessage(msg);
			              }else{
			                	closeDialog(mpDialog);
			                	showMsg("图片上传失败");
			              }
		    		}
				}
			});
    		
    	}else if(requestCode == CAMERA_WITH_DATA){ //照相
    		if(resultCode==Activity.RESULT_OK){
    			if(data==null)
    				return ;
    			showDialog("提交中...","提交失败...",20 * 1000);
    			uploadimgPath = outputFileUri.getPath();
    			
    			if(!TextUtils.isEmpty(uploadimgPath)){
    				Message msg = Message.obtain();
                 	msg.arg1 = SUBMITPHOTO;
                 	handler.sendMessage(msg);
                }else{
                	closeDialog(mpDialog);
                	showMsg("图片上传失败");
                }
    		}
    	}else if(requestCode == 1003){
    		if(resultCode==Activity.RESULT_OK){
    			if(data==null)
    				return ;
    			List<PhotoWallModel> list = (List<PhotoWallModel>) data.getSerializableExtra("resultList");
    			updatePhotoWall(list);
    		}
    	}else if(requestCode==SCANPHOTO){
    		if(resultCode==Activity.RESULT_OK){
    			if(data==null)
    				return ;
    			List<PhotoWallModel> list = (List<PhotoWallModel>) data.getSerializableExtra("resultList");
    			picNUm = list.size();
    			updatePhotoWall(list);
    		}
    	}
	}
	
	private void updatePhotoWall(List<PhotoWallModel> list){
		if(list!=null){
			this.list = list;
			if(!list.isEmpty()){
				this.picId = list.get(list.size()-1).getPicId();
			}
			
			if(identityflag==0){
				Integer flag = UserManager.getInstance(AlbumActivity.this).getCurrentUser().getIsVIP();
				int sum = MyApplication.getApp().getSpUtil().getPicNumLimit();
				if(flag!=null&&flag==1){
					if(sum==Integer.MAX_VALUE){
						tvVip.setText("VIP相册");
					}else{
						tvVip.setText("VIP相册容量:"+picNUm+"/"+sum);
					}
				}else{
					tvVip.setText("普通相册容量:"+picNUm+"/"+sum);
				}
			}
			photoAdapter.reset(list);
			Message msg = Message.obtain();
			msg.arg1  =REFRESH;
			handler.sendMessage(msg);
		}
	}
	/**
	 * 根据相册数量以及会员限制，是否允许上传图片
	 * @return 如果相册数量大于30或超过会员相册上传限制，则返回false，否则返回true
	 */
	private boolean isCanAddPhoto() {
		// TODO Auto-generated method stub
		if(picNUm<MyApplication.getApp().getSpUtil().getPicNumLimit())
			return true;
		return false;
	}
	/**
	 * 删除图片
	 * @param picId 图片id
	 * @param position 
	 */
	private void delPhoto(@NonNull String picId,final int position){
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("picId", picId);
		
		BottleRestClient.post("delPic", this, jo, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				super.onStart();
				showDialog("正在删除...", "删除失败...", 15*1000);
			}
	
			@Override
			public void onFinish() {
				super.onFinish();
				closeDialog(mpDialog);
			}
	
			@SuppressWarnings("null")
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				Gson gson = new Gson();
				BaseModel bm = gson.fromJson(str, BaseModel.class);
				if(bm.getCode().equals("0")){
					PhotoWallModel model = photoAdapter.getItem(position);
					photoAdapter.remove(model);
					list.remove(model);
					showMsg("删除成功");
					Message msg = Message.obtain();
					msg.arg1  =REFRESH;
					handler.sendMessage(msg);
					isUpdatePhoto = true;
					picNUm -=1;
					if(picNUm==0){
						mPullToRefreshGridView.setVisibility(View.GONE);
						findViewById(R.id.no_message).setVisibility(View.VISIBLE);
						TextView tvTip = (TextView)findViewById(R.id.tv_no_message);
						tvTip.setText(getResources().getString(R.string.tip_no_album));
						tvTip.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.defalut_empt_albumy), null, null);
					}else{
						mPullToRefreshGridView.setVisibility(View.VISIBLE);
						findViewById(R.id.no_message).setVisibility(View.GONE);
					}
					if(identityflag==0){
						Integer flag = UserManager.getInstance(AlbumActivity.this).getCurrentUser().getIsVIP();
						int sum = MyApplication.getApp().getSpUtil().getPicNumLimit();
						if(flag!=null&&flag==1){
							if(sum==Integer.MAX_VALUE){
								tvVip.setText("VIP相册");
							}else{
								tvVip.setText("VIP相册容量:"+picNUm+"/"+sum);
							}
						}else{
							tvVip.setText("普通相册容量:"+picNUm+"/"+sum);
						}
					}
				}else{
					showMsg(bm.getMsg());	
				}
			}
	
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("删除失败");
			}
		});
	}
	
	

	
}
