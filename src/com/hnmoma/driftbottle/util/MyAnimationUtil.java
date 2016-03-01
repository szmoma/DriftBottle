package com.hnmoma.driftbottle.util;

import android.annotation.TargetApi;
import android.os.Build;

public class MyAnimationUtil {
	/**
	 * Layout动画
	 * 
	 * @return
	 */
	@TargetApi(value=Build.VERSION_CODES.HONEYCOMB)
	protected android.view.animation.LayoutAnimationController getAnimationController() {
		int duration=300;
		 android.view.animation.AnimationSet set = new  android.view.animation.AnimationSet(true);

		 android.view.animation.Animation animation = new android.view.animation.AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(duration);
		set.addAnimation(animation);

		animation = new android.view.animation.TranslateAnimation(android.view.animation.Animation.RELATIVE_TO_SELF, 0.0f,
				android.view.animation.Animation.RELATIVE_TO_SELF, 0.0f, android.view.animation.Animation.RELATIVE_TO_SELF,
				-1.0f, android.view.animation.Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(duration);
		set.addAnimation(animation);

		android.view.animation.LayoutAnimationController controller = new android.view.animation.LayoutAnimationController(set, 0.5f);
		controller.setOrder(android.view.animation.LayoutAnimationController.ORDER_NORMAL);
		return controller;
	}

}
