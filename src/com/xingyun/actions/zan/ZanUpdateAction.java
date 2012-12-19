package com.xingyun.actions.zan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.AuthUserAction;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.services.index.MyIndexService;
import com.xingyun.services.zan.ZanService;
import com.xingyun.util.JsonObjectUtil;
import com.xingyun.util.MessageUtil;

public class ZanUpdateAction extends AuthUserAction {
	private static final long serialVersionUID = -6227250614899775800L;
	private static final Logger log = Logger.getLogger(ZanUpdateAction.class);
	private int topicId;
	private String topicUserId;                             //星语或作品作者用户ID
	private int source;
	private int type = XingyunCommonConstant.COMMENT_SOURCE_ALL;
	private int totalRecord;
	private int receiveTotalRecord;
	private int sendTotalRecord;
	private List<Map<Object,Object>> zanInfoList;
	private UserHeaderBean userHeaderBean;
	private List<Map<Object,Object>> newDaXiaList; 			// 最新加入的大侠
	private List<Map<Object,Object>> recommendedWorkList; 	//星云推荐的作品
	/**
	 * 添加星语赞
	 */
	public void addZanByType(){
		try {
			if(topicId == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			boolean isSuccess = ZanService.getInstance().addZanData(topicId, user.getUserId(), type, XingyunCommonConstant.FROM_TYPE_WEB);
			if(!isSuccess){
				sendResponseMsg("over");
				return;
			}
			MessageUtil.addNewMessage(topicUserId, "zancount");
			Map<String, Object> map = new HashMap<String, Object>();				
			map.put("userId", user.getUserId());
			map.put("userHref", user.getUserHref());
			map.put("logoUrl", user.getLogoUrl());
			map.put("nickName", user.getNickName());
			sendResponseMsg(JsonObjectUtil.getJsonStr(map));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 获取赞列表
	 */
	public String getZanList(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			MessageUtil.clearNewMessage(user.getUserId(), "zancount");
			receiveTotalRecord = ZanService.getInstance().getZanCount(user.getUserId(), XingyunCommonConstant.COMMENT_TYPE_RECEIVE, type);
			sendTotalRecord = ZanService.getInstance().getZanCount(user.getUserId(), XingyunCommonConstant.COMMENT_TYPE_SENT, type);
			totalRecord = ZanService.getInstance().getZanCount(user.getUserId(), source, type);
			zanInfoList = ZanService.getInstance().getZanList(user.getUserId(), source, type, curPage);
			getMainRightData();
			return "showZan";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 获取赞列表:Ajax分页
	 */
	public String getZanItemAjax(){
		try{
			if(totalRecord == 0)
				totalRecord = ZanService.getInstance().getZanCount(user.getUserId(), source, type);
			zanInfoList = ZanService.getInstance().getZanList(user.getUserId(), source, type, curPage);
			return "showZanItem";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 右侧栏数据显示
	 */
	private void getMainRightData() throws Throwable{
		newDaXiaList = MyIndexService.getInstance().getNewDaXiaList();
		recommendedWorkList = MyIndexService.getInstance().getRecommWorks();
	}

	public int getTopicId() {
		return topicId;
	}

	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public List<Map<Object, Object>> getZanInfoList() {
		return zanInfoList;
	}

	public void setZanInfoList(List<Map<Object, Object>> zanInfoList) {
		this.zanInfoList = zanInfoList;
	}
	public int getReceiveTotalRecord() {
		return receiveTotalRecord;
	}
	public void setReceiveTotalRecord(int receiveTotalRecord) {
		this.receiveTotalRecord = receiveTotalRecord;
	}
	public int getSendTotalRecord() {
		return sendTotalRecord;
	}
	public void setSendTotalRecord(int sendTotalRecord) {
		this.sendTotalRecord = sendTotalRecord;
	}
	public UserHeaderBean getUserHeaderBean() {
		return userHeaderBean;
	}
	public void setUserHeaderBean(UserHeaderBean userHeaderBean) {
		this.userHeaderBean = userHeaderBean;
	}
	public List<Map<Object, Object>> getNewDaXiaList() {
		return newDaXiaList;
	}
	public void setNewDaXiaList(List<Map<Object, Object>> newDaXiaList) {
		this.newDaXiaList = newDaXiaList;
	}
	public List<Map<Object, Object>> getRecommendedWorkList() {
		return recommendedWorkList;
	}
	public void setRecommendedWorkList(List<Map<Object, Object>> recommendedWorkList) {
		this.recommendedWorkList = recommendedWorkList;
	}
	public String getTopicUserId() {
		return topicUserId;
	}
	public void setTopicUserId(String topicUserId) {
		this.topicUserId = topicUserId;
	}
}
