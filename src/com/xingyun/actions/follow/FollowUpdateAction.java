package com.xingyun.actions.follow;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.AuthUserAction;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.services.follow.FollowService;
import com.xingyun.services.index.MyIndexService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.SpecialCharFilterUtil;

public class FollowUpdateAction extends AuthUserAction {
	private static final long serialVersionUID = -9214136890789999759L;
	private static final Logger log = Logger.getLogger(FollowUpdateAction.class);
	private String toUserId;
	private UserHeaderBean userHeaderBean;
	private int sort;
	private int totalRecord;
	private List<Map<Object,Object>> followList;
	private List<Map<Object,Object>> newDaXiaList; 			// 最新加入的大侠
	private List<Map<Object,Object>> recommendedWorkList; 	//星云推荐的作品
	
	private String searchNickName;							//搜索用户昵称
	private int searchTotalRecord;							//搜索总数
	
	/**
	 * 添加关注
	 */
	public void addFollow() {
		try {
			sendResponseMsg(FollowService.getInstance().addFollowInfo(user.getUserId(), toUserId));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 取消关注
	 */
	public void cancleFollow() {
		try {
			if(CommonUtil.checkIsXingyunUID(toUserId)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			sendResponseMsg(FollowService.getInstance().cancleFollowInfo(user.getUserId(), toUserId));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 相互关注的人
	 */
	public String showFollowDoubleList(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, userid);
			if(userHeaderBean == null)
				return "del";
			getMainRightData();
			totalRecord = userHeaderBean.getBiFollowCount();
			if(totalRecord == 0)
				return "followDoubleList";
			followList = FollowService.getInstance().getFollowDoubleList(user, userid, sort, curPage);
			return "followDoubleList";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 相互关注的人:Ajax分页
	 */
	public String showFollowDoubleItem(){
		try{
			followList = FollowService.getInstance().getFollowDoubleList(user, userid, sort, curPage);
			return "followDoubleItem";
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
	 * 按昵称搜索相互关注的人数据
	 */
	public String searchFollowDoubleByNickname(){
		try{
			if(StringUtils.isBlank(searchNickName)){
				sendResponseMsg("nickName is null");
				return null;
			}
			searchNickName = null == searchNickName ? "" : SpecialCharFilterUtil.encodeSpecialChar(searchNickName);
			totalRecord = FollowService.getInstance().getBiFollowCount(user.getUserId());
			if(totalRecord > 0){
				List<Map<Object, Object>> followIndexList = FollowService.getInstance().findFollowDoubleByNickNameIndexList(user.getUserId(), searchNickName, sort);
				if(followIndexList != null && followIndexList.size() > 0){
					searchTotalRecord = followIndexList.size();
					followList = FollowService.getInstance().getFollowListByNickname(user, followIndexList, curPage, 2);
				}
			}
			return "followDoubleSearchNickNameItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 按昵称搜索相互关注的人提示数据
	 */
	public void searchFollowDoubleByNicknameTishi(){
		try{
			String followTishi = FollowService.getInstance().getFollowDoubleByNicknameTishi(user.getUserId(), searchNickName);
			sendResponseMsg(followTishi);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg("");
		}
	}
	
	/**
	 * 根据用户ID 相互关注我的人信息
	 */
	public String showFollowDoubleByUserID(){
		try{
			totalRecord = FollowService.getInstance().getBiFollowCount(user.getUserId());
			if(totalRecord > 0){
				followList = FollowService.getInstance().getFollowDoubleByUserID(user, user.getUserId(), toUserId);
				if(followList != null && followList.size() > 0){
					searchTotalRecord = followList.size();
					searchNickName = followList.get(0).get("nickname").toString();
				}
			}
			return "followDoubleSearchNickNameItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	public String getToUserId() {
		return toUserId;
	}
	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
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
	public int getTotalRecord() {
		return totalRecord;
	}
	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}
	public List<Map<Object, Object>> getFollowList() {
		return followList;
	}
	public void setFollowList(List<Map<Object, Object>> followList) {
		this.followList = followList;
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
}
