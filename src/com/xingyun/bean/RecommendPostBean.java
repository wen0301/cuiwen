package com.xingyun.bean;

import java.util.List;
import java.util.Map;

public class RecommendPostBean {
	private int tradeId;                           //行业ID
	private String tradeName;                      //行业名称
	private String tradeEnglishName;               //行业英文名称
	private List<Map<Object,Object>> postList;     //作品列表集合
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
	public int getTotalRecord() {
		return totalRecord;
	}
	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}
	public String getTradeEnglishName() {
		return tradeEnglishName;
	}
	public void setTradeEnglishName(String tradeEnglishName) {
		this.tradeEnglishName = tradeEnglishName;
	}
	public List<Map<Object, Object>> getPostList() {
		return postList;
	}
	public void setPostList(List<Map<Object, Object>> postList) {
		this.postList = postList;
	}
}
