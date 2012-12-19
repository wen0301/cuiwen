package com.xingyun.actions.follow;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.XingyunBaseAction;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.services.follow.FollowService;
import com.xingyun.services.index.MyIndexService;
import com.xingyun.util.MessageUtil;
import com.xingyun.util.SpecialCharFilterUtil;

public class FollowShowAction extends XingyunBaseAction{
	private static final long serialVersionUID = -2317542681313939630L;
	private static final Logger log = Logger.getLogger(FollowShowAction.class);
	private List<Map<Object,Object>> followList;
	private List<Map<Object,Object>> newDaXiaList; 			// 最新加入的大侠
	private List<Map<Object,Object>> recommendedWorkList; 	//星云推荐的作品
	private UserHeaderBean userHeaderBean;
	private int sort;
	private int totalRecord;
	private String TA;
	private int gender;
	
	private String searchNickName;							//搜索用户昵称
	private int searchTotalRecord;							//搜索总数
	private String toUserId;								//搜索用户ID
	
	/**
	 * 关注我的人
	 */
	public String showFansList(){
		try{
			if(!FollowService.getInstance().checkIsShowFollow(user, userid))
				return "del";
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, userid);
			if(userHeaderBean == null)
				return "del";
			if(XingyunCommonConstant.USER_EDIT.equals(userHeaderBean.getLookState()))
				MessageUtil.clearNewMessage(userHeaderBean.getUserId(), "fanscount");
			getMainRightData(); 		// 右侧栏数据显示
			TA = FollowService.getInstance().getTA(user, userid, userHeaderBean.getGender());
			totalRecord = userHeaderBean.getFansCount();
			if(totalRecord == 0)
				return "fansList";
			followList = FollowService.getInstance().getFansList(user, userid, sort, curPage);
			return "fansList";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 关注我的人:Ajax分页
	 */
	public String showFansItem(){
		try{
			TA = FollowService.getInstance().getTA(user, userid, gender);
			followList = FollowService.getInstance().getFansList(user, userid, sort, curPage);
			return "fansListItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 我关注的人
	 */
	public String showFollowList(){
		try{
			if(!FollowService.getInstance().checkIsShowFollow(user, userid))
				return "del";
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, userid);
			if(userHeaderBean == null)
				return "del";
			getMainRightData();
			TA = FollowService.getInstance().getTA(user, userid, userHeaderBean.getGender());
			totalRecord = userHeaderBean.getFollowCount();
			if(totalRecord == 0)
				return "followList";
			followList = FollowService.getInstance().getFollowUserList(user, userid, sort, curPage);
			return "followList";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 我关注的人:Ajax分页
	 */
	public String showFollowItem(){
		try{
			TA = FollowService.getInstance().getTA(user, userid, gender);
			followList = FollowService.getInstance().getFollowUserList(user, userid, sort, curPage);
			return "followListItem";
		}catch(Throwable e){
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
	
	/**
	 * 按昵称搜索我关注的人数据
	 */
	public String searchFollowUserByNickname(){
		try{
			if(StringUtils.isBlank(searchNickName)){
				sendResponseMsg("nickName is null");
				return null;
			}
			if(!FollowService.getInstance().checkIsShowFollow(user, userid)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, userid);
			if(userHeaderBean == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			TA = FollowService.getInstance().getTA(user, userid, userHeaderBean.getGender());
			searchNickName = null == searchNickName ? "" : SpecialCharFilterUtil.encodeSpecialChar(searchNickName);
			totalRecord = userHeaderBean.getFollowCount();
			if(totalRecord > 0){
				List<Map<Object, Object>> followIndexList = FollowService.getInstance().findFollowUserByNickNameIndexList(userid, searchNickName, sort);
				if(followIndexList != null && followIndexList.size() > 0){
					searchTotalRecord = followIndexList.size();
					followList = FollowService.getInstance().getFollowListByNickname(user, followIndexList, curPage, 0);
				}
			}
			return "followUserSearchNickNameItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 按昵称搜索我关注的人提示数据
	 */
	public void searchFollowUserByNicknameTishi(){
		try{
			String followTishi = FollowService.getInstance().getFollowUserByNicknameTishi(userid, searchNickName);
			sendResponseMsg(followTishi);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg("");
		}
	}
	
	/**
	 * 根据用户ID 我关注的人信息
	 */
	public String showFollowUserByUserID(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			TA = FollowService.getInstance().getTA(user, userid, userHeaderBean.getGender());
			totalRecord = userHeaderBean.getFollowCount();
			followList = FollowService.getInstance().getFollowUserByUserID(user, userid, toUserId);
			if(followList != null && followList.size() > 0){
				searchTotalRecord = followList.size();
				searchNickName = followList.get(0).get("nickname").toString();
			}
			return "followUserSearchNickNameItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 按昵称搜索关注我的人数据
	 */
	public String searchFollowFansByNickname(){
		try{
			if(StringUtils.isBlank(searchNickName)){
				sendResponseMsg("nickName is null");
				return null;
			}
			if(!FollowService.getInstance().checkIsShowFollow(user, userid)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, userid);
			if(userHeaderBean == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			TA = FollowService.getInstance().getTA(user, userid, userHeaderBean.getGender());
			searchNickName = null == searchNickName ? "" : SpecialCharFilterUtil.encodeSpecialChar(searchNickName);
			totalRecord = userHeaderBean.getFansCount();
			if(totalRecord > 0){
				List<Map<Object, Object>> followIndexList = FollowService.getInstance().findFollowFansByNickNameIndexList(userid, searchNickName, sort);
				if(followIndexList != null && followIndexList.size() > 0){
					searchTotalRecord = followIndexList.size();
					followList = FollowService.getInstance().getFollowListByNickname(user, followIndexList, curPage, 1);
				}
			}
			return "followFansSearchNickNameItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 按昵称搜索关注我的人提示数据
	 */
	public void searchFollowFansByNicknameTishi(){
		try{
			String followTishi = FollowService.getInstance().getFollowFansByNicknameTishi(userid, searchNickName);
			sendResponseMsg(followTishi);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg("");
		}
	}
	
	/**
	 * 根据用户ID 关注我的人信息
	 */
	public String showFollowFansByUserID(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			TA = FollowService.getInstance().getTA(user, userid, userHeaderBean.getGender());
			totalRecord = userHeaderBean.getFansCount();
			followList = FollowService.getInstance().getFollowFansByUserID(user, toUserId, userid);
			if(followList != null && followList.size() > 0){
				searchTotalRecord = followList.size();
				searchNickName = followList.get(0).get("nickname").toString();
			}
			return "followFansSearchNickNameItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}

	public List<Map<Object, Object>> getRecommendedWorkList() {
		return recommendedWorkList;
	}

	public void setRecommendedWorkList(List<Map<Object, Object>> recommendedWorkList) {
		this.recommendedWorkList = recommendedWorkList;
	}

	public UserHeaderBean getUserHeaderBean() {
		return userHeaderBean;
	}

	public void setUserHeaderBean(UserHeaderBean userHeaderBean) {
		this.userHeaderBean = userHeaderBean;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public List<Map<Object, Object>> getNewDaXiaList() {
		return newDaXiaList;
	}

	public void setNewDaXiaList(List<Map<Object, Object>> newDaXiaList) {
		this.newDaXiaList = newDaXiaList;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public String getTA() {
		return TA;
	}

	public void setTA(String ta) {
		TA = ta;
	}
	public List<Map<Object, Object>> getFollowList() {
		return followList;
	}
	public void setFollowList(List<Map<Object, Object>> followList) {
		this.followList = followList;
	}
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public String getSearchNickName() {
		return searchNickName;
	}
	public void setSearchNickName(String searchNickName) {
		this.searchNickName = searchNickName;
	}
	public int getSearchTotalRecord() {
		return searchTotalRecord;
	}
	public void setSearchTotalRecord(int searchTotalRecord) {
		this.searchTotalRecord = searchTotalRecord;
	}
	public String getToUserId() {
		return toUserId;
	}
	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}
}
