package com.hnmoma.driftbottle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hnmoma.driftbottle.custom.LocusPassWordView;
import com.hnmoma.driftbottle.custom.LocusPassWordView.OnCompleteListener;
import com.hnmoma.driftbottle.util.MoMaLog;
/**
 * 设置九宫锁
 * yangsy 2015-3-30 修改
 * <p>
 * 在默认情况下，用户必须输入两次相同的密码，才能保存；输入第一遍密码后，提示用户再一次输入密码，在最上边提示用户的第一次输入
 * </p>
 */
public class SetLockScreenActivity extends Activity {
	public static final int REFRESH = 1;
	public static final int RESET = 0;
	
	private LocusPassWordView lpwv; //九宫锁视图
	private TextView tvTip;
	
	
	private  String password ;
	
	//private boolean needverify = true;
	private Toast toast;
//	private boolean resetlock = false;
//	private Button btnSave ,btnReset;
	
	private View preView ;
	private View mPreviewViews[][] = new View[3][3];
	
	private int times = 0;	//输入密码的次数
	
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			MoMaLog.e("debug","oper="+msg.what);
			if(msg.what == REFRESH){
				String pwd = (String) msg.obj;
				String[] args = pwd.split(",");
				updatePreviewViews(args);
			}else if(msg.what==RESET){
				resetPreviewViews();
			}
		};
	};
	
	private void showToast(CharSequence message) {
		if (null == toast) {
			toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
//			toast.setGravity(Gravity.CENTER, 0, 0);
		} else {
			toast.setText(message);
		}

		toast.show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setlockscreen);
		
//		resetlock = getIntent().getBooleanExtra("resetlock", false);
		setupView();
	
		// 如果密码为空,直接输入密码
		if (lpwv.isPasswordEmpty()) {
			//this.needverify = false;
//			showToast("请输入密码");
		}
		
		setLinstener();
	}

	private void setupView() {
		// TODO Auto-generated method stub
		lpwv = (LocusPassWordView) findViewById(R.id.mLocusPassWordView);
		lpwv.enableTouch();
		
//		btnSave = (Button) this.findViewById(R.id.tvSave);
//		btnReset = (Button) this.findViewById(R.id.tvReset);
//		
//		btnSave.setClickable(false);	//默认情况下，不能保存手势
		
		tvTip = (TextView) this.findViewById(R.id.tv_tip_gesture);
		tvTip.setText(getResources().getString(R.string.tip_gesture_local_start));
		
		initPreviewViews();
	}
	
	private void initPreviewViews() {
		preView = findViewById(R.id.gesturepwd_preview);
//		preView.setVisibility(View.GONE);
		
		mPreviewViews = new View[3][3];
		mPreviewViews[0][0] = findViewById(R.id.gesturepwd_setting_preview_0);
		mPreviewViews[0][1] = findViewById(R.id.gesturepwd_setting_preview_1);
		mPreviewViews[0][2] = findViewById(R.id.gesturepwd_setting_preview_2);
		mPreviewViews[1][0] = findViewById(R.id.gesturepwd_setting_preview_3);
		mPreviewViews[1][1] = findViewById(R.id.gesturepwd_setting_preview_4);
		mPreviewViews[1][2] = findViewById(R.id.gesturepwd_setting_preview_5);
		mPreviewViews[2][0] = findViewById(R.id.gesturepwd_setting_preview_6);
		mPreviewViews[2][1] = findViewById(R.id.gesturepwd_setting_preview_7);
		mPreviewViews[2][2] = findViewById(R.id.gesturepwd_setting_preview_8);
	}
	
	@SuppressLint("NewApi")
	private void updatePreviewViews(String[] args) {
		// TODO Auto-generated method stub
		//将数字转化为九宫数组
		int[][] points = toPoints(args);
		for(int i=0;i<points.length;i++)
			for(int j=0;j<points[0].length;j++){
				if(points[i][j]==1){
					mPreviewViews[i][j].setSelected(true);
//					mPreviewViews[i][j].setBackground(getResources().getDrawable(R.drawable.gesture_create_grid_selected));
//					Log.e("debug","x="+i+",y="+j);
				}
			}
//		preView.setVisibility(View.VISIBLE);
		preView.invalidate();
	}
	
	@SuppressLint("NewApi")
	private void resetPreviewViews() {
		// TODO Auto-generated method stub
		for(int i=0;i<mPreviewViews.length;i++)
			for(int j=0;j<mPreviewViews[0].length;j++){
				mPreviewViews[i][j].setSelected(false);
//				mPreviewViews[i][j].setBackground(getResources().getDrawable(R.drawable.gesture_create_grid_bg));
			}
		preView.invalidate();
	}
	
	
	/**
	 * 将一维数组转为化二维数组对应的九宫格
	 * @param ary
	 * @return
	 */
	private int[][] toPoints(String[] ary){
		int[][] points = new int[3][3];
		int x = 0;
		for(String str:ary){
			x = Integer.valueOf(str);
			if(x==0)
				points[0][0]= 1;
			else if(x==1)
				points[0][1] = 1;
			else if(x==2)
				points[0][2]=1;
			else if(x==3)
				points[1][0]=1;
			else if(x==4)
				points[1][1]=1;
			else if(x==5)
				points[1][2]=1;
			else if(x==6)
				points[2][0]=1;
			else if(x==7)
				points[2][1]=1;
			else if(x==8)
				points[2][2]=1;
		}
		return points;
	}

	private void setLinstener() {
		// TODO Auto-generated method stub
		//监听输入
		lpwv.setOnCompleteListener(new OnCompleteListener() {
			@Override
			public void onComplete(String mPassword) {
				times = times+1;
				if(times==1){
					// 第一次输入密码
					Message msg = Message.obtain();
					password = mPassword;
					msg.what = REFRESH;
					msg.obj = password;
					handler.sendMessage(msg);
					
					tvTip.setText(getResources().getString(R.string.tip_gesture_local_agin));
					try {
						Thread.sleep(500);//睡眠500ms
						lpwv.clearPassword();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					// 第二次输入密码
					if(password.equals(mPassword)){
						//两次密码相同，才能保存手势
//						btnSave.setClickable(true);
//						btnSave.setFocusable(true);
//						tvTip.setText(getResources().getString(R.string.tip_gesture_localed));
//						tvTip.setTextColor(0xffa3b377);
						lpwv.clearPassword();
						
						Intent intent = new Intent();
						intent.putExtra("password", password);
						setResult(RESULT_OK, intent);
						
						showToast(getResources().getString(R.string.tip_rember_pwd));
						finish();
					}else{
						lpwv.markError();
						tvTip.setText(getResources().getString(R.string.tip_gesture_error));
						tvTip.setTextColor(Color.RED);
						try {
							Thread.sleep(500);//睡眠500ms
							//lpwv.clearPassword();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			}
		});
		
		findViewById(R.id.bt_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

}
