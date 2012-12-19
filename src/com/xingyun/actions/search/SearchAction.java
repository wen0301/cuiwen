package com.xingyun.actions.search;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.AuthUserAction;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunSearchConstant;
import com.xingyun.services.friend.FriendService;
import com.xingyun.services.index.MyIndexService;
import com.xingyun.services.search.SearchService;
import com.xingyun.util.SpecialCharFilterUtil;

public class SearchAction extends AuthUserAction {

	private static final long serialVersionUID = 819058497964771095L;
	private static final Logger log = Logger.getLogger(SearchAction.class);

	private String searchContent;							//搜索内容
	private int totalRecord;								//总数
	private List<Map<Object, Object>> searchUserList;		//搜索用户数据
	private int searchFilterType;							//搜索用户过滤类型 0：全部 1： 精英 1：明星 
	
	private UserHeaderBean userHeaderBean;					//头部数据
	private List<Map<Object,Object>> newDaXiaList; 			//最新加入的大侠
	private List<Map<Object,Object>> recommendedWorkList; 	//星云推荐的作品
	private List<Map<Object,Object>> recentContactList;     //最近联系的人
	
	/**
	 * 导航搜索用户 
	 */
	public String searchUser(){
		try {
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			
			getMainRightData();		//整理页面右边数据
			searchUserData();		//整理搜索数据
			return "showSearchUserPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 导航搜索用户 ajax翻页
	 */
	public String searchUserAjax(){
		try {
			searchUserData();
			return "showSearchUserPageAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 整理搜索显示数据
	 */
	private void searchUserData() throws Throwable{
		searchContent = null == searchContent ? "" : SpecialCharFilterUtil.encodeSpecialChar(searchContent);
		List<Map<Object, Object>> indexList = SearchService.getInstance().getSearchUserNavIndex(searchContent, searchFilterType);
		if(indexList != null && indexList.size() > 0){
			totalRecord = indexList.size();
			curPage = curPage > XingyunSearchConstant.SEARCH_LIST_MAXPAGE ? XingyunSearchConstant.SEARCH_LIST_MAXPAGE : curPage;
			searchUserList = SearchService.getInstance().getSearchUserData(indexList, curPage, XingyunSearchConstant.SEARCH_LIST_PAGESIZE);
		}
	}

	/**
	 * 右侧栏数据显示
	 */
	private void getMainRightData() throws Throwable{
		newDaXiaList = MyIndexService.getInstance().getNewDaXiaList();								//最新加入大侠	
		recommendedWorkList = MyIndexService.getInstance().getRecommWorks();						//推荐作品			
		recentContactList = FriendService.getInstance().getRecentContactUserList(user.getUserId());	//最近联系人
	}
	
	public String getSearchContent() {
		return searchContent;
	}

	public void setSearchContent(String searchContent) {
		this.searchContent = searchContent;
	}

	public List<Map<Object, Object>> getSearchUserList() {
		return searchUserList;
	}

	public void setSearchUserList(List<Map<Object, Object>> searchUserList) {
		this.searchUserList = searchUserList;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
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

	public List<Map<Object, Object>> getRecentContactList() {
		return recentContactList;
	}

	public void setRecentContactList(List<Map<Object, Object>> recentContactList) {
		this.recentContactList = recentContactList;
	}

	public int getSearchFilterType() {
		return searchFilterType;
	}

	public void setSearchFilterType(int searchFilterType) {
		this.searchFilterType = searchFilterType;
	}
}
