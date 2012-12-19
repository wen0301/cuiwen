package com.xingyun.actions.header;

import org.apache.commons.lang.StringUtils;

import com.xingyun.base.XingyunBaseAction;
import com.xingyun.bean.User;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.services.header.UserHeaderService;
import com.xingyun.util.CommonUtil;

public class UserHeaderUtil  extends XingyunBaseAction{
	private static final long serialVersionUID = -5825856586085379842L;
	/**
	 * 根据用户wkey 初始化页面头部信息 	普通用户
	 * @param user 		当前用户信息
	 * @param wKey	被访问用户的WKEY   
	 */
	public static UserHeaderBean getUserHeaderByWKey(User user, String wKey) throws Throwable{
		if(StringUtils.isBlank(wKey))
			return null;
		String toUserID = CommonUtil.getUserIdBywKey(wKey);
		if(StringUtils.EMPTY.equals(toUserID))
		    return null;
		return UserHeaderService.getInstance().initUserHeaderData(user, toUserID);		//根据WKey 整理普通用户头部数据	
	}
	
	/**
	 * 根据用户userID 初始化页面头部信息 
	 * @param user 		当前用户信息
	 * @param toUserID		被访问用户的userID    
	 */
	public static UserHeaderBean getUserHeaderByUserID(User user, String toUserID) throws Throwable{
		if(StringUtils.EMPTY.equals(toUserID))
			return null;
		return UserHeaderService.getInstance().initUserHeaderData(user, toUserID);		//根据userID 整理普通用户头部数据
	}
	/**
	 * 获取动态页左边用户数据
	 */
	public static UserHeaderBean getUserLeftByUserID(User user, String toUserID) throws Throwable{
		return UserHeaderService.getInstance().initUserLeftData(user, toUserID);
	}
	/**
	 * 获取关注及忽略，黑名单列表左边用户数据
	 */
	public static UserHeaderBean getUserHeaderByIphoneUserID(String toUserID, String fromType) throws Throwable{
		return UserHeaderService.getInstance().initIphoneUserData(toUserID, fromType);
	}
}
