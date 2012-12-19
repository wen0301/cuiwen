package com.xingyun.bean;

public class CooperationPriceBean {

	private int id;					//合作报价ID
	private String userid;			//合作报价用户ID
	private String name;			//合作报价内容
	private String price_min;		//合作报价最低价格
	private String price_max;		//合作报价最高价格
	private int price_type;			//合作报价价格类型
	private String content;			//合作报价联络方式
	private String fmt_price_min;	//合作报价最低价格 (格式化 1,000,000)
	private String fmt_price_max;	//合作报价最高价格 (格式化 1,000,000)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPrice_min() {
		return price_min;
	}
	public void setPrice_min(String price_min) {
		this.price_min = price_min;
	}
	public String getPrice_max() {
		return price_max;
	}
	public void setPrice_max(String price_max) {
		this.price_max = price_max;
	}
	public int getPrice_type() {
		return price_type;
	}
	public void setPrice_type(int price_type) {
		this.price_type = price_type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getFmt_price_min() {
		return fmt_price_min;
	}
	public void setFmt_price_min(String fmt_price_min) {
		this.fmt_price_min = fmt_price_min;
	}
	public String getFmt_price_max() {
		return fmt_price_max;
	}
	public void setFmt_price_max(String fmt_price_max) {
		this.fmt_price_max = fmt_price_max;
	}
}
