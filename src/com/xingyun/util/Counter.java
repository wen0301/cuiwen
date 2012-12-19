package com.xingyun.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.xingyun.cache.MClient;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.db.DBOperate;

public class Counter {

	private static final Logger log = Logger.getLogger(Counter.class);
	
	private String table_name;	//表名
	private String col_name;	//点击量 列名
	private String key_name;	//记录Id列名，表示唯一记录
	
	private static final DBOperate db = new DBOperate();
	public static final int BUFFER_SIZE = 30; 	//memcached存储点击量最大值
	
	/**
	* 参数为 
	* 1.表名
	* 2.计数器的列名
	* 3.查询条件对应的key的列名（也是where xxx = ）
	*/
	public Counter(String table_name, String count_col_name,String key_name) {
		this.table_name = table_name;
		this.col_name = count_col_name;
		this.key_name = key_name;
	}
	
	/**
	 * 返回组装后的键值
	 */
	private String mcKey(Object key_value){
		return table_name + ":" + col_name + ":" + key_value;
	}
	
	/**
	 * 设置memcached中的点击量，即点击量加1
	 */
	public void setCount(Object key_value) throws Throwable {
		String mc_key = mcKey(key_value);
		boolean isSetSuccess = true;
		int count = getCount(key_value) + 1;
		if(count >= BUFFER_SIZE)
			flushCount(key_value, count);		
		else
			isSetSuccess = MClient.getInstance().set(mc_key, count);
		if(!isSetSuccess)
			flushCount(key_value,1);		
	}
	
	/**
	 * 从memcached中取出点击量
	 */
	public int getCount(Object key_value){		
		try {
			String mc_key = mcKey(key_value);
			Object object = MClient.getInstance().get(mc_key);
			int count = object == null ? 0 : Integer.parseInt(object.toString());
			return count;
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return 0;
		}
	}
	
	/**
	 * 当达到memcached最大点击量 达到定义buffer_size值时 并且不是非法刷新操作时将数据更新到数据库 
	 */
	private void flushCount(Object key_value, int count) throws Throwable{
		updateDBCount(key_value,count);
		MClient.getInstance().set(mcKey(key_value), 0);
	}
	
	/**
	 * 将memcached中的点击量 更新到数据库中
	 */
	private void updateDBCount(Object key_value, int count) throws Throwable{
		String sql="UPDATE " + table_name +" SET " + col_name + "=" + col_name + "+" + count +" WHERE " + key_name + " = ?";	
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(key_value);		
		db.updateData(sql, valueList);
	}
	
	/**
	 * 记录用户最后登录时间
	 */
	public static void updateUserLastLoginTime(String userID){
		try {
			String key = "USER_LAST_LOGIN_TIME_" + userID;
			Object obj = MClient.getInstance().get(key);
			long nowTime = System.currentTimeMillis();
			boolean updateTag = false;
			if(obj == null){
				updateTag = true;
			}else{
				long upTime = Long.parseLong(obj.toString());
				if((nowTime - upTime) >= (1000 * 60 * 20))
					updateTag = true;
			}
			if(updateTag){
				MClient.getInstance().set(key, XingyunCommonConstant.SWITCH_CITY_MC_SAVE_TIME, nowTime); 
				String sql = "INSERT INTO user_login_msg (userid, lastlogintime) VALUES(?, ?) ON DUPLICATE KEY UPDATE lastlogintime = ?";
				List<Object> vList = new ArrayList<Object>();
				vList.add(userID);
				vList.add(new Date());
				vList.add(new Date());
				db.updateData(sql, vList);
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
	}
}