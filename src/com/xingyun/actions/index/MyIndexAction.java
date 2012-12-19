package com.xingyun.actions.index;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.AuthUserAction;
import com.xingyun.bean.DynamicBean;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunAdConstant;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunMyIndexConstant;
import com.xingyun.services.index.IndexService;
import com.xingyun.services.index.MyIndexService;
import com.xingyun.services.recommend.RecommendService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.MessageUtil;

public class MyIndexAction extends AuthUserAction {

	private static final long serialVersionUID = 3695428098561638024L;
	private static final Logger log = Logger.getLogger(MyIndexAction.class);
	
	private UserHeaderBean userHeaderBean;					//头部数据bean
	private List<DynamicBean> dynamicList;					//用户动态数据
	private int totalRecord;								//总数
	private int dynameicCount;								//动态总数
	private List<Map<Object, Object>> sysMessageList;		//用户系统消息数据
	private List<Map<Object, Object>> recommendList;		//推荐我的数据集合
	private List<Map<Object,Object>> mayKnowUserList;  		//可能认识的人
	private List<Map<Object,Object>> newDaXiaList; 			//最新加入的大侠
	private List<Map<Object,Object>> newJingYingList; 		//最新加入的精英
	private List<Map<Object,Object>> recommendedWorkList; 	//星云推荐的作品
	private int showType;									//动态显示类型
	private List<Map<Object,Object>> adPicList;
	private int recommendType = XingyunCommonConstant.RECOMMEND_TYPE_USER_AND_POST;	//推荐类型 显示数据类型 默认显示全部
	private String toUserId;
	private boolean isFromIos;
	private int dynamicId;

	/**
	 * 我的星云首页
	 */
	public String execute(){
		try {
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			setDynamicData();		//整理动态数据
			getMainRightData();		// 右侧栏数据显示
			getMayKnowUserInfoList();
			newJingYingList = MyIndexService.getInstance().getNewJingYingList();
			isFromIos = CommonUtil.checkIsIOSDevice(servletRequest);
			return "myIndex";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 整理动态数据
	 */
	private void setDynamicData() throws Throwable{
		adPicList = IndexService.getInstance().getAdPicList(XingyunAdConstant.XY_AD_TYPE_HOME);
		
		//查询动态索引数据
		List<Map<Object, Object>> dynamicIndexList = MyIndexService.getInstance().findUserDynamicIndex(showType, user.getUserId());
		if(dynamicIndexList != null && dynamicIndexList.size() > 0){
			dynameicCount = dynamicIndexList.size();
			dynamicList = MyIndexService.getInstance().getUserDynamicData(user.getUserId(), dynamicIndexList, curPage);
		}
	}
	
	/**
	 * 整理动态数据
	 */
	public String getDynamicListAjax(){
		try {
			setDynamicData();
			return "dynamicListAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 查看系统消息
	 */
	public String getSysMessage(){
		try {
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			MessageUtil.clearNewMessage(userHeaderBean.getUserId(), "noticecount");
			getMainRightData();		// 右侧栏数据显示
			setSysMessge();
			return "sysMessageList";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 设置系统消息
	 */
	private void setSysMessge() throws Throwable {
		totalRecord = MyIndexService.getInstance().getSysMessageCount(user.getUserId());
		if(totalRecord > 0)
			sysMessageList = MyIndexService.getInstance().getSysMessageData(user.getUserId(), curPage);
	}

	/**
	 * 整理系统消息 ajax 翻页
	 */
	public String getSysMessageAjax(){
		try {
			setSysMessge();
			return "sysMessageListAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 查看推荐我的数据
	 */
	public String showRecommendToUser(){
		try {
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			
			MessageUtil.clearNewMessage(userHeaderBean.getUserId(), "recommendcount");
			getMainRightData();		// 右侧栏数据显示
			setRecommendToUserData();
			return "recommendToUserList";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 推荐我的数据
	 */
	private void setRecommendToUserData() throws Throwable{
		List<Map<Object, Object>> recommendIndexList = RecommendService.getInstance().findRecommendToUserIndexList(user.getUserId(), recommendType);
		totalRecord = recommendIndexList.size();
		if(totalRecord > 0)
			recommendList = RecommendService.getInstance().setRecommendToUserDataList(recommendIndexList, curPage, XingyunMyIndexConstant.MYINDEX_RECOMMDEND_PAGE_SIZE);
	}
	
	/**
	 * 整理推荐我的数据 ajax 翻页
	 */
	public String getRecommendToUserListAjax(){
		try {
			setRecommendToUserData();
			return "recommendToUserListAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 右侧栏部分数据显示
	 */
	private void getMainRightData() throws Throwable{
		newDaXiaList = MyIndexService.getInstance().getNewDaXiaList();
		recommendedWorkList = MyIndexService.getInstance().getRecommWorks();
	}
	
	/**
	 * 可能认识的人：换一换功能
	 */
	public String mayKnowUserForChange(){
		try{
			getMayKnowUserInfoList();
			return "mayknowItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 可能认识的人
	 */
	private void getMayKnowUserInfoList() throws Throwable{
		List<Map<Object,Object>> mayKnowUserIndexList = MyIndexService.getInstance().getMayKnowUserIndexList(user.getUserId());
		totalRecord = mayKnowUserIndexList == null ? 0 : mayKnowUserIndexList.size();
		mayKnowUserList = MyIndexService.getInstance().setMayKnowUserInfoList(mayKnowUserIndexList);
	}
	/**
	 * 关闭可能认识的人
	 */
	public void closeMayKnowUser(){
		try{
			MyIndexService.getInstance().closeMayKnowUser(user.getUserId(), toUserId);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 删除动态
	 */
	public void delDynamicById(){
		try{
			MyIndexService.getInstance().delDynamicById(user.getUserId(), dynamicId);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	public UserHeaderBean getUserHeaderBean() {
		return userHeaderBean;
	}

	public void setUserHeaderBean(UserHeaderBean userHeaderBean) {
		this.userHeaderBean = userHeaderBean;
	}

	public List<DynamicBean> getDynamicList() {
		return dynamicList;
	}

	public void setDynamicList(List<DynamicBean> dynamicList) {
		this.dynamicList = dynamicList;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public List<Map<Object, Object>> getSysMessageList() {
		return sysMessageList;
	}

	public void setSysMessageList(List<Map<Object, Object>> sysMessageList) {
		this.sysMessageList = sysMessageList;
	}

	public List<Map<Object, Object>> getMayKnowUserList() {
		return mayKnowUserList;
	}

	public void setMayKnowUserList(List<Map<Object, Object>> mayKnowUserList) {
		this.mayKnowUserList = mayKnowUserList;
	}

	public List<Map<Object, Object>> getRecommendList() {
		return recommendList;
	}

	public void setRecommendList(List<Map<Object, Object>> recommendList) {
		this.recommendList = recommendList;
	}

	public List<Map<Object, Object>> getRecommendedWorkList() {
		return recommendedWorkList;
	}

	public void setRecommendedWorkList(List<Map<Object, Object>> recommendedWorkList) {
		this.recommendedWorkList = recommendedWorkList;
	}

	public int getRecommendType() {
		return recommendType;
	}

	public void setRecommendType(int recommendType) {
		this.recommendType = recommendType;
	}

	public int getShowType() {
		return showType;
	}

	public void setShowType(int showType) {
		this.showType = showType;
	}

	public List<Map<Object, Object>> getAdPicList() {
		return adPicList;
	}

	public void setAdPicList(List<Map<Object, Object>> adPicList) {
		this.adPicList = adPicList;
	}

	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

	public int getDynameicCount() {
		return dynameicCount;
	}

	public void setDynameicCount(int dynameicCount) {
		this.dynameicCount = dynameicCount;
	}

	public List<Map<Object, Object>> getNewDaXiaList() {
		return newDaXiaList;
	}

	public void setNewDaXiaList(List<Map<Object, Object>> newDaXiaList) {
		this.newDaXiaList = newDaXiaList;
	}

	public List<Map<Object, Object>> getNewJingYingList() {
		return newJingYingList;
	}

	public void setNewJingYingList(List<Map<Object, Object>> newJingYingList) {
		this.newJingYingList = newJingYingList;
	}

	public boolean getIsFromIos() {
		return isFromIos;
	}

	public void setIsFromIos(boolean isFromIos) {
		this.isFromIos = isFromIos;
	}

	public int getDynamicId() {
		return dynamicId;
	}

	public void setDynamicId(int dynamicId) {
		this.dynamicId = dynamicId;
	}
}
