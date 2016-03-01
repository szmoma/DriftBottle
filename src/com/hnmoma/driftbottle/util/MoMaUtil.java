package com.hnmoma.driftbottle.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


public class MoMaUtil {
	
	private static final String LOGTAG = MoMaUtil.makeLogTag(MoMaUtil.class);
	
	/**
     * 去除字符串中的空格,回车,换行符,制表符
     * */
    public static String rmStrBlank(String str) {
    	if(str==null||"".equals(str)){
    		return "";
    	}
    	
       str = str.trim();
       return str.replaceAll("\\s*|\t|\r|\n", "");
    }
    
    /** 传入一个Date，判断是多久前，返回示例"XX小时前" */
    public static String getHowLongStr(Date date) {
       String rs = "";
       Long i = date.getTime();
       Date now = new Date();
       Long j = now.getTime();
       long day = 1000 * 60 * 60 * 24;
       long hour = 1000 * 60 * 60;
       long min = 1000 * 60;
       long sec = 1000;
       if (((j - i) / day) > 0)
           rs = ((j - i) / day) + "天前";
       else if (((j - i) / hour) > 0)
           rs = ((j - i) / hour) + "小时前";
       else if (((j - i) / min) > 0)
           rs = ((j - i) / min) + "分钟前";
       else if (((j - i) / sec) > 0)
           rs = ((j - i) / sec) + "秒前";
       return rs;
    }
    
    /**
     * 删除文件夹 main
     * @param filePathAndName String
     * @param fileContent String
     * @return boolean
     */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
//			System.out.println("删除文件夹操作出错");
//			e.printStackTrace();
			MoMaLog.e(LOGTAG, e.getMessage());
		}
	}

	/**
	 * 删除文件夹里面的所有文件
	 * 
	 * @param path
	 *            String 文件夹路径 如 c:/fqf
	 */
	public static void delAllFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
			}
		}
	}
	
	public static void delOutTimeFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		
		long now = System.currentTimeMillis();
		long max = 1000*60*60*24*10; 
		
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			
			if(now - temp.lastModified() >= max){
				temp.delete();
			}
		}
	}
	/**
	 * 清除path目录下的超时文件
	 * @param path 文件路径
	 * @param max 最长时间
	 */
	public static void delOutTimeFile(String path,long max) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
			return ;
		}
		
		//获取path目录下的文件
		for(File file:dir.listFiles()){
			 if(file.isFile()){// 判断是否文件
	               long lastModify = file.lastModified();//文件最后修改时间
	               long now = System.currentTimeMillis();
	               if((now - lastModify)>=max)
	            		   file.delete();
	         }else{
	        	 delOutTimeFile(file.getPath(),max);
	         }
		}
	}
	/**
	 * 保存文件
	 * @param filePath 文件路径和文件名
	 * @param content 文件内容
	 */
	public static void writeFile(String filePath,byte[] content){
		File file = new File(filePath);
		File dir = new File(file.getParent());
		FileOutputStream fos = null;
		try {
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			if(!file.exists())
				file.createNewFile();
			fos = new FileOutputStream(file);
			fos.write(content);
			fos.close();
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(fos!=null){
				try {
					fos.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				fos = null;
			}
		}
	}
	
	/** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  
    
    /** 
     * 将px值转换为sp值，保证文字大小不变 
     *  
     * @param pxValue 
     * @param fontScale 
     *            （DisplayMetrics类中属性scaledDensity） 
     * @return 
     */  
    public static int px2sp(Context context, float pxValue) {  
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
        return (int) (pxValue / fontScale + 0.5f);  
    }  
  
    /** 
     * 将sp值转换为px值，保证文字大小不变 
     *  
     * @param spValue 
     * @param fontScale 
     *            （DisplayMetrics类中属性scaledDensity） 
     * @return 
     */  
    public static int sp2px(Context context, float spValue) {  
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
        return (int) (spValue * fontScale + 0.5f);  
    }  
    
    public static String makeLogTag(Class<?> cls) {
        return "DriftBottle_" + cls.getSimpleName();
    }
    
    public static boolean isEmail(String email){     
        String str="^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
           Pattern p = Pattern.compile(str);     
           Matcher m = p.matcher(email);     
           MoMaLog.e(LOGTAG, m.matches()+"---");
           return m.matches();     
       } 
    
    public static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3).toUpperCase());
        }
        return sb.toString();
    }
    
    public static String md5 (String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return hex (md.digest(message.getBytes("CP1252")));
        } catch (Exception e) {
        }
        return null;
    }
    
    private static final String HASH_ALGORITHM = "MD5";
	private static final int RADIX = 10 + 26; // 10 digits + 26 letters

	public static String generatemd5(String imageUri) {
		byte[] md5 = getMD5(imageUri.getBytes());
		BigInteger bi = new BigInteger(md5).abs();
		return bi.toString(RADIX);
	}

	private static byte[] getMD5(byte[] data) {
		byte[] hash = null;
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			digest.update(data);
			hash = digest.digest();
		} catch (NoSuchAlgorithmException e) {
		}
		return hash;
	}
    
	
	public static String getDeviceIdFromSys(Context ctx) {
		/**
		 * yangsy 
		 * 2015-3-30 修改，修改理由：
		 * 如果设备都有电话功能，是可以获取到唯一设备号；
		 * 如果设备没有手机功能（如仅支持wifi的平板），需要获取设备的序列号（从android2.3开始，设备均有序列号）；
		 */
		String id  = null;
		TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		id = telephonyManager.getDeviceId() ;// Get deviceId 获取设备ID
		if(TextUtils.isEmpty(id)){
		 if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.GINGERBREAD){
		  try {
		   Class<?> c = Class.forName("android.os.SystemProperties");  
		   Method get = c.getMethod("get", String.class, String.class );  
		   id = (String)(get.invoke(c, "ro.serialno", "unknown" )  );
		  } catch (Exception e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
		   MoMaLog.i(MoMaUtil.class.getName(), "获取设备id失败");
		  } 
		 }
		}
		return id;
	}
    
    /**
	 * 网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] info = mgr.getAllNetworkInfo();
	    if (info != null) {
	    	for (int i = 0; i < info.length; i++) {
	    		if (info[i].getState() == NetworkInfo.State.CONNECTED) {
	    			return true;
	    		}
	    	}
	    }
		return false;
	}
	
	/**
	 * 以最省内存的方式读取本地资源的图片
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readBitMapFromResource(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}
	
	
	/**
	 * 以最省内存的方式读sdcard图片
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readBitMapFromSDcard(String imageUri) {
//		BitmapFactory.Options opts = new BitmapFactory.Options();
//		opts.inJustDecodeBounds = true;
//		BitmapFactory.decodeFile(imageUri, opts);
//		             
//		opts.inSampleSize = computeSampleSize(opts, -1, 128*128);
//		//这里一定要将其设置回false，因为之前我们将其设置成了true      
//		opts.inJustDecodeBounds = false;
		
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		
		return BitmapFactory.decodeFile(imageUri, opt);
	}
	
	/**
	 * 以最省内存的方式读InputStream图片
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readBitMapFromIs(InputStream is) {
//		BitmapFactory.Options opts = new BitmapFactory.Options();
//		opts.inJustDecodeBounds = true;
//		BitmapFactory.decodeStream(is, null, opts);
//		             
//		opts.inSampleSize = computeSampleSize(opts, -1, 128*128);
//		//这里一定要将其设置回false，因为之前我们将其设置成了true      
//		opts.inJustDecodeBounds = false;
		
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		
		return BitmapFactory.decodeStream(is, null, opt);
	}
	
	
	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
	
	
	public static byte[] inputStreamToByte(InputStream is) {
		try{
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			int ch;
			while ((ch = is.read()) != -1) {
				bytestream.write(ch);
			}
			byte imgdata[] = bytestream.toByteArray();
			bytestream.close();
			return imgdata;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static int random(int s, int e){
		Random random = new Random();
		return random.nextInt(e+1-s)+s;
		
	}

	/**
	 * 猜拳游戏，住码转成int数组
	 * @param str
	 * @return
	 */
	public static int[] cq_change2Array(String str) {
		if(TextUtils.isEmpty(str))
			throw  new NullPointerException();
		str = str.trim();
		int [] result = new int[3];
		String [] zm = str.split("[|]");
		for(int i = 0; i< zm.length; i++){
			result [i] = Integer.parseInt(zm[i].trim());
		}
		return result;
	}
	/**
	 *验证是否是电话号码，主要包括的波段是13、15、18、17（4G）、14（上网卡）
	 * @param phoneNum 需要验证的电话号码
	 * @return 如果是电话号码，则返回true，否则返回false
	 */
	public static boolean isPhone(String phoneNum){
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9])|(17[0-9])|(14[0-9]))\\d{8}$");  
        return p.matcher(phoneNum).matches();
	}
	/**
	 * 格式化时间字符串。此处有一个bug，就是处理诸如2015112的字符串时，采用贪婪形式，转化为20151102
	 * @param sourceTime 需要转化的时间字符串
	 * @return 转化为yyyyMMdd格式的时间字符串
	 */
	public static String formatTimeString(String sourceTime){
		StringBuilder desTime = new StringBuilder();
		desTime.append(sourceTime.substring(0, 4));//年
		if(sourceTime.length()==6){//如格式：201511，表示2015年1月1日
			desTime.append("0").append(sourceTime.substring(4, 5));//月份补0
			desTime.append("0").append(sourceTime.substring(5,sourceTime.length()));//天数补0
		}else if(sourceTime.length()==8){ //如果格式20151224，表示2015年12月24日
			desTime.append(sourceTime.substring(4));
		}else{
			//分为两种情况：111可能表示11月1日，也可能表示1月11日，
			//	获取第5个字符
			int tmpMonth = new Integer(sourceTime.substring(4, 5));
			if(tmpMonth>1){ 	//如格式：2015223表示2015年2月23日
				desTime.append("0").append(sourceTime.substring(4,5));//月份补0
				desTime.append(sourceTime.subSequence(5, sourceTime.length()));//直接把日放进去
			}else {
				int tmp = new Integer(sourceTime.substring(5, 6));
				if(tmp>2){	//如格式：2015131，表示2015年1月31日
					desTime.append("0").append(sourceTime.subSequence(4, 5));//月补0
					desTime.append(sourceTime.subSequence(5, sourceTime.length()));	
				}else if(tmp==2){	//如格式：2015114，有可能是2015年11月4日，也可能是2015年1月14日,但优先处理2015年1月14日
					desTime.append("0").append(sourceTime.subSequence(4, 6));//日补0
					desTime.append(sourceTime.subSequence(6, sourceTime.length()));
				}else{
					desTime.append(sourceTime.subSequence(4, 6));
					desTime.append("0").append(sourceTime.subSequence(6, sourceTime.length()));//日补0
				}
			}
		}
		return desTime.toString();
	}
	/**
	 * 
	 * @param value 待验证的字符串
	 * @return 如果value是整数，则返回true，否则返回false
	 */
	public static boolean isDigist(String value){
		if(TextUtils.isEmpty(value))
			return false;
//		String regular = "^\\+?[1-9][0-9]*$";//验证非0正整数
		String regular = "^\\d+$"; //验证数字
		return value.matches(regular);
	}
	/**
	 * 判断字符是时间格式：yyyy-MM-dd,除此外，必须符合DateTime时间范围
	 * @param value 如果符合格式yyyy-MM-dd，则返回true，否则返回false
	 */
	public static boolean isDateFormat(String value) {
		// TODO Auto-generated method stub
		if(TextUtils.isEmpty(value))
			return false;
		String regular = "^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$";
		return value.matches(regular);
	}
	/**
	 * 获取两个时间的间隔天数
	 * @param startDate 起始时间，小值
	 * @param endDate 结束时间，大值
	 * @return 如果两个时间相等，则返回0；如果startDate小于endDate，则返回-1；如果endDate大于startEnd，则返回计算的天数
	 */
	public static int getGapCount(Date startDate, Date endDate) {
		int days = -1;
		if(endDate.getTime()-startDate.getTime()<0){
			days = -1;
		}else if(endDate.getTime()-startDate.getTime()==0){
			days = 0;
		}else{
			Calendar fromCalendar = Calendar.getInstance();  
	        fromCalendar.setTime(startDate);  
	        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);  
	        fromCalendar.set(Calendar.MINUTE, 0);  
	        fromCalendar.set(Calendar.SECOND, 0);  
	        fromCalendar.set(Calendar.MILLISECOND, 0);  
	  
	        Calendar toCalendar = Calendar.getInstance();  
	        toCalendar.setTime(endDate);  
	        toCalendar.set(Calendar.HOUR_OF_DAY, 0);  
	        toCalendar.set(Calendar.MINUTE, 0);  
	        toCalendar.set(Calendar.SECOND, 0);  
	        toCalendar.set(Calendar.MILLISECOND, 0); 
	        days =  (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24)+Math.ceil((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) % (1000 * 60 * 60 * 24)));
		}
        return days;
	}
	
	/**
	 * 检测网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetWorkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}

		return false;
	}

	/**
	 * 检测Sdcard是否存在
	 * 
	 * @return
	 */
	public static boolean isExitsSdcard() {
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			return true;
		else
			return false;
	}
	/**
	 * 应用程序位于堆栈的顶层  
	 * @param context
	 * @return
	 */
	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null)
			return runningTaskInfos.get(0).topActivity.getPackageName();
		else
			return "";
	}
	
	public static String[] getLevel(int grade){
		String[] result = new String[3];
		if(grade<=200){
			result[0] = "1";
			result[2] = "200";
		}else if(grade<=900){
			result[0] = "2";
			result[2] = "900";
		}else if(grade<=1800){
			result[0] = "3";
			result[2] = "1800";
		}else if(grade<=2900){
			result[0] = "3";
			result[2] = "2900";
		}else if(grade<=4200){
			result[0] = "4";
			result[2] = "4200";
		}else if(grade<=5700){
			result[0] = "5";
			result[2] = "5700";
		}else if(grade<=7400){
			result[0] = "6";
			result[2] = "7400";
		}else if(grade<=9300){
			result[0] = "8";
			result[2] = "9300";
		}else if(grade<=11400){
			result[0] = "9";
			result[2] = "1400";
		}else if(grade<=1500){
			result[0] = "10";
			result[2] = "1500";
		}else{
			//注意此处：添加或变更等级时，必须处理此处
			result[0] = "1";
			result[2] = "200";
		}
		
		return result;
	}
}
