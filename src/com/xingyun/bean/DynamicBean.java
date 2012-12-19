package com.xingyun.bean;

import java.util.Map;
import java.util.List;

public class DynamicBean {

	private int dynamicID;			//动态ID
	private int dynamicType;		//动态类型 0：发布作品 1：推荐用户 2：推荐作品
	private int dynamicFromType;	//动态来源 0：星云
	
	private String fromUserID;		//产生动态用户ID
	private String fromWkey;		//产生动态用户wkey
	private String fromNickName;	//产生动态用户昵称	
	private String fromUserLogo;	//产生动态用户头像
	private String fromUserHref;	//产生动态用户首页地址
	private int fromUserLevel;		//产生动态用户等级
	private int fromUserVerified;   //产生动态用户是否已认证
	private String fromUserVerifiedReason;  //产生动态用户认证理由
	
	private int postID;				//作品ID
	private String postTitle;		//作品标题
	private String postCoverPic;	//作品封面图片
	private int postType;			//作品类型
	private int postZanCount;		//作品被赞数量
	private boolean postIsZan;		//作品是否赞过 true 赞过 false 未赞过
	
	private String toUserID;		//被推荐用户userid
	private String toWkey;			//被推荐用户wkey
	private String toNickName;		//被推荐用户昵称
	private String toUserLogo;		//被推荐用户头像
	private String toUserHref;	    //被推荐用户首页地址
	private int toUserLevel;		//被推荐用户等级
	private int toUserVerified;     //被推荐用户是否已认证
	private String recommendContent;//推荐内容
	
	private int recommendCount;		//推荐数量
	private int fansCount;	        //粉丝数量
	private int viewCount;			//点击量
	private String systime;			//动态产生时间
	private int isShowfollow;
	
	private XingyuBean xyBean;		//星语主题bean
	
	private Map<Object,Object> contactMap;
	private String jobStatus;
	private List<Map<Object,Object>> skillList;
	private List<Map<Object,Object>> rencaiList;
	private List<Map<Object,Object>> sliderList;
	private List<Map<Object,Object>> videoList;
	
	public int getDynamicID() {
		return dynamicID;
	}
	public void setDynamicID(int dynamicID) {
		this.dynamicID = dynamicID;
	}
	public int getDynamicType() {
		return dynamicType;
	}
	public void setDynamicType(int dynamicType) {
		this.dynamicType = dynamicType;
	}
	public int getDynamicFromType() {
		return dynamicFromType;
	}
	public void setDynamicFromType(int dynamicFromType) {
		this.dynamicFromType = dynamicFromType;
	}
	public String getFromUserID() {
		return fromUserID;
	}
	public void setFromUserID(String fromUserID) {
		this.fromUserID = fromUserID;
	}
	public String getFromWkey() {
		return fromWkey;
	}
	public void setFromWkey(String fromWkey) {
		this.fromWkey = fromWkey;
	}
	public String getFromNickName() {
		return fromNickName;
	}
	public void setFromNickName(String fromNickName) {
		this.fromNickName = fromNickName;
	}
	public int getPostID() {
		return postID;
	}
	public void setPostID(int postID) {
		this.postID = postID;
	}
	public String getPostTitle() {
		return postTitle;
	}
	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}
	public String getPostCoverPic() {
		return postCoverPic;
	}
	public void setPostCoverPic(String postCoverPic) {
		this.postCoverPic = postCoverPic;
	}
	public String getSystime() {
		return systime;
	}
	public void setSystime(String systime) {
		this.systime = systime;
	}
	public String getFromUserLogo() {
		return fromUserLogo;
	}
	public void setFromUserLogo(String fromUserLogo) {
		this.fromUserLogo = fromUserLogo;
	}
	public String getFromUserHref() {
		return fromUserHref;
	}
	public void setFromUserHref(String fromUserHref) {
		this.fromUserHref = fromUserHref;
	}
	public String getToUserID() {
		return toUserID;
	}
	public void setToUserID(String toUserID) {
		this.toUserID = toUserID;
	}
	public String getToWkey() {
		return toWkey;
	}
	public void setToWkey(String toWkey) {
		this.toWkey = toWkey;
	}
	public String getToNickName() {
		return toNickName;
	}
	public void setToNickName(String toNickName) {
		this.toNickName = toNickName;
	}
	public String getRecommendContent() {
		return recommendContent;
	}
	public void setRecommendContent(String recommendContent) {
		this.recommendContent = recommendContent;
	}
	public int getFromUserLevel() {
		return fromUserLevel;
	}
	public void setFromUserLevel(int fromUserLevel) {
		this.fromUserLevel = fromUserLevel;
	}
	public int getToUserLevel() {
		return toUserLevel;
	}
	public void setToUserLevel(int toUserLevel) {
		this.toUserLevel = toUserLevel;
	}
	public String getToUserLogo() {
		return toUserLogo;
	}
	public void setToUserLogo(String toUserLogo) {
		this.toUserLogo = toUserLogo;
	}
	public String getToUserHref() {
		return toUserHref;
	}
	public void setToUserHref(String toUserHref) {
		this.toUserHref = toUserHref;
	}
	public int getRecommendCount() {
		return recommendCount;
	}
	public void setRecommendCount(int recommendCount) {
		this.recommendCount = recommendCount;
	}
	public int getViewCount() {
		return viewCount;
	}
	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}
	public int getFromUserVerified() {
		return fromUserVerified;
	}
	public void setFromUserVerified(int fromUserVerified) {
		this.fromUserVerified = fromUserVerified;
	}
	public int getToUserVerified() {
		return toUserVerified;
	}
	public void setToUserVerified(int toUserVerified) {
		this.toUserVerified = toUserVerified;
	}
	public XingyuBean getXyBean() {
		return xyBean;
	}
	public void setXyBean(XingyuBean xyBean) {
		this.xyBean = xyBean;
	}
	public int getPostType() {
		return postType;
	}
	public void setPostType(int postType) {
		this.postType = postType;
	}
	public int getFansCount() {
		return fansCount;
	}
	public void setFansCount(int fansCount) {
		this.fansCount = fansCount;
	}
	public int getIsShowfollow() {
		return isShowfollow;
	}
	public void setIsShowfollow(int isShowfollow) {
		this.isShowfollow = isShowfollow;
	}
	public int getPostZanCount() {
		return postZanCount;
	}
	public void setPostZanCount(int postZanCount) {
		this.postZanCount = postZanCount;
	}
	public boolean isPostIsZan() {
		return postIsZan;
	}
	public void setPostIsZan(boolean postIsZan) {
		this.postIsZan = postIsZan;
	}
	public String getJobStatus() {
		return jobStatus;
	}
	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}
	public List<Map<Object, Object>> getSkillList() {
		return skillList;
	}
	public void setSkillList(List<Map<Object, Object>> skillList) {
		this.skillList = skillList;
	}
	public List<Map<Object, Object>> getRencaiList() {
		return rencaiList;
	}
	public void setRencaiList(List<Map<Object, Object>> rencaiList) {
		this.rencaiList = rencaiList;
	}
	public List<Map<Object, Object>> getSliderList() {
		return sliderList;
	}
	public void setSliderList(List<Map<Object, Object>> sliderList) {
		this.sliderList = sliderList;
	}
	public List<Map<Object, Object>> getVideoList() {
		return videoList;
	}
	public void setVideoList(List<Map<Object, Object>> videoList) {
		this.videoList = videoList;
	}
	public String getFromUserVerifiedReason() {
		return fromUserVerifiedReason;
	}
	public void setFromUserVerifiedReason(String fromUserVerifiedReason) {
		this.fromUserVerifiedReason = fromUserVerifiedReason;
	}
	public Map<Object, Object> getContactMap() {
		return contactMap;
	}
	public void setContactMap(Map<Object, Object> contactMap) {
		this.contactMap = contactMap;
	}
}
