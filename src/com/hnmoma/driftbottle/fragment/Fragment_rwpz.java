package com.hnmoma.driftbottle.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.WebFrameWithCacheActivity;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.AnimRelativeLayout;
import com.hnmoma.driftbottle.model.ActionNumModel;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.CommentModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.HttpGet;
/**
 * 任务瓶子
 * @author Administrator
 *
 */
public class Fragment_rwpz extends BaseFragment implements OnClickListener{
	
	PickBottleModel model;
	BottleModel bottleModel;
	ActionNumModel actionNumModel;
	List<CommentModel>  commentModels;
	
	Button btnAbandon;	//放弃任务
	Button btnAccept;	//接受任务
	
	ImageView iv_pic;
	TextView tv_desc;
	TextView tv_jl;
	TextView tv_count;
	
	// 瓶子扔掉动画
	LinearLayout ll_first;
	RelativeLayout rl_second;
	AnimRelativeLayout chuck_bottle_layout;
	AnimationSet set;
	private ImageView chuck_spray1;
	
	PackageBroadcastReceiver receiver;
	
	long waitTime = 5000L;  
	long touchTime = 0L; 

	public static BaseFragment newInstance(PickBottleModel model) {
		Fragment_rwpz fragment = new Fragment_rwpz();
		Bundle bundle = new Bundle();
		bundle.putSerializable("PickBottleModel", model);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fishing_rwpz, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//监听包成功安装否
		receiver = new PackageBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		filter.addDataScheme("package");
		act.registerReceiver(receiver, filter);
		
		Bundle bundle = getArguments();
		model = (PickBottleModel) bundle.getSerializable("PickBottleModel");
		
		btnAbandon = (Button) findViewById(R.id.bt_01);
		btnAccept = (Button) findViewById(R.id.bt_02);
		btnAbandon.setOnClickListener(this);
		btnAccept.setOnClickListener(this);
		
		iv_pic = (ImageView) findViewById(R.id.iv_pic);
		tv_desc = (TextView) findViewById(R.id.tv_desc);
		tv_jl = (TextView) findViewById(R.id.tv_jl);
		tv_count = (TextView) findViewById(R.id.tv_count);
		
		ll_first = (LinearLayout) findViewById(R.id.ll_first);
		rl_second = (RelativeLayout) findViewById(R.id.rl_second);
		chuck_bottle_layout = (AnimRelativeLayout) findViewById(R.id.chuck_bottle_layout);
		chuck_spray1=(ImageView) findViewById(R.id.chuck_spray1);
		
		initAnimation();
		initDate(model);
	}
	
	class PackageBroadcastReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();

	        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
	        	getTaskJL("stask1005");
	        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {

	        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
	        	getTaskJL("stask1005");
	        }
	    }
	};

	private void initDate(PickBottleModel model){
		bottleModel = model.getBottleInfo();
		actionNumModel = model.getActionNum();
		
		//bottle content
		String cnt = bottleModel.getContent();
		cnt = cnt == null ? "" : cnt;
		
		imageLoader.displayImage(bottleModel.getShortPic(), iv_pic, options);
		tv_desc.setText(cnt);
		
		String awardType = bottleModel.getAwardType();
		int awardNum = bottleModel.getAwardNum();
		if(awardType.equals("award1001")){//奖励扇贝
			tv_jl.setText(awardNum+"扇贝");
		}else{//奖励瓶子
			tv_jl.setText(awardNum+"瓶子");
		}
		
		int doNum = actionNumModel.getDoNum();
		tv_count.setText(Html.fromHtml("<font color=#767676>已经有</font>" + "<font color=#ff7800>" + doNum + "</font>" + "<font color=#767676>人完成任务</font>"));
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
			case R.id.bt_01:
				long currentTime = System.currentTimeMillis();
				 if((currentTime-touchTime)>=waitTime) {  
			           touchTime = currentTime;  
			           ready2Send();
				    }
				break;
			case R.id.bt_02:
				//下载，打开页面，市场评论
				//做完任务后要通知服务端
				String taskType = bottleModel.getRemark();
				if(taskType.equals("10100")){//市场评论
					doSupport();
				}else if(taskType.equals("10101")){//Cpa下载
//					showDownDialog(bottleModel.getUrl());
					
					if (MoMaUtil.isExitsSdcard()) {
						String path = MyApplication.mAppPath + "/.temp/download/";
						
						File dir = new File(path);
						if (!dir.exists()) {
							dir.mkdirs();
						}
						
						File taskApk = new File(dir, "task.apk");
						
						dlt = new DownloadTask(taskApk);
						dlt.execute();
					}else{
						showMsg("无内存空间");
					}
				}else{//cpc页面
					Intent intent = new Intent(act, WebFrameWithCacheActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("webUrl", bottleModel.getUrl());
					startActivity(intent);
					getTaskJL("stask1005");
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
	
	
	
	
	DownloadTask dlt;
	LayoutInflater layoutInflator;
	
	//download
	public Dialog pd;
	public class DownloadTask extends AsyncTask<Void, Integer, Integer> {
		File file;
		ProgressBar progress;
		TextView progressText;
		Button bt;
		
		public DownloadTask(File file){
			this.file = file;
		}
		
		protected void onPreExecute() {
			super.onPreExecute();
			pd = new Dialog(act, R.style.dialogStyle);
//			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setTitle("下载中");
//			pd.setMessage("开始下载，请稍等");
//			//设置进度条是否不明确  明确的话是根据实际进度行进的，不明确的话是说只是在走。
//			pd.setIndeterminate(false);
			pd.setCancelable(true);
			pd.setCanceledOnTouchOutside(false);
			
			pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					DownloadTask.this.cancel(true);
				}
			}); 
			
			pd.show();
			
			if(layoutInflator==null){
				layoutInflator = LayoutInflater.from(act);
			}
			View v = layoutInflator.inflate(R.layout.download, null);
			progress = (ProgressBar) v.findViewById(R.id.myProssBarhandle);
			progressText = (TextView) v.findViewById(R.id.myProssBarhandleText);
			bt = (Button)v.findViewById(R.id.bt);
			bt.setOnClickListener(new OnClickListener(){
				public void onClick(View arg0) {
					pd.cancel();
				}
			});
			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			pd.setContentView(v, lp);
			pd.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			int result = 0;
			
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet httpget = new HttpGet(bottleModel.getUrl());
				HttpResponse response = httpclient.execute(httpget);

				if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
					HttpEntity entity = response.getEntity();
					if (entity != null && entity.getContentLength() != 0) {
						long length = entity.getContentLength();
						progress.setMax((int) (length * 2));

						FileOutputStream f = new FileOutputStream(file);

						InputStream in = entity.getContent();

						// 下载的代码
						byte[] buffer = new byte[1024];
						int len1 = 0;
						long total = 0;

						while ((len1 = in.read(buffer)) > 0) {
							total += len1; // total = total + len1
							f.write(buffer, 0, len1);
							publishProgress((int) total);
						}
						f.close();
						in.close();
					}else{
						result = -1;
					}
				}
				
			} catch (Exception e) {
				result = -1;
			}
			
			return result;
		}

		@Override
		protected void onProgressUpdate(Integer... arg) {
			progress.setProgress(arg[0]);  
			
			float num = (float)progress.getProgress()/(float)progress.getMax();
			int result = (int)(num*100);
			progressText.setText(result+ "%");
			super.onProgressUpdate(arg);
		}

		@Override
		protected void onPostExecute(Integer params) {
			//判断是否为正常下载了，正常下载了就开始解压
			if(pd.isShowing()){
				pd.dismiss();
			}
			
			if(params == -1){
				showMsg("下载失败...");
			}else{
				showMsg("下载完成，安装并试玩后即可获取奖励...");
				installApk(file);
			}
			
			super.onPostExecute(params);
		}
	}
	
//	protected void showDownDialog(String appUrl) {
//			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//				String path = MyApplication.mAppPath + "/.temp/download/";
//				
//				File dir = new File(path);
//				if (!dir.exists()) {
//					dir.mkdirs();
//				}
//				
//				final File taskApk = new File(Environment.getExternalStorageDirectory(), "task.apk");
//				
//				intiProgress();
//				BottleRestClient.get(appUrl, new FileAsyncHttpResponseHandler(taskApk) {
//					@Override
//					public boolean deleteTargetFile() {
//						return super.deleteTargetFile();
//					}
//					
//					@Override
//					public void onStart() {
//						super.onStart();
//					}
//
//					@Override
//					public void onFinish() {
//						super.onFinish();
//						pd.dismiss();
//					}
//
//					@Override
//					public void onProgress(int bytesWritten, int totalSize) {
//						int result = bytesWritten*100/totalSize;
//						progress.setMax(totalSize);
//						progress.setProgress(bytesWritten);
//						progressText.setText(result+ "%");
//					}
//					@Override
//					public void onSuccess(int statusCode, Header[] headers, File file) {
//						if(statusCode == 200) {
//							showMsg("下载完成，安装并试玩后即可获取奖励...");
//							installApk(file);
//						}
//					}
//					@Override
//					public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
//					}
//				});
//			}
//	}

//	public Dialog pd;
//	Dialog pb;
//	ProgressBar progress;
//	TextView progressText;
//	private void intiProgress() {
//		//Button bt;
//		pd = new Dialog(act, R.style.dialogStyle);
//		pd.setCancelable(false);
//		pd.setCanceledOnTouchOutside(false);
//		pd.setOnKeyListener(onKeyListener);
//		
//		View v =act.getLayoutInflater().inflate(R.layout.updateprogress, null);
//		progress = (ProgressBar) v.findViewById(R.id.myProssBar);
//		progressText = (TextView) v.findViewById(R.id.myProssBarText);
//		
//		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		pd.setContentView(v, lp);
//		pd.show();
//	}
//	
//	BuyDialog dialogToBuy;
//	 private OnKeyListener onKeyListener = new OnKeyListener() {
//	        @Override
//	        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//	            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_UP) {
//	            	if(dialogToBuy==null){
//	            		dialogToBuy = new BuyDialog(act, R.style.style_dialog_ballon);
//	            		dialogToBuy.show();
//						dialogToBuy.setContent(null, "下载并安装即可领取奖励，是否取消？");
//						dialogToBuy.setOnSubmitClick(new OnClickListener() {
//							@Override
//							public void onClick(View v) {
//								dialogToBuy.cancel();
//								pd.cancel();
//								
//								dlt.cancel(true);
//							}
//						});
//	            	}else{
//	            		dialogToBuy.show();
//	            	}
//					
//					return true;
//	            }
//	            return false;
//	        }
//	    };
	
	private void installApk(File t) {
		Uri uri = Uri.fromFile(t);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		startActivity(intent);
	}
	
	private void doSupport(){
		//这里获取渠道标识，只评论相应渠道,如果没有这个市场，就去其他随机市场
		String channelId="";
		int index = -1;
		
		try {
			ApplicationInfo appInfo = act.getPackageManager().getApplicationInfo(act.getPackageName(), PackageManager.GET_META_DATA);
			String str =  appInfo.metaData.getString("UMENG_CHANNEL");
			channelId = str==null?"":str;
		} catch (NameNotFoundException e) {
		}
		
		String packageName = "com.hnmoma.driftbottle";
		PackageManager localPackageManager = act.getPackageManager();
		Intent localIntent1 = new Intent(Intent.ACTION_VIEW);
		Uri localUri1 = Uri.parse("market://details?id=" + packageName);
		Intent localIntent3 = localIntent1.setData(localUri1);
		List localList = localPackageManager.queryIntentActivities(localIntent1, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
		for (int i = 0; i < localList.size(); i++) {
			ResolveInfo localResolveInfo = (ResolveInfo) localList.get(i);
			String str2 = localResolveInfo.activityInfo.name;
			
			if(str2.contains(channelId)){
				index = i;
			}
		}
		
		//为0时直接返回
		if(localList.size() <= 0){
			return;
		}else if(localList.size() == 1){
			index = 0;
		}else{//大于2时随机
			if(index == -1){
				Random r = new Random();
				index = r.nextInt(localList.size()-1);
			}
		}
		
		
		ResolveInfo localResolveInfo = (ResolveInfo) localList.get(index);
		String str2 = localResolveInfo.activityInfo.packageName;
		String str3 = localResolveInfo.activityInfo.name;
		
		Uri uri = Uri.parse("market://details?id=" + packageName);
		localIntent3.setData(uri);
		localIntent3.setClassName(str2, str3);
		localIntent3.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivityForResult(localIntent3, 100);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 100){
			getTaskJL("stask1005");
		}else if(requestCode == 101){
		}
	}
	/**
	 * 更新UI
	 */
	private void showJLMsg(){
		StringBuilder sb = new StringBuilder();
		sb.append("<font color=#767676>已完成任务，成功获取</font>");
		
		String awardType = bottleModel.getAwardType();
		int awardNum = bottleModel.getAwardNum();
		sb.append("<font color=#fe7800>").append(awardNum).append("</font>");
		if(awardType.equals("award1001")){//奖励扇贝
			
			sb.append("<font color=#767676>").append("扇贝").append("</font>");
			int myMoney = MyApplication.getApp().getSpUtil().getMyMoney();
			MyApplication.getApp().getSpUtil().setMyMoney((myMoney+awardNum));
		}else{//奖励瓶子
			sb.append("<font color=#767676>").append("瓶子").append("</font>");
		}
		tv_count.setText(Html.fromHtml(sb.toString()));
		
		btnAccept.setText("完成任务");
		btnAccept.setTextColor(getResources().getColor(R.color.rwpz_complete));
		btnAccept.setClickable(false);
		btnAccept.setEnabled(false);
		
	}
	
	//获取任务奖励
		private void getTaskJL(String type){
			JsonObject jo = new JsonObject();
			jo.addProperty("userId", UserManager.getInstance(act).getCurrentUserId());
			jo.addProperty("deviceId", act.getDeviceId());
			jo.addProperty("type", type);
			jo.addProperty("come", "6000");
			jo.addProperty("bottleId", bottleModel.getBottleId());
			
			BottleRestClient.post("doTask", act, jo, new AsyncHttpResponseHandler(){
				@Override
				public void onStart() {
					// TODO Auto-generated method stub
					super.onStart();
					MoMaLog.e("debug", "开始做任务");
				}
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					try {
						String _response = new String(responseBody);
						JSONObject response = new JSONObject(_response);
						int code = response.getInt("code");
						if(code==0){
							showJLMsg();
						}else{
							showMsg(response.getString("msg"));
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//					showMsg("登出失败");
				}
				
				@Override
				public void onFinish() {
					super.onFinish();
				}
	        });
		}
		
		@Override
		public void onDestroy() {
			act.unregisterReceiver(receiver);
			super.onDestroy();
		}
		
}
