package com.xingyun.actions.sinaoauth;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.base.XingyunBaseAction;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunOauthConstant;
import com.xingyun.services.sinaoauth.SinaOauthService;
import com.xingyun.session.SessionCookie;
import com.xingyun.session.UserSessionEnvironment;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.UserManageUtil;
import com.xingyun.weibo4j.http.AccessToken;

public class SinaOauthAction extends XingyunBaseAction {
	private static final long serialVersionUID = 2881354612327558641L;
	private static final Logger log = Logger.getLogger(SinaOauthAction.class);
	private String state;
	private String screenName;
	private String email;
	private String phone;
	private String inviteCode;
	private String userId;
	private boolean isWeiboBind;
	private String referer;
	
	/**
	 * 新浪api的回调
	 */
	public String callback(){
		try{
			//当用户或授权服务器拒绝授予数据访问权限时
			if(servletRequest.getParameter("error_code") != null && XingyunOauthConstant.ERROR_CODE_DENY.equals(servletRequest.getParameter("error_code"))){
				referer = SessionCookie.getCookieValue(servletRequest, XingyunOauthConstant.XINGYUN_SINA_REFERER);
				if(StringUtils.EMPTY.equals(referer))
					getResponse().sendRedirect("/");
				else{
					SessionCookie.setCookie(XingyunOauthConstant.XINGYUN_SINA_REFERER, null, 0);
					getResponse().sendRedirect(referer);
				}
				return null;
			}
			if(servletRequest.getParameter("code") == null)
				return "del";
			String code = servletRequest.getParameter("code");
			if(servletRequest.getParameter("state") == null)
				return "del";
			state = servletRequest.getParameter("state");
			AccessToken accessToken = SinaOauthService.getInstance().getSinaAccessToken(code);
			if(accessToken == null)
				return "del";
			String sinaUserId = accessToken.getUid();
			String userId = SinaOauthService.getInstance().getUserIdBySinaUserId(sinaUserId);
			/**用户在登录情况下，发布作品或推荐用户/作品时授权关联 */
			if(user != null && (XingyunOauthConstant.SOURCE_SHARE_OTHER.equals(state) || XingyunOauthConstant.SOURCE_SHARE_POST.equals(state))){
				isWeiboBind = SinaOauthService.getInstance().updateWeiboToken(userId, sinaUserId, accessToken.getAccessToken(), accessToken.getExpireIn(), false);
				return "callbackPop";
			}
			/**新浪微博账号未授权关联美空用户，注册星云游客 */
			if(XingyunOauthConstant.SOURCE_YOUKE.equals(state) && StringUtils.EMPTY.equals(userId)){
				SinaOauthService.getInstance().setCookie(accessToken, sinaUserId);
				getResponse().sendRedirect("/bound/");
				return null;
			}
			/**新浪微博账号已与星云用户授权关联过，自动登录 */
			UserSessionEnvironment.setLogin(userId);//设置登录状态
			user = PublicQueryUtil.getInstance().getUserById(userId);
			SinaOauthService.getInstance().updateWeiboToken(userId, sinaUserId, accessToken.getAccessToken(), accessToken.getExpireIn(), true);
			referer = SessionCookie.getCookieValue(servletRequest, XingyunOauthConstant.XINGYUN_SINA_REFERER);
			if(!StringUtils.EMPTY.equals(referer)){
				SessionCookie.setCookie(XingyunOauthConstant.XINGYUN_SINA_REFERER, null, 0);
				getResponse().sendRedirect(referer);
				return null;
			}
			return "callbackPop";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 通过新浪微博注册游客
	 */
	public void registerYouke(){
		try{
			String checkEmail = CommonUtil.checkIsExistEmail(email);
			if(!XingyunCommonConstant.RESPONSE_SUCCESS_STRING.equals(checkEmail)){
				sendResponseMsg(checkEmail);
				return;
			}
			String maxId = SinaOauthService.getInstance().registerYouke(servletRequest, email, phone);
			if(StringUtils.EMPTY.equals(maxId)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			UserSessionEnvironment.setLogin(maxId);//设置登录状态
			user = PublicQueryUtil.getInstance().getUserById(maxId);
			if(!StringUtils.EMPTY.equals(maxId)){
				sendResponseMsg(user.getLid() == XingyunCommonConstant.USER_LEVEL_YOUKE ? "youke" : "jingying");
				return;
			}
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 通过新浪微博绑定邀请用户
	 */
	public void connectInviteUser(){
		try{
			if(user == null || user.getLid() != XingyunCommonConstant.USER_LEVEL_YOUKE){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			Map<Object,Object> map = SinaOauthService.getInstance().getInviteMapByInviteCode(inviteCode);
			if(map == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			String inviteSinaUserId = CommonUtil.getStringValue(map.get("sinauserid"));
			String sinaUserId = SinaOauthService.getInstance().getSinaUserIdByUserId(user.getUserId());
			if(StringUtils.isNotBlank(inviteSinaUserId) && !sinaUserId.equals(inviteSinaUserId)){
				sendResponseMsg("invitecode error");
				return;
			}
			String inviteUserId = CommonUtil.getStringValue(map.get("userid"));
			UserManageUtil.getInstance().connectInviteUserInfo(user.getUserId(), inviteUserId, sinaUserId, inviteCode);
			UserSessionEnvironment.setLogin(inviteUserId);//设置登录状态
			user = PublicQueryUtil.getInstance().getUserById(inviteUserId);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 检测邀请码是否存在
	 */
	public void checkIsValidInviteCode(){
		try{
			sendResponseMsg(SinaOauthService.getInstance().checkInviteCodeIsActivate(inviteCode) + "");
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 检测Token是否有效
	 */
	public void checkTokenIsValidAjax(){
		try{
			if(user == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			Map<Object,Object> tokenInfoMap = SinaOauthService.getInstance().getUserTokenInfo(user.getUserId());
			if(tokenInfoMap != null && XingyunOauthConstant.TOKEN_T.equals(SinaOauthService.getInstance().checkTokenIsValid(tokenInfoMap))){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
				return;
			}
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}catch(Throwable e){
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			log.error(e.getMessage(), e);
		}
	}
	/**
	 * 检测昵称
	 */
	public void checkNickNameIsValid(){
		try{
			sendResponseMsg(CommonUtil.checkIsExistNickName(screenName));
		}catch(Throwable e){
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			log.error(e.getMessage(), e);
		}
	}
	/**
	 * 检测邮箱
	 */
	public void checkEmailIsValid(){
		try{
			sendResponseMsg(CommonUtil.checkIsExistEmail(email));
		}catch(Throwable e){
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			log.error(e.getMessage(), e);
		}
	}
	/**
	 * 检测wkey
	 */
	public void checkWkeyIsValid(){
		try{
			sendResponseMsg(CommonUtil.checkIsExistWkey(wKey));
		}catch(Throwable e){
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			log.error(e.getMessage(), e);
		}
	}
	/**
	 * 登出
	 */
	public void loginOut(){
		try {
			UserSessionEnvironment.clearLogin(servletRequest);// 清除用户环境
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			log.error(e.getMessage(), e);
		}
	}
	/**
	 * 通过邀请码升级
	 */
	public String goInviteConnectUser(){
		try{
			if(user == null || user.getLid() != XingyunCommonConstant.USER_LEVEL_YOUKE)
				return "del";
			return "inviteConnect";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	public void loginByWeibo(){
		try {
			SessionCookie.setCookie(XingyunOauthConstant.XINGYUN_SINA_REFERER, referer, XingyunOauthConstant.XINGYUN_TOKEN_SAVE_TIME);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			log.error(e.getMessage(), e);
		}
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	public boolean getIsWeiboBind() {
		return isWeiboBind;
	}

	public void setIsWeiboBind(boolean isWeiboBind) {
		this.isWeiboBind = isWeiboBind;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}
}
