package com.xingyun.actions.common;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.base.XingyunBaseAction;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.services.common.CommonService;
import com.xingyun.services.recommend.RecommendService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.ResponseUtil;
import com.xingyun.util.UploadPicUtil;
import com.xingyun.util.UploadUrlUtils;

public class CommonAction extends XingyunBaseAction{
	
	private static final long serialVersionUID = 279450079699816491L;
	private static final Logger log = Logger.getLogger(CommonAction.class);
	private String upkey;
	private String callback;
	private String uploadType;
	private String imgServerName;
	private Map<Object,Object> userFloatLayerMap;
	private String resPicID;	//图片资源ID
	
	/**
	 * 获取上传服务信息
	 */
	public void getUploadUrl(){
		try {
			if(StringUtils.isBlank(callback)){
				ResponseUtil.sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);	// 返回错误信息
				return;
			}
			String uploadJson = callback + "(" + UploadUrlUtils.createUploadUrl() + ")";
			sendResponseMsg(uploadJson);		// 返回上传地址信息
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);	// 返回错误信息
		}
	}
	
	/**
	 * 清理上传信息
	 */
	public void cancelUpload(){
		try {
			if(StringUtils.isBlank(upkey))
				return;
			UploadUrlUtils.cancelUpload(upkey);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 根据图片资源ID 上传服务信息
	 */
	public void getResPicMsg(){
		try {
			if(StringUtils.isBlank(callback)){
				ResponseUtil.sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);	// 返回错误信息
				return;
			}
			
			String picMsg = UploadPicUtil.getResPicMsg(resPicID);
			String uploadJson = StringUtils.isBlank(resPicID) ? XingyunCommonConstant.RESPONSE_ERR_STRING : callback + "(" + picMsg + ")";
			sendResponseMsg(uploadJson);		// 返回上传地址信息
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);	// 返回错误信息
		}
	}
	/**
	 * 推荐用户
	 */
	public void recommendUserInfo() {
		try {
			if(!checkRequestCommonParam()){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			String fromUserId = servletRequest.getParameter("fromuserid");
			
			if(servletRequest.getParameter("touserid") == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			String toUserId = servletRequest.getParameter("touserid");
			
			if(RecommendService.getInstance().checkIsRecommendOverByType(XingyunCommonConstant.RECOMMEND_TYPE_USER, fromUserId, toUserId) > 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			
			String reason = servletRequest.getParameter("reason");
			if(servletRequest.getParameter("reason") == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			
			RecommendService.getInstance().recommendUserInfo(fromUserId, toUserId, reason, XingyunCommonConstant.XINGYUN_SHAREDYNAMIC_YES);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {// 处理失败了
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 取消推荐用户
	 */
	public void cancleRecommendUserInfo() {
		try {
			if(!checkRequestCommonParam()){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			String fromUserId = servletRequest.getParameter("fromuserid");
			
			if(servletRequest.getParameter("touserid") == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			String toUserId = servletRequest.getParameter("touserid");
			
			boolean isSuccess = RecommendService.getInstance().cancelRecommendUser(fromUserId, toUserId);
			sendResponseMsg(isSuccess ? XingyunCommonConstant.RESPONSE_SUCCESS_STRING : XingyunCommonConstant.RESPONSE_ERR_STRING);
		} catch (Throwable e) {// 处理失败了
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 推荐作品
	 */
	public void recommendPostInfo() {
		try {
			if(!checkRequestCommonParam()){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			String fromUserId = servletRequest.getParameter("fromuserid");
			
			if(servletRequest.getParameter("postid") == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			int postId = Integer.parseInt(servletRequest.getParameter("postid"));
			
			if(servletRequest.getParameter("touserid") == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			String toUserId = servletRequest.getParameter("touserid");
			
			if(RecommendService.getInstance().checkIsRecommendOverByType(XingyunCommonConstant.RECOMMEND_TYPE_POST, fromUserId, postId + "") > 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			
			String reason = servletRequest.getParameter("reason");
			if(servletRequest.getParameter("reason") == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			
			RecommendService.getInstance().addRecommendPostData(fromUserId, postId, toUserId, reason, XingyunCommonConstant.XINGYUN_SHAREDYNAMIC_YES);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {// 处理失败了
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 取消推荐作品
	 */
	public void cancleRecommendPostInfo() {
		try {
			if(!checkRequestCommonParam()){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			String fromUserId = servletRequest.getParameter("fromuserid");
			
			if(servletRequest.getParameter("postid") == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			int postId = Integer.parseInt(servletRequest.getParameter("postid"));
			
			boolean isSuccess = RecommendService.getInstance().delRecommendPost(fromUserId, postId);
			sendResponseMsg(isSuccess ? XingyunCommonConstant.RESPONSE_SUCCESS_STRING : XingyunCommonConstant.RESPONSE_ERR_STRING);
		} catch (Throwable e) {// 处理失败了
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 获取系统上传新图片名Url
	 */
	public void getSysNewPicName(){
		try {
			if(StringUtils.isBlank(callback)){
				ResponseUtil.sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);	// 返回错误信息
				return;
			}
			String uploadJson = callback + "(" + UploadUrlUtils.createUploadUrl() + ")";
			sendResponseMsg(uploadJson);		// 返回上传地址信息
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);	// 返回错误信息
		}
	}
	
	/**
	 * 根据UID或wKey获取用户资料浮动层信息
	 */
	public String getUserFloatLayer(){
		try{
			userFloatLayerMap = CommonService.getInstance().getUserFloatLayerData(user, userid);
			return "showUserFloatLayer";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	private boolean checkRequestCommonParam() throws Throwable{
		if(servletRequest.getParameter("username") == null)
			return false;
		String userName = servletRequest.getParameter("username");
		if(servletRequest.getParameter("password") == null)
			return false;
		String password = servletRequest.getParameter("password");
		if(!CommonService.getInstance().checkIsAdminUser(userName, password))
			return false;
		if(servletRequest.getParameter("fromuserid") == null)
			return false;
		String fromUserId = servletRequest.getParameter("fromuserid");
		if(!CommonUtil.checkIsXingyunUID(fromUserId))
			return false;
		return true;
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public String getUpkey() {
		return upkey;
	}

	public void setUpkey(String upkey) {
		this.upkey = upkey;
	}

	public String getUploadType() {
		return uploadType;
	}

	public void setUploadType(String uploadType) {
		this.uploadType = uploadType;
	}

	public String getImgServerName() {
		return imgServerName;
	}

	public void setImgServerName(String imgServerName) {
		this.imgServerName = imgServerName;
	}

	public Map<Object, Object> getUserFloatLayerMap() {
		return userFloatLayerMap;
	}

	public void setUserFloatLayerMap(Map<Object, Object> userFloatLayerMap) {
		this.userFloatLayerMap = userFloatLayerMap;
	}

	public String getResPicID() {
		return resPicID;
	}

	public void setResPicID(String resPicID) {
		this.resPicID = resPicID;
	}
	
}
