package com.xingyun.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.MapMaker;
import com.xingyun.cache.MClient;
import com.xingyun.constant.UploadFileConstant;

public class UploadUrlUtils {
	
	private static final int UPKEY_CACHE_TIME = 3600;			// upkey 缓存时间 单位秒
	private static final int UPLOAD_STATUS_CACHE_TIME = 60;		//上传进度缓存时间 60 分钟
	private static final String UPKEY = "upKey";				//UPKEY 参数名
	private static final ConcurrentMap<String, Object> uploadCache = new MapMaker().expiration(UPLOAD_STATUS_CACHE_TIME, TimeUnit.MINUTES).makeMap();// 构建一个 Map;
	
	/**
	 * 生成上传url 前台 调用
	 */
	public static String createUploadUrl() throws Throwable{
		String upkey = CommonUtil.getUUID();						//生成upkey值
		String uploadServer = UploadDBUtil.getUploadServer();
		String uploadUrl = getUploadUrl(uploadServer, upkey);		//整理上传url
		setUpKeyToMC(upkey);										//缓存MC upkey值
		return getReturnJson(upkey, uploadUrl, uploadServer);		//返回json 上传地址
	}
	
	/**
	 * 设置上传进度信息	upload 调用
	 */
	public static void putUploadStatus(String upKey, Object bean) throws Throwable{
		uploadCache.put(upKey, bean);		//缓存上传进度信息
		removeUpKeyFromMC(upKey);			//清理MC upkey
	}
	
	/**
	 * 清理上传数据调用
	 */
	public static void cancelUpload(String upkey) throws Throwable{
		removeUpKeyFromMC(upkey);			//清理MC upkey
		uploadCache.remove(upkey);			//清理上传进度信息
	}
	
	/**
	 * 获取上传进度信息	upload 调用
	 */
	public static Object getUploadStatus(HttpServletRequest request){
		String upkey = getUpKeuToRequest(request);
		if(StringUtils.isBlank(upkey))
			return null;
		return uploadCache.get(upkey);
	}
	
	/**
	 * 从request请求中获取upkey
	 */
	private static String getUpKeuToRequest(HttpServletRequest request){
		return request.getParameter(UPKEY);
	}
	
	/**
	 * 获取upkey信息
	 */
	public static String getUpKey(HttpServletRequest request) throws Throwable{
		String upkey = getUpKeuToRequest(request);	//从request中获取upkey值
		if(StringUtils.isBlank(upkey))
			return StringUtils.EMPTY;
		return getUpKeyFromMC(upkey);				//根据upkey值 从MC中获取 upkey值
	}
	
	/**
	 * 整理响应json
	 */
	private static String getReturnJson(String upkey, String uploadUrl, String uploadServer){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("uploadurl", uploadUrl);
		map.put(UPKEY, upkey);
		map.put("uploadserver", uploadServer);
		map.put("webserver", UploadDBUtil.PIC_WEBSERVER_BY_UPLOADSERVER.get(uploadServer));
		return JsonObjectUtil.getJsonStr(map);
	}
	
	/**
	 * 根据upkey 生成上传url
	 */
	private static String getUploadUrl(String uploadServer, String upkey) throws Throwable{
		StringBuilder uploadUrlBuder = new StringBuilder();
		uploadUrlBuder.append(uploadServer);
		uploadUrlBuder.append(UploadFileConstant.UPLOAD_FILE_URL);
		uploadUrlBuder.append("?").append(UPKEY).append("=");
		uploadUrlBuder.append(upkey);
		return uploadUrlBuder.toString();
	}
	
	/**
	 * 设置upkey到MC
	 */
	private static void setUpKeyToMC(String upkey) throws Throwable {
		MClient.getInstance().set(upkey, UPKEY_CACHE_TIME, upkey);
	}
	
	/**
	 * 从MC中获取upkey 
	 */
	public static String getUpKeyFromMC(String upkey) throws Throwable {
		Object obj = MClient.getInstance().get(upkey);
		if(obj == null)
			return StringUtils.EMPTY;
		return obj.toString();
	}
	
	/**
	 * 清除upkey
	 */
	private static void removeUpKeyFromMC(String upkey) throws Throwable {
		MClient.getInstance().delete(upkey);
	}
}
