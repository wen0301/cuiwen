package com.xingyun.bean;

import java.io.Serializable;

public class SwitchCityBean implements Serializable{

	private static final long serialVersionUID = -5007587055444632146L;
	private int provinceid;		//城市切换省ID
	private int cityid;			//城市切换市ID
	private String cityName;	//选择省/市名字
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public int getProvinceid() {
		return provinceid;
	}
	public void setProvinceid(int provinceid) {
		this.provinceid = provinceid;
	}
	public int getCityid() {
		return cityid;
	}
	public void setCityid(int cityid) {
		this.cityid = cityid;
	}
}
