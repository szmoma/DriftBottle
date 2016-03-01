package com.hnmoma.driftbottle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;

public class BaseFrameShowActivity extends BaseActivity {

	/**
	 * 保存方法
	 * 
	 * @param uri
	 * @param imageLoader
	 * @param isGif
	 */
	public void savetoLocal(String uri, ImageLoader imageLoader, boolean isGif) {
		File cacheFile = DiscCacheUtil.findInCache(uri,
				imageLoader.getDiscCache());
		
		File localFile;
		String picName;
		if (isGif) {
			picName = uri.substring(uri.lastIndexOf("/") + 1) + ".gif";
			localFile = new File(MyApplication.mAppPath + "/pictures/" + picName);
		} else {
			picName = uri.substring(uri.lastIndexOf("/") + 1) + ".png";
			localFile = new File(MyApplication.mAppPath + "/pictures/" + picName);
		}
		if (localFile.exists()) {
			Toast.makeText(this, "图片已保存至相册", Toast.LENGTH_SHORT).show();
			return;
		}

		// 判断原文件是否存在
		if (!cacheFile.exists()) {
			return;
		}

		if (!localFile.getParentFile().exists()) {
			// 如果目标文件所在的目录不存在，则创建目录
			if (!localFile.getParentFile().mkdirs()) {
//				showMsg("创建目标文件所在的目录失败！");
				return;
			}
		}

		// 复制文件
		int byteread = 0;// 读取的位数
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(cacheFile);
			out = new FileOutputStream(localFile);
			byte[] buffer = new byte[1024];
			while ((byteread = in.read(buffer)) != -1) {
				// 将读取的字节写入输出流
				out.write(buffer, 0, byteread);
			}
			showMsg("图片已保存至相册");
			try {
				MediaStore.Images.Media.insertImage(this.getContentResolver(), localFile.getAbsolutePath(), picName, null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+ localFile.getAbsolutePath())));
			return;
		} catch (Exception e) {
			showMsg("图片保存失败");
			return;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
}