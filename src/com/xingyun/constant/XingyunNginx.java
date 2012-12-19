package com.xingyun.constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.cache.MClient;
import com.xingyun.db.DBOperate;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.PathUrlUtil;

public class XingyunNginx {

	private static final Logger log = Logger.getLogger(XingyunNginx.class);
	private static final DBOperate db = new DBOperate();	
	private static final String CSS_JS = "";
	private static final String SCRIPT = CSS_JS + "script";
	private static final String CSS = CSS_JS + "css";
	
	static{
		initJsCssVersion();
	}
	
	public static String getScript(String fileName) throws Throwable {
		return getWebUrl(SCRIPT, fileName, XingyunLine.CSS_JS_DOMAIN);
	}

	public static String getCss(String fileName) throws Throwable {
		return getWebUrl(CSS, fileName, XingyunLine.CSS_JS_DOMAIN);
	}
	
	/**
	 * 整理js css weburl
	 */
	private static String getWebUrl(String fold,String fileName,String domaiName) throws Throwable {
		String url = (String)MClient.getInstance().get(fileName);
		// mc 存在
		if (url != null && !"".equals(url)){
			return url;
		}
		//获取文件版本号
		String version = getVersionByFileName(fileName);
		//整理文件访问url
		url = getUrl(domaiName, fold, version, fileName);			
		MClient.getInstance().set(fileName, url);
		return url;
	}
	
	/**
	 * 整理文件访问url
	 * @param domaiName	域名
	 * @param fold		文件路径
	 * @param version	版本号
	 * @param fileName	文件名
	 */
	private static String getUrl(String domaiName, String fold, String version,String fileName) {
		String flag = XingyunSystemConfig.getConfigValue(XingyunSystemConfig.DOMAIN_FLAG);
		if (StringUtils.isNotBlank(flag))
			return domaiName + "/" + fold + "/" + flag + "_" + version + "_" + fileName;
		else
			return domaiName + "/" + fold + "/" + fileName;
	}
	
	/** 根据文件名获得版本 */
	private static String getVersionByFileName(String fileName) throws Throwable{
		String sql = "SELECT version FROM js_config WHERE name='" + fileName + "'";
		List<Map<Object, Object>> list = db.retrieveSQL(sql);
		String version = list.size() == 0 ? "1" : list.get(0).get("version").toString();
		while(version.length() < 4){
			version = "0" + version;
		}
		int length = version.length();
		return version.substring(length - 4, length - 2) + "_" + version.substring(length - 2);
	}
	
	/**
	 * 初始化 js css 版本信息
	 */
	public static void initJsCssVersion(){
		try {
			List<String> sqlList = new ArrayList<String>();
			Map<String, Map<Object, Object>> cssjsMap = getCssJssMap();
			sqlList.addAll(getJsCssUpdateSql(cssjsMap, SCRIPT, ".js"));
			sqlList.addAll(getJsCssUpdateSql(cssjsMap, CSS, ".css"));
			if(sqlList.size() > 0)
				db.batchExecute(sqlList, true);
		} catch (Throwable e) {
			log.error(e);
		}
	}
	
	private static List<String> getJsCssUpdateSql(Map<String, Map<Object, Object>> cssjsMap, String filePath, String tag) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		String jsPath = PathUrlUtil.getFullStorePath(filePath);
		File[] files = new File(jsPath).listFiles();
		String fileName = "";
		String hashcode = "";
		String sql = "";
		for(File file : files){
			fileName = file.getName();
			if(!fileName.endsWith(tag))
				continue;
			
			sql = "";
			hashcode = "" + file.lastModified();
			if(cssjsMap == null || cssjsMap.get(fileName) == null)
				sql = "INSERT INTO js_config(name, hashcode, version, systime) VALUES('" + fileName + "', '" + hashcode + "', 1, NOW())";
			else if(cssjsMap.get(fileName) != null && !hashcode.equals(cssjsMap.get(fileName).get("hashcode").toString()))
				sql = "UPDATE js_config SET version = version + 1, hashcode = '" + hashcode + "' WHERE id = " + cssjsMap.get(fileName).get("id").toString();
			
			MClient.getInstance().delete(fileName);
			if(StringUtils.isNotBlank(sql))
				sqlList.add(sql);
		}
		return sqlList;
	}
	
	/**
	 * 获取文件MD5值
	 */
	public static String getFileMD5(File file) throws Throwable{
		StringBuilder sb = new StringBuilder();
		InputStream is = new FileInputStream(file);
		int length = 1024;
		byte[] str = new byte[length];
		int num;  
		while ((num = is.read(str, 0, length)) != -1){ 
			sb.append(new String(str, 0, num, "utf-8"));
		}
		is.close();
		return CommonUtil.getMD5(sb.toString());
	}
	
	private static Map<String, Map<Object, Object>> getCssJssMap() throws Throwable{
		Map<String, Map<Object, Object>> cssjsMap = new HashMap<String, Map<Object,Object>>();
		String sql = "SELECT id, hashcode, version, name FROM js_config";
		List<Map<Object, Object>> list = db.retrieveSQL(sql);
		if(list.size() == 0)
			return cssjsMap;
		
		for(Map<Object, Object> map : list)
			cssjsMap.put(map.get("name").toString(), map);
		return cssjsMap;
	}
}
