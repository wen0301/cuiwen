package com.xingyun.session;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.xingyun.bean.SwitchCityBean;
import com.xingyun.cache.MClient;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunLine;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.ResponseUtil;

public class SessionCookie{
	private static final long serialVersionUID = -7729203674656299829L;		
	
	/**根据cookie键值，获取对应值*/
	public static String getCookieValue(HttpServletRequest request, String key){	
		String value = "";
		Cookie[] cookies = request.getCookies();
		if(cookies != null){
			for(Cookie c : cookies){
				if(c.getName().equalsIgnoreCase(key)){
					value = c.getValue();
					break;
				}
			}
		}
		return value;
	}
	/**设置cookie*/
	public static void setCookie(String name, String value, int saveTime) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(saveTime);
		cookie.setPath("/");
		cookie.setDomain(XingyunLine.COOKIE_FLAG);
		ResponseUtil.addCookie(cookie);
	}
	
	/**设置cookie*/
	public static void setCookie(String name, String value, int saveTime, String domain) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(saveTime);
		cookie.setPath("/");
		cookie.setDomain(domain);
		ResponseUtil.addCookie(cookie);
	}
	
	/**
	 * 记录前台登陆用户cookie、mem信息
	 */
	public static void setXingyunCookieMC(String userID) throws Throwable{
		int cookieSaveTime = XingyunCommonConstant.LOGIN_REMEMBER_COOKIE_SAVE_TIME;
		int mcSaveTime = XingyunCommonConstant.LOGIN_REMEMBER_COOKIE_SAVE_TIME;
		String loginkey = CommonUtil.getUUID();
		setCookie(XingyunCommonConstant.XINGYUN_USER_LOGINKEY, loginkey, cookieSaveTime);		//给客户端 写cookie	
		MClient.getInstance().set(loginkey, mcSaveTime, userID); 						//将用户登录的信息写入MC
	}
	
	/**
	 * 记录用户首页切换城市cookie、mem信息
	 */
	public static void setIndexSwitchCityCookieMC(SwitchCityBean switchCityBean) throws Throwable{
		int cookieSaveTime = XingyunCommonConstant.SWITCH_CITY_COOKIE_SAVE_TIME;
		int mcSaveTime = XingyunCommonConstant.SWITCH_CITY_MC_SAVE_TIME;
		String uuid = CommonUtil.getUUID();
		String cookieValue = uuid + "_" + switchCityBean.getProvinceid() + "_" + switchCityBean.getCityid();
		setCookie(XingyunCommonConstant.XINGYUN_SWITCH_CITY, cookieValue, cookieSaveTime);		//给客户端 写cookie
		MClient.getInstance().set(uuid, mcSaveTime, switchCityBean); 							//将用户登录的信息写入MC
	}
	
	/**
	 * 获取用户首页切换城市信息
	 */
	public static SwitchCityBean getIndexSwitchCityBean(HttpServletRequest request) throws Throwable{
		String cookieValue = getCookieValue(request, XingyunCommonConstant.XINGYUN_SWITCH_CITY);
		if(StringUtils.isBlank(cookieValue))
			return new SwitchCityBean();
		
		String[] values = cookieValue.split("_");
		if(values.length != 3)
			return new SwitchCityBean();
		
		Object mcObject = MClient.getInstance().get(values[0]);
		if(mcObject != null)
			return (SwitchCityBean) mcObject;
		
		SwitchCityBean bean = new SwitchCityBean();
		bean.setProvinceid(Integer.parseInt(values[1]));
		bean.setCityid(Integer.parseInt(values[2]));
		MClient.getInstance().set(values[0], XingyunCommonConstant.SWITCH_CITY_MC_SAVE_TIME, bean);	//将用户登录的信息写入MC
		return bean;
	}
	
	/**
	 * 清理cookie数据
	 */
	public static void clearCookie(){
		setCookie(XingyunCommonConstant.XINGYUN_USER_LOGINKEY, null, 0);		
	}
	
	/**
	 * 获取用户登录 loginkey
	 */
	public static String getUserLoginKey(HttpServletRequest request) throws Throwable{
		String loginKey = getCookieValue(request, XingyunCommonConstant.XINGYUN_USER_LOGINKEY);
		if(StringUtils.isBlank(loginKey))
			return StringUtils.EMPTY;
		Object loginValue = MClient.getInstance().get(loginKey);
		if(loginValue == null)
			return StringUtils.EMPTY;
		return loginValue.toString();
	}
}