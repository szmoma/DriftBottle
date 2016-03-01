package com.hnmoma.driftbottle.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.cache.CacheInfo;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;

public class FilesUtils {
	private Context mContext;

	public FilesUtils(Context context) {
		this.mContext = context;
	}
	/**
	 * 下载图片，并做缓存
	 * @param url
	 * @return
	 */
	public CacheInfo downloadImage(URL url) {
		CacheInfo cacheInfo = null;
		byte[] imageBuffer = (byte[]) null;
		BufferedInputStream is = null;
		ByteArrayOutputStream baos = null;
		try {
			URLConnection conn = url.openConnection();
			is = new BufferedInputStream(conn.getInputStream());
			baos = new ByteArrayOutputStream();
			long size = 0L;
			int b = -1;
			while ((b = is.read()) != -1) {
				baos.write(b);
			}
			imageBuffer = baos.toByteArray();
			size = imageBuffer.length;
			Bitmap bm = BitmapFactory.decodeByteArray(imageBuffer, 0, imageBuffer.length);
			cacheInfo = new CacheInfo(url, size, bm);
		} catch (IOException e) {
			MoMaLog.i(this.mContext.getPackageName(), url.toString() + " is unavailable");
			return null;
		} finally {
			try {
				baos.close();
			} catch (Exception e) {
				return null;
			}
		}
		return cacheInfo;
	}
	/**
	 * 保存图片，默认保存为PNG格式
	 * @param bitmap 图片对象
	 * @param fileName 文件名称
	 * @return
	 */
	public boolean saveImage(Bitmap bitmap, String fileName) {
		if(bitmap==null){
			return false;
		}
		boolean bool = false;
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = null;
		try {
			File file = new File(MyApplication.mAppPath+"/cache/"+fileName);
			
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			
			FileOutputStream fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			
//			bos = new BufferedOutputStream(this.mContext.openFileOutput(fileName, 0));
			baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			bis = new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));
			int b = -1;
			while ((b = bis.read()) != -1) {
				bos.write(b);
			}
			bool = true;
		} catch (Exception e) {
			bool = false;
			MoMaLog.i(this.mContext.getPackageName(), "the local storage is not available");
			try {
				bos.close();
				bis.close();
			} catch (IOException ee) {
				bool = false;
				MoMaLog.i(this.mContext.getPackageName(), "the local storage is not available");
			}
		} finally {
			try {
				bos.close();
				bis.close();
			} catch (IOException e) {
				bool = false;
				MoMaLog.i(this.mContext.getPackageName(), "the local storage is not available");
			}
		}
		return bool;
	}

	public Bitmap readImage(String fileName) {
		Bitmap bm = null;
		InputStream is = null;
		try {
			File file = new File(MyApplication.mAppPath+"/cache/"+fileName);
			FileInputStream fis = new FileInputStream(file);
			
			is = new BufferedInputStream(fis);
			bm = BitmapFactory.decodeStream(is);
		} catch (FileNotFoundException e) {
			MoMaLog.i(this.mContext.getPackageName(), "image resource is not found int the cache directory");
		}
		return bm;
	}
	/**
	 * 删除图片
	 * @param fileName 文件名称
	 * @return
	 */
	public boolean deleteImage(String fileName) {
		File file = new File(MyApplication.mAppPath+"/cache/"+fileName);
		return file.delete();
	}

	//---------------------------------------mf----------------------------------------------
	
	public boolean saveMf(File file, String fileName) {
		if(file==null){
			return false;
		}
		boolean bool = false;
		return bool;
	}

	public CacheInfo downloadMf(URL url) {
		CacheInfo cacheInfo = null;
		byte[] mfBuffer = (byte[]) null;
		BufferedInputStream is = null;
		ByteArrayOutputStream baos = null;
		try {
			URLConnection conn = url.openConnection();
			is = new BufferedInputStream(conn.getInputStream());
			baos = new ByteArrayOutputStream();
			long size = 0L;
			int b = -1;
			while ((b = is.read()) != -1) {
				baos.write(b);
			}
			mfBuffer = baos.toByteArray();
			size = mfBuffer.length;
			
			String fileName = UUID.randomUUID().toString();
			File file = new File(MyApplication.mAppPath+"/netmfs/"+fileName);
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(file);
			
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(mfBuffer);
			
			//将缓冲区中的数据全部写出   
			bos.flush();   
			bos.close();
			fos.close();
			
			cacheInfo = new CacheInfo(url, size, file);
		} catch (IOException e) {
			MoMaLog.i(this.mContext.getPackageName(), url.toString() + " is unavailable");
			return null;
		} finally {
			try {
				is.close();
				baos.close();
			} catch (Exception e) {
				return null;
			}
		}
		return cacheInfo;
	}

	public File readMf(String fileName) {
		File file = null;
		InputStream is = null;
		try {
			file = new File(MyApplication.mAppPath+"/netmfs/"+fileName);
			FileInputStream fis = new FileInputStream(file);
			
//			file = new File(mContext.getFilesDir(), fileName);
//			FileInputStream fis = new FileInputStream(file);
			is = new BufferedInputStream(fis);
			is.close();
		} catch (FileNotFoundException e) {
			MoMaLog.i(this.mContext.getPackageName(), "image resource is not found int the cache directory");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	public boolean deleteFile(String fileName) {
		File file = new File(MyApplication.mAppPath+"/netmfs/"+fileName);
		return file.delete();
	}
	
	
}