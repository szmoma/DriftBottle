package com.hnmoma.driftbottle.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hnmoma.driftbottle.AppendPersonInfoActivity;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.custom.CustomDialog;
import com.hnmoma.driftbottle.custom.CustomDialog.CustomDialogItemClickListener;
import com.hnmoma.driftbottle.itfc.IFragmentCallback;
import com.hnmoma.driftbottle.model.User;
import com.hnmoma.driftbottle.sensitivewords.SensitivewordFilter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 用户的基本信息，比如用户头像，昵称，密码等
 * @author Administrator
 *
 */
public class FragmentUserInfoBasic extends Fragment {
	public final static String PATH="headUri";
	public final static String NICK="nickName";
	public final static String SEX = "sex";
	
	private static FragmentUserInfoBasic fragment;
	private IFragmentCallback callback;
	
	private Button btnNext;
	
	private final String[] strarray = new String[] {"拍照", "从图库选择"};
	private String cacheDir = MyApplication.mAppPath + "/.temp/upload/";	//上传头像的文件目录
	
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	
	private View root;
	private CircularImage ivHead;//头像
	private EditText etNickName;	//昵称
	private TextView tvSex;
	private TextView tvSexTip;
	
	private User user;
	
	private String headUri;	//头像路径
	private String nickName;	//昵称
	private String strSex;	//性别
	
	private PopupWindow mPopupWindow = null;
	
	// 屏幕的宽高
	public static int screen_width = 0;
	public static int screen_height = 0;
	
	private TranslateAnimation animation;// 出现的动画效果
	
//	private View window;
	private RadioGroup rgMale,rgFemal;
	
	public static FragmentUserInfoBasic newInstance(Bundle bundle){
		if(fragment==null)
			fragment = new FragmentUserInfoBasic();
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putString(PATH, headUri);
		outState.putString(NICK, nickName);
		outState.putString(SEX, strSex);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if(savedInstanceState!=null){
			headUri = savedInstanceState.getString(PATH);
			nickName = savedInstanceState.getString(NICK);
			strSex = savedInstanceState.getString(SEX);
		}
		Bundle args = getArguments();
		if(args!=null){
			user = (User) args.getSerializable("user");
			headUri = user.getHeadImg();
			strSex = user.getIdentityType();
			nickName = user.getNickName();
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		 root = inflater.inflate(R.layout.fragment_add_userinfo_basic, container,false);
		initConfig();
		setupView(root);
		setLinstener();
		init();
		return root;
	}

	private void initConfig() {
		// TODO Auto-generated method stub
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.avatar_default)         //加载开始默认的图片      
        .showImageForEmptyUri(R.drawable.avatar_default)     //url爲空會显示该图片，自己放在drawable里面的
        .showImageOnFail(R.drawable.avatar_default)                //加载图片出现问题，会显示该图片
        .cacheInMemory(true)                                               //缓存用
        .cacheOnDisc(true)                                                    //缓存用
        .considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
        .build();
		
		// 获取手机屏幕的大小
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm); 
		screen_width = dm.widthPixels;
		screen_height = dm.heightPixels;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			callback =  (IFragmentCallback) activity;
		} catch (ClassCastException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		 callback = null;
	}


	private void setupView(View view) {
		// TODO Auto-generated method stub
		btnNext = (Button) view.findViewById(R.id.btn_next);
		ivHead = (CircularImage) view.findViewById(R.id.iv_head);
		etNickName = (EditText) view.findViewById(R.id.et_nickname);
		tvSex = (TextView) view.findViewById(R.id.tv_content);
		tvSexTip = (TextView) view.findViewById(R.id.tv_tip_sex);
	}
	/**
	 * 初始化控件，数据与控件进行绑定
	 */
	private void init() {
		// TODO Auto-generated method stub
		String tip = getActivity().getResources().getString(R.string.tip_sex);
		tvSexTip.setText(tip);
        
		// 获取控件在屏幕中的位置,方便展示Popupwindow
		int[] location = new int[2];
		root.findViewById(R.id.ll_contain).getLocationOnScreen(location);
		animation = new TranslateAnimation(0, 0, -700, location[1]);
		animation.setDuration(500);
		
		if(!TextUtils.isEmpty(headUri)){
			if(headUri.startsWith("http")){
				//如果是网络地址
				imageLoader.displayImage(headUri, ivHead, options);
			}else{
				BitmapFactory.Options op = new BitmapFactory.Options();
				op.inJustDecodeBounds = true;
				Bitmap bmp = BitmapFactory.decodeFile(headUri, op); //获取尺寸信息
				//获取比例大小
				int wRatio = (int)Math.ceil(op.outWidth/100);
				int hRatio = (int)Math.ceil(op.outHeight/100);
				//如果超出指定大小，则缩小相应的比例
				if(wRatio > 1 && hRatio > 1){
					if(wRatio > hRatio){
						op.inSampleSize = wRatio;
					}else{
						op.inSampleSize = hRatio;
					}
				}
				op.inJustDecodeBounds = false;
				bmp = BitmapFactory.decodeFile(headUri, op);
				ivHead.setImageBitmap(bmp);
			}
		}
		if("男".equalsIgnoreCase(strSex)||"女".equalsIgnoreCase(strSex))
			tvSex.setText(strSex);
		etNickName.setSaveEnabled(false);
		
		if(TextUtils.isEmpty(nickName)){
			etNickName.setHint(getActivity().getString(R.string.hint_nikcname));
		}else{
			etNickName.setText(nickName);
		}
		
		
	}
	
	private void setLinstener() {
		// TODO Auto-generated method stub
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//如果验证通过，启动另一个fragment
				Bundle arg = new Bundle();
				if(TextUtils.isEmpty(headUri)){
					showMsg("请上传头像");
					return ;
				}
				if(TextUtils.isEmpty(etNickName.getText())){
					showMsg("请输入昵称");
					return ;
				}
				
				if(TextUtils.isEmpty(tvSex.getText())||getActivity().getResources().getString(R.string.tip_selector_sex).toString().equals(tvSex.getText().toString())){
					showMsg("请输入性别");
					return ;
				}
				strSex = tvSex.getText().toString();
				nickName = etNickName.getText().toString();
				if(checkSensitiveWord(nickName))
					return ;
				arg.putString(PATH, headUri);	//图片位置
				arg.putCharSequence(SEX, strSex);
				arg.putCharSequence(NICK, nickName);
				callback.startDetailFragment(arg,AppendPersonInfoActivity.OPERADDR);
			}
		});
		
		ivHead.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showPickDialog();
			}
		});
		MyOnclickListener listener = new MyOnclickListener();
		root.findViewById(R.id.ll_content).setOnClickListener(listener);
		root.findViewById(R.id.tv_lable).setOnClickListener(listener);
		root.findViewById(R.id.tv_content).setOnClickListener(listener);
		root.findViewById(R.id.iv_arrow).setOnClickListener(listener);
		
	}
	
	class MyOnclickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.ll_content:
			case R.id.tv_lable:
			case R.id.tv_content:
			case R.id.iv_arrow:
//				showPupupWindow();
				String[] item={"男","女"};
				new CustomDialog().showListDialog(getActivity(), "请选择您的性别",item,new CustomDialogItemClickListener() {
					@Override
					public void confirm(String result) {
						tvSex.setText(result);
					}
				});
				break;

			default:
				break;
			}
		}
		
	}
	
	/**
	 * 初始化 PopupWindow
	 * 
	 * @param view
	 */
	public void initPopuWindow(View view) {
		/* 第一个参数弹出显示view 后两个是窗口大小 */
		int width = (int) (screen_width*0.9);
		int height = (int) (screen_height*0.9);
		mPopupWindow = new PopupWindow(view, width, height);
		/* 设置背景显示 */
		mPopupWindow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.base_action_bar_bg));
		/* 设置触摸外面时消失 */
		// mPopupWindow.setOutsideTouchable(true);

		mPopupWindow.update();
		mPopupWindow.setTouchable(true);
		/* 设置点击menu以外其他地方以及返回键退出 */
		mPopupWindow.setFocusable(true);

		/**
		 * 1.解决再次点击MENU键无反应问题 2.sub_view是PopupWindow的子View
		 */
		view.setFocusableInTouchMode(true);
	}
	
	class MyOnCheckedChangeListener implements OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			boolean hasGender = false;
			try {
				String identity =UserManager.getInstance(getActivity()).getCurrentUser().getIdentityType();
				if(!TextUtils.isEmpty(identity)) {
					hasGender = true;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(rgMale == group){
				if(hasGender) {
					rgFemal.setVisibility(View.GONE);
				} else {
					rgFemal.setOnCheckedChangeListener(null);
					rgFemal.clearCheck();
					rgFemal.setOnCheckedChangeListener(this);
				}
			}else{
				if(hasGender) {
					rgMale.setVisibility(View.GONE);
				} else {
					rgMale.setOnCheckedChangeListener(null);
					rgMale.clearCheck();
					rgMale.setOnCheckedChangeListener(this);
				}
				
			}
			switch(group.getCheckedRadioButtonId()){
				case R.id.rb_11:
					strSex = "男";
					break;
				case R.id.rb_12:
					strSex = "小正太";
					break;
				case R.id.rb_13:
					strSex = "魅力少年";
					break;
				case R.id.rb_14:
					strSex = "成熟男生";
					break;
				case R.id.rb_21:
					strSex = "女";
					break;
				case R.id.rb_22:
					strSex = "小萝莉";
					break;
				case R.id.rb_23:
					strSex = "花样少女";
					break;
				case R.id.rb_24:
					strSex = "知性熟女";
					break;
			}
			mPopupWindow.dismiss();
			tvSex.setText(strSex);
		}
		
	}
	
	public int getViewIdBykey(String key){
		Map<String,Integer> map = new HashMap<String,Integer>();
		map.put("小萝莉", R.id.rb_22);
		map.put("花样少女", R.id.rb_23);
		map.put("知性熟女", R.id.rb_24);
		map.put("小正太", R.id.rb_12);
		map.put("魅力少年", R.id.rb_13);
		map.put("成熟男生", R.id.rb_14);
		map.put("男", R.id.rb_11);
		map.put("女", R.id.rb_21);
		
		return (Integer) map.get(key);
	}
	
	
	/**
	 * 显示上传头像的对话框
	 */
	private void showPickDialog() {
		new AlertDialog.Builder(getActivity())
			.setTitle("头像")
			.setItems(strarray, new DialogInterface.OnClickListener() {
				     @Override
				     public void onClick(DialogInterface dialog, int which) {
				    	 if(which == 0){
				    		dialog.dismiss();
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							// 下面这句指定调用相机拍照后的照片存储的路径
							File file = new File(cacheDir);
							if (!file.exists()) {
								file.mkdirs();
							}
							intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(cacheDir, "headimg.jpg")));
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
							startActivityForResult(intent, 2);
				    	 }else if(which == 1){
				    		dialog.dismiss();
							Intent intent = new Intent(Intent.ACTION_PICK, null);
							intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
							startActivityForResult(intent, 1);
				    	 }
				     }
			    })
			.show();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		// 如果是直接从相册获取
		case 1:
			if(data!=null){
				startPhotoZoom(data.getData());
			}
			break;
		// 如果是调用相机拍照时
		case 2:
			File temp = new File(cacheDir, "headimg.jpg");
			startPhotoZoom(Uri.fromFile(temp));
			break;
		// 取得裁剪后的图片
		case 3:
			/**
			 * 非空判断大家一定要验证，如果不验证的话， 在剪裁之后如果发现不满意，要重新裁剪，丢弃
			 * 当前功能时，会报NullException，只 在这个地方加下，大家可以根据不同情况在合适的 地方做判断处理类似情况
			 * 
			 */
			if (data != null) {
				setPicToView(data);
			}
			break;
		
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */

	public void startPhotoZoom(Uri uri) {
		/*
		 * 至于下面这个Intent的ACTION是怎么知道的，大家可以看下自己路径下的如下网页
		 * yourself_sdk_path/docs/reference/android/content/Intent.html
		 * 直接在里面Ctrl+F搜：CROP ，之前没仔细看过，其实安卓系统早已经有自带图片裁剪功能, 是直接调本地库的
		 */
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 100);
		intent.putExtra("outputY", 100);
		intent.putExtra("return-data", true);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivityForResult(intent, 3);
	}

	/**
	 * 保存裁剪之后的图片数据
	 * 
	 * @param picdata
	 */
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			Bitmap headImg = extras.getParcelable("data");
			
			try {
				File file = new File(cacheDir);
				if (!file.exists()) {
					file.mkdirs();
				}
				File tmp = new File(cacheDir, "imgcrop.jpg");
				
				headUri = tmp.getPath();
				FileOutputStream fos = new FileOutputStream(headUri);
				headImg.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				fos.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			ivHead.setImageBitmap(headImg);
			
		}
	}
	
	private String getIdentityByCode(String code){
		Map<String, String> map = new HashMap<String, String>();
		map.put("2000", "小萝莉");
		map.put("2001", "花样少女");
		map.put("2002", "知性熟女");
		map.put("2003", "小正太");
		map.put("2004", "魅力少年");
		map.put("2005", "成熟男生");
		map.put("2006", "男");
		map.put("2007", "女");
		
		return map.get(code);
	}
	
	private void showMsg(String msg){
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}
	/**
	 * 检查字符串是否含有敏感词
	 * @param msg 需要检查的字符串
	 * @return 如果字符串中含有敏感词汇，则返回true，否则返回false
	 */
	private boolean checkSensitiveWord(String msg){
		SensitivewordFilter filter = MyApplication.getApp().getSensitiveFilter();
		if(TextUtils.isEmpty(msg)){
			return false;
		}else{
	        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
	        Matcher m = p.matcher(msg);
	        msg = m.replaceAll(""); //去掉空格
			//敏感词处理
			Set<String> set = filter.getSensitiveWord(msg, 1);
			if(set.size()!=0){
				showMsg("请注意，含有敏感词汇");
				return true;
			}
		}
		
		return false;
	}

}
