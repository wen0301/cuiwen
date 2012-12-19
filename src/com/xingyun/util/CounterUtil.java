package com.xingyun.util;

/**
 *	存取各个功能的memcached点击量 
 */
public class CounterUtil {

	/** 单个作品点击量 Counter */
	private static Counter getPostViewCountCounter(){
		return new Counter("post_counter", "viewcount", "postid");
	}
	
	/** 设置单个作品的点击量 */
	public static void setPostViewCount(int postID) throws Throwable{
		getPostViewCountCounter().setCount(postID);
	}
	
	/** 获取单个作品 memcached中的总点击量 */
	public static int getPostViewCount(int postID){
		return getPostViewCountCounter().getCount(postID);
	}
	
	/** 个人主页总点击量 Counter */
	private static Counter getHomeCountCounter(){
		return new Counter("user_visit_counter", "homecount", "userid");
	}
	
	/** 设置个人主页总点击量 */
	public static void setHomeCount(String userId) throws Throwable{
		getHomeCountCounter().setCount(userId);
	}
	
	/** 获取单个用户主页memcached中的总点击量 */
	public static int getHomeCount(String userId){
		return getHomeCountCounter().getCount(userId);
	}
	
	/** 档案总点击量 Counter */
	private static Counter getProfileCountCounter(){
		return new Counter("user_visit_counter", "profilecount", "userid");
	}
	
	/** 设置档案总点击量 */
	public static void setProfileCount(String userId) throws Throwable{
		getProfileCountCounter().setCount(userId);
	}
	
	/** 获取档案memcached中的总点击量 */
	public static int getProfileCount(String userId){
		return getProfileCountCounter().getCount(userId);
	}
	
	/** 展示总点击量 Counter */
	private static Counter getPostCountCounter(){
		return new Counter("user_visit_counter", "postcount", "userid");
	}
	
	/** 设置展示总点击量 */
	public static void setPostCount(String userId) throws Throwable{
		getPostCountCounter().setCount(userId);
	}
	
	/** 获取单个用户展示memcached中的总点击量 */
	public static int getPostCount(String userId){
		return getPostCountCounter().getCount(userId);
	}
	
	/** 推荐总点击量 Counter */
	private static Counter getRecommendCountCounter(){
		return new Counter("user_visit_counter", "recommendcount", "userid");
	}
	
	/** 设置推荐总点击量 */
	public static void setRecommendCount(String userId) throws Throwable{
		getRecommendCountCounter().setCount(userId);
	}
	
	/** 获取单个用户推荐memcached中的总点击量 */
	public static int getRecommendCount(String userId){
		return getRecommendCountCounter().getCount(userId);
	}
	/** 星语总点击量 Counter */
	private static Counter getXingyuCountCounter(){
		return new Counter("user_visit_counter", "xingyucount", "userid");
	}
	
	/** 设置星语总点击量 */
	public static void setXingyuCount(String userId) throws Throwable{
		getXingyuCountCounter().setCount(userId);
	}
	
	/** 获取星语memcached中的总点击量 */
	public static int getXingyuCount(String userId){
		return getXingyuCountCounter().getCount(userId);
	}
}
