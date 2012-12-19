package com.xingyun.services.comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xingyun.bean.CommentBean;
import com.xingyun.bean.User;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.friend.FriendService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.MessageUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.SpecialCharFilterUtil;
import com.xingyun.util.UploadPicUtil;

public class CommentService {

	private static final DBOperate db = new DBOperate();
	private static final CommentService commentService = new CommentService();
	private CommentService(){}
	public static CommentService getInstance() {
		return commentService;
	}
	
	/**
	 * 添加作品评论
	 */
	public CommentBean addPostCommentData(int postId, String userId, String content, int fromType, Map<Object,Object> postInfoMap) throws Throwable{
		String upUserId = postInfoMap.get("userid").toString();
		String upContent = postInfoMap.get("title").toString();
		int commentId = addCommentData(postId, upUserId, XingyunCommonConstant.COMMENT_SOURCE_POST, userId, content, upUserId, upContent, fromType);
		if(!userId.equals(upUserId))
			MessageUtil.addNewMessage(upUserId, "commentcount");
		return setCommentBean(XingyunCommonConstant.COMMENT_SOURCE_POST, XingyunCommonConstant.COMMENT_TYPE_PINGLUN, commentId, userId, content, postId, upUserId, upUserId, upContent, fromType);
	}
	
	/**
	 * 添加作品评论回复
	 */
	public CommentBean addPostReCommentData(int postId, String userId, String content, int upCommentId, int fromType, Map<Object,Object> postInfoMap, Map<Object,Object> commentInfoMap) throws Throwable{
		String upUserId = commentInfoMap.get("fromuserid").toString();
		String upContent = commentInfoMap.get("content").toString();
		String topicUserId = CommonUtil.getStringValue(postInfoMap.get("userid"));
		int commentId = addReCommentData(postId, topicUserId, XingyunCommonConstant.COMMENT_SOURCE_POST, userId, content, upUserId, upContent, fromType);
		if(!userId.equals(upUserId))
			MessageUtil.addNewMessage(upUserId, "commentcount");
		if(!userId.equals(topicUserId) && !topicUserId.equals(upUserId))
			MessageUtil.addNewMessage(topicUserId, "commentcount");
		return setCommentBean(XingyunCommonConstant.COMMENT_SOURCE_POST, XingyunCommonConstant.COMMENT_TYPE_HUIFU, commentId, userId, content, postId, topicUserId, upUserId, upContent, fromType);
	}
	
	/**
	 * 添加星语评论
	 */
	public CommentBean addXingyuCommentData(int xingyunId, String userId, String content, int fromType, Map<Object,Object> xingyuInfoMap) throws Throwable{
		String upUserId = xingyuInfoMap.get("userid").toString();
		String upContent = xingyuInfoMap.get("content").toString();
		int commentId = addCommentData(xingyunId, upUserId, XingyunCommonConstant.COMMENT_SOURCE_XINGYU, userId, content, upUserId, upContent, fromType);
		if(!userId.equals(upUserId))
			MessageUtil.addNewMessage(upUserId, "commentcount");
		return setCommentBean(XingyunCommonConstant.COMMENT_SOURCE_XINGYU, XingyunCommonConstant.COMMENT_TYPE_PINGLUN, commentId, userId, content, xingyunId, upUserId, upUserId, upContent, fromType);
	}
	
	/**
	 * 添加星语评论回复
	 */
	public CommentBean addXingyunReCommentData(int xingyunId, String userId, String content, int upCommentId, int fromType, Map<Object,Object> xingyuInfoMap, Map<Object,Object> commentInfoMap) throws Throwable{
		String upUserId = commentInfoMap.get("fromuserid").toString();
		String upContent = commentInfoMap.get("content").toString();
		String topicUserId = CommonUtil.getStringValue(xingyuInfoMap.get("userid"));
		int commentId = addReCommentData(xingyunId, topicUserId, XingyunCommonConstant.COMMENT_SOURCE_XINGYU, userId, content, upUserId, upContent, fromType);
		if(!userId.equals(upUserId))
			MessageUtil.addNewMessage(upUserId, "commentcount");
		if(!userId.equals(topicUserId) && !topicUserId.equals(upUserId))
			MessageUtil.addNewMessage(topicUserId, "commentcount");
		return setCommentBean(XingyunCommonConstant.COMMENT_SOURCE_XINGYU, XingyunCommonConstant.COMMENT_TYPE_HUIFU, commentId, userId, content, xingyunId, topicUserId, upUserId, upContent, fromType);
	}
	
	/**
	 * 添加评论
	 */
	private int addCommentData(int topicId, String topicUserId, int type, String userId, String content, String upUserId, String upContent, int fromType) throws Throwable{
		int commentId = 0;
		try{
			commentId = addCommentTodb(topicId, topicUserId, type, XingyunCommonConstant.COMMENT_TYPE_PINGLUN, userId, content, upUserId, upContent, fromType);
			updateCommentCount(type, topicId);
			return commentId;
		}catch(Throwable e){
			if(commentId > 0)
				delCommentDataById(commentId);
			throw new Throwable(e);
		}
	}
	
	/**
	 * 添加评论回复
	 */
	private int addReCommentData(int topicId, String topicUserId, int type, String userId, String content, String upUserId, String upContent, int fromType) throws Throwable{
		int commentId = 0;
		try{
			commentId = addCommentTodb(topicId, topicUserId, type, XingyunCommonConstant.COMMENT_TYPE_HUIFU, userId, content, upUserId, upContent, fromType);
			updateCommentCount(type, topicId);
			return commentId;
		}catch(Throwable e){
			if(commentId > 0)
				delCommentDataById(commentId);
			throw new Throwable(e);
		}
	}
	/**
	 * 添加评论数据到表中
	 */
	private int addCommentTodb(int topicId, String topicUserId, int type, int commentType, String userId, String content, String upUserId, String upContent, int fromType) throws Throwable{
		String sql = "INSERT INTO comment(topicid,topicuserid,type,commenttype,fromuserid,content,upuserid,upcontent,fromtype,systime) VALUES(?,?,?,?,?,?,?,?,?,?)";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(topicId);
		valueList.add(topicUserId);
		valueList.add(type);
		valueList.add(commentType);
		valueList.add(userId);
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(content, 500));
		valueList.add(upUserId);
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(upContent, 500));
		valueList.add(fromType);
		valueList.add(new Date());
		return db.insertData(sql, valueList);
	}
	/**
	 * 整理评论Bean
	 */
	private CommentBean setCommentBean(int type, int commentType, int commentId, String userId, String content, int topicId, String topicUserId, String upUserId, String upContent, int fromType) throws Throwable{
		Map<Object,Object> userInfoMap = getUserInfoMap(userId);
		if(userInfoMap == null)
			return null;
		Map<Object,Object> upUserInfoMap = getUserInfoMap(upUserId);
		if(upUserInfoMap == null)
			return null;
		CommentBean commentBean = new CommentBean();
		commentBean.setType(type);
		commentBean.setCommentID(commentId);
		commentBean.setCommentType(commentType);
		commentBean.setFromUserID(userId);
		commentBean.setUserHref(CommonUtil.getUserIndexHref(userId, userInfoMap.get("wkey")));
		commentBean.setNickName(userInfoMap.get("nickname").toString());
		commentBean.setLogoUrl(userInfoMap.get("logourl").toString());
		commentBean.setLid(Integer.parseInt(userInfoMap.get("lid").toString()));
		commentBean.setVerified(CommonUtil.getIntValue(userInfoMap.get("verified")));
		commentBean.setContent(content);
		commentBean.setSystime(DateUtil.getBlogSystime(new Date()));
		commentBean.setTopicID(topicId);
		commentBean.setTopicUserID(topicUserId);
		commentBean.setUpUserID(upUserId);
		commentBean.setUpNickName(CommonUtil.getStringValue(upUserInfoMap.get("nickname")));
		commentBean.setUpLid(CommonUtil.getIntValue(upUserInfoMap.get("lid")));
		commentBean.setUpUserHref(CommonUtil.getUserIndexHref(upUserId, upUserInfoMap.get("wkey")));
		commentBean.setUpContent(CommonUtil.getStringValue(upUserInfoMap.get("nickname")));
		commentBean.setUpVerified(CommonUtil.getIntValue(upUserInfoMap.get("verified")));
		commentBean.setFromType(CommonUtil.checkCommentFromType(fromType));
		return commentBean;
	}
	/**
	 * 获取用户信息
	 */
	private Map<Object,Object> getUserInfoMap(String userId) throws Throwable{
		String sql = "SELECT nickname, wkey, logourl, lid, verified FROM user WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		Map<Object,Object> map = list.get(0);
		map.put("logourl", UploadPicUtil.getPicWebUrl(map.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50));
		return map;
	}
	/**
	 * 获取作品信息
	 */
	public Map<Object,Object> getPostInfoMap(int postId) throws Throwable{
		String sql = "SELECT userid, title, status, isdel FROM post WHERE id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	/**
	 * 获取评论信息
	 */
	public Map<Object,Object> getCommentInfoMap(int commentId) throws Throwable{
		String sql = "SELECT fromuserid, content FROM comment WHERE id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(commentId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	/**
	 * 获取星语信息
	 */
	public Map<Object,Object> getXingyuInfoMap(int xingyunId) throws Throwable{
		String sql = "SELECT userid, content, showtype FROM xingyu WHERE id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(xingyunId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	/**
	 * 删除作品评论
	 */
	public void delPostCommentById(int postId, int commentId, String userId) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("DELETE FROM comment WHERE id = " + commentId);
		sqlList.add("UPDATE post_counter SET commentcount = commentcount - 1 WHERE postid = " + postId);
		db.batchExecute(sqlList, true);
	}
	/**
	 * 删除星云评论
	 */
	public void delXingyuCommentById(int xingyunId, int commentId, String userId) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("DELETE FROM comment WHERE id = " + commentId);
		sqlList.add("UPDATE xingyu SET commentcount = commentcount - 1 WHERE id = " + xingyunId);
		db.batchExecute(sqlList, true);
	}
	/**
	 * 检查用户是否可以删除作品评论
	 */
	public boolean checkDelPostComment(String userId,int postId, int commentId) throws Throwable{
		String sql = "SELECT userid FROM post WHERE id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postId);
		String postUserId = CommonUtil.getStringValue(sql, valueList, "userid");
		if(StringUtils.EMPTY.equals(postUserId))
			return false;
		if(postUserId.equals(userId))
			return true;
		sql = "SELECT COUNT(*) FROM comment WHERE id = ? AND fromuserid = ?";
		valueList.clear();
		valueList.add(commentId);
		valueList.add(userId);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
	
	/**
	 * 检查用户是否可以删除星语评论
	 */
	public boolean checkDelXingyuComment(String userId,int xingyunId, int commentId) throws Throwable{
		String sql = "SELECT userid FROM xingyu WHERE id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(xingyunId);
		String xingyuUserId = CommonUtil.getStringValue(sql, valueList, "userid");
		if(StringUtils.EMPTY.equals(xingyuUserId))
			return false;
		if(xingyuUserId.equals(userId))
			return true;
		sql = "SELECT COUNT(*) FROM comment WHERE id = ? AND fromuserid = ?";
		valueList.clear();
		valueList.add(commentId);
		valueList.add(userId);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
	/**
	 * 添加评论或评论回复时修改评论总数
	 */
	private void updateCommentCount(int type, int topicId) throws Throwable{
		String sql = "UPDATE post_counter SET commentcount = commentcount + 1 WHERE postid = " + topicId;
		if(type == XingyunCommonConstant.COMMENT_SOURCE_XINGYU)
			sql = "UPDATE xingyu SET commentcount = commentcount + 1 WHERE id = " + topicId;
		db.updateData(sql);
	}
	/**
	 * 根据评论来源和主题ID获取评论列表
	 * @param type       评论来源类型 0:星语 1:作品
	 * @param topicId    星语ID或作品ID
	 */
	public List<CommentBean> getCommentList(int type, int topicId, int curPage, int maxSize) throws Throwable{
		List<Map<Object,Object>> commentIndexList = getCommentIndexList(type, topicId, curPage, maxSize);
		if(commentIndexList == null || commentIndexList.size() == 0)
			return null;
		return setCommentBeanList(commentIndexList);
	}
	/**
	 * 根据评论来源和主题ID获取评论数量
	 * @param type       评论来源类型 0:星语 1:作品
	 * @param topicId    星语ID或作品ID
	 */
	public int getCommentCount(int type, int topicId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM comment WHERE type = ? AND topicid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(type);
		valueList.add(topicId);
		return db.getRecordCountSQL(sql, valueList);
	}
	/**
	 * 根据评论来源和主题ID获取评论索引列表
	 * @param type       评论来源类型 0:星语 1:作品
	 * @param topicId    星语ID或作品ID
	 */
	private List<Map<Object,Object>> getCommentIndexList(int type, int topicId, int curPage, int maxSize) throws Throwable{
		String sql = "SELECT id, type, topicid, topicuserid, commenttype, fromuserid, content, upuserid, upcontent, fromtype, systime FROM comment" +
				" WHERE type = ? AND topicid = ? ORDER BY id DESC LIMIT ?, ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(type);
		valueList.add(topicId);
		valueList.add((curPage -1) * maxSize);
		valueList.add(maxSize);
		return db.retrieveSQL(sql, valueList);
	}
	/**
	 * 整理评论Bean列表集合
	 */
	@SuppressWarnings("unchecked")
	private List<CommentBean> setCommentBeanList(List<Map<Object,Object>> commentIndexList) throws Throwable{
		String fromUserIds = CommonUtil.getStringUserID(commentIndexList, "fromuserid");
		Map<Object, Map<Object,Object>> fromUserMap = PublicQueryUtil.getInstance().getUserInfoMapByUserIds(fromUserIds);
		String upUserIds = CommonUtil.getStringUserID(commentIndexList, "upuserid");
		Map<Object, Map<Object,Object>> upUserMap = PublicQueryUtil.getInstance().getUserInfoMapByUserIds(upUserIds);
		Map<Object,Object> UserTmpMap = null;
		Map<Object,Object> upUserTmpMap = null;
		String fromUserId, upUserId;
		List<CommentBean> commentBeanList = new ArrayList<CommentBean>();
		CommentBean commentBean = null;
		for(Map<Object,Object> map : commentIndexList){
			fromUserId = CommonUtil.getStringValue(map.get("fromuserid"));
			UserTmpMap = (Map<Object,Object>)fromUserMap.get(fromUserId);
			if(UserTmpMap == null)
				continue;
			commentBean = new CommentBean();
			commentBean.setType(CommonUtil.getIntValue(map.get("type")));
			commentBean.setCommentID(CommonUtil.getIntValue(map.get("id")));
			commentBean.setCommentType(CommonUtil.getIntValue(map.get("commenttype")));
			commentBean.setFromUserID(fromUserId);
			commentBean.setUserHref(CommonUtil.getUserIndexHref(fromUserId, UserTmpMap.get("wkey")));
			commentBean.setNickName(UserTmpMap.get("nickname").toString());
			commentBean.setLogoUrl(UserTmpMap.get("logourl").toString());
			commentBean.setLid(Integer.parseInt(UserTmpMap.get("lid").toString()));
			commentBean.setVerified(CommonUtil.getIntValue(UserTmpMap.get("verified")));
			commentBean.setContent(CommonUtil.getStringValue(map.get("content")));
			commentBean.setSystime(DateUtil.getBlogSystime((Date)map.get("systime")));
			commentBean.setTopicID(CommonUtil.getIntValue(map.get("topicid")));
			commentBean.setTopicUserID(CommonUtil.getStringValue(map.get("topicuserid")));
			commentBean.setFromType(CommonUtil.checkCommentFromType(CommonUtil.getIntValue(map.get("fromtype"))));
			upUserId = CommonUtil.getStringValue(map.get("upuserid"));
			upUserTmpMap = (Map<Object,Object>)upUserMap.get(upUserId);
			if(upUserTmpMap != null){
				commentBean.setUpUserID(upUserId);
				commentBean.setUpNickName(CommonUtil.getStringValue(upUserTmpMap.get("nickname")));
				commentBean.setUpLid(CommonUtil.getIntValue(upUserTmpMap.get("lid")));
				commentBean.setUpVerified(CommonUtil.getIntValue(upUserTmpMap.get("verified")));
				commentBean.setUpUserHref(CommonUtil.getUserIndexHref(upUserId, upUserTmpMap.get("wkey")));
				commentBean.setUpContent(CommonUtil.getStringValue(map.get("upcontent")));
			}
			commentBeanList.add(commentBean);
		}
		return commentBeanList;
	}
	
	/**
	 * 评论列表_获取收到的评论数
	 */
	public int getMyReceivedCommentCount(String userid, int sort) throws Throwable {
		String sql = "";
		// 回复我评论的评论
		List<Object> valueList = new ArrayList<Object>();  
		valueList.add(userid); // upuserid的值
		if (XingyunCommonConstant.COMMENT_SOURCE_ALL == sort) {
			sql = "SELECT COUNT(*) FROM comment WHERE upuserid = ?";
		} else {
			sql = "SELECT COUNT(*) FROM comment WHERE upuserid = ? AND type = ?";
			valueList.add(sort);
		}
		int comment = db.getRecordCountSQL(sql, valueList); 
		
		// 回复我作品或星语的评论
		List<Object> otherList = new ArrayList<Object>();
		otherList.add(userid); // topicuserid的值
		if (XingyunCommonConstant.COMMENT_SOURCE_ALL == sort) {
			sql = "SELECT fromuserid,upuserid FROM comment WHERE topicuserid = ?";
		} else {
			sql = "SELECT fromuserid,upuserid FROM comment WHERE topicuserid = ? AND type = ?";
			otherList.add(sort);
		}
		
		List<Map<Object, Object>> multiCommentList = new ArrayList<Map<Object, Object>>(); 
		for (Map<Object, Object> tmpMap : db.retrieveSQL(sql, otherList)) {
			if (!userid.equals(CommonUtil.getStringValue(tmpMap.get("upuserid") ) ) && !userid.equals(CommonUtil.getStringValue(tmpMap.get("fromuserid") ) )) {
				multiCommentList.add(tmpMap);
			}
		}
		int multiComment = multiCommentList.size();
		
		return comment + multiComment;
	}
	
	/**
	 * 评论列表_获取发出的评论数
	 */
	public int getMySentCommentCount(String userid, int sort) throws Throwable {
		String sql = "";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userid);
		if (XingyunCommonConstant.COMMENT_SOURCE_ALL == sort) {
			sql = "SELECT COUNT(*) FROM comment WHERE fromuserid = ?";
		} else {
			sql = "SELECT COUNT(*) FROM comment WHERE fromuserid = ? AND type = ?";
			valueList.add(sort);
		}
		
		return db.getRecordCountSQL(sql, valueList); 
	}
	
	/**
	 * 整理收到的评论总数
	 */
	public int getCommendCount(String usreID) throws Throwable{
		return CommentService.getInstance().getReceivedCommendIndexList(usreID).size();
	}
	
	/**
	 * 评论列表_获取发出的评论索引数据
	 */
	public List<Map<Object, Object>> getSentCommendIndexList(String userid) throws Throwable {
		String sql = "SELECT id, type, topicuserid, fromuserid, upuserid, commenttype, topicid, content, fromtype, upcontent, systime FROM comment WHERE fromuserid = ? ORDER BY id DESC";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userid);
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 评论列表_获取收到的评论索引数据
	 */
	public List<Map<Object, Object>> getReceivedCommendIndexList(String userid) throws Throwable {
		//评论或回复 我作品星语 索引数据
		String sql = "SELECT id, type, topicuserid, fromuserid, upuserid FROM comment WHERE topicuserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userid);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, valueList);
		
		//回复 我发出的评论数据
		sql = "SELECT id, type, topicuserid, fromuserid, upuserid FROM comment WHERE upuserid = ?";
		list.addAll(db.retrieveSQL(sql, valueList));
		if(list.size() == 0)
			return list;
		
		//去除重复
		CommonUtil.setList(list);
		//过滤数据
		StringBuilder idSB = new StringBuilder();
		List<Map<Object, Object>> indexList = new ArrayList<Map<Object,Object>>();
		for(Map<Object, Object> map : list){
			if(userid.equals(map.get("topicuserid").toString()) && userid.equals(map.get("fromuserid").toString()))
				continue;
			idSB.append(map.get("id").toString()).append(",");
		}
		if(idSB.length() == 0)
			return indexList;
		
		idSB.deleteCharAt(idSB.length() - 1);
		sql = "SELECT id, type, topicuserid, fromuserid, upuserid, commenttype, topicid, content, fromtype, upcontent, systime FROM comment WHERE id IN(" + idSB.toString() + ") ORDER BY id DESC";
		indexList = db.retrieveSQL(sql);
		return indexList;
	}
	
	/**
	 * 整理评论列表显示数据
	 */
	public List<CommentBean> getCommentDataList(List<Map<Object, Object>> indexList, int pageIndex) throws Throwable {
		//评论分页索引数据
		indexList = CommonUtil.subList(indexList, pageIndex, XingyunCommonConstant.COMMENT_PAGE_SIZE);
		if(indexList == null || indexList.size() == 0)
			return null;
		
		String fromUserIds = CommonUtil.getStringUserID(indexList, "fromuserid");
		String upUserIds = CommonUtil.getStringUserID(indexList, "upuserid");
		String topicUserIds = CommonUtil.getStringUserID(indexList, "topicuserid");
		Map<Object, Map<Object,Object>> userMap = PublicQueryUtil.getInstance().getUserInfoMapByUserIds(fromUserIds + "," + upUserIds + "," + topicUserIds);
		Map<Object, Object> UserTmpMap = null;
		Map<Object, Object> upUserTmpMap = null;
		Map<Object,Object> topicUserTmpMap = null;
		String fromUserId, upUserId, topicUserId;
		List<CommentBean> commentBeanList = new ArrayList<CommentBean>();
		CommentBean commentBean = null;
		for(Map<Object,Object> map : indexList){
			fromUserId = CommonUtil.getStringValue(map.get("fromuserid"));
			UserTmpMap = userMap.get(fromUserId);
			topicUserId = CommonUtil.getStringValue(map.get("topicuserid"));
			topicUserTmpMap = userMap.get(topicUserId);
			upUserId = CommonUtil.getStringValue(map.get("upuserid"));
			upUserTmpMap = userMap.get(upUserId);
			if(UserTmpMap == null || topicUserTmpMap == null || upUserTmpMap == null)
				continue;
			commentBean = new CommentBean();
			commentBean.setType(CommonUtil.getIntValue(map.get("type")));
			commentBean.setCommentID(CommonUtil.getIntValue(map.get("id")));
			commentBean.setCommentType(CommonUtil.getIntValue(map.get("commenttype")));
			commentBean.setFromUserID(fromUserId);
			commentBean.setUserHref(CommonUtil.getUserIndexHref(fromUserId, UserTmpMap.get("wkey")));
			commentBean.setNickName(UserTmpMap.get("nickname").toString());
			commentBean.setLogoUrl(UserTmpMap.get("logourl").toString());
			commentBean.setLid(Integer.parseInt(UserTmpMap.get("lid").toString()));
			commentBean.setVerified(CommonUtil.getIntValue(UserTmpMap.get("verified")));
			commentBean.setContent(CommonUtil.getStringValue(map.get("content")));
			commentBean.setSystime(DateUtil.getBlogSystime((Date)map.get("systime")));
			commentBean.setTopicID(CommonUtil.getIntValue(map.get("topicid")));
			commentBean.setTopicUserID(topicUserId);
			commentBean.setTopicUserHref(CommonUtil.getUserIndexHref(topicUserId, topicUserTmpMap.get("wkey")));
			commentBean.setFromType(CommonUtil.checkCommentFromType(CommonUtil.getIntValue(map.get("fromtype"))));
			if(upUserTmpMap != null){
				commentBean.setUpUserID(upUserId);
				commentBean.setUpNickName(CommonUtil.getStringValue(upUserTmpMap.get("nickname")));
				commentBean.setUpLid(CommonUtil.getIntValue(upUserTmpMap.get("lid")));
				commentBean.setUpVerified(CommonUtil.getIntValue(upUserTmpMap.get("verified")));
				commentBean.setUpUserHref(CommonUtil.getUserIndexHref(upUserId, upUserTmpMap.get("wkey")));
				commentBean.setUpContent(CommonUtil.getStringValue(map.get("upcontent")));
			}
			commentBeanList.add(commentBean);
		}
		return commentBeanList;
	}
	
	/**
	 * 评论列表_获取收到的评论列表
	 */
	public List<CommentBean> getMyCommentList(String userId, int source, int sort, int curPage) throws Throwable {
		List<Map<Object, Object>> tmpList = new ArrayList<Map<Object, Object>>();
		if (source == XingyunCommonConstant.COMMENT_TYPE_RECEIVE) {
			List<Map<Object, Object>> commentList = getMyCommentListByType(userId, sort);
			List<Map<Object, Object>> multiCommentList = getMyMultiCommentList(userId, sort);
			if (commentList != null && commentList.size() != 0)
				tmpList.addAll(commentList);
			if (multiCommentList != null && multiCommentList.size() != 0)
				tmpList.addAll(multiCommentList);
		} else {
			List<Map<Object, Object>> sentCommentList = getMySentCommentList(userId, sort);
			tmpList.addAll(sentCommentList);
		}
		
		if (tmpList.size() == 0)
			return null;
		CommonUtil.compositor(tmpList, new String[]{"systime"}, 1);
		tmpList = CommonUtil.subList(tmpList, curPage, XingyunCommonConstant.COMMENT_PAGE_SIZE);
		return setCommentBeanList(tmpList);
	}
	
	/**
	 * 评论列表_获取收到的给星语或作品评论的列表
	 */
	private List<Map<Object, Object>> getMyCommentListByType(String userid, int sort) throws Throwable {
		String sql = "";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userid);
		if (XingyunCommonConstant.COMMENT_SOURCE_ALL == sort) {
			sql = "SELECT id, type, topicid, topicuserid, commenttype, fromuserid, content, upuserid, upcontent, fromtype, systime FROM comment WHERE upuserid = ?";
		} else {
			sql = "SELECT id, type, topicid, topicuserid, commenttype, fromuserid, content, upuserid, upcontent, fromtype, systime FROM comment WHERE upuserid = ? AND type = ?";
			valueList.add(sort);
		}
		
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 评论列表_获取收到的评论的列表(A评论我的星语，B评论A的评论，就是B这种评论列表)
	 */
	private List<Map<Object, Object>> getMyMultiCommentList(String userid, int sort) throws Throwable {
		String sql = "";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userid);
		if (XingyunCommonConstant.COMMENT_SOURCE_ALL == sort) {
			sql = "SELECT id, topicid, type, commenttype, fromuserid, content, topicuserid, upuserid, upcontent, fromtype, systime FROM comment WHERE topicuserid = ?";
		} else {
			sql = "SELECT id, topicid, type, commenttype, fromuserid, content, topicuserid, upuserid, upcontent, fromtype, systime FROM comment WHERE topicuserid = ? AND type = ?";
			valueList.add(sort);
		}
		
		List<Map<Object, Object>> multiCommentList = new ArrayList<Map<Object, Object>>(); 
		for (Map<Object, Object> tmpMap : db.retrieveSQL(sql, valueList)) {
			if (!userid.equals(CommonUtil.getStringValue(tmpMap.get("upuserid") ) ) && !userid.equals(CommonUtil.getStringValue(tmpMap.get("fromuserid") ) )) {
				multiCommentList.add(tmpMap);
			}
		}
		
		return multiCommentList;
	}
	
	/**
	 * 评论列表_获取发出评论列表 
	 */
	private List<Map<Object, Object>> getMySentCommentList(String userid, int sort) throws Throwable {
		String sql = "";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userid);
		if (XingyunCommonConstant.COMMENT_SOURCE_ALL == sort) {
			sql = "SELECT id, topicid, type, commenttype, fromuserid, content, topicuserid, upuserid, upcontent, fromtype, systime FROM comment WHERE fromuserid = ?";
		} else {
			sql = "SELECT id, topicid, type, commenttype, fromuserid, content, topicuserid, upuserid, upcontent, fromtype, systime FROM comment WHERE fromuserid = ? AND type = ?";
			valueList.add(sort);
		}
		
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 检查是否在10分钟内发送评论相同的内容
	 */
	public boolean checkCommentInterval(int type, int topicId, String userId, String content) throws Throwable {
		content = SpecialCharFilterUtil.filterEncodeAndForbidValue(content, 500);
		String sql = "SELECT MAX(systime) AS systime FROM comment WHERE type = ? AND topicid = ? AND fromuserid = ? AND content = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(type);
		valueList.add(topicId);
		valueList.add(userId);
		valueList.add(content);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return true;
		Date lastTime = (Date)list.get(0).get("systime");
		if(lastTime == null || (new Date().getTime() - lastTime.getTime()) > 600000)
			return true;
		return false;
	}
	
	/**
	 * 检查用户是否可以删除作品评论
	 */
	public boolean checkIsAllowComment(User user, String userId, int type) throws Throwable{
		if(userId.equals(user.getUserId()))
			return true;
		int commentType = getCommentControl(userId, type);
		if(commentType == XingyunCommonConstant.USER_CONTROL_COMMENT_ALL)
			return true;
		if(commentType == XingyunCommonConstant.USER_CONTROL_COMMENT_FRIEND)
			return FriendService.getInstance().checkFriendRelation(userId, user.getUserId());
		if(commentType == XingyunCommonConstant.USER_CONTROL_COMMENT_JINGYING)
			return (user.getLid() >= XingyunCommonConstant.USER_LEVEL_JINGYING || FriendService.getInstance().checkFriendRelation(userId, user.getUserId()));
		return false;
	}
	/**
	 * 获取用户的评论设置权限
	 * 使用索引：index_user_control_userid
	 */
	private int getCommentControl(String userId, int type) throws Throwable{
		String columnName = "postcomment";
		if(type == XingyunCommonConstant.COMMENT_SOURCE_XINGYU)
			columnName = "xingyucomment";
		String sql = "SELECT "+columnName+" FROM user_control WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return CommonUtil.getIntValue(sql, valueList, columnName);
	}
	/**
	 * 根据评论ID删除评论
	 */
	private void delCommentDataById(int commentId) throws Throwable{
		String sql = "DELETE FROM comment WHERE id = " + commentId;
		db.deleteData(sql);
	}
}
