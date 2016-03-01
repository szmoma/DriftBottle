package com.hnmoma.driftbottle.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.hnmoma.driftbottle.AppendPersonInfoActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.itfc.IFragmentCallback;
import com.hnmoma.driftbottle.model.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 用户的更多信息，添加用户的生日
 * @author Administrator
 *
 */
public class FragmentUserInfoBirthDay extends Fragment {
	public final static String BIRTHDAY="birthDay";
	
	private static FragmentUserInfoBirthDay fragment;
	private IFragmentCallback callback;
	
	private Button btnSubmit;
	
	private TextView tvBirthDay;
	private View root;
	
	private String strDate;
	private CircularImage mCircularImage;
	
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	
	public static Fragment newInstance(Bundle arg) {
		// TODO Auto-generated method stub
		if(fragment==null)
			fragment = new FragmentUserInfoBirthDay();
		fragment.setArguments(arg);
		return fragment;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState!=null){
			strDate = savedInstanceState.getString(BIRTHDAY);
		}
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.avatar_default)         //加载开始默认的图片      
        .showImageForEmptyUri(R.drawable.avatar_default)     //url爲空會显示该图片，自己放在drawable里面的
        .showImageOnFail(R.drawable.avatar_default)                //加载图片出现问题，会显示该图片
        .cacheInMemory(true)                                               //缓存用
        .cacheOnDisc(true)                                                    //缓存用
        .considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
        .build();
	};
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putString(BIRTHDAY, strDate);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		root = inflater.inflate(R.layout.fragment_add_userinfo_more, container,false);
		setupView(root);
		init();
		setListener()	;
		return root;
	}
	
	private void init() {
		// TODO Auto-generated method stub
		((TextView)root.findViewById(R.id.tv_lable)).setText(getActivity().getResources().getString(R.string.tv_birthday));
		TextView tvTip = (TextView)root.findViewById(R.id.tv_tip);
		tvTip.setText(getActivity().getResources().getString(R.string.tip_birthday));
		tvTip.setVisibility(View.GONE);
		
		String headUri = null; //头像地址
		Bundle args = getArguments();
		if(args!=null){
			User user = (User) args.getSerializable("user");
			strDate = user.getBirthday();
			headUri = user.getHeadImg();
		}
		
		if(!TextUtils.isEmpty(headUri)){
			if(headUri.startsWith("http")){
				//如果是网络地址
				imageLoader.displayImage(headUri, mCircularImage, options);
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
				mCircularImage.setImageBitmap(bmp);
			}
		}
		if(TextUtils.isEmpty(strDate)){
			tvBirthDay.setText(getActivity().getResources().getString(R.string.tip_select_birthday));
		}else
			tvBirthDay.setText(strDate);
	}

	private void setListener() {
		// TODO Auto-generated method stub
		btnSubmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//验证填写的信息
				if(TextUtils.isEmpty(strDate)||getActivity().getResources().getString(R.string.tip_select_birthday).toString().equals(strDate)){
					Toast.makeText(getActivity(), "请选择出生日期", Toast.LENGTH_SHORT).show();
					return ;
				}
				
				Calendar currnetCal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				 Date birthDay = null;
				   try {
				    birthDay = sdf.parse(strDate);
				   } catch (ParseException e) {
				    e.printStackTrace();
				   }
				Calendar birthCal = Calendar.getInstance();
				birthCal.setTime(birthDay);
		        if (currnetCal.before(birthCal)) {
		        	Toast.makeText(getActivity(), "日期超过今天，请重新输入",Toast.LENGTH_SHORT).show();;
		        	return ;
		        }
					
				Bundle args = new Bundle();
			    args.putString(BIRTHDAY, strDate);
			    callback.startDetailFragment(args, AppendPersonInfoActivity.OPERSUBMIT);
			}
		});
		
		MyOnclickListener listener = new MyOnclickListener();
		root.findViewById(R.id.ll_content).setOnClickListener(listener);
		root.findViewById(R.id.tv_lable).setOnClickListener(listener);
		root.findViewById(R.id.tv_content).setOnClickListener(listener);
		root.findViewById(R.id.iv_arrow).setOnClickListener(listener);
	}

	private void setupView(View view) {
		// TODO Auto-generated method stub
		btnSubmit = (Button) view.findViewById(R.id.btn_next);
		btnSubmit.setText(getActivity().getResources().getString(R.string.finish));
		
		tvBirthDay= (TextView) view.findViewById(R.id.tv_content);
		tvBirthDay.setText(getActivity().getString(R.string.tip_select_birthday));
		mCircularImage  = (CircularImage) view.findViewById(R.id.iv_head);
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
	
	
	class MyOnclickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.ll_content:
			case R.id.tv_lable:
			case R.id.tv_content:
			case R.id.iv_arrow:
				showDatePicker();
				break;

			default:
				break;
			}
		}
		
	}
	
	// 生日选择框
	private void showDatePicker() {
		// TODO Auto-generated method stub
		DatePickerDialog datePicker = new DatePickerDialog(getActivity(), new DateSet(), 1995, 0, 1);
		datePicker.setTitle("生日");
		datePicker.show();
	}	
	
	// 监听datepicker 日期变化
	class DateSet implements OnDateSetListener {
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Date date = new Date(year-1900, monthOfYear, dayOfMonth);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        
		    strDate = sdf.format(date) ;
		    tvBirthDay.setText(strDate);
		}
	}
		
		
}
