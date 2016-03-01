package com.hnmoma.driftbottle.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import com.easemob.util.ImageUtils;

public class BitmapUtil {
	private static int MAXSIZE = 100;
	
	/**
	 * 根据路径获得突破并压缩返回bitmap用于显示
	 * 
	 * @param imagesrc
	 * @return
	 */
	public static Bitmap getSmallBitmap(String filePath,int quality) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = MoMaUtil.computeSampleSize(options, -1, 512*512);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
		
		//获取图片的后缀
		String imageForm = filePath.substring(filePath.lastIndexOf("."));
		try {
			Bitmap newBitmap = compressImage(bitmap, quality,imageForm);
			bitmap = newBitmap;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MoMaLog.d("debug", e.getMessage());
		}
		
		return bitmap;
	}
	
	/**
	 * 按质量压缩图片，仅仅支持 JPEG、PNG、 WEBP
	 * @param image 需要压缩的图片
	 * @param quality 图片质量，范围是0~100
	 * @param imageForm 图片格式
	 * @return 返回处理过的图片
	 */
	@SuppressLint("NewApi")
	public static  Bitmap compressImage(Bitmap image,int quality,String imageFormat) {
		if(quality>100||quality<0)
			quality = 100;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CompressFormat compressFormat = null;
		if("jpeg".equalsIgnoreCase(imageFormat)||"jpg".equalsIgnoreCase(imageFormat)){
			compressFormat = Bitmap.CompressFormat.JPEG;
		}else if("PNG".equalsIgnoreCase(imageFormat)){
			compressFormat = Bitmap.CompressFormat.PNG;
		}else if("WEBP".equalsIgnoreCase(imageFormat)){
			if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				compressFormat = Bitmap.CompressFormat.WEBP;
			else
				compressFormat = Bitmap.CompressFormat.JPEG;
		}else 
			throw new IllegalArgumentException("不支持"+imageFormat+"格式压缩");
		
		image.compress(compressFormat, quality, baos);
		MoMaLog.e("debug", "第一次压缩的大小："+baos.toByteArray().length/1024);
		int options = quality;
		while ( baos.toByteArray().length / 1024>MAXSIZE) {	//循环判断如果压缩后图片是否大于1M,大于继续压缩	
			if(options>30)
				options = options - 10;//每次都减少10
			else
				break;
			baos.reset();//重置baos即清空baos
			image.compress(compressFormat, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
		}
		
		MoMaLog.e("debug", "最后压缩的大小："+baos.toByteArray().length/1024);
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
		MoMaLog.e("debug", "圖片质量是："+options);
		return bitmap;
	}
	
	/**
	 * 保存图片
	 * @param path
	 * @param bm
	 */
	public static void writeBitmap(File file,Bitmap bm ,boolean isCover){
		try { 
			if(bm==null)
				return ;
			File dirFile = new File(file.getParent());
			if(!dirFile.exists())
				dirFile.mkdirs();
			
			if (!file.exists()&&file.isFile()) { 
				if(!isCover){
					return ;
				}
				file.delete(); 
			} 
			
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file); 
			bm.compress(Bitmap.CompressFormat.JPEG, 100, out); 
			out.flush(); 
			out.close(); 
		} catch (FileNotFoundException e) { 
		// TODO Auto-generated catch block 
		e.printStackTrace(); 
		} catch (IOException e) { 
		// TODO Auto-generated catch block 
		e.printStackTrace(); 
		}
	}
}
