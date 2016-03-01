package com.hnmoma.driftbottle.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

@SuppressLint("CommitPrefEdits")
public class SysSharePreferenceUtil {
	private SharedPreferences mSharedPreferences;
	private static SharedPreferences.Editor editor;

	public SysSharePreferenceUtil(Context context, String name) {
		mSharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		editor = mSharedPreferences.edit();
	}
	
	public boolean isNtcSoundOpend() {
		return mSharedPreferences.getBoolean("ntcSoundOpend", true);
	}

	public void setNtcSoundOpend(boolean ntcSoundOpend) {
		editor.putBoolean("ntcSoundOpend", ntcSoundOpend);
		editor.commit();
	}
	
	public boolean isSoundOpend() {
		return mSharedPreferences.getBoolean("soundOpend", true);
	}

	public void setSoundOpend(boolean soundOpend) {
		editor.putBoolean("soundOpend", soundOpend);
		editor.commit();
	}
	
//	public boolean  isScreenLocked() {
//		return mSharedPreferences.getBoolean("lockScreen", false);
//	}
//	
//	public void setScreenLocked(boolean lockScreen) {
//		editor.putBoolean("lockScreen", lockScreen);
//		editor.commit();
//	}
	
	public boolean isFirstTime() {
		return mSharedPreferences.getBoolean("isFirstTime", true);
	}
	
	public void setIsFirstTime(boolean isFirstTime) {
		editor.putBoolean("isFirstTime", isFirstTime).commit();
	}
	
	public void setKeyVersion(int keyVersion){
		editor.putInt("keyVersion", keyVersion);
		editor.commit();
	}
	
	public int getKeyVersion(){
		return mSharedPreferences.getInt("keyVersion", 0);
	}
}
