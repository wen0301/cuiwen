package com.xingyun.session;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.cache.MClient;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.Counter;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.bean.User;

public class UserSessionEnvironment {
	
	/**
	 * 初始用户环境 记录登录用户cookie mem 信息
	 * @param userID		登录用户ID
	 */
	public static void setLogin(String userID) throws Throwable{
		SessionCookie.setXingyunCookieMC(userID);
	}
	
	/** 清除用户环境 */
	public static void clearLogin(HttpServletRequest request) throws Throwable{
		String loginkey = SessionCookie.getCookieValue(request, XingyunCommonConstant.XINGYUN_USER_LOGINKEY);
		if(StringUtils.isBlank(loginkey))
			return;
		// 清除mc
		MClient.getInstance().get(loginkey);
		MClient.getInstance().delete(loginkey);
		// 删除客户端cookie
		SessionCookie.clearCookie();
		// 清除session
		request.getSession().invalidate();
	}
	
	/**整理登录用户信息*/
	public static User getXingyunUser(HttpServletRequest request){
		try {
			String userID = SessionCookie.getUserLoginKey(request);
			if(StringUtils.isBlank(userID))
				return null;
			Counter.updateUserLastLoginTime(userID);	//更新用户最后登录时间
			return PublicQueryUtil.getInstance().getUserById(userID);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	private static Logger log = Logger.getLogger(UserSessionEnvironment.class);
}