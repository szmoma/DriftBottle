package com.hnmoma.driftbottle.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.hnmoma.driftbottle.AppendPersonInfoActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.adapter.ChildAdapter;
import com.hnmoma.driftbottle.adapter.GroupAdapter;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.itfc.IFragmentCallback;
import com.hnmoma.driftbottle.model.User;
/**
 * 用户的更多信息，添加用户的地址
 * @author Administrator
 *
 */
public class FragmentUserInfoAddr extends Fragment {
	//传递参数的标志
	public final static String PROVINCE = "province";
	public final static String CITY = "city";
	
	
	private static FragmentUserInfoAddr fragment;
	private IFragmentCallback callback; //回调接口
	
	private View root;//根视图
	
	private Button btnNext;	
	private TextView tvAddr;//地址
	private TextView tvTip;	//提示
	
	private String province="";	//省份
	private String city="";//城市
	
	// 屏幕的宽高
	public static int screen_width = 0;
	public static int screen_height = 0;
	
	private PopupWindow mPopupWindow = null;
	
	private GroupAdapter groupAdapter;
	private ChildAdapter childAdapter;
	
	private ListView lvPronvice;
	private ListView lvCity ;
	
	private TranslateAnimation animation;// 出现的动画效果
	
	private View window;
	
	private int groupIndex;
	
	private CircularImage mCircularImage;
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				String[] childData = getActivity().getResources().getStringArray(getResouceId(msg.arg1));
				childAdapter.setChildData(childData);
				childAdapter.notifyDataSetChanged();
				groupAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}

		};
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState!=null){
			province = savedInstanceState.getString(PROVINCE);
			city = savedInstanceState.getString(CITY);
		}
		
	};
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		root = inflater.inflate(R.layout.fragment_add_userinfo_more, container,false);
		
		// 获取手机屏幕的大小
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm); 
		screen_width = dm.widthPixels;
		screen_height = dm.heightPixels;
		
		// 获取控件在屏幕中的位置,方便展示Popupwindow
		int[] location = new int[2];
		root.findViewById(R.id.ll_content).getLocationInWindow(location);
		animation = new TranslateAnimation(0, 0, -700, location[1]);
		animation.setDuration(500);
		
		setupView(root);
		setListener();
		init();
		return root;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putString(PROVINCE, province);
		outState.putString(CITY, city);
	}

	private void init() {
		// TODO Auto-generated method stub
		String tip = getActivity().getString(R.string.tip_addr);
		tvTip.setText(tip);
		
		Bundle bundle = getArguments();
		if(bundle!=null){ 
			User user = (User) bundle.getSerializable("user");
			if(user!=null){
				province = user.getProvince();
				city = user.getCity();
			}
		}
		if(TextUtils.isEmpty(province))
			province = "北京";
		if(TextUtils.isEmpty(city))
			city = "昌平";
		tvAddr.setText(province+" " +city);
	}
	
	/**
	 * 监听fragment控件的函数，比如button
	 */
	private void setListener() {
		// TODO Auto-generated method stub
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//验证填写的信息
				if(TextUtils.isEmpty(province)||TextUtils.isEmpty(city)){
					Toast.makeText(getActivity(), "请选择常驻地", Toast.LENGTH_SHORT).show();
					return ;
				}
				Bundle args = new Bundle();
				args.putString(PROVINCE, province);
				args.putString(CITY, city);
				callback.startDetailFragment(args, AppendPersonInfoActivity.OPERBIRTH);
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
		btnNext = (Button) view.findViewById(R.id.btn_next);
		tvAddr = (TextView) view.findViewById(R.id.tv_content);
		tvTip = (TextView) view.findViewById(R.id.tv_tip);
		mCircularImage  = (CircularImage) view.findViewById(R.id.iv_head);
		mCircularImage .setImageDrawable(getActivity().getResources().getDrawable(R.drawable.icon_location));
		
	}
	
	public static Fragment newInstance(Bundle arg) {
		// TODO Auto-generated method stub
		if(fragment==null)
			fragment = new FragmentUserInfoAddr();
		fragment.setArguments(arg);
		return fragment;
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
	
	/**
	 * 初始化 PopupWindow
	 * 
	 * @param view
	 */
	public void initPopuWindow(View view) {
		/* 第一个参数弹出显示view 后两个是窗口大小 */
		int height = (int) (screen_height*0.9+0.5);
		mPopupWindow = new PopupWindow(view, screen_width, height);
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
	
	/**
	 * 展示区域选择的对话框
	 */
	private void showPupupWindow() {
		if (mPopupWindow == null) {
			String[] province = getActivity().getResources().getStringArray(R.array.province);
			 LayoutInflater mLayoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
		     window = mLayoutInflater.inflate(R.layout.view_popwindow, null);  
			
			initPopuWindow(window);
			
			lvPronvice = (ListView)window.findViewById(R.id.lv_province);
		    lvCity = (ListView)window.findViewById(R.id.lv_city);

			groupAdapter = new GroupAdapter(getActivity(), province);
			lvPronvice.setAdapter(groupAdapter);
			groupIndex = 0;
			groupAdapter.setSelectedPosition(groupIndex);
			
			Message msg = Message.obtain();
			if (childAdapter == null) {
				childAdapter = new ChildAdapter(getActivity());
				lvCity.setAdapter(childAdapter);
			}
			
			msg.what = 1;
			msg.arg1 = groupIndex;
			handler.sendMessage(msg);
		}
		window.setAnimation(animation);
		window.startAnimation(animation);

//		mPopupWindow.showAtLocation(root.findViewById(R.id.ll_content), Gravity.CENTER, 20, 150);
		mPopupWindow.showAsDropDown(root.findViewById(R.id.ll_contain));
		setPopuWindListener();
	}
	/**
	 * 设置PopuWindow的监听函数
	 */
	private void setPopuWindListener() {
		// TODO Auto-generated method stub
		lvPronvice.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				groupAdapter.setSelectedPosition(position);
				groupIndex = position;
				province = (String) parent.getItemAtPosition(position);
				if (childAdapter == null) {
					childAdapter = new ChildAdapter(getActivity());
					lvCity.setAdapter(childAdapter);
				}

				Message msg = Message.obtain();
				msg.arg1 = groupIndex;
				msg.what = 1;
				handler.sendMessage(msg);
			}
		});
		
		lvCity.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				province = (String) groupAdapter.getItem(groupIndex);
				city = (String) parent.getItemAtPosition(position);
				tvAddr.setText(province+"  "+city);
				mPopupWindow.dismiss();
			}
			
			
		});
	}

	public int getResouceId(int position){
		int result = 0;
		switch(position){
			case 0:
				result = R.array.北京;
				break;
			case 1:
				result = R.array.上海;
				break;
			case 2:
				result = R.array.天津;
				break;
			case 3:
				result = R.array.重庆;
				break;
			case 4:
				result = R.array.黑龙江;
				break;
			case 5:
				result = R.array.吉林;
				break;
			case 6:
				result = R.array.辽宁;
				break;
			case 7:
				result = R.array.内蒙古;
				break;
			case 8:
				result = R.array.河北;
				break;
			case 9:
				result = R.array.山西;
				break;
			case 10:
				result = R.array.陕西;
				break;
			case 11:
				result = R.array.山东;
				break;
			case 12:
				result = R.array.新疆;
				break;
			case 13:
				result = R.array.西藏;
				break;
			case 14:
				result = R.array.青海;
				break;
			case 15:
				result = R.array.甘肃;
				break;
			case 16:
				result = R.array.宁夏;
				break;
			case 17:
				result = R.array.河南;
				break;
			case 18:
				result = R.array.江苏;
				break;
			case 19:
				result = R.array.湖北;
				break;
			case 20:
				result = R.array.浙江;
				break;
			case 21:
				result = R.array.安徽;
				break;
			case 22:
				result = R.array.福建;
				break;
			case 23:
				result = R.array.江西;
				break;
			case 24:
				result = R.array.湖南;
				break;
			case 25:
				result = R.array.贵州;
				break;
			case 26:
				result = R.array.四川;
				break;
			case 27:
				result = R.array.广东;
				break;
			case 28:
				result = R.array.云南;
				break;
			case 29:
				result = R.array.广西;
				break;
			case 30:
				result = R.array.海南;
				break;
			case 31:
				result = R.array.香港;
				break;
			case 32:
				result = R.array.澳门;
				break;
			case 33:
				result = R.array.台湾;
				break;
		}
		return result;
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
				showPupupWindow();
				break;
			default:
				break;
			}
		}
		
	}
}
