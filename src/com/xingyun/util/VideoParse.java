package com.xingyun.util;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class VideoParse {

	private static final Logger log = Logger.getLogger(VideoParse.class);
	
	/**
	 * 新浪 服务
	 */
	private static final String API_SINA_VIDEOINFO = "http://video.sina.com.cn/api/sinaVideoApi.php";
	/**
	 * 土豆 服务
	 */
	private static final String API_TUDOU_VIDEOINFO = "http://api.tudou.com/v3/gw";
	
	/**
	 * 优酷 服务
	 */
	private static final String API_YOUKU_VIDEOINFO = "https://openapi.youku.com/v2/videos/show_basic.json";
	
	/**
	 * 土豆 邮箱或用户名
	 */
//	private static final String API_TUDOU_USERNAME = "mokovideo@163.com";
	private static final String API_TUDOU_USERNAME = "allen@xingyun.cn";
	
	/**
	 * 土豆 密码
	 */
//	private static final String API_TUDOU_PASSWORD = "mokomokomoko";
	private static final String API_TUDOU_PASSWORD = "feipeng123";
	
	/**
	 * 土豆 服务返回的数据格式
	 */
	private static final String API_TUDOU_FORMAT = "json";
	
	/**
	 * 土豆 App Key
	 */
//	private static final String API_TUDOU_APPKEY = "2fe616283b1e6c54";
	private static final String API_TUDOU_APPKEY = "cb17aeefbe5ac0e0";
	
	
	/**
	 * 优酷 App Key
	 */
	private static final String API_YOUKU_APPKEY = "14ad3afe204d62fa";
	
	/**
	 * 请求成功的状态
	 */
	public static final String REQUEST_SUCCESS_CODE = "A200";
	
	/**
	 * 请求失败的状态
	 */
	public static final String REQUEST_FAIL_CODE = "A404";
	
	/**
	 * 请求超时(或异常)的状态
	 */
	private static final String REQUEST_ERROR_CODE = "A408";
	
	/**
	 * 请求失败标记
	 */
	private static boolean ERROR_FLAG = false;
	
	/**
	 * 视频链接引用处理
	 */
	public static String process(String value) throws Throwable {
		Map<String, Object> videoMap = null;
		String errorCode = REQUEST_ERROR_CODE;
		if (StringUtils.isBlank(value))
			return getErrorJson(errorCode);
		if(value.contains("sina.com"))
			videoMap = getSinaVideoSwf(value);
		else if(value.contains("youku.com") && value.indexOf("http://v.youku.com/v_show/id_") != -1)
			videoMap = getYoukuSwf(value);
		else if(value.contains("tudou.com"))
			videoMap = getTuDouSwf(value);
		else
			errorCode = REQUEST_FAIL_CODE;
		if (videoMap == null || ERROR_FLAG){
			errorCode = REQUEST_FAIL_CODE;
			return getErrorJson(errorCode);
		}
		return JsonObjectUtil.getJsonStr(videoMap);
	}
	
	/**
	 * 解析失败返回的json字符串
	 */
	public static String getErrorJson(String errorCode){
		if(StringUtils.EMPTY.equals(errorCode))
			errorCode = REQUEST_ERROR_CODE;
		Map<String,Object> videoMap = new HashMap<String, Object>();
		videoMap.put("code", errorCode);
		return JsonObjectUtil.getJsonStr(videoMap);
	}
	
	/**
	 * 获取土豆视频信息
	 * 例子：http://api.tudou.com/v3/gw?method=repaste.info.get&appKey=2fe616283b1e6c54&url=http://www.tudou.com/programs/view/IS4rinYNMuE/&format=json
	 * 
	 * @param value     swf、html 地址
	 * @param attr      要获取的属性（item 节点下的）
	 */
	public static Map<String,Object> getTuDouSwf(String value){
		try{
			Multimap<String, String> mulMap = ArrayListMultimap.create();
			mulMap.put("method", "repaste.info.get"); // (土豆固定参数值)
			mulMap.put("appKey", API_TUDOU_APPKEY);
			mulMap.put("url", value);
			mulMap.put("format", API_TUDOU_FORMAT);
			String result = getRequest(API_TUDOU_VIDEOINFO, mulMap, true);
			if (StringUtils.isBlank(result))
				return null;
			JSONObject json = new JSONObject(result);
			if (json.length() == 0)
				return null;
			if(json.getJSONObject("repasteInfo") == null)
				return null;
			if(json.getJSONObject("repasteInfo").getJSONObject("itemInfo") == null)
				return null;
			JSONObject itemInfoJson = json.getJSONObject("repasteInfo").getJSONObject("itemInfo");
			if(!itemInfoJson.has("itemUrl") || !itemInfoJson.has("outerPlayerUrl") || !itemInfoJson.has("html5Url") || !itemInfoJson.has("itemCode"))
				return null;
			return setVideoInfoMap(itemInfoJson.getString("itemUrl"), itemInfoJson.getString("outerPlayerUrl"), itemInfoJson.getString("html5Url"), itemInfoJson.getString("itemCode"));
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * 获取优酷视频信息
	 * http://v.youku.com/v_show/id_XNDYyNDU2NTQ0.html
	 * 例子：https://openapi.youku.com/v2/videos/show_basic.json?client_id=14ad3afe204d62fa&video_id=XNDYyNDU2NTg0
	 * 
	 * @param value     swf、html 地址
	 * @param attr      要获取的属性（item 节点下的）
	 */
	public static Map<String,Object> getYoukuSwf(String url){
		try{
			String vid = "";
			if(url.indexOf("http://v.youku.com/v_show/id_") != -1)
				vid = url.substring(url.indexOf("http://v.youku.com/v_show/id_") + 29, url.lastIndexOf(".html"));
			if(StringUtils.EMPTY.equals(vid))
				return null;
			Multimap<String, String> mulMap = ArrayListMultimap.create();
			mulMap.put("client_id", API_YOUKU_APPKEY);
			mulMap.put("video_id", vid);
			String result = getRequest(API_YOUKU_VIDEOINFO, mulMap, true);
			if (StringUtils.isBlank(result))
				return null;
			JSONObject json = new JSONObject(result);
			if (json.length() == 0)
				return null;
			if(!json.has("link") || !json.has("player"))
				return null;
			return setVideoInfoMap(json.getString("link"), json.getString("player"), json.getString("link"), vid);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * 获取新浪视频信息
	 * 例子：http://video.sina.com.cn/api/sinaVideoApi.php?pid=70&data=json&url=http%3a%2f%2fvideo.sina.com.cn%2fv%2fb%2f67833798-2035929804.html
	 * @param value     swf、html 地址
	 */
	public static Map<String,Object> getSinaVideoSwf(String value) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(API_SINA_VIDEOINFO).append("?pid=70&data=json&url=").append(URLEncoder.encode(value, "UTF-8"));
			String result = getRequest(sb.toString());
			if (StringUtils.isBlank(result))
				return null;

			JSONObject json = new JSONObject(result);
			if (json.length() == 0 || !json.has("playlink") || !json.has("playswf") || !json.has("ipad_url") || !json.has("vid"))
				return null;
			return setVideoInfoMap(json.getString("playlink"), json.getString("playswf"), json.getString("ipad_url"), json.getString("vid"));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	private static Map<String,Object> setVideoInfoMap(String playlink, String playswf, String ipadUrl, String vid) throws Throwable{
		Map<String,Object> videoMap = new HashMap<String, Object>();
		videoMap.put("code", REQUEST_SUCCESS_CODE);
		videoMap.put("playlink", playlink);
		videoMap.put("playswf", playswf);
		videoMap.put("ipad_url", ipadUrl);
		videoMap.put("vid", vid);
		return videoMap;
	}
	
	/**
	 * http get请求
	 * @param uri         请求地址
	 */
	private static String getRequest(String uri) {
		final HttpClient client = new HttpClient();
		final GetMethod method = new GetMethod(uri);
		try {
			final int status = client.executeMethod(method);
			if (status == HttpStatus.SC_OK) {
				ERROR_FLAG = false;
				byte[] responseBody = method.getResponseBody();
				return new String(responseBody, "UTF-8");
			} else {
				ERROR_FLAG = true;
			}
		} catch (Throwable e) {
			ERROR_FLAG = true;
			log.error(e.getMessage(), e);
//			SendMails.sendMail(LogUtil.sendLogMsg(e.getMessage(), e));
		} finally {
			if (method != null)
				method.releaseConnection();
			if (client != null)
				client.getHttpConnectionManager().closeIdleConnections(0);
		}
		return StringUtils.EMPTY;
	}
	
	/**
	 * http get请求
	 * 
	 * @param uri         请求地址
	 * @param mulMap      请求参数
	 * @param isAuth      是否使用安全认证
	 */
	private static String getRequest(String uri, Multimap<String, String> mulMap, boolean isAuth) {
		final HttpClient client = new HttpClient();
		final GetMethod method = new GetMethod(uri);
		try {
			if (mulMap != null && mulMap.size() > 0) {
				List<NameValuePair> requestValues = new ArrayList<NameValuePair>(mulMap.size());
				for (final String key : mulMap.keySet()) {
					for (final String value : mulMap.get(key)) {
						requestValues.add(new NameValuePair(key, value));
					}
				}
				method.setRequestHeader("Content-Type", "application/xml; charset=UTF-8");
				method.setQueryString(requestValues.toArray(new NameValuePair[0]));
			}
			if (isAuth)
				method.setRequestHeader("Authorization", makeBathAuth(API_TUDOU_USERNAME, API_TUDOU_PASSWORD));
			final int status = client.executeMethod(method);
			if (status == HttpStatus.SC_OK) {
				ERROR_FLAG = false;
				byte[] responseBody = method.getResponseBody();
				return new String(responseBody, "UTF-8");
			} else {
				ERROR_FLAG = true;
			}
		} catch (Throwable e) {
			ERROR_FLAG = true;
			log.error(e.getMessage(), e);
		} finally {
			if (method != null)
				method.releaseConnection();
			if (client != null)
				client.getHttpConnectionManager().closeIdleConnections(0);
		}
		return StringUtils.EMPTY;
	}
	
	/**
	 * basic认证
	 */
	private static String makeBathAuth(String user, String password) {
		byte[] auth = Base64.encodeBase64((user + ":" + password).getBytes());
		return "Basic " + new String(auth);
	}
	
	public static String getSinaVideoJson(String url) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(API_SINA_VIDEOINFO).append("?pid=70&data=json&url=").append(URLEncoder.encode(url, "UTF-8"));
			String result = getRequest(sb.toString());
			if (StringUtils.isBlank(result))
				return setSinaVideoJson("", "", "");
			JSONObject json = new JSONObject(result);
			if (json.length() == 0 || !json.has("playswf") || !json.has("ipad_url") || !json.has("vid"))
				return setSinaVideoJson("", "", "");
			return setSinaVideoJson(json.getString("vid"), json.getString("playswf"), json.getString("ipad_url"));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return setSinaVideoJson("", "", "");
		}
	}
	
	private static String setSinaVideoJson(String vid, String playswf, String ipadurl){
		Map<String,Object> videoMap = new HashMap<String, Object>();
		videoMap.put("vid", vid);
		videoMap.put("playswf", playswf);
		videoMap.put("ipad_url", ipadurl);
		return JsonObjectUtil.getJsonStr(videoMap);
	}
}
