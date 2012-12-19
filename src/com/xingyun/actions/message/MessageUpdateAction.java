package com.xingyun.actions.message;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.AuthUserAction;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.services.friend.FriendService;
import com.xingyun.services.header.UserHeaderService;
import com.xingyun.services.index.MyIndexService;
import com.xingyun.services.message.MessageService;
import com.xingyun.services.search.SearchService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.MessageUtil;
import com.xingyun.util.PinyinUtil;
import com.xingyun.util.SpecialCharFilterUtil;

public class MessageUpdateAction extends AuthUserAction {
	private static final long serialVersionUID = 9151028108710539859L;
	private static final Logger log = Logger.getLogger(MessageUpdateAction.class);
	private String content;
	private String toUserId;
	private List<Map<Object,Object>> messageList;
	private List<Map<Object,Object>> newDaXiaList; 			// 最新加入的大侠
	private List<Map<Object,Object>> recommendedWorkList; 	//星云推荐的作品
	private Map<Object,Object> messageMap;
	private int totalRecord;
	private List<String> toUserIds;
	private int messageId;
	private String nickName;
	private UserHeaderBean userHeaderBean;
	private int source;
	private int messageType;
	private boolean showBizMessageTip;
	private boolean showBizMessageTipWait;
	private int showBizMessageCount;
	private int isBizMessageOpen;

	/**
	 * 头部发送云信
	 */
	public void sendMessagePop(){
		try{
			if(StringUtils.isBlank(content)){
				sendResponseMsg("content null");
				return;
			}
			String msg = MessageService.getInstance().addMessageInfo(user.getUserId(), toUserId, content, messageType);
			MessageService.getInstance().updateMessageBizRelation(user.getUserId(), toUserId, messageType);
			sendResponseMsg(msg);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 对话页：发送云信
	 */
	public String sendMessage(){
		try{
			if(StringUtils.isBlank(content)){
				sendResponseMsg("content null");
				return null;
			}
			MessageService.getInstance().addMessageInfo(user.getUserId(), toUserId, content, messageType);
			MessageService.getInstance().updateMessageBizRelation(user.getUserId(), toUserId, messageType);
			messageMap = new HashMap<Object, Object>();
			messageMap.put("fromUserId", user.getUserId());
			messageMap.put("toUserId", toUserId);
			messageMap.put("nickName", user.getNickName());
			messageMap.put("content", content);
			messageMap.put("logoUrl", user.getLogoUrl());
			messageMap.put("lid", user.getLid());
			messageMap.put("verified", user.getVerified() + "");
			messageMap.put("systime", DateUtil.getBlogSystime(new Date()));
			messageMap.put("messageType", messageType);
			return "showMessageItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 对话列表页：快速回复
	 */
	public String sendMessageInFast(){
		try{
			if(StringUtils.isBlank(content)){
				sendResponseMsg("content null");
				return null;
			}
			MessageService.getInstance().addMessageInfo(user.getUserId(), toUserId, content, messageType);
			MessageService.getInstance().updateMessageBizRelation(user.getUserId(), toUserId, messageType);
			Map<Object,Object> summaryMap = MessageService.getInstance().getMessageIdInSummary(user.getUserId(), toUserId, messageType);
			messageMap = new HashMap<Object, Object>();
			messageMap.put("id", summaryMap.get("id"));
			messageMap.put("newcount", summaryMap.get("newcount"));
			messageMap.put("totalcount", summaryMap.get("totalcount"));
			messageMap.put("source", summaryMap.get("source"));
			messageMap.put("fromUserId", user.getUserId());
			messageMap.put("toUserId", toUserId);
			messageMap.put("nickName", summaryMap.get("nickname"));
			messageMap.put("verified", summaryMap.get("verified"));
			messageMap.put("content", content);
			messageMap.put("logoUrl", summaryMap.get("logourl"));
			messageMap.put("lid", summaryMap.get("lid"));
			messageMap.put("systime", DateUtil.getBlogSystime(new Date()));
			messageMap.put("isXingyunUID", CommonUtil.checkIsXingyunUID(toUserId));
			messageMap.put("messageType", messageType);
			return "showMessageFastItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 显示云信列表页
	 */
	public String showMessageSummaryList(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			MessageUtil.clearNewMessage(user.getUserId(), "yunxincount");
			messageType = XingyunCommonConstant.MESSAGE_TYPE_DEFAULT;
			totalRecord = MessageService.getInstance().getMessageCount(user.getUserId(), messageType);
			if(totalRecord > 0)
				messageList = MessageService.getInstance().getMessageSummaryList(user.getUserId(), curPage, messageType);
			getMainRightData(); 	// 右侧栏数据显示
			return "showSummary";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 显示商业云信列表页
	 */
	public String showbizMessageSummaryList(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			MessageUtil.clearNewMessage(user.getUserId(), "bizyunxincount");
			messageType = XingyunCommonConstant.MESSAGE_TYPE_BIZ;
			totalRecord = MessageService.getInstance().getMessageCount(user.getUserId(), messageType);
			if(totalRecord > 0)
				messageList = MessageService.getInstance().getMessageSummaryList(user.getUserId(), curPage, messageType);
			getMainRightData(); 	// 右侧栏数据显示
			return "showbizSummary";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	public String showMessageSummaryItem(){
		try{
			if(totalRecord == 0)
				totalRecord = MessageService.getInstance().getMessageCount(user.getUserId(), messageType);
			messageList = MessageService.getInstance().getMessageSummaryList(user.getUserId(), curPage, messageType);
			return "showSummaryItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 显示云信对话列表页
	 */
	public String showMessageDialogueList(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			messageType = XingyunCommonConstant.MESSAGE_TYPE_DEFAULT;
			MessageService.getInstance().clearMessageDialogueNum(user.getUserId(), toUserId, messageType);
			getMainRightData(); 	// 右侧栏数据显示
			totalRecord = MessageService.getInstance().getMessageSendCount(user.getUserId(), toUserId, messageType) + MessageService.getInstance().getMessageReceiveCount(user.getUserId(), toUserId, messageType);
			if(totalRecord == 0)
				return "showDialogue";
			messageList = MessageService.getInstance().getMessageDialogueList(user.getUserId(), toUserId, curPage, messageType);
			nickName = MessageService.getInstance().getUserNickName(toUserId);
			return "showDialogue";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 显示云信对话列表页
	 */
	public String showbizMessageDialogueList(){
		try{
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			messageType = XingyunCommonConstant.MESSAGE_TYPE_BIZ;
			
			MessageService.getInstance().clearMessageDialogueNum(user.getUserId(), toUserId, messageType);
			getMainRightData(); 	// 右侧栏数据显示
			setIsShowBizMessageTip();
			totalRecord = MessageService.getInstance().getMessageSendCount(user.getUserId(), toUserId, messageType) + MessageService.getInstance().getMessageReceiveCount(user.getUserId(), toUserId, messageType);
			if(totalRecord == 0)
				return "showbizDialogue";
			messageList = MessageService.getInstance().getMessageDialogueList(user.getUserId(), toUserId, curPage, messageType);
			nickName = MessageService.getInstance().getUserNickName(toUserId);
			return "showbizDialogue";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 设置前台方案的处理
	 */
	private void setIsShowBizMessageTip() throws Throwable{
		Map<Object,Object> fromBizRelationMap = MessageService.getInstance().getMessageBizMessageMap(user.getUserId(), toUserId);
		if(fromBizRelationMap != null){
			if(Integer.parseInt(fromBizRelationMap.get("isopen").toString()) == XingyunCommonConstant.MESSAGE_BIZRELATION_OPEN)
				showBizMessageTipWait = true;
			else
				showBizMessageCount = CommonUtil.getIntValue(fromBizRelationMap.get("bizyunxincount"));
			isBizMessageOpen = CommonUtil.getIntValue(fromBizRelationMap.get("isopen"));
			showBizMessageTip = false;
			source = 0;
			return;
		}
		Map<Object,Object> toBizRelationMap = MessageService.getInstance().getMessageBizMessageMap(toUserId, user.getUserId());
		if(toBizRelationMap != null){
			if(Integer.parseInt(toBizRelationMap.get("isopen").toString()) != XingyunCommonConstant.MESSAGE_BIZRELATION_OPEN)
				showBizMessageTip = true;
			showBizMessageTipWait = false;
			isBizMessageOpen = CommonUtil.getIntValue(toBizRelationMap.get("isopen"));
			source = 1;
		}
	}
	
	/**
	 * 显示云信对话列表页
	 */
	public String showMessageDialogueItem(){
		try{
			if(totalRecord == 0)
				totalRecord = MessageService.getInstance().getMessageSendCount(user.getUserId(), toUserId, messageType) + MessageService.getInstance().getMessageReceiveCount(user.getUserId(), toUserId, messageType);
			messageList = MessageService.getInstance().getMessageDialogueList(user.getUserId(), toUserId, curPage, messageType);
			return "showDialogueItem";
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
	 * 标记已读操作
	 */
	public void readMessageOver(){
		try{
			MessageService.getInstance().clearMessageDialogueNum(user.getUserId(), toUserId, messageType);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 标记本页为已读
	 */
	public void readMessageOverPage(){
		try{
			MessageService.getInstance().clearMessageDialogueNum(user.getUserId(), toUserIds, messageType);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 删除私信组
	 */
	public void delMessageGroup(){
		try{
			MessageService.getInstance().delMessageGroupInfo(user.getUserId(), toUserId, messageType);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 删除单条云信
	 */
	public void delMessageSingle(){
		try{
			sendResponseMsg(MessageService.getInstance().delMessageSingle(messageId, source, user.getUserId(), toUserId, messageType));
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 *  忽略全部新消息提示
	 */
	public void clearMessageAll() throws Throwable{
		try{
			MessageUtil.clearNewMessage(user.getUserId(), "requestcount");    //好友请求
			MessageUtil.clearNewMessage(user.getUserId(), "yunxincount");     //私信
			MessageUtil.clearNewMessage(user.getUserId(), "bizyunxincount");     //商业云信
			MessageUtil.clearNewMessage(user.getUserId(), "fanscount");       //粉丝数
			MessageUtil.clearNewMessage(user.getUserId(), "recommendcount");  //推荐
			MessageUtil.clearNewMessage(user.getUserId(), "noticecount");     //通知
			MessageUtil.clearNewMessage(user.getUserId(), "commentcount");    //评论
			MessageUtil.clearNewMessage(user.getUserId(), "zancount");        //赞
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 处理用户消息闪动
	 */
	public void getNewMessage() {
		try {
			if(user == null){
				sendResponseMsg("{\"status\":\"0\"}");
				return;
			}
			String messagejson = MessageService.getInstance().getNewMessageJson(user);
			sendResponseMsg(messagejson);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg("{\"status\":\"0\"}");
		}
	}
	
	/**
	 * 检测是否可以发私信
	 */
	public void checkSendMessage(){
		try{
			int relationType = FriendService.getInstance().checkFriendRelationTypeToMessage(toUserId, user.getUserId());
			int status = 0;
			if(relationType == XingyunCommonConstant.FRIEND_RELATION_DOUBLE || relationType == XingyunCommonConstant.FRIEND_RELATION_PAY_USER || UserHeaderService.getInstance().getIsPayUser(user.getUserId()))
				status = 1;
			sendResponseMsg(status == 1 ? XingyunCommonConstant.RESPONSE_SUCCESS_STRING : "single");
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 检测是否可以发商业云信
	 */
	public void checkSendbizMessage(){
		try{
			Map<Object,Object> map = MessageService.getInstance().getMessageBizMessageMap(user.getUserId(), toUserId);
			if(map == null){
				Map<Object,Object> toUserMap = MessageService.getInstance().getMessageBizMessageMap(toUserId, user.getUserId());
				if(toUserMap != null)
					sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
				else if(MessageService.getInstance().checkUserInBizMessageListMc(user.getUserId(), toUserId))
					sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
				else
					sendResponseMsg("bizdaylimit");
				return;
			}
			if(Integer.parseInt(map.get("isopen").toString()) == XingyunCommonConstant.MESSAGE_BIZRELATION_OPEN)
				sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
			else if(Integer.parseInt(map.get("bizyunxincount").toString()) < XingyunCommonConstant.MESSAGE_TYPE_BIZ_LIMTCOUNT)
				sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
			else
				sendResponseMsg("tencountlimt");
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 按昵称搜索好友
	 */
	public String searchMessageSummaryByNickname(){
		try{
			nickName = SearchService.getInstance().replaceSearchContent(nickName);	//替换搜索内容特殊字符
			if(StringUtils.isBlank(nickName))
				return "del";
			
			String pinyinName = SpecialCharFilterUtil.replaceSpecialSqlChar(nickName); // 替换特sql殊符号为空
			pinyinName = PinyinUtil.getInstance().getSelling(pinyinName ); // 汉字转拼音
			totalRecord = MessageService.getInstance().getMessageCountByNickname(user.getUserId(), pinyinName, messageType);
			if(totalRecord != 0)
				messageList = MessageService.getInstance().getMessageSummaryListByNickname(user.getUserId(), pinyinName, curPage, messageType);
			return "showSummaryItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public List<Map<Object, Object>> getMessageList() {
		return messageList;
	}

	public void setMessageList(List<Map<Object, Object>> messageList) {
		this.messageList = messageList;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}
	public Map<Object, Object> getMessageMap() {
		return messageMap;
	}
	public void setMessageMap(Map<Object, Object> messageMap) {
		this.messageMap = messageMap;
	}
	public int getMessageId() {
		return messageId;
	}
	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
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
	public String getToUserId() {
		return toUserId;
	}
	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}
	public List<String> getToUserIds() {
		return toUserIds;
	}
	public void setToUserIds(List<String> toUserIds) {
		this.toUserIds = toUserIds;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public List<Map<Object, Object>> getNewDaXiaList() {
		return newDaXiaList;
	}
	public void setNewDaXiaList(List<Map<Object, Object>> newDaXiaList) {
		this.newDaXiaList = newDaXiaList;
	}
	public int getMessageType() {
		return messageType;
	}
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public boolean isShowBizMessageTip() {
		return showBizMessageTip;
	}

	public void setShowBizMessageTip(boolean showBizMessageTip) {
		this.showBizMessageTip = showBizMessageTip;
	}

	public boolean isShowBizMessageTipWait() {
		return showBizMessageTipWait;
	}

	public void setShowBizMessageTipWait(boolean showBizMessageTipWait) {
		this.showBizMessageTipWait = showBizMessageTipWait;
	}

	public int getShowBizMessageCount() {
		return showBizMessageCount;
	}

	public void setShowBizMessageCount(int showBizMessageCount) {
		this.showBizMessageCount = showBizMessageCount;
	}

	public int getIsBizMessageOpen() {
		return isBizMessageOpen;
	}

	public void setIsBizMessageOpen(int isBizMessageOpen) {
		this.isBizMessageOpen = isBizMessageOpen;
	}
}