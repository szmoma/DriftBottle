package com.hnmoma.driftbottle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.adapter.DqAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.UnionLoginModel;
import com.hnmoma.driftbottle.model.User;
import com.hnmoma.driftbottle.model.UserDataModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.sensitivewords.SensitivewordFilter;
import com.hnmoma.driftbottle.util.Base64;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/*
 * 用户资料
 */
public class PersonInfoActivity extends BaseActivity implements OnClickListener {

	protected static final int PHOTO_PICKED_WITH_DATA = 1001; //不高于api level 4.3使用
	 private static final int KITKAT_PHOTO_PICKED_WITH_DATA = 1003;  //不低于api level 4.4使用
	 
	private static final int SET_ALBUM_PICTURE_KITKAT = 1004;
	private static final int SET_ALBUM_PICTURE = 1005;
	
	private static final int SET_PICTURE = 1006; //对拍照的图片剪切
	
	protected static final int TAKE_A_PICTURE = 1007; //拍照片
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	boolean isSelf;// 查看的是否是自己的资料
	String userId;
	boolean isFirst;
//	String visitUserId;
	
	ImageButton bt_ok;
	Button bt_back;
	CircularImage iv_head;
	EditText etUsername;
	EditText etSign;
	TextView tv_title;
	TextView tvGender;
	TextView tvBirthday;
	TextView tvAge;
	TextView tv_constellation;//星座
	TextView tv_region;
	TextView tv_job;
	EditText etHobby;
	ImageView ivVIP;//vip徽章
	
	User user;
	
	DqAdapter dqadt;
	ListView lv;
	String resultString;
	boolean isUpdateUserHead;//用户是否更新头像
	private DatePickerDialog datePicker;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)         //加载开始默认的图片      
        .showImageForEmptyUri(R.drawable.defalutimg)     //url爲空會显示该图片，自己放在drawable里面的
        .showImageOnFail(R.drawable.defalutimg)                //加载图片出现问题，会显示该图片
        .cacheInMemory(true)                                               //缓存用
        .cacheOnDisc(true)                                                    //缓存用
        .considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
        .build();
		
		userId =getIntent().getStringExtra("userId");
		isFirst = getIntent().getBooleanExtra("isFirst", false);// 是否是新注册的用户
		isSelf = UserManager.getInstance(this).getCurrentUserId().equals(userId);
		datePicker = new DatePickerDialog(this, new DateSet(), 1995, 1, 1);
		initView();
		initData();
	}

	private void initView() {
		setContentView(R.layout.fragment_personinfo);
		bt_back = (Button) findViewById(R.id.bt_back);
		bt_ok = (ImageButton) findViewById(R.id.bt_ok);
		iv_head = (CircularImage) findViewById(R.id.iv_head);
		tv_title = (TextView) findViewById(R.id.tv_title);
		etSign = (EditText) findViewById(R.id.tv_sign);
		etUsername = (EditText) findViewById(R.id.tv_username);
		tvGender = (TextView) findViewById(R.id.tv_gender);
		tvBirthday = (TextView) findViewById(R.id.tv_birthday);
		tvAge = (TextView) findViewById(R.id.tv_age);
		tv_constellation = (TextView) findViewById(R.id.tv_constellation);
		tv_region = (TextView) findViewById(R.id.tv_region);
		tv_job = (TextView) findViewById(R.id.tv_job);
		etHobby = (EditText) findViewById(R.id.tv_hobby);
		ivVIP = (ImageView) findViewById(R.id.iv_vip);
		
		if(isFirst) {
			bt_back.setVisibility(View.GONE);
		}
		
		etSign.setSaveEnabled(false);
		etSign.setSaveEnabled(false);
		etHobby.setSaveEnabled(false);
	}
	
	private void initData() {
		if(isSelf) {
			user = UserManager.getInstance(this).getCurrentUser();
			iv_head.setOnClickListener(this);
			tv_title.setText(getResources().getString(R.string.tv_edit_personal));
			tvAge.setOnClickListener(this);
			tvBirthday.setOnClickListener(this);
			tv_constellation.setOnClickListener(this);
			tvGender.setOnClickListener(this);
			etHobby.setOnClickListener(this);
			tv_job.setOnClickListener(this);
			tv_region.setOnClickListener(this);
			etSign.setOnClickListener(this);
			etUsername.setOnClickListener(this);
			ivVIP.setOnClickListener(this);
		}else {
			tv_title.setText(getResources().getString(R.string.tv_personal));
			bt_ok.setVisibility(View.GONE);
			etUsername.setFocusable(false);
			etHobby.setFocusable(false);
			etSign.setFocusable(false);
		}
		getUserInfo();
	}
	
	// 填充界面数据
	private void setUserInfo(User user) {
		if(user.getUserId().equals(UserManager.getInstance(this).getCurrentUserId()))
			if(!TextUtils.isEmpty(user.getTempHeadImg()))//未审核通过
				imageLoader.displayImage(user.getTempHeadImg(), iv_head, options);
			if(TextUtils.isEmpty(user.getTempHeadImg())&&!TextUtils.isEmpty(user.getHeadImg())) //审核通过
					imageLoader.displayImage(user.getHeadImg(), iv_head, options);
		else
			imageLoader.displayImage(user.getHeadImg(), iv_head, options);
		etUsername.setText(user.getNickName());
		if(!TextUtils.isEmpty(user.getDescript())){
			etSign.setText(user.getDescript());
		}else
			etSign.setHint(getResources().getString(R.string.no_sign));
		
		if(TextUtils.isEmpty(user.getBirthday())) {
			if(isSelf){ //如果是自己
				tvBirthday.setText(getResources().getString(R.string.tip_select_birthday));
				tvBirthday.setTextColor(Color.parseColor("#999999"));
			} else {
				tvBirthday.setText("");
			}
			if(user.getAge()!=null&&user.getAge()>0)
				tvAge.setText(user.getAge()+"");
			else
				tvAge.setText("0");
			
			if(user.getConstell()!=null)
				tv_constellation.setText(user.getConstell());
			else
				tv_constellation.setText("");
		} else {
			tvBirthday.setTextColor(Color.parseColor("#666666"));
			tvBirthday.setText(user.getBirthday());
			tvAge.setText(user.getAge()+"");
			tv_constellation.setText(user.getConstell());
		}
		String identity = user.getIdentityType();
		if("2000".equals(identity) || "2001".equals(identity) || "2002".equals(identity) || "2007".equals(identity) ) {
			tvGender.setText("女");
		} else {
			tvGender.setText("男");
		}
		
		if(TextUtils.isEmpty(user.getHobby())) {
			etHobby.setText("");
		} else {
			etHobby.setText(user.getHobby());
		}
		String job = TextUtils.isEmpty(user.getJob()) ? "无" : user.getJob();
		tv_job.setText(job);
		if(TextUtils.isEmpty(user.getProvince())) {
			String city = user.getCity()==null? "" : user.getCity();
			tv_region.setText(city);
		} else {
			tv_region.setText(user.getProvince()+ "-" + user.getCity());
		}
		if(user.getIsVIP()!=null&&user.getIsVIP()==1){//vip用户
			ivVIP.setImageDrawable(getResources().getDrawable(R.drawable.ic_vip_big2));
		}else{
			ivVIP.setImageDrawable(getResources().getDrawable(R.drawable.ic_vip_not_big));
		}
		
	}
	
	// 查询用户资料信息
	private void getUserInfo() {
		// success set user
		JsonObject jo = new JsonObject();
		jo.addProperty("id", userId);

		BottleRestClient.post("queryUserInfo", this, jo, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				super.onStart();
				showDialog("努力加载....", "加载失败...", 15*1000);
			}
	
			@Override
			public void onFinish() {
				super.onFinish();
				closeDialog(mpDialog);
			}
	
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					UserDataModel model = gson.fromJson(str, UserDataModel.class);
					
					if (model != null && !TextUtils.isEmpty(model.getCode())) {
						if ("0".equals(model.getCode())) {
							user = model.getUserInfo();
							setUserInfo(user);
						} else {
							showMsg(model.getMsg());
						}
					} else {
						showMsg("服务器繁忙");
					}
				} else {
					showMsg("服务器繁忙");
				}
			}
	
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				closeDialog(mpDialog);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_back:
			this.finish();
			break;
		case R.id.bt_ok:
			// 检查提交
			if(check()) {
				doSubmit(v);
			}
			break;
		case R.id.iv_head:
			showPickDialog();
			break;
		case R.id.tv_gender:
//			String[] item={"男","女"};
//			new CustomDialog().showListDialog(this, "请选择您的性别",item,new CustomDialogItemClickListener() {
//				@Override
//				public void confirm(String result) {
//					tv_gender.setText(result);
//				}
//			});
			break;
		case R.id.tv_birthday:
			showDatePicker();
			break;
		case R.id.tv_region:
			Intent province = new Intent(this, InfomationActivity.class);
			province.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			province.putExtra("category", "province");
			startActivityForResult(province, 400);
			break;
		case R.id.tv_job:
			Intent job = new Intent(this, InfomationActivity.class);
			job.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			job.putExtra("category", "job");
			startActivityForResult(job, 500);
			break;
		case R.id.iv_vip:
			Intent vip = new Intent(this, VIPIntroductionActivity.class);
			vip.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			vip.putExtra("category", "job");
			startActivity(vip);
			break;
		}
		
	}
	
	// 生日选择框
	private void showDatePicker() {
		// TODO Auto-generated method stub
		String userBirthDay = user.getBirthday();
		int year ;
		int monthy ;
		int day ;
		if(!MoMaUtil.isDateFormat(userBirthDay)){
			 year = 1995;
			 monthy = 0;
			 day = 1;
		}else{
			try {
				String pattern = "yyyy-MM-dd";
				SimpleDateFormat sdf = new SimpleDateFormat(pattern);
				Date date = sdf.parse(userBirthDay);
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				year = cal.get(Calendar.YEAR);
				monthy = cal.get(Calendar.MONTH);
				day = cal.get(Calendar.DAY_OF_MONTH);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				 year = 1995;
				 monthy = 0;
				 day = 1;
			}
		}
		this.datePicker.updateDate(year, monthy, day);
		datePicker.setTitle("生日");
		datePicker.show();
	}
	// 监听datepicker 日期变化
	class DateSet implements OnDateSetListener {
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Date date = new Date(year-1900, monthOfYear+1, dayOfMonth);
			int age = getAge(date);
			if(age == -1) {
				return;
			}
			//格式化月份和天，必须是占有两位，不足者前者加0
			String month = null;
			if((monthOfYear+1)<10)
				month ="0"+(monthOfYear+1);
			else
				month = ""+(monthOfYear+1);
			String day =null;
			if(dayOfMonth<10)
				day ="0"+dayOfMonth;
			else
				day = ""+dayOfMonth;
			
			String birth = year +"-" + month + "-" + day;
			String constellation  = getAstro(monthOfYear+1, dayOfMonth); //星座
			tvBirthday.setText(birth);
			tvAge.setText(age+"");
			tv_constellation.setText(constellation);
		}
	}
	
	private String getAstro(int month, int day) {
        String[] astro = new String[] { "摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座",
                        "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座" };
        int[] arr = new int[] { 20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22, 22 };// 两个星座分割日
        int index = month;
        // 所查询日期在分割日之前，索引-1，否则不变
        if (day < arr[month - 1]) {
                index = index - 1;
        }
        // 返回索引指向的星座string
        return astro[index];
	}
	
	/**
	  * @param brithday
	  * @return
	  *   根据生日获取年龄;
	  */
	public int getAge(Date birthDay) {
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthDay)) {
        	showMsg("日期超过今天，请重新输入");
        	return -1;
        }

        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                //monthNow==monthBirth
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                } else {
                    //do nothing
                }
            } else {
                //monthNow>monthBirth
                age--;
            }
        } else {
            //monthNow<monthBirth
            //donothing
        }

        return age;
    }

	
	
	private boolean check() {
		// TODO 检查是否更改，敏感词检查
		String dest = "";
		SensitivewordFilter filter = MyApplication.getApp().getSensitiveFilter();
		// 检查是否改变
		String username = etUsername.getText().toString().trim();
		String signature = etSign.getText().toString().trim();
		String hobby = etHobby.getText().toString().trim();
		if(TextUtils.isEmpty(username)){
			showMsg("昵称不能为空");
			return false;
		}else if(username.length()>15){
			showMsg("昵称太长");
		}else{
	        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
	        Matcher m = p.matcher(username);
	        dest = m.replaceAll("");
			//敏感词处理
			Set<String> set = filter.getSensitiveWord(dest, 1);
			if(set.size()!=0){
				showMsg("昵称含有敏感词");
				return false;
			}
		}
		// 检查签名
		if(!TextUtils.isEmpty(signature)) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
	        Matcher m = p.matcher(signature);
	        dest = m.replaceAll("");
			Set<String> set = filter.getSensitiveWord(dest, 1);
			if(set.size()!=0){
				showMsg("签名含有敏感词");
				return false;
			}
		}
		// 检查兴趣爱好
		if(!TextUtils.isEmpty(hobby)) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
	        Matcher m = p.matcher(hobby);
	        dest = m.replaceAll("");
			Set<String> set = filter.getSensitiveWord(dest, 1);
			if(set.size()!=0){
				showMsg("爱好含有敏感词");
				return false;
			}
		}
		
		
		String birthDay = tvBirthday.getText().toString().trim();
		if(!MoMaUtil.isDateFormat(birthDay)||getResources().getString(R.string.tip_select_birthday).equals(birthDay)){
			showMsg(getResources().getString(R.string.tip_select_birthday));
			return false;
		}
		return true;
	}
	/*
	 * 头像设置
	 */
	final String[] strarray = new String[] {"拍照", "从图库选择"};
	String cacheDir = MyApplication.mAppPath + "/.temp/upload/";
	File head;
	
	private void showPickDialog() {
		new AlertDialog.Builder(this)
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
							startActivityForResult(intent, TAKE_A_PICTURE);
				    	 }else if(which == 1){
				    		dialog.dismiss();
				    		//根据用户交互：系统不低于4.4时候，也要采用selectImageUri()方法浏览相册
//				    		if(android.os.Build.VERSION.SDK_INT >=android.os.Build.VERSION_CODES.KITKAT){
//				    			selectImageUriAfterKikat();
//				    		}else{
				    			selectImageUri();
//				    		}
				    			 
				    	 }
				     }
			    })
			.show();
	}
	
	/*
	 * 提交保存资料
	 */
	public void doSubmit(final View view){
		boolean isChange = false;	//如果用户的信息发生改变，则为true，否则为false;
		
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		//修改的字段，才能提交
		if(TextUtils.isEmpty(user.getNickName())){
			if(!TextUtils.isEmpty(etUsername.getText())){
				jo.addProperty("nickName", etUsername.getText().toString().trim());
				isChange = true;
			}
		}else{
			if(!user.getNickName().equals(etUsername.getText().toString().trim())){
				jo.addProperty("nickName", etUsername.getText().toString().trim());
				isChange = true;
			}
		}
		
		String[] regions = tv_region.getText().toString().trim().split("-");
		if(TextUtils.isEmpty(user.getProvince())){
			if(regions.length>1&&!TextUtils.isEmpty(regions[0])){
				jo.addProperty("province", regions[0]);
				isChange = true;
			}
		}else{
			if(!user.getProvince().equals( regions[0])){
				jo.addProperty("province", regions[0]);
				isChange = true;
			}
		}
		
		if(TextUtils.isEmpty(user.getCity())){
			if(regions.length>1&&!TextUtils.isEmpty(regions[1])){
				jo.addProperty("city", regions[1]);
				isChange = true;
			}
		}else{
			if(regions.length>1&&!TextUtils.isEmpty(regions[1])&&!user.getCity().equals(regions[1])){
				jo.addProperty("city", regions[1]);
				isChange = true;
			}
		}
		
		int identityType =2006;
		if("女".equals(tvGender.getText().toString().trim()))	 {
			identityType = 2007;
		}
		if(TextUtils.isEmpty(user.getIdentityType())){
			if(!TextUtils.isEmpty(String.valueOf(identityType))){
				jo.addProperty("identityType", identityType);
				isChange = true;
			}
		}else{
			if(!user.getIdentityType().equals(String.valueOf(identityType))){
				jo.addProperty("identityType", identityType);
				isChange = true;
			}
		}
		
		try {
			if(TextUtils.isEmpty(user.getHeadImg())){
				if(head!=null){
					FileInputStream fis = new FileInputStream(head);
					String headStrs = Base64.encode(MoMaUtil.inputStreamToByte(fis));
//					String headStrs = android.util.Base64.encodeToString(MoMaUtil.inputStreamToByte(new FileInputStream(head)), android.util.Base64.DEFAULT); 
					
					jo.addProperty("headImg", headStrs);
					isChange = true;
					isUpdateUserHead = isChange;
					fis.close();
					fis = null;
				}
			}else{
				if(head!=null&&!TextUtils.isEmpty(head.getPath())&&!user.getHeadImg().equals(head.getPath())){
					FileInputStream fis = new FileInputStream(head);
					String headStrs = Base64.encode(MoMaUtil.inputStreamToByte(fis));
					jo.addProperty("headImg", headStrs);
					isChange = true;
					isUpdateUserHead = isChange;
					fis.close();
					fis = null;
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(TextUtils.isEmpty(user.getBirthday())){
			if(!TextUtils.isEmpty(tvBirthday.getText())){//出生日期发生改变,年龄和星座可能发生改变
				jo.addProperty("birthday", tvBirthday.getText().toString().trim());
				isChange = true;
				if(user.getAge()==null||(user.getAge()!=null&&user.getAge()!=Integer.valueOf(tvAge.getText().toString()))){
					jo.addProperty("age", tvAge.getText().toString().trim());
					isChange = true;
				}
				if(TextUtils.isEmpty(user.getConstell())||(user.getConstell()!=null&&!user.getConstell().equals(tv_constellation.getText().toString().trim()))){
					jo.addProperty("constell", tv_constellation.getText().toString().trim());
					isChange = true;
				}
			}
		}else{
			if(!user.getBirthday().equals(tvBirthday.getText().toString().trim())){
				jo.addProperty("birthday", tvBirthday.getText().toString().trim());
				isChange = true;
				if(user.getAge()==null||(user.getAge()!=null&&user.getAge()!=Integer.valueOf(tvAge.getText().toString()))){
					jo.addProperty("age", tvAge.getText().toString().trim());
					isChange = true;
				}
				if(TextUtils.isEmpty(user.getConstell())||(user.getConstell()!=null&&!user.getConstell().equals(tv_constellation.getText().toString().trim()))){
					jo.addProperty("constell", tv_constellation.getText().toString().trim());
					isChange = true;
				}
			}
		}
		
		if(TextUtils.isEmpty(user.getJob())){
			if(!TextUtils.isEmpty( tv_job.getText())&&!"无".equals(tv_job.getText().toString().trim())){
				jo.addProperty("job", tv_job.getText().toString().trim());
				isChange = true;
			}
		}else{
			if(!user.getJob().equals(etHobby.getText().toString().trim())&&!"无".equals(tv_job.getText().toString().trim())){
				jo.addProperty("job", tv_job.getText().toString().trim());
				isChange = true;
			}
		}
		
		if(TextUtils.isEmpty(user.getHobby())){
			if(!TextUtils.isEmpty(etHobby.getText())){
				jo.addProperty("hobby", etHobby.getText().toString().trim());
				isChange = true;
			}
		}else{
			if(!user.getHobby().equals(etHobby.getText().toString().trim())){
				jo.addProperty("hobby", etHobby.getText().toString().trim());
				isChange = true;
			}
		}
		
		if(TextUtils.isEmpty(user.getDescript())){
			if(!TextUtils.isEmpty(etSign.getText())){
				jo.addProperty("descript", etSign.getText().toString().trim());
				isChange = true;
			}
		}else {
			if(!user.getDescript().equals(etSign.getText().toString().trim())){
				jo.addProperty("descript", etSign.getText().toString().trim());
				isChange = true;
			}
		}
		
		if(!isChange){
			showMsg("没必要保存用户信息!");
			return ;
		}
		
		BottleRestClient.post("updateUserInfo", this, jo, new AsyncHttpResponseHandler (){
			@Override
			public void onStart() {
				super.onStart();
				view.setEnabled(false);
				showDialog("提交中...","提交失败...",15*1000);
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				view.setEnabled(true);
				closeDialog(mpDialog);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					//TODO 
					Gson gson = new Gson();
					UnionLoginModel ulm = gson.fromJson(str, UnionLoginModel.class);
					if(ulm!=null&&"0".equals(ulm.getCode())){
						if( ulm.getUserInfo()==null){
							showMsg("修改失败");
							return ;
						}
							
						showMsg("修改成功");
						UserManager.getInstance(PersonInfoActivity.this).login(ulm.getUserInfo());
						//如果用户更新了头像，则发送广播，更改用户头像
						if(isUpdateUserHead){
							Intent intent = new Intent();  
							intent.setAction(MyConstants.USERINFOCHANGE_BROADCAST_ACTION);  
							PersonInfoActivity.this.sendBroadcast(intent);  
						}
						
						Intent intent = new Intent();
						intent.putExtra("UserInfoModel", ulm.getUserInfo());
						setResult(Activity.RESULT_OK,intent);
						
						PersonInfoActivity.this.finish();
					}else {
						showMsg("修改失败");
						MoMaLog.e("debug",ulm.getMsg() );
					}
				}else{
					showMsg("修改失败");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("修改失败");
				MoMaLog.e("debug",String.valueOf(responseBody));
				closeDialog(mpDialog);
			}
        });
	}
	
	/*
	 *获取用户性别
	 */
	public String getIdentityByCode(String code){
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
	
	public String getIdentityCode(String name){
		Map<String, String> map = new HashMap<String, String>();
		map.put("小萝莉", "2000");
		map.put("花样少女", "2001");
		map.put("知性熟女", "2002");
		map.put("小正太", "2003");
		map.put("魅力少年", "2004");
		map.put("成熟男生", "2005");
		map.put("男", "2006");
		map.put("女", "2007");
		
		return map.get(name);
	}
	
	@Override
	public void onBackPressed() {
		if(isFirst) {
			return;
		}
		super.onBackPressed();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==RESULT_OK){
			switch (requestCode) {
			// 如果是直接从相册获取
			case PHOTO_PICKED_WITH_DATA:
				MoMaLog.d("debug", "android 不高于4.3");
				if(data!=null){
					cropImageUri(data.getData());
				}
				break;
			case KITKAT_PHOTO_PICKED_WITH_DATA:
				MoMaLog.d("debug", "android 不低于4.4");
				if(data!=null){
					cropImageUriAfterKikat(data.getData());
				}
				break;
			
			case TAKE_A_PICTURE:// 如果是调用相机拍照时，对照片的裁剪
				File temp = new File(cacheDir, "headimg.jpg");
				cameraCropImageUri(Uri.fromFile(temp));
				break;
			case SET_PICTURE://拍照后经过裁剪的设置头像  不考虑版本
				MoMaLog.e("debug","摄像机");
				if (data != null) {
					setPicToView(data);
				}
				break;
			// 取得裁剪后的图片
			case SET_ALBUM_PICTURE:
				/**
				 * 非空判断一定要验证，如果不验证的话， 在剪裁之后如果发现不满意，要重新裁剪，丢弃
				 * 当前功能时，会报NullException，只 在这个地方加下，大家可以根据不同情况在合适的 地方做判断处理类似情况
				 * 
				 */
				MoMaLog.e("debug", "不高于4.3");
				if (data != null) {
					setPicToView(data);
				}
				break;
			case SET_ALBUM_PICTURE_KITKAT:
				MoMaLog.e("debug", "不低于4.4");
				if (data != null) {
					setPicToView(data);
				}
				break;
			case 400: //地区职业修改
				if(resultCode == Activity.RESULT_OK && data!=null){
					String resultString = data.getStringExtra("resultString");
					tv_region.setText(resultString);
				}
				break;
			case 500:
				if(resultCode == Activity.RESULT_OK && data!=null){
					String resultString = data.getStringExtra("resultString");
					tv_job.setText(resultString);
				}
				break;

			}
		}
		super.onActivityResult(requestCode, resultCode, data);
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
				FileOutputStream fos = new FileOutputStream(new File(cacheDir, "imgcrop.jpg"));
				headImg.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				fos.close();
				head = new File(cacheDir, "imgcrop.jpg");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			iv_head.setImageBitmap(headImg);
		}
	}
	
	
	/** 
	 * <br>功能简述:对拍照的图片剪切------相机
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param uri
	 */
	private void cameraCropImageUri(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/jpeg");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		intent.putExtra("scale", true);
//		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) {
//			intent.putExtra("return-data", true);
//		} else {
			intent.putExtra("return-data", true);
//			intent.putExtra("return-data", false);
//			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//		}
		
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true);
		startActivityForResult(intent, SET_PICTURE);
	}
	
	/**
	 *<br/> 功能简述:4.4以下从相册选照片并剪切--- 相册
	 * <br/>裁剪图片方法实现
	 * 
	 * @param uri
	 */

	public void cropImageUri(Uri uri) {
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
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		intent.putExtra("return-data", true);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivityForResult(intent, SET_ALBUM_PICTURE);
	}
	
	/** 
	 * <br>功能简述: 4.4及以上选取照片后剪切方法--- 相册
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param uri
	 */
	private void cropImageUriAfterKikat(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true); //返回数据bitmap
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, SET_ALBUM_PICTURE_KITKAT);
	}
	
	/** 
	 * <br>功能简述:4.4及以上从相册选择照片 
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public void selectImageUriAfterKikat() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		startActivityForResult(intent, KITKAT_PHOTO_PICKED_WITH_DATA);
	}
	/**
	 * <br>功能简述：api level 不高于4.3时，从相册选择照片
	 */
	public void selectImageUri(){
		Intent intent = new Intent();
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		intent.setAction(Intent.ACTION_PICK); 
		startActivityForResult(intent, PHOTO_PICKED_WITH_DATA); 
	}
}
