package com.xingyun.actions.comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.AuthUserAction;
import com.xingyun.bean.CommentBean;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunPostConstant;
import com.xingyun.services.comment.CommentService;
import com.xingyun.services.friend.FriendService;
import com.xingyun.services.index.MyIndexService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.MessageUtil;

public class CommentUpdateAction extends AuthUserAction {
	private static final long serialVersionUID = 3488567102424295062L;
	private static final Logger log = Logger.getLogger(CommentUpdateAction.class);
	
	private int topicId;              						//评论主题：作品ID或星语ID
	private String content;           						//评论内容
	private int commentId;            						//评论ID
	private CommentBean commentBean;  						//评论Bean
	private UserHeaderBean userHeaderBean;					//用户信息Bean
	private List<Map<Object,Object>> newDaXiaList; 			//最新加入的大侠
	private List<Map<Object,Object>> recommendedWorkList; 	//星云推荐的作品
	private List<CommentBean> commentBeanList;         		//评论列表
	private int totalReceivedCommentCount;					//收到评论总数
	private int totalSentCommentCount;						//发出评论总数
	private int totalRecord;                            	//评论总数
	private int source;								        //评论标志位 0：收到，1：发出
	private int sort = XingyunCommonConstant.COMMENT_SOURCE_ALL; //评论来源类型 0：星语， 1：作品， 2：全部
	private int type;                                       //评论类型：星语或作品
	private String topicUserId;                             //星语或作品作者用户ID
	
	/**
	 * 作品评论
	 */
	public String addPostCommentAjax(){
		try{
			if(StringUtils.isBlank(content) || StringUtils.isBlank(topicUserId) || topicId == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			if(!CommentService.getInstance().checkIsAllowComment(user, topicUserId, XingyunCommonConstant.COMMENT_SOURCE_POST)){
				sendResponseMsg("no allow");
				return null;
			}
			if(!CommentService.getInstance().checkCommentInterval(XingyunCommonConstant.COMMENT_SOURCE_POST, topicId, user.getUserId(), content)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_REQUEST_HOURLY_STRING);
				return null;
			}
			Map<Object,Object> postInfoMap = CommentService.getInstance().getPostInfoMap(topicId);
			if(postInfoMap == null){
				sendResponseMsg("post not exist");
				return null;
			}
			if(CommonUtil.getIntValue(postInfoMap.get("status")) != XingyunPostConstant.ZP_STATUS_TYPE_FB || CommonUtil.getIntValue(postInfoMap.get("isdel")) != XingyunPostConstant.ZP_DEL_TYPE_NO){
				sendResponseMsg("no allow");
				return null;
			}
			commentBean = CommentService.getInstance().addPostCommentData(topicId, user.getUserId(), content, XingyunCommonConstant.FROM_TYPE_WEB, postInfoMap);
			return "showPostCommentItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 作品评论回复
	 */
	public String addPostReCommentAjax(){
		try{
			if(StringUtils.isBlank(content) || StringUtils.isBlank(topicUserId) || topicId == 0 || commentId == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			if(!CommentService.getInstance().checkIsAllowComment(user, topicUserId, XingyunCommonConstant.COMMENT_SOURCE_POST)){
				sendResponseMsg("no allow");
				return null;
			}
			if(!CommentService.getInstance().checkCommentInterval(XingyunCommonConstant.COMMENT_SOURCE_POST, topicId, user.getUserId(), content)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_REQUEST_HOURLY_STRING);
				return null;
			}
			Map<Object,Object> postInfoMap = CommentService.getInstance().getPostInfoMap(topicId);
			if(postInfoMap == null){
				sendResponseMsg("post not exist");
				return null;
			}
			if(CommonUtil.getIntValue(postInfoMap.get("status")) != XingyunPostConstant.ZP_STATUS_TYPE_FB || CommonUtil.getIntValue(postInfoMap.get("isdel")) != XingyunPostConstant.ZP_DEL_TYPE_NO){
				sendResponseMsg("no allow");
				return null;
			}
			Map<Object,Object> commentInfoMap = CommentService.getInstance().getCommentInfoMap(commentId);
			if(commentInfoMap == null){
				sendResponseMsg("comment not exist");
				return null;
			}
			commentBean = CommentService.getInstance().addPostReCommentData(topicId, user.getUserId(), content, commentId, XingyunCommonConstant.FROM_TYPE_WEB, postInfoMap, commentInfoMap);
			return "showPostCommentItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 星语评论
	 */
	public String addXingyuCommentAjax(){
		try{
			if(StringUtils.isBlank(content) || StringUtils.isBlank(topicUserId) || topicId == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			if(!CommentService.getInstance().checkIsAllowComment(user, topicUserId, XingyunCommonConstant.COMMENT_SOURCE_XINGYU)){
				sendResponseMsg("no allow");
				return null;
			}
			if(!CommentService.getInstance().checkCommentInterval(XingyunCommonConstant.COMMENT_SOURCE_XINGYU, topicId, user.getUserId(), content)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_REQUEST_HOURLY_STRING);
				return null;
			}
			Map<Object,Object> xingyuInfoMap = CommentService.getInstance().getXingyuInfoMap(topicId);
			if(xingyuInfoMap == null){
				sendResponseMsg("xingyu not exist");
				return null;
			}
			if(!checkXingyuComment(xingyuInfoMap)){
				sendResponseMsg("no allow");
				return null;
			}
			commentBean = CommentService.getInstance().addXingyuCommentData(topicId, user.getUserId(), content, XingyunCommonConstant.FROM_TYPE_WEB, xingyuInfoMap);
			return "showXingyuCommentItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 检查星云是否可以评论 
	 */
	private boolean checkXingyuComment(Map<Object,Object> xingyuInfoMap) throws Throwable{
		if(CommonUtil.getIntValue(xingyuInfoMap.get("showtype")) == XingyunCommonConstant.XINGYU_SHOW_TYPE_PUB)
			return true;
		if(user.getUserId().equals(CommonUtil.getStringValue(xingyuInfoMap.get("userid"))))
			return true;
		return FriendService.getInstance().checkFriendRelation(CommonUtil.getStringValue(xingyuInfoMap.get("userid")), user.getUserId());
	}
	
	/**
	 * 星语评论回复
	 */
	public String addXingyuReCommentAjax(){
		try{
			if(StringUtils.isBlank(content) || StringUtils.isBlank(topicUserId) || topicId == 0 || commentId == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			if(!CommentService.getInstance().checkIsAllowComment(user, topicUserId, XingyunCommonConstant.COMMENT_SOURCE_XINGYU)){
				sendResponseMsg("no allow");
				return null;
			}
			if(!CommentService.getInstance().checkCommentInterval(XingyunCommonConstant.COMMENT_SOURCE_XINGYU, topicId, user.getUserId(), content)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_REQUEST_HOURLY_STRING);
				return null;
			}
			Map<Object,Object> xingyuInfoMap = CommentService.getInstance().getXingyuInfoMap(topicId);
			if(xingyuInfoMap == null){
				sendResponseMsg("xingyu not exist");
				return null;
			}
			if(!checkXingyuComment(xingyuInfoMap)){
				sendResponseMsg("no allow");
				return null;
			}
			Map<Object,Object> commentInfoMap = CommentService.getInstance().getCommentInfoMap(commentId);
			if(commentInfoMap == null){
				sendResponseMsg("comment not exist");
				return null;
			}
			commentBean = CommentService.getInstance().addXingyunReCommentData(topicId, user.getUserId(), content, commentId, XingyunCommonConstant.FROM_TYPE_WEB, xingyuInfoMap, commentInfoMap);
			return "showXingyuCommentItem";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 我的评论：回复评论
	 */
	public void addMyCommentAjax(){
		try{
			if(type != XingyunCommonConstant.COMMENT_SOURCE_POST && type != XingyunCommonConstant.COMMENT_SOURCE_XINGYU){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(type == XingyunCommonConstant.COMMENT_SOURCE_POST)
				addPostReCommentAjax();
			else
				addXingyuReCommentAjax();
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 删除作品评论
	 */
	public void delPostCommentAjax(){
		try{
			if(topicId == 0 || commentId == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(!CommentService.getInstance().checkDelPostComment(user.getUserId(), topicId, commentId)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			CommentService.getInstance().delPostCommentById(topicId, commentId, user.getUserId());
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 删除星语评论
	 */
	public void delXingyuCommentAjax(){
		try{
			if(topicId == 0 || commentId == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(!CommentService.getInstance().checkDelXingyuComment(user.getUserId(), topicId, commentId)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			CommentService.getInstance().delXingyuCommentById(topicId, commentId, user.getUserId());
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 删除评论:我的评论
	 */
	public void delMyCommentAjax(){
		if(type != XingyunCommonConstant.COMMENT_SOURCE_POST && type != XingyunCommonConstant.COMMENT_SOURCE_XINGYU){
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return;
		}
		if(type == XingyunCommonConstant.COMMENT_SOURCE_POST)
			delPostCommentAjax();
		else
			delXingyuCommentAjax();
	}
	
	/**
	 * 评论列表_获取收到的评论
	 */
	public String showCommentsList() {
		try {
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			
//			getCommentCount(user.getUserId(),sort);
//			if (sentOrReceive == XingyunCommonConstant.COMMENT_TYPE_RECEIVE && totalReceivedCommentCount > 0 || 
//					sentOrReceive == XingyunCommonConstant.COMMENT_TYPE_SENT && totalSentCommentCount > 0 )
//				commentBeanList = CommentService.getInstance().getMyCommentList(user.getUserId(), sentOrReceive, sort, curPage );
			
			getCommentData();	//整理评论列表显示数据
			getMainRightData();
			MessageUtil.clearNewMessage(user.getUserId(), "commentcount");
			return "showComments";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 评论列表_获取收到的评论（翻页，按条件赛选）
	 */
	public String showCommentsListAjax() {
		try {
//			getCommentCount(user.getUserId(),sort);
//			if (sentOrReceive == XingyunCommonConstant.COMMENT_TYPE_RECEIVE && totalReceivedCommentCount > 0 || 
//					sentOrReceive == XingyunCommonConstant.COMMENT_TYPE_SENT && totalSentCommentCount > 0 )
//				commentBeanList = CommentService.getInstance().getMyCommentList(user.getUserId(), sentOrReceive, sort, curPage );
			getCommentData();	//整理评论列表显示数据
			return "showCommentsAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 评论列表_获取收到的和发出的评论数
	 */
//	private void getCommentCount(String userId, int sort2) throws Throwable {
//		totalReceivedCommentCount = CommentService.getInstance().getMyReceivedCommentCount(user.getUserId(),sort );
//		totalSentCommentCount = CommentService.getInstance().getMySentCommentCount(user.getUserId(), sort);
//	}
	
	/**
	 * 整理评论列表显示数据
	 */
	private void getCommentData() throws Throwable{
		List<Map<Object, Object>> sentCommendIndexList = CommentService.getInstance().getSentCommendIndexList(user.getUserId());
		List<Map<Object, Object>> receivedCommendIndexList = CommentService.getInstance().getReceivedCommendIndexList(user.getUserId());
		totalSentCommentCount = sentCommendIndexList == null ? 0 : sentCommendIndexList.size();
		totalReceivedCommentCount = receivedCommendIndexList == null ? 0 :  receivedCommendIndexList.size();
		//收到的评论
		if(source == XingyunCommonConstant.COMMENT_TYPE_RECEIVE && totalReceivedCommentCount > 0)
			getCommendIndexByType(user.getUserId(), receivedCommendIndexList, sort, curPage);
		else if(source == XingyunCommonConstant.COMMENT_TYPE_SENT  && totalSentCommentCount > 0)	//发出的评论
			getCommendIndexByType(user.getUserId(), sentCommendIndexList, sort, curPage);
	}
	
	/**
	 * 整理评论列表显示数据
	 */
	private void getCommendIndexByType(String userID, List<Map<Object, Object>> allIndexList, int commentType, int curPage) throws Throwable {
		if(com.xingyun.constant.XingyunCommonConstant.COMMENT_SOURCE_ALL == commentType){
			totalRecord = allIndexList.size();
			commentBeanList = CommentService.getInstance().getCommentDataList(allIndexList, curPage);
			return;
		}
		
		int count = 0;
		List<Map<Object, Object>> indexList = new ArrayList<Map<Object,Object>>();
		for(Map<Object, Object> map : allIndexList){
			if(commentType == Integer.parseInt(map.get("type").toString())){
				indexList.add(map);
				count++;
			}
		}
		totalRecord = count;
		commentBeanList = CommentService.getInstance().getCommentDataList(indexList, curPage);
	}
	
	
	/**
	 * 评论列表_右侧栏数据显示
	 */
	private void getMainRightData() throws Throwable{
		newDaXiaList = MyIndexService.getInstance().getNewDaXiaList();			//最新加入大侠	
		recommendedWorkList = MyIndexService.getInstance().getRecommWorks();	//推荐作品			
	}
	
	/**
	 * 检测用户是否能评论星语或作品
	 */
	public void checkIsAllowCommentAjax(){
		try{
			if(StringUtils.isBlank(topicUserId) || (type != XingyunCommonConstant.COMMENT_SOURCE_POST && type != XingyunCommonConstant.COMMENT_SOURCE_XINGYU)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			boolean isAllow = CommentService.getInstance().checkIsAllowComment(user, topicUserId, type);
			sendResponseMsg(isAllow ? "yes" : "no");
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public CommentBean getCommentBean() {
		return commentBean;
	}

	public void setCommentBean(CommentBean commentBean) {
		this.commentBean = commentBean;
	}

	public int getTopicId() {
		return topicId;
	}
	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}
	public int getCommentId() {
		return commentId;
	}
	public void setCommentId(int commentId) {
		this.commentId = commentId;
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

	public int getTotalSentCommentCount() {
		return totalSentCommentCount;
	}

	public void setTotalSentCommentCount(int totalSentCommentCount) {
		this.totalSentCommentCount = totalSentCommentCount;
	}

	public int getTotalReceivedCommentCount() {
		return totalReceivedCommentCount;
	}

	public void setTotalReceivedCommentCount(int totalReceivedCommentCount) {
		this.totalReceivedCommentCount = totalReceivedCommentCount;
	}

	public List<CommentBean> getCommentBeanList() {
		return commentBeanList;
	}

	public void setCommentBeanList(List<CommentBean> commentBeanList) {
		this.commentBeanList = commentBeanList;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getTopicUserId() {
		return topicUserId;
	}
	public void setTopicUserId(String topicUserId) {
		this.topicUserId = topicUserId;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
}
