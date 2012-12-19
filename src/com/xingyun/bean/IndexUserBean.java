package com.xingyun.bean;

import java.util.List;
import java.util.Map;

public class IndexUserBean {
	private int tradeId;                           //行业ID
	private String tradeName;                      //行业名称
	private String tradeEnglishName;               //行业英文名称
	private String tradeIconName;				   //行业标签名
	private List<Map<Object,Object>> vocationList; //职业列表集合
	private List<Map<Object,Object>> userList;     //用户列表集合
	private int totalRecord;                       //用户总数
	public int getTradeId() {
		return tradeId;
	}
	public void setTradeId(int tradeId) {
		this.tradeId = tradeId;
	}
	public String getTradeName() {
		return tradeName;
	}
	public void setTradeName(String tradeName) {
		this.tradeName = tradeName;
	}
	public List<Map<Object, Object>> getVocationList() {
		return vocationList;
	}
	public void setVocationList(List<Map<Object, Object>> vocationList) {
		this.vocationList = vocationList;
	}
	public int getTotalRecord() {
		return totalRecord;
	}
	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}
	public List<Map<Object, Object>> getUserList() {
		return userList;
	}
	public void setUserList(List<Map<Object, Object>> userList) {
		this.userList = userList;
	}
	public String getTradeIconName() {
		return tradeIconName;
	}
	public void setTradeIconName(String tradeIconName) {
		this.tradeIconName = tradeIconName;
	}
	public String getTradeEnglishName() {
		return tradeEnglishName;
	}
	public void setTradeEnglishName(String tradeEnglishName) {
		this.tradeEnglishName = tradeEnglishName;
	}
}
