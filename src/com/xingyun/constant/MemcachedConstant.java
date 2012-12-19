package com.xingyun.constant;

import java.util.Map;

import com.xingyun.cache.MClient;

public class MemcachedConstant {
	/** 用户在线时间 mc 缓存时间 24小时*/
	public static final int MC_SAVE_TIME = 24 * 60 * 60;
	
	/** 用户公共数据 缓存key值 */
	private static final String USER_COMMON_DATA_KEY = "user_common_data";
	
	/** 展示公共数据 缓存key值 */
	private static final String POST_COMMON_DATA_KEY = "post_common_data";
	
	/**
	 * 获取用户公共数据key
	 */
	private static String getUserCommonDataKey(int userID){
		return USER_COMMON_DATA_KEY + "_" + userID; 
	}
	
	/**
	 * 获取展示公共数据key
	 */
	private static String getPostCommonDataKey(int postID){
		return POST_COMMON_DATA_KEY + "_" + postID;
	}
	
	/**
	 * 设置用户公共数据 缓存
	 * @param userID	用户ID
	 * @param userMap	用户公共数据
	 */
	public static void setUserCommonData(int userID, Map<Object, Object> userMap) throws Throwable{
		String key = getUserCommonDataKey(userID);
		MClient.getInstance().set(key, MC_SAVE_TIME, userMap);
	}
	
	/**
	 * 获取用户公共数据
	 * @param userID	用户ID
	 */
	public static Object getUserCommonData(int userID) throws Throwable{
		String key = getUserCommonDataKey(userID);
		return MClient.getInstance().get(key);
	}
	
	/**
	 * 清理删除 用户公共数据
	 * @param userID	用户ID
	 */
	public static void clearUserCommonData(int userID) throws Throwable{
		String key = getUserCommonDataKey(userID);
		MClient.getInstance().delete(key);
	}
	
	/**
	 * 设置展示公共数据 缓存
	 * @param postID	展示ID
	 * @param postMap	用户公共数据
	 */
	public static void setPostCommonData(int postID, Map<Object, Object> postMap) throws Throwable{
		String key = getPostCommonDataKey(postID);
		MClient.getInstance().set(key, MC_SAVE_TIME, postMap);
	}
	
	/**
	 * 获取展示公共数据
	 * @param postID	展示ID
	 */
	public static Object getPostCommonData(int postID) throws Throwable{
		String key = getPostCommonDataKey(postID);
		return MClient.getInstance().get(key);
	}
	
	/**
	 * 清理删除 展示公共数据
	 * @param postID	展示ID
	 */
	public static void clearPostCommonData(int postID) throws Throwable{
		String key = getPostCommonDataKey(postID);
		MClient.getInstance().delete(key);
	}
}
