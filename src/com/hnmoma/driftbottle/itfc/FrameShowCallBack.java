package com.hnmoma.driftbottle.itfc;

import com.hnmoma.driftbottle.fragment.BaseFragment;


public interface FrameShowCallBack {

	public static final int COMEFROMPICK = 0;
	public static final int COMEFROMCHAT = 1;
	
	Object[] onFragmentInit(BaseFragment fragment);
}
