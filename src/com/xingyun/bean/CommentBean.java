package com.xingyun.bean;

public class CommentBean {
	private int type;           //评论来源类型：0 星语 1 作品
	private int commentID;		//评论表ID
	private int commentType;    //评论类型 0:评论 1:评论回复
	private String fromUserID;	//评论人ID
	private String userHref;	//评论人链接
	private String nickName;	//评论人nickName
	private String logoUrl;		//评论人头像
	private int lid;			//评论人级别
	private int verified;       //评论人认证
	private String content;		//评论内容
	private String systime;		//评论时间
	private int topicID;        //主题ID
	private String topicUserID; //主题作者ID
	private String topicUserHref; //主题作者链接
	private String upUserID;    //上文用户ID
	private String upNickName;  //上文用户昵称
	private int upLid;          //上文用户级别
	private String upUserHref;  //上文用户链接
	private int upVerified;     //上文用户认证
	private String upContent;   //上文内容
	private String fromType;    //评论来源 星云网站 or 星云IPhone客户端
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getCommentID() {
		return commentID;
	}
	public void setCommentID(int commentID) {
		this.commentID = commentID;
	}
	public int getCommentType() {
		return commentType;
	}
	public void setCommentType(int commentType) {
		this.commentType = commentType;
	}
	public String getUserHref() {
		return userHref;
	}
	public void setUserHref(String userHref) {
		this.userHref = userHref;
	}
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
	public int getLid() {
		return lid;
	}
	public void setLid(int lid) {
		this.lid = lid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSystime() {
		return systime;
	}
	public void setSystime(String systime) {
		this.systime = systime;
	}
	public int getTopicID() {
		return topicID;
	}
	public void setTopicID(int topicID) {
		this.topicID = topicID;
	}
	public String getUpNickName() {
		return upNickName;
	}
	public void setUpNickName(String upNickName) {
		this.upNickName = upNickName;
	}
	public int getUpLid() {
		return upLid;
	}
	public void setUpLid(int upLid) {
		this.upLid = upLid;
	}
	public String getUpUserHref() {
		return upUserHref;
	}
	public void setUpUserHref(String upUserHref) {
		this.upUserHref = upUserHref;
	}
	public String getUpContent() {
		return upContent;
	}
	public void setUpContent(String upContent) {
		this.upContent = upContent;
	}
	public String getFromUserID() {
		return fromUserID;
	}
	public void setFromUserID(String fromUserID) {
		this.fromUserID = fromUserID;
	}
	public String getUpUserID() {
		return upUserID;
	}
	public void setUpUserID(String upUserID) {
		this.upUserID = upUserID;
	}
	public String getTopicUserID() {
		return topicUserID;
	}
	public void setTopicUserID(String topicUserID) {
		this.topicUserID = topicUserID;
	}
	public int getVerified() {
		return verified;
	}
	public void setVerified(int verified) {
		this.verified = verified;
	}
	public int getUpVerified() {
		return upVerified;
	}
	public void setUpVerified(int upVerified) {
		this.upVerified = upVerified;
	}
	public String getFromType() {
		return fromType;
	}
	public void setFromType(String fromType) {
		this.fromType = fromType;
	}
	public String getTopicUserHref() {
		return topicUserHref;
	}
	public void setTopicUserHref(String topicUserHref) {
		this.topicUserHref = topicUserHref;
	}
}
