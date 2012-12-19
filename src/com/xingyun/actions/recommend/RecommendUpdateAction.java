package com.xingyun.actions.recommend;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.base.AuthUserAction;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunLine;
import com.xingyun.services.recommend.RecommendService;
import com.xingyun.services.sinaoauth.SinaOauthService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.PublicQueryUtil;

public class RecommendUpdateAction extends AuthUserAction {

	private static final long serialVersionUID = -5227193236533170504L;
	private static final Logger log = Logger.getLogger(RecommendUpdateAction.class);
	
	private int isShareWeibo;               //是否分享到新浪微博
	private int isShareDynamic;             //是否分享到动态
	private String content;
	private String toUserId;
	private int postID;						//作品ID
	private String postUserID;				//作品用户ID	
	private UserHeaderBean userHeaderBean;	//左侧栏用户数据bean
	private String zpTitle;
	

	/**
	 * 推荐此人才操作
	 */
	public void recommendUser(){
		try{
			if(RecommendService.getInstance().checkIsRecommendOverByType(XingyunCommonConstant.RECOMMEND_TYPE_USER, user.getUserId(), toUserId) > 0){
				sendResponseMsg("recommended");
				return;
			}
			content = StringUtils.isBlank(content) ? "推荐此人" : content;
			RecommendService.getInstance().recommendUserInfo(user.getUserId(), toUserId, content, isShareDynamic);
			if(isShareWeibo == XingyunCommonConstant.XINGYUN_SHAREWEIBO_YES){
				content = "我在最新冒出来#星云网# "+XingyunLine.XINGYUN_CN+" 向大家推荐星云人才 @"+PublicQueryUtil.getInstance().getScreenNameByUserId(toUserId)+" 查看ta的最新星云个人网站>> "+XingyunLine.XINGYUN_CN+CommonUtil.getUserIndexHref(toUserId, PublicQueryUtil.getInstance().findUserWkey(toUserId, false))+" 推荐理由：< "+content+" > 各位火速围观！";
				SinaOauthService.getInstance().shareToWeibo(user.getUserId(), content, RecommendService.getInstance().getLogoUrlByUserId(toUserId));
			}
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 检测此用户是否被推荐过
	 */
	public void checkRecommendUserOver(){
		try{
			if(RecommendService.getInstance().checkIsRecommendOverByType(XingyunCommonConstant.RECOMMEND_TYPE_USER, user.getUserId(), toUserId) > 0){
				sendResponseMsg("recommended");
				return;
			}
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 取消推荐此人
	 */
	public void cancelRecommendUser(){
		try{
			RecommendService.getInstance().cancelRecommendUser(user.getUserId(), toUserId);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 推荐作品
	 */
	public void addRecommendPostAjax(){
		try {
			if(StringUtils.isBlank(content))
				content = "推荐此作品";
			
			if(!RecommendService.getInstance().checkRecommendPost(user.getUserId(), postID, postUserID)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			boolean addTag = RecommendService.getInstance().addRecommendPostData(user.getUserId(), postID, postUserID, content, isShareDynamic);	//添加推荐作品数据
			if(addTag && isShareWeibo == XingyunCommonConstant.XINGYUN_SHAREWEIBO_YES){
				content = "我在最新冒出来#星云网# "+XingyunLine.XINGYUN_CN+" 向大家推荐星云人才 @"+PublicQueryUtil.getInstance().getScreenNameByUserId(postUserID)+" 的一组作品("+zpTitle+")点击链接>> "+XingyunLine.XINGYUN_CN+"/show/"+postID+".html 各位火速围观！";
				SinaOauthService.getInstance().shareToWeibo(user.getUserId(), content, RecommendService.getInstance().getCoverPathByPostId(postID));
			}
			sendResponseMsg(addTag ? XingyunCommonConstant.RESPONSE_SUCCESS_STRING : XingyunCommonConstant.RESPONSE_ERR_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 取消推荐作品
	 */
	public void cancelRecommendPost(){
		try {
			boolean delTag = RecommendService.getInstance().delRecommendPost(user.getUserId(), postID);	//删除推荐作品
			sendResponseMsg(delTag ? XingyunCommonConstant.RESPONSE_SUCCESS_STRING : XingyunCommonConstant.RESPONSE_ERR_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}

	public int getPostID() {
		return postID;
	}

	public void setPostID(int postID) {
		this.postID = postID;
	}
	public UserHeaderBean getUserHeaderBean() {
		return userHeaderBean;
	}

	public void setUserHeaderBean(UserHeaderBean userHeaderBean) {
		this.userHeaderBean = userHeaderBean;
	}

	public int getIsShareWeibo() {
		return isShareWeibo;
	}

	public void setIsShareWeibo(int isShareWeibo) {
		this.isShareWeibo = isShareWeibo;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

	public String getPostUserID() {
		return postUserID;
	}

	public void setPostUserID(String postUserID) {
		this.postUserID = postUserID;
	}

	public String getZpTitle() {
		return zpTitle;
	}

	public void setZpTitle(String zpTitle) {
		this.zpTitle = zpTitle;
	}

	public int getIsShareDynamic() {
		return isShareDynamic;
	}

	public void setIsShareDynamic(int isShareDynamic) {
		this.isShareDynamic = isShareDynamic;
	}
	
}
