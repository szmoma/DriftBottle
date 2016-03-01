package com.hnmoma.driftbottle.sensitivewords;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.model.DaoSession;
import com.hnmoma.driftbottle.model.SensitiveWord;
import com.hnmoma.driftbottle.model.SensitiveWordDao;

/**
 * @Description: 初始化敏感词库，将敏感词加入到HashMap中，构建DFA算法模型
 * @Project：test
 * @Author : moma
 * @version 1.0
 */
public class SensitiveWordInit {
	private String ENCODING = "UTF-8";    //字符编码
	@SuppressWarnings("rawtypes")
	public HashMap sensitiveWordMap;
	
	public SensitiveWordInit(){
		super();
	}
	
	/**
	 * @author moma 
	 * @date 2014年4月20日 下午2:28:32
	 * @version 1.0
	 */
	@SuppressWarnings("rawtypes")
	public Map initKeyWord(){
		//读取敏感词库
		Set<String> keyWordSet = readSensitiveWordFile();
		//将敏感词库加入到HashMap中
		addSensitiveWordToHashMap(keyWordSet);
		//spring获取application，然后application.setAttribute("sensitiveWordMap",sensitiveWordMap);
		return sensitiveWordMap;
	}

	/**
	 * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型：<br>
	 * 中 = {
	 *      isEnd = 0
	 *      国 = {<br>
	 *      	 isEnd = 1
	 *           人 = {isEnd = 0
	 *                民 = {isEnd = 1}
	 *                }
	 *           男  = {
	 *           	   isEnd = 0
	 *           		人 = {
	 *           			 isEnd = 1
	 *           			}
	 *           	}
	 *           }
	 *      }
	 *  五 = {
	 *      isEnd = 0
	 *      星 = {
	 *      	isEnd = 0
	 *      	红 = {
	 *              isEnd = 0
	 *              旗 = {
	 *                   isEnd = 1
	 *                  }
	 *              }
	 *      	}
	 *      }
	 * @author moma 
	 * @date 2014年4月20日 下午3:04:20
	 * @param keyWordSet  敏感词库
	 * @version 1.0
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addSensitiveWordToHashMap(Set<String> keyWordSet) {
		sensitiveWordMap = new HashMap(keyWordSet.size());     //初始化敏感词容器，减少扩容操作
		String key = null;  
		Map nowMap = null;
		Map<String, String> newWorMap = null;
		//迭代keyWordSet
		Iterator<String> iterator = keyWordSet.iterator();
		while(iterator.hasNext()){
			key = iterator.next();    //关键字
			nowMap = sensitiveWordMap;
			for(int i = 0 ; i < key.length() ; i++){
				char keyChar = key.charAt(i);       //转换成char型
				Object wordMap = nowMap.get(keyChar);       //获取
				
				if(wordMap != null){        //如果存在该key，直接赋值
					nowMap = (Map) wordMap;
				}
				else{     //不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
					newWorMap = new HashMap<String,String>();
					newWorMap.put("isEnd", "0");     //不是最后一个
					nowMap.put(keyChar, newWorMap);
					nowMap = newWorMap;
				}
				
				if(i == key.length() - 1){
					nowMap.put("isEnd", "1");    //最后一个
				}
			}
		}
	}
	
	/**
	 * 读取敏感词库中的内容，将内容添加到set集合中
	 * @author moma 
	 * @date 2014年4月20日 下午2:31:18
	 * @return
	 * @version 1.0
	 * @throws Exception 
	 */
	private Set<String> readSensitiveWordFile(){
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		SensitiveWordDao sensitiveWordDao = daoSession.getSensitiveWordDao();
		Set<String> set = new HashSet<String>();
		List<SensitiveWord> list = sensitiveWordDao.loadAll();
		for(SensitiveWord sw : list){
			set.add(sw.getKeyword());
		}
		
		return set;
	}

//	/**
//	 * 读取敏感词库中的内容，将内容添加到set集合中
//	 * @author moma 
//	 * @date 2014年4月20日 下午2:31:18
//	 * @return
//	 * @version 1.0
//	 * @throws Exception 
//	 */
//	private Set<String> readSensitiveWordFile(Context context) throws Exception{
//		Set<String> set = null;
//		
//		AssetManager asm = context.getAssets();
//		InputStream is = asm.open("SensitiveWord");
//		InputStreamReader read = new InputStreamReader(is, ENCODING);
//		set = new HashSet<String>();
//		BufferedReader bufferedReader = new BufferedReader(read);
//		String txt = null;
//		while((txt = bufferedReader.readLine()) != null){    //读取文件，将文件内容放入到set中
//			set.add(txt);
//	    }
//		read.close();
//		
//		return set;
//	}
}
