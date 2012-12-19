package com.xingyun.bean;

import java.util.List;
import java.util.Map;

public class UserHeaderBean {
	private String userId;                              //用户id
	private String nickName;                            //昵称
	private String email;                               //邮箱
	private String logoUrl;                             //头像
	private String logoUrl_640;							//头像原始大小
	private boolean isDefaultLogo;						//默认头像
	private int gender;                                 //性别
	private Map<Object,Object> location;                //所在地
	private int lid;                                    //级别
	private List<Map<Object,Object>> vocationList;		//用户行业
	private int verified;                               //是否认证
	private String verified_reason;                     //认证内容
	private int followCount;                            //关注数
	private int fansCount;                              //粉丝数
	private int biFollowCount;                          //相互关注数
	private int commentCount;                           //评论数
	private int zanCount;                               //赞数
	private int friendCount;                            //好友数
	private int recommendPoint;                         //推荐指数
	private int recommendUserCount;                     //推荐人才数
	private int visitCount;                             //总访问量
	private String joinDate;                            //加入时间
	private int picPostCount;                           //图片作品数
	private int videoPostCount;                         //视频作品数
	private int recommendAllCount;                      //总推荐数
	private int recommentToUserAndPostCount;            //推荐过他的用户总数和推荐过他作品的用户总数
	private String introduction;                        //一句话简介
	private String lookState;                     	    //当前用户的访问状态（自己、其他登陆用户、游客）
	private int isShowfollow;                           //是否显示头部关注数
	private boolean isRecommendUser;					//是否已经推荐过此人
	private String userHref;							//用户首页链接地址	
	private boolean isXingyunUID;                       //是否为星云用户
	private String xyNumber;                            //星云号
	private int followType;							    //关注按钮显示类型(收藏，取消收藏)
	private int friendRelationType;                     //好友关系
	private int profileCount;                           //档案数量
	private String screenName;                          //新浪微博昵称
	private int xyProxy;                          		//星云商业代理
	private boolean isPayUser;							//是否商业付费会员
	private int xingyuCount;							//星语数量
	private String jobStatus;							//求职状态
	private int collectionPostCount;                    //收藏作品数
	private boolean isRencai;                      		//是否是星云人才
	
	
	/** 以下为帐号设置里联系方式属性 */
	private String mobile;								//手机
	private String qq;									//QQ
	private String weixin;								//微信
	private String msn;									//MSN
	private String weibourl;							//微博地址
	private String brokertel;							//经纪人电话
	private List<String> contactStatusList; 		    // 联系方式显示与否状态列
	
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getLogoUrl() {
		return logoUrl;
	}
	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public Map<Object, Object> getLocation() {
		return location;
	}
	public void setLocation(Map<Object, Object> location) {
		this.location = location;
	}
	public int getLid() {
		return lid;
	}
	public void setLid(int lid) {
		this.lid = lid;
	}
	public List<Map<Object, Object>> getVocationList() {
		return vocationList;
	}
	public void setVocationList(List<Map<Object, Object>> vocationList) {
		this.vocationList = vocationList;
	}
	public int getRecommendPoint() {
		return recommendPoint;
	}
	public void setRecommendPoint(int recommendPoint) {
		this.recommendPoint = recommendPoint;
	}
	public int getRecommendUserCount() {
		return recommendUserCount;
	}
	public void setRecommendUserCount(int recommendUserCount) {
		this.recommendUserCount = recommendUserCount;
	}
	public int getVisitCount() {
		return visitCount;
	}
	public void setVisitCount(int visitCount) {
		this.visitCount = visitCount;
	}
	public String getJoinDate() {
		return joinDate;
	}
	public void setJoinDate(String joinDate) {
		this.joinDate = joinDate;
	}
	public String getIntroduction() {
		return introduction;
	}
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
	public String getLookState() {
		return lookState;
	}
	public void setLookState(String lookState) {
		this.lookState = lookState;
	}
	public int getRecommendAllCount() {
		return recommendAllCount;
	}
	public void setRecommendAllCount(int recommendAllCount) {
		this.recommendAllCount = recommendAllCount;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public boolean getIsRecommendUser() {
		return isRecommendUser;
	}
	public void setIsRecommendUser(boolean isRecommendUser) {
		this.isRecommendUser = isRecommendUser;
	}
	public void setRecommendUser(boolean isRecommendUser) {
		this.isRecommendUser = isRecommendUser;
	}
	public String getVerified_reason() {
		return verified_reason;
	}
	public void setVerified_reason(String verified_reason) {
		this.verified_reason = verified_reason;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserHref() {
		return userHref;
	}
	public void setUserHref(String userHref) {
		this.userHref = userHref;
	}
	public boolean getIsXingyunUID() {
		return isXingyunUID;
	}
	public void setIsXingyunUID(boolean isXingyunUID) {
		this.isXingyunUID = isXingyunUID;
	}
	public void setXingyunUID(boolean isXingyunUID) {
		this.isXingyunUID = isXingyunUID;
	}
	public String getXyNumber() {
		return xyNumber;
	}
	public void setXyNumber(String xyNumber) {
		this.xyNumber = xyNumber;
	}
	public int getFriendRelationType() {
		return friendRelationType;
	}
	public void setFriendRelationType(int friendRelationType) {
		this.friendRelationType = friendRelationType;
	}
	public int getFriendCount() {
		return friendCount;
	}
	public void setFriendCount(int friendCount) {
		this.friendCount = friendCount;
	}
	public int getProfileCount() {
		return profileCount;
	}
	public void setProfileCount(int profileCount) {
		this.profileCount = profileCount;
	}
	public boolean getIsDefaultLogo() {
		return isDefaultLogo;
	}
	public void setIsDefaultLogo(boolean isDefaultLogo) {
		this.isDefaultLogo = isDefaultLogo;
	}
	public String getLogoUrl_640() {
		return logoUrl_640;
	}
	public void setLogoUrl_640(String logoUrl_640) {
		this.logoUrl_640 = logoUrl_640;
	}
	public String getScreenName() {
		return screenName;
	}
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	public int getVerified() {
		return verified;
	}
	public void setVerified(int verified) {
		this.verified = verified;
	}
	public int getXyProxy() {
		return xyProxy;
	}
	public void setXyProxy(int xyProxy) {
		this.xyProxy = xyProxy;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getQq() {
		return qq;
	}
	public void setQq(String qq) {
		this.qq = qq;
	}
	public String getMsn() {
		return msn;
	}
	public void setMsn(String msn) {
		this.msn = msn;
	}
	public String getWeibourl() {
		return weibourl;
	}
	public void setWeibourl(String weibourl) {
		this.weibourl = weibourl;
	}
	public String getBrokertel() {
		return brokertel;
	}
	public void setBrokertel(String brokertel) {
		this.brokertel = brokertel;
	}
	public boolean getIsPayUser() {
		return isPayUser;
	}
	public void setIsPayUser(boolean isPayUser) {
		this.isPayUser = isPayUser;
	}
	public int getXingyuCount() {
		return xingyuCount;
	}
	public void setXingyuCount(int xingyuCount) {
		this.xingyuCount = xingyuCount;
	}
	public int getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}
	public int getZanCount() {
		return zanCount;
	}
	public void setZanCount(int zanCount) {
		this.zanCount = zanCount;
	}
	public int getRecommentToUserAndPostCount() {
		return recommentToUserAndPostCount;
	}
	public void setRecommentToUserAndPostCount(int recommentToUserAndPostCount) {
		this.recommentToUserAndPostCount = recommentToUserAndPostCount;
	}
	public String getWeixin() {
		return weixin;
	}
	public void setWeixin(String weixin) {
		this.weixin = weixin;
	}
	public List<String> getContactStatusList() {
		return contactStatusList;
	}
	public void setContactStatusList(List<String> contactStatusList) {
		this.contactStatusList = contactStatusList;
	}
	public int getCollectionPostCount() {
		return collectionPostCount;
	}
	public void setCollectionPostCount(int collectionPostCount) {
		this.collectionPostCount = collectionPostCount;
	}
	public String getJobStatus() {
		return jobStatus;
	}
	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}
	public boolean getIsRencai() {
		return isRencai;
	}
	public void setIsRencai(boolean isRencai) {
		this.isRencai = isRencai;
	}
	public int getFollowCount() {
		return followCount;
	}
	public void setFollowCount(int followCount) {
		this.followCount = followCount;
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
	public int getFollowType() {
		return followType;
	}
	public void setFollowType(int followType) {
		this.followType = followType;
	}
	public int getBiFollowCount() {
		return biFollowCount;
	}
	public void setBiFollowCount(int biFollowCount) {
		this.biFollowCount = biFollowCount;
	}
	public int getPicPostCount() {
		return picPostCount;
	}
	public void setPicPostCount(int picPostCount) {
		this.picPostCount = picPostCount;
	}
	public int getVideoPostCount() {
		return videoPostCount;
	}
	public void setVideoPostCount(int videoPostCount) {
		this.videoPostCount = videoPostCount;
	}
}
