package com.hnmoma.driftbottle.adapter;

import java.util.HashMap;

import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.authorize.AuthorizeAdapter;
import cn.sharesdk.sina.weibo.SinaWeibo;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.util.MyConstants;

public class MyAuthorizeAdapter extends AuthorizeAdapter implements OnClickListener, PlatformActionListener {
	
	private CheckedTextView ctvFollow;
	private PlatformActionListener backListener;
	
	public void onCreate() {
		hideShareSDKLogo();
		disablePopUpAnimation();
		
		getTitleLayout().setBackgroundResource(R.drawable.webviewtab_bg);
		getTitleLayout().getTvTitle().setGravity(Gravity.CENTER);
		int dp_48 = cn.sharesdk.framework.utils.R.dipToPx(getActivity(), 48);
		getTitleLayout().getTvTitle().setPadding(0, 0, dp_48, 0);
		
		String platName = getPlatformName();
		if (SinaWeibo.NAME.equals(platName)) {
			initUi(platName);
			interceptPlatformActionListener(platName);
			return;
		}
	}
	
	
	private void initUi(String platName) {
		ctvFollow = new CheckedTextView(getActivity());
		try {
			ctvFollow.setBackgroundResource(R.drawable.auth_follow_bg);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		ctvFollow.setChecked(true);
		int dp_10 = cn.sharesdk.framework.utils.R.dipToPx(getActivity(), 10);
		ctvFollow.setCompoundDrawablePadding(dp_10);
		ctvFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.auth_cb, 0, 0, 0);
		ctvFollow.setGravity(Gravity.CENTER_VERTICAL);
		ctvFollow.setPadding(dp_10, dp_10, dp_10, dp_10);
		ctvFollow.setText(R.string.sm_item_fl_weibo);
		
		ctvFollow.setTextColor(0xff909090);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		ctvFollow.setLayoutParams(lp);
		LinearLayout llBody = (LinearLayout) getBodyView().getChildAt(0);
		llBody.addView(ctvFollow);
		ctvFollow.setOnClickListener(this);

		ctvFollow.measure(0, 0);
		int height = ctvFollow.getMeasuredHeight();
		TranslateAnimation animShow = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0,
				Animation.ABSOLUTE, height,
				Animation.ABSOLUTE, 0);
		animShow.setDuration(1000);
		getWebBody().startAnimation(animShow);
		ctvFollow.startAnimation(animShow);
	}

	private void interceptPlatformActionListener(String platName) {
		Platform plat = ShareSDK.getPlatform(getActivity(), platName);
		// 备份此前设置的事件监听器
		backListener = plat.getPlatformActionListener();
		// 设置新的监听器，实现事件拦截
		plat.setPlatformActionListener(this);
	}

	public void onError(Platform plat, int action, Throwable t) {
		if (action == Platform.ACTION_AUTHORIZING) {
			// 授权时即发生错误
			plat.setPlatformActionListener(backListener);
			if (backListener != null) {
				backListener.onError(plat, action, t);
			}
		}
		else {
			// 关注时发生错误
			plat.setPlatformActionListener(backListener);
			if (backListener != null) {
				backListener.onComplete(plat, Platform.ACTION_AUTHORIZING, null);
			}
		}
	}

	public void onComplete(Platform plat, int action,
			HashMap<String, Object> res) {
		if (action == Platform.ACTION_FOLLOWING_USER) {
			// 当作授权以后不做任何事情
			plat.setPlatformActionListener(backListener);
			if (backListener != null) {
				backListener.onComplete(plat, Platform.ACTION_AUTHORIZING, null);
			}
		}
		else if (ctvFollow.isChecked()) {
			// 授权成功，执行关注
			String account = MyConstants.SDK_SINAWEIBO_UID;
			
			plat.followFriend(account);
		}
		else {
			// 如果没有标记为“授权并关注”则直接返回
			plat.setPlatformActionListener(backListener);
			if (backListener != null) {
				// 关注成功也只是当作授权成功返回
				backListener.onComplete(plat, Platform.ACTION_AUTHORIZING, null);
			}
		}
	}

	public void onCancel(Platform plat, int action) {
		plat.setPlatformActionListener(backListener);
		if (action == Platform.ACTION_AUTHORIZING) {
			// 授权前取消
			if (backListener != null) {
				backListener.onCancel(plat, action);
			}
		}
		else {
			// 当作授权以后不做任何事情
			if (backListener != null) {
				backListener.onComplete(plat, Platform.ACTION_AUTHORIZING, null);
			}

		}
	}

	public void onClick(View v) {
		CheckedTextView ctv = (CheckedTextView) v;
		ctv.setChecked(!ctv.isChecked());
	}

	public void onResize(int w, int h, int oldw, int oldh) {
		if (ctvFollow != null) {
			if (oldh - h > 100) {
				ctvFollow.setVisibility(View.GONE);
			}
			else {
				ctvFollow.setVisibility(View.VISIBLE);
			}
		}
	}
}