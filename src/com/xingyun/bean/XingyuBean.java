package com.xingyun.bean;

import java.util.List;
import java.util.Map;

public class XingyuBean {

	private int id;					//星语ID
	private String userid;			//星语用户ID
	private String content;			//星语文本内容
	private int showtype;			//星语查看类型
	private int commentcount;		//星语评论数量
	private int zancount;			//星语赞数量	
	private String fromtype;	 	//星语来源
	private String systime;			//星语发布时间
	private boolean isZan;			//星语是否赞过 true 赞过 false 未赞过
	private List<Map<Object, Object>> picList;	//星语图片
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getShowtype() {
		return showtype;
	}
	public void setShowtype(int showtype) {
		this.showtype = showtype;
	}
	public int getCommentcount() {
		return commentcount;
	}
	public void setCommentcount(int commentcount) {
		this.commentcount = commentcount;
	}
	public int getZancount() {
		return zancount;
	}
	public void setZancount(int zancount) {
		this.zancount = zancount;
	}
	public String getSystime() {
		return systime;
	}
	public void setSystime(String systime) {
		this.systime = systime;
	}
	public List<Map<Object, Object>> getPicList() {
		return picList;
	}
	public void setPicList(List<Map<Object, Object>> picList) {
		this.picList = picList;
	}
	public boolean getIsZan() {
		return isZan;
	}
	public void setIsZan(boolean isZan) {
		this.isZan = isZan;
	}
	public String getFromtype() {
		return fromtype;
	}
	public void setFromtype(String fromtype) {
		this.fromtype = fromtype;
	}
}
