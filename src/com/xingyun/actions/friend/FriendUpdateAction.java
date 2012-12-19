package com.xingyun.actions.friend;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.AuthUserAction;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.services.friend.FriendService;
import com.xingyun.services.index.MyIndexService;
import com.xingyun.util.MessageUtil;
import com.xingyun.util.SpecialCharFilterUtil;

public class FriendUpdateAction extends AuthUserAction {
	private static final long serialVersionUID = 5973883894478578778L;
	private static final Logger log = Logger.getLogger(FriendUpdateAction.class);
	private String toUserId;
	private String content;
	private UserHeaderBean userHeaderBean;
	private List<Map<Object,Object>> newDaXiaList; 			//最新加入的大侠
	private List<Map<Object,Object>> recommendedWorkList; 	//星云推荐的作品
	private List<Map<Object,Object>> recentContactList;     //最近联系的人
	private String xyNumber;
	private String nickName;
	private Map<Object,Object> searchUserMap;
	private int totalRecord;
	private List<Map<Object,Object>> friendList;
	private int sort;
	
	/**
	 * 检测是否已经发送过加好友请求
	 */
	public void checkFriendRequestOver(){
		try{
			if(StringUtils.EMPTY.equals(toUserId)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(FriendService.getInstance().checkIsExistFriendRequest(user.getUserId(), toUserId)){
				sendResponseMsg("request over");
				return;
			}
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 添加好友请求
	 */
	public void addFriendRequest(){
		try{
			if(StringUtils.EMPTY.equals(toUserId) || StringUtils.EMPTY.equals(content)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(user.getUserId().equals(toUserId)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(FriendService.getInstance().checkIsExistFriendRequest(user.getUserId(), toUserId)){
				sendResponseMsg("request over");
				return;
			}
			sendResponseMsg(FriendService.getInstance().addFriendRequestInfo(user.getUserId(), toUserId, content) == 0 ? XingyunCommonConstant.RESPONSE_ERR_STRING : XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 通过星云号搜索用户信息
	 */
	public String searchUserByXynumber(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(xyNumber.equals(user.getXynumber()))
				return "searchUser";
			searchUserMap = FriendService.getInstance().getUserInfoMapByXynumber(xyNumber);
			return "searchUser";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 添加好友请求
	 */
	public void addFriend(){
		try{
			if(StringUtils.EMPTY.equals(toUserId)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(user.getUserId().equals(toUserId)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			int relationType = FriendService.getInstance().checkFriendRelationType(toUserId, user.getUserId());
			if(relationType == XingyunCommonConstant.FRIEND_RELATION_DOUBLE){
				FriendService.getInstance().ignoreFriendRequest(user.getUserId(), toUserId);
				sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
				return;
			}
			FriendService.getInstance().addFriendInfo(user.getUserId(), toUserId);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 忽略好友
	 */
	public void ignoreFriendRequest(){
		try{
			if(StringUtils.EMPTY.equals(toUserId)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(user.getUserId().equals(toUserId)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			FriendService.getInstance().ignoreFriendRequest(user.getUserId(), toUserId);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 显示好友列表
	 */
	public String showFriendList(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			getMainRightData();			//设置右边数据
			setShowFriendItemData();	//设置好友列表数据
			return "showFriend";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 好友列表：ajax分页
	 */
	public String showFriendItem(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			setShowFriendItemData();	//设置好友列表数据
			return "showFriendItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 设置好友列表数据
	 */
	private void setShowFriendItemData() throws Throwable{
		if(totalRecord == 0)
			totalRecord = FriendService.getInstance().getFriendCount(user.getUserId());
		friendList = FriendService.getInstance().getFriendList(user.getUserId(), sort, curPage);
	}
	
	/**
	 * 显示好友请求列表
	 */
	public String showFriendRequestList(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			MessageUtil.clearNewMessage(user.getUserId(), "requestcount");
			getMainRightData();
			totalRecord = FriendService.getInstance().getFriendRequestCount(user.getUserId());
			if(totalRecord > 0)
				friendList = FriendService.getInstance().getFriendRequestList(user.getUserId(), curPage);
			return "showFriendRequest";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 好友请求：ajax分页
	 */
	public String showFriendRequestItem(){
		try{
			if(totalRecord == 0)
				totalRecord = FriendService.getInstance().getFriendRequestCount(user.getUserId());
			friendList = FriendService.getInstance().getFriendRequestList(user.getUserId(), curPage);
			return "showFriendRequestItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 删除好友
	 */
	public void delFriend(){
		try{
			if(StringUtils.EMPTY.equals(toUserId)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(user.getUserId().equals(toUserId)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			FriendService.getInstance().delFriend(user.getUserId(), toUserId);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 右侧栏数据显示
	 */
	private void getMainRightData() throws Throwable{
		newDaXiaList = MyIndexService.getInstance().getNewDaXiaList();			//最新加入大侠	
		recommendedWorkList = MyIndexService.getInstance().getRecommWorks();	//推荐作品			
		recentContactList = FriendService.getInstance().getRecentContactUserList(user.getUserId());		//最近联系人
	}
	
	/**
	 * 按昵称搜索好友
	 */
	public String searchFriendByNickname(){
		try{
			if(StringUtils.isBlank(nickName)){
				sendResponseMsg("nickName is null");
				return null;
			}
			nickName = null == nickName ? "" : SpecialCharFilterUtil.encodeSpecialChar(nickName);
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			List<Map<Object, Object>> friendIndexList = FriendService.getInstance().findFriendByNickNameIndexList(user.getUserId(), nickName, sort);
			if(friendIndexList != null && friendIndexList.size() > 0){
				totalRecord = friendIndexList.size();
				friendList = FriendService.getInstance().getFriendListByNickname(friendIndexList, curPage);
			}
			return "showFriendSearchNickNameItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 按昵称搜索好友提示
	 */
	public void searchFriendByNicknameTishi(){
		try{
			nickName = null == nickName ? "" : SpecialCharFilterUtil.encodeSpecialChar(nickName);
			String friendTishi = FriendService.getInstance().getFriendListByNicknameTishi(user.getUserId(), nickName);
			sendResponseMsg(friendTishi);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg("");
		}
	}
	
	/**
	 * 根据用户ID 显示好友信息
	 */
	public String showFriendByUserID(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			friendList = FriendService.getInstance().getFriendByUserID(user.getUserId(), toUserId);
			if(friendList != null && friendList.size() > 0){
				totalRecord = friendList.size();
				nickName = friendList.get(0).get("nickname").toString();
			}
			return "showFriendSearchNickNameItem";
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public UserHeaderBean getUserHeaderBean() {
		return userHeaderBean;
	}

	public void setUserHeaderBean(UserHeaderBean userHeaderBean) {
		this.userHeaderBean = userHeaderBean;
	}

	public List<Map<Object, Object>> getRecommendedWorkList() {
		return recommendedWorkList;
	}

	public void setRecommendedWorkList(List<Map<Object, Object>> recommendedWorkList) {
		this.recommendedWorkList = recommendedWorkList;
	}

	public Map<Object, Object> getSearchUserMap() {
		return searchUserMap;
	}

	public void setSearchUserMap(Map<Object, Object> searchUserMap) {
		this.searchUserMap = searchUserMap;
	}

	public String getXyNumber() {
		return xyNumber;
	}

	public void setXyNumber(String xyNumber) {
		this.xyNumber = xyNumber;
	}

	public List<Map<Object, Object>> getFriendList() {
		return friendList;
	}

	public void setFriendList(List<Map<Object, Object>> friendList) {
		this.friendList = friendList;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public List<Map<Object, Object>> getNewDaXiaList() {
		return newDaXiaList;
	}

	public void setNewDaXiaList(List<Map<Object, Object>> newDaXiaList) {
		this.newDaXiaList = newDaXiaList;
	}

	public List<Map<Object, Object>> getRecentContactList() {
		return recentContactList;
	}

	public void setRecentContactList(List<Map<Object, Object>> recentContactList) {
		this.recentContactList = recentContactList;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}
}
