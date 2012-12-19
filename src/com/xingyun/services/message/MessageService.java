package com.xingyun.services.message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xingyun.bean.User;
import com.xingyun.cache.MClient;
import com.xingyun.constant.MemcachedConstant;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.JsonObjectUtil;
import com.xingyun.util.MessageUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.SpecialCharFilterUtil;
import com.xingyun.util.UploadPicUtil;


public class MessageService {
	private static final DBOperate db = new DBOperate();
	private static final MessageService messageService = new MessageService();
	private MessageService(){}
	public static MessageService getInstance(){
		return messageService;
	}
	/**
	 * 发送云信
	 */
	public String addMessageInfo(String fromUserId, String toUserId, String content, int messageType) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		content = SpecialCharFilterUtil.filterEncodeAndForbidValue(content, 500);
		addMessage(sqlList, fromUserId, toUserId, content, messageType);
		addOrUpdateMessageSummary(sqlList, fromUserId, toUserId, content, XingyunCommonConstant.MESSAGE_RESOURCE_SEND, messageType);
		addOrUpdateMessageSummary(sqlList, toUserId, fromUserId, content, XingyunCommonConstant.MESSAGE_RESOURCE_RECEIVE, messageType);
		db.batchExecute(sqlList, true);
		if(messageType == XingyunCommonConstant.MESSAGE_TYPE_DEFAULT)
			MessageUtil.addNewMessage(toUserId, "yunxincount");
		else
			MessageUtil.addNewMessage(toUserId, "bizyunxincount");
		return XingyunCommonConstant.RESPONSE_SUCCESS_STRING;
	}
	/**
	 * 添加云信到发件箱，收件箱及云信历史表
	 */
	private void addMessage(List<String> sqlList, String fromUserId, String toUserId, String content, int messageType) throws Throwable{
		String time = DateUtil.getSimpleDateFormat();
		sqlList.add("INSERT INTO message_send(fromuserid,touserid,content,systime,messagetype) VALUES('"+fromUserId+"','"+toUserId+"','"+content+"','"+time+"',"+messageType+")");
		sqlList.add("INSERT INTO message_receive(fromuserid,touserid,content,systime,messagetype) VALUES('"+toUserId+"','"+fromUserId+"','"+content+"','"+time+"',"+messageType+")");
		sqlList.add("INSERT INTO message_history(fromuserid,touserid,content,systime,messagetype) VALUES('"+fromUserId+"','"+toUserId+"','"+content+"','"+time+"',"+messageType+")");
		if(messageType == XingyunCommonConstant.MESSAGE_TYPE_BIZ)
			addMessageBizRelation(fromUserId, toUserId, messageType, sqlList);
	}

	/**
	 * 添加或修改云信组表
	 * @param source   云信来源 0：发出 1：接收
	 */
	private void addOrUpdateMessageSummary(List<String> sqlList, String fromUserId, String toUserId, String content, int source, int messageType) throws Throwable{
		String sql;
		String time = DateUtil.getSimpleDateFormat();
		if(checkMessageInSummary(fromUserId, toUserId, messageType)){
			sql = "UPDATE message_summary SET content = '"+content+"',";
			if(source != XingyunCommonConstant.MESSAGE_RESOURCE_SEND)
				sql += "newcount = newcount + 1,";
		    sql += "totalcount = totalcount + 1, source="+source+", systime = '"+time+"' WHERE fromuserid = '" + fromUserId +"' AND touserid = '" + toUserId +"' AND messagetype = " + messageType;
		}else{
			int count = 0;
			if(source != XingyunCommonConstant.MESSAGE_RESOURCE_SEND)
				count = 1;
			sql = "INSERT INTO message_summary(fromuserid,touserid,content,newcount,totalcount,source,systime,messagetype) VALUES('"+fromUserId+"','"+toUserId+"','"+content+"',"+count+",1,"+source+",'"+time+"',"+messageType+")";
		}
		sqlList.add(sql);
	}
	/**
	 * 检测用户收件箱中是否有数据
	 * 使用索引：index_message_summary_fromuserid_touserid_messagetype
	 */
	private boolean checkMessageInSummary(String fromUserId,String toUserId,int messageType) throws Throwable{
		String sql = "SELECT COUNT(*) FROM message_summary WHERE fromuserid = ? AND touserid = ? AND messagetype = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		valueList.add(messageType);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
	
	/**
	 * 查找云信summary表中id
	 * 使用索引：index_message_summary_fromuserid_touserid_messagetype
	 */
	public Map<Object,Object> getMessageIdInSummary(String fromUserId,String toUserId, int messageType) throws Throwable{
		String sql = "SELECT m.id, m.newcount,m.totalcount,m.source,u.userid,u.logourl,u.nickname,u.lid,u.verified FROM message_summary m, user u WHERE m.touserid = u.userid AND m.fromuserid = ? AND m.touserid = ? AND m.messagetype = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		valueList.add(messageType);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		Map<Object,Object> map = list.get(0);
		map.put("logourl", UploadPicUtil.getPicWebUrl(map.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50));
		return map;
	}

	/**
	 * 查询云信组总数
	 * 使用索引： index_message_summary_fromuserid_messagetype_systime
	 */
	public int getMessageCount(String userId, int messageType) throws Throwable{
		String sql = "SELECT COUNT(*) FROM message_summary WHERE fromuserid = ? AND messagetype = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(messageType);
		return db.getRecordCountSQL(sql, valueList);
	}
	
	/**
	 * 查询云信组总数
	 * 使用索引： index_message_summary_fromuserid_messagetype_systime
	 */
	public int getMessageCountByNickname(String userId, String nickName, int messageType) throws Throwable{
		String sql = "SELECT count(*) FROM message_summary m, user u WHERE m.touserid = u.userid AND m.fromuserid = ? AND m.messagetype = ? AND u.pinyinname LIKE '%"+nickName+"%'";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(messageType);
		return db.getRecordCountSQL(sql, valueList);
	}
	/**
	 * 云信组列表信息
	 */
	public List<Map<Object,Object>> getMessageSummaryList(String userId, int curPage, int messageType) throws Throwable{
		List<Map<Object,Object>> summaryIndexList = getMessageySummaryIndexList(userId, curPage, messageType);
		if(summaryIndexList == null || summaryIndexList.size() == 0)
			return null;
		return setSummaryMessageInfoList(summaryIndexList);
	}
	
	/**
	 * 根据云信类型和状态查询云信组列表
	 * 使用索引： index_message_summary_fromuserid_messagetype_systime
	 */
	private List<Map<Object,Object>> getMessageySummaryIndexList(String userId, int curPage, int messageType) throws Throwable{
		String sql = "SELECT id,fromuserid,touserid,content,newcount,totalcount,source,systime FROM message_summary WHERE fromuserid = ? AND messagetype = ? ORDER BY systime DESC LIMIT ?, ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(messageType);
		valueList.add((curPage -1) * XingyunCommonConstant.MESSAGE_PAGENUM);
		valueList.add(XingyunCommonConstant.MESSAGE_PAGENUM);
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 有搜索条件（昵称）时，筛选私信
	 */
	public List<Map<Object,Object>> getMessageSummaryListByNickname(String userId, String nickName, int curPage, int messageType) throws Throwable {
		List<Map<Object,Object>> summaryIndexList = getMessageySummaryIndexListByNickname(userId, nickName, curPage, messageType);
		if(summaryIndexList == null || summaryIndexList.size() == 0)
			return null;
		return setSummaryMessageInfoList(summaryIndexList);
	}
	
	/**
	 * 根据云信类型和状态查询云信组列表
	 * 使用索引： index_message_summary_fromuserid_messagetype_systime
	 */
	private List<Map<Object,Object>> getMessageySummaryIndexListByNickname(String userId, String nickName, int curPage, int messageType) throws Throwable{
		String sql = "SELECT m.id,m.fromuserid,m.touserid,m.content,m.newcount,m.totalcount,m.source,m.systime FROM message_summary m, user u" +
				" WHERE m.touserid = u.userid AND m.fromuserid = ? AND m.messagetype = ? AND u.pinyinname LIKE '%"+nickName+"%' ORDER BY m.systime DESC LIMIT ?, ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(messageType);
		valueList.add((curPage -1) * XingyunCommonConstant.MESSAGE_PAGENUM);
		valueList.add(XingyunCommonConstant.MESSAGE_PAGENUM);
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 设置云信组列表
	 */
	public List<Map<Object,Object>> setSummaryMessageInfoList(List<Map<Object,Object>> list) throws Throwable{
		String toUserId;
		Map<Object,Object> tmpMap = null;
		List<Map<Object,Object>> summaryList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> map : list){
			toUserId = map.get("touserid").toString();
			tmpMap = PublicQueryUtil.getInstance().findUserCommonMap(toUserId);
			if(tmpMap == null)
				continue;
			map.putAll(tmpMap);
			map.put("logourl", UploadPicUtil.getPicWebUrl(tmpMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50));
			map.put("systime", DateUtil.getBlogSystime((Date)map.get("systime")));
			summaryList.add(map);
		}
		return summaryList;
	}
	/**
	 * 获取云信对话列表
	 */
	public List<Map<Object,Object>> getMessageDialogueList(String fromUserId, String toUserId, int curPage, int messageType) throws Throwable{
		List<Map<Object,Object>> dialogueList = new ArrayList<Map<Object,Object>>();
		dialogueList.addAll(getMessageSendList(fromUserId, toUserId, messageType));
		dialogueList.addAll(getMessageReceiveList(fromUserId, toUserId, messageType));
		CommonUtil.compositor(dialogueList, new String[]{"systime"}, 1);
		dialogueList = CommonUtil.subList(dialogueList, curPage, XingyunCommonConstant.MESSAGE_PAGENUM);
		return setDialogueMessageInfoList(dialogueList);
	}
	/**
	 * 获取发件箱列表
	 * 使用索引：index_message_send_fromuserid_touserid_messagetype
	 */
	private List<Map<Object,Object>> getMessageSendList(String fromUserId, String toUserId, int messageType) throws Throwable{
		String sql = "SELECT id,fromuserid,touserid,content,systime,"+XingyunCommonConstant.MESSAGE_RESOURCE_SEND+" AS source FROM message_send WHERE fromuserid = ? AND touserid = ? AND messagetype = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		valueList.add(messageType);
		return db.retrieveSQL(sql, valueList);
	}
	/**
	 * 获取收件箱列表
	 * 使用索引：index_message_receive_fromuserid_touserid_messagetype
	 */
	private List<Map<Object,Object>> getMessageReceiveList(String fromUserId, String toUserId, int messageType) throws Throwable{
		String sql = "SELECT id,fromuserid,touserid,content,systime,"+XingyunCommonConstant.MESSAGE_RESOURCE_RECEIVE+" AS source FROM message_receive WHERE fromuserid = ? AND touserid = ? AND messagetype = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		valueList.add(messageType);
		return db.retrieveSQL(sql, valueList);
	}
	/**
	 * 获取发件箱总数
	 * 使用索引：index_message_send_fromuserid_touserid_messagetype
	 */
	public int getMessageSendCount(String fromUserId, String toUserId, int messageType) throws Throwable{
		String sql = "SELECT COUNT(*) FROM message_send WHERE fromuserid = ? AND touserid = ? AND messagetype = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		valueList.add(messageType);
		return db.getRecordCountSQL(sql, valueList);
	}
	/**
	 * 获取收件箱总数
	 * 使用索引：index_message_receive_fromuserid_touserid_messagetype
	 */
	public int getMessageReceiveCount(String fromUserId, String toUserId, int messageType) throws Throwable{
		String sql = "SELECT COUNT(*) FROM message_receive WHERE fromuserid = ? AND touserid = ? AND messagetype = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		valueList.add(messageType);
		return db.getRecordCountSQL(sql, valueList);
	}
	/**
	 * 整理云信对话列表详情
	 */
	private List<Map<Object,Object>> setDialogueMessageInfoList(List<Map<Object,Object>> list) throws Throwable{
		String toUserId;
		int source;
		Map<Object,Object> tmpMap = null;
		List<Map<Object,Object>> summaryList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> map : list){
			source = Integer.parseInt(map.get("source").toString());
			if(source == XingyunCommonConstant.MESSAGE_RESOURCE_SEND)
				toUserId = map.get("fromuserid").toString();
			else
				toUserId = map.get("touserid").toString();
			tmpMap = PublicQueryUtil.getInstance().findUserCommonMap(toUserId);
			if(tmpMap == null)
				continue;
			map.putAll(tmpMap);
			map.put("logourl", UploadPicUtil.getPicWebUrl(tmpMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50));
			map.put("systime", DateUtil.getBlogSystime((Date)map.get("systime")));
			summaryList.add(map);
		}
		return summaryList;
	}
	/**
	 * 删除云信组及忽略拉黑操作
	 */
	public String delMessageGroupInfo(String fromUserId, String toUserId, int messageType) throws Throwable{
		delMessageGroup(fromUserId, toUserId, messageType);
		return XingyunCommonConstant.RESPONSE_SUCCESS_STRING;
	}
	/**
	 * 删除云信组
	 */
	public void delMessageGroup(String fromUserId, String toUserId, int messageType) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("DELETE FROM message_send WHERE fromuserid='" + fromUserId +"' AND touserid='"+toUserId+"' AND messagetype = " + messageType);
		sqlList.add("DELETE FROM message_receive WHERE fromuserid='" + fromUserId +"' AND touserid='"+toUserId+"' AND messagetype = " + messageType);
		sqlList.add("DELETE FROM message_summary WHERE fromuserid='" + fromUserId +"' AND touserid='"+toUserId+"' AND messagetype = " + messageType);
		db.batchExecute(sqlList, true);
	}
	/**
	 * 标示单条云信组已读
	 */
	public void clearMessageDialogueNum(String fromUserId, String toUserId, int messageType) throws Throwable{
		String sql = "UPDATE message_summary SET newcount = 0 WHERE fromuserid = ? AND touserid = ? AND messagetype = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		valueList.add(messageType);
		db.updateData(sql, valueList);
	}
	/**
	 * 标示整页云信组已读
	 */
	public void clearMessageDialogueNum(String userId, List<String> toUserIds, int messageType) throws Throwable{
		for(String toUserId : toUserIds){
			clearMessageDialogueNum(userId, toUserId, messageType);
		}
	}

	/**
	 * 删除单条云信
	 * @param source  云信来源 0：发出 1：接收
	 */
	public String delMessageSingle(int messageId, int source, String fromUserId, String toUserId, int messageType) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		if(source == XingyunCommonConstant.MESSAGE_RESOURCE_SEND)
			sqlList.add("DELETE FROM message_send WHERE id = " + messageId);
		else
			sqlList.add("DELETE FROM message_receive WHERE id = " + messageId);
		int count = getMessageSendCount(fromUserId, toUserId, messageType) + getMessageReceiveCount(fromUserId, toUserId, messageType);
		if(count == 1) // 最后一条数据还没有删除，所以应该等于1
			sqlList.add("DELETE FROM message_summary WHERE fromuserid = '" + fromUserId + "' AND touserid = '" + toUserId + "' AND messagetype = " + messageType);
		else{
			Map<Object,Object> newMessageMap = getNewMessageMap(fromUserId, toUserId, source, messageId, messageType);
			sqlList.add("UPDATE message_summary SET content='"+newMessageMap.get("content")+"',totalcount="+(count - 1)+",systime='"+newMessageMap.get("systime")+"',source="+newMessageMap.get("type")+" WHERE fromuserid='"+fromUserId+"' AND touserid='"+toUserId+"' AND messagetype = " + messageType);
		}
		db.batchExecute(sqlList, true);
		return XingyunCommonConstant.RESPONSE_SUCCESS_STRING;
	}
	/**
	 * 获取最新发件或收件云信
	 * @param source  云信来源 0：发出 1：接收
	 * 使用索引：index_message_receive_fromuserid_touserid_messagetype  index_message_send_fromuserid_touserid_messagetype
	 */
	private Map<Object,Object> getNewMessageMap(String fromUserId, String toUserId, int source, int messageId, int messageType) throws Throwable{
		String sql;
		List<Map<Object,Object>> newMessageList = new ArrayList<Map<Object,Object>>();
		if(getMessageSendCount(fromUserId, toUserId, messageType) > 0){
			sql = "SELECT content,systime,"+XingyunCommonConstant.MESSAGE_RESOURCE_SEND+" AS type FROM message_send WHERE fromuserid = '" + fromUserId + "' AND touserid = '" + toUserId + "' AND messagetype = " + messageType;
			if(source == XingyunCommonConstant.MESSAGE_RESOURCE_SEND)
				sql += " AND id NOT IN ("+messageId+")";
			sql += " ORDER BY id DESC LIMIT 1";
			newMessageList = db.retrieveSQL(sql);
		}
		if(getMessageReceiveCount(fromUserId, toUserId, messageType) > 0){
			sql = "SELECT content,systime,"+XingyunCommonConstant.MESSAGE_RESOURCE_RECEIVE+" AS type FROM message_receive WHERE fromuserid = '" + fromUserId + "' AND touserid = '" + toUserId + "' AND messagetype = " + messageType;
			if(source == XingyunCommonConstant.MESSAGE_RESOURCE_RECEIVE)
				sql += " AND id NOT IN ("+messageId+")";
			sql += " ORDER BY id DESC LIMIT 1";
			newMessageList.addAll(db.retrieveSQL(sql));
		}
		
		CommonUtil.compositor(newMessageList, new String[]{"systime"}, 1);
		return newMessageList.get(0);
	}
	/**
	 * 获取用户昵称
	 */
	public String getUserNickName(String userId) throws Throwable{
		String sql = "SELECT nickname FROM user WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return StringUtils.EMPTY;
		return CommonUtil.getStringValue(list.get(0).get("nickname"));
	}
	
	/**
	 * 拼接用户消息json字符串
	 * @param userId 用户Id
	 */
	public String getNewMessageJson(User user) throws Throwable {
		if(StringUtils.EMPTY.equals(user.getUserId()))
			return "{\"status\":\"0\"}";	//status 状态码 0标示错误的用户Id
		
		Map<Object,Object> map = getNewMessageCount(user.getUserId());
		if(map == null)
			return "{\"status\":\"0\"}";	//status 状态码 0标示错误的用户Id
		
		int requestCount = CommonUtil.getIntValue(map.get("requestcount"));
		int yunxinCount = CommonUtil.getIntValue(map.get("yunxincount"));
		int bizyunxinCount = CommonUtil.getIntValue(map.get("bizyunxincount"));
		int fansCount = CommonUtil.getIntValue(map.get("fanscount"));
		int recommendCount = CommonUtil.getIntValue(map.get("recommendcount"));
		int noticeCount = CommonUtil.getIntValue(map.get("noticecount"));
		int commentCount = CommonUtil.getIntValue(map.get("commentcount"));
		int zanCount = CommonUtil.getIntValue(map.get("zancount"));
		if (requestCount + yunxinCount + bizyunxinCount + fansCount + recommendCount + noticeCount + commentCount + zanCount == 0)
			return "{\"status\":\"1\",\"userHref\":\"" + CommonUtil.getUserIndexHref(user.getUserId(), user.getWkey()) + "\"}"; // status 状态码 1标示请求成功,没有新消息
		
		Map<String, Object> msgMap = new HashMap<String, Object>();
		msgMap.put("status", "2");			//status 状态码 2标示请求成功,有新消息	
		msgMap.put("requestcount", requestCount);
		msgMap.put("yunxincount", yunxinCount);
		msgMap.put("bizyunxincount", bizyunxinCount);
		msgMap.put("fanscount", fansCount);
		msgMap.put("recommendcount", recommendCount);
		msgMap.put("noticecount", noticeCount);
		msgMap.put("commentcount", commentCount);
		msgMap.put("zancount", zanCount);
		msgMap.put("userHref", CommonUtil.getUserIndexHref(user.getUserId(), user.getWkey()));
		return JsonObjectUtil.getJsonStr(msgMap);
	}
	/**
	 * 根据用户ID查询用户消息记录
	 */
	@SuppressWarnings("unchecked")
	public Map<Object, Object> getNewMessageCount(String userID) throws Throwable{			
		String key = MessageUtil.getUserMessageKey(userID);
		Object userMsg = MClient.getInstance().get(key);
		if(userMsg == null)
			return findNewMessageCount(userID);
		return (Map<Object,Object>)userMsg;	
	}
	
	/**
	 * 根据用户ID查询用户消息记录
	 * 使用索引：index_message_all_userid
	 */
	private Map<Object,Object> findNewMessageCount(String userID) throws Throwable{
		String sql = "SELECT requestcount, yunxincount, fanscount, recommendcount, noticecount, commentcount, zancount, bizyunxincount FROM message_all WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userID);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);		
		if(list.size() == 0)
			return null;
		Map<Object,Object> map = list.get(0);
		setUserMessageMC(userID, map);		//将数据放入MC
		return map;
	}
	/**
	 * 将用户消息放到MC里
	 */
	private void setUserMessageMC(String userID, Map<Object,Object> map)throws Throwable{
		String key = MessageUtil.getUserMessageKey(userID);				
		MClient.getInstance().set(key, MemcachedConstant.MC_SAVE_TIME, map);
	}
	/**
	 * 获取最近联系的人用户id集合
	 * 使用索引： index_message_summary_fromuserid_messagetype_systime
	 */
	public List<Map<Object,Object>> getRecentContactUserIndexList(String userId, int messageType) throws Throwable{
		String sql = "SELECT touserid AS userid FROM message_summary WHERE fromuserid = ? AND messagetype = ? ORDER BY systime DESC LIMIT ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(messageType);
		valueList.add(XingyunCommonConstant.RECENT_CONTACT_SIZE);
		return db.retrieveSQL(sql, valueList);
	}
	/**
	 * 获取商业云信关系数据
	 * 使用索引：index_message_bizrelation_fromuserid_touserid
	 */
	public Map<Object,Object> getMessageBizMessageMap(String fromUserId, String toUserId) throws Throwable{
		String sql = "SELECT isopen, bizyunxincount FROM message_bizrelation WHERE fromuserid = ? AND touserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}

	/**
	 * 添加商业云信关系数据
	 */
	private void addMessageBizRelation(String fromUserId, String toUserId, int messageType, List<String> sqlList) throws Throwable{
		if(messageType == XingyunCommonConstant.MESSAGE_TYPE_DEFAULT)
			return;
		if(checkIsExistInBizRelation(fromUserId, toUserId))
			sqlList.add("UPDATE message_bizrelation SET bizyunxincount = bizyunxincount + 1 WHERE fromuserid = '"+fromUserId+"' AND touserid = '"+toUserId+"'");
		else
			sqlList.add("INSERT INTO message_bizrelation(fromuserid,touserid,isopen,bizyunxincount,systime) VALUES('"+fromUserId+"','"+toUserId+"',0,1,NOW())");
	}
	
	/**
	 * 检测商业云信关系表里是否有数据
	 * 使用索引：index_message_bizrelation_fromuserid_touserid
	 */
	private boolean checkIsExistInBizRelation(String fromUserId, String toUserId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM message_bizrelation WHERE fromuserid = ? AND touserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
	/**
	 * 标识商业云信标识已回复过
	 */
	public void updateMessageBizRelation(String fromUserId, String toUserId, int messageType) throws Throwable{
		if(messageType == XingyunCommonConstant.MESSAGE_TYPE_DEFAULT)
			return;
		Map<Object,Object> map = getMessageBizMessageMap(toUserId, fromUserId);
		if(map == null){
			setBizMessageListMc(fromUserId, toUserId);
			return;
		}
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("UPDATE message_bizrelation SET isopen = " + XingyunCommonConstant.MESSAGE_BIZRELATION_OPEN + " WHERE fromuserid = '"+toUserId+"' AND touserid = '"+fromUserId+"'");
		sqlList.add("UPDATE message_bizrelation SET isopen = " + XingyunCommonConstant.MESSAGE_BIZRELATION_OPEN + " WHERE fromuserid = '"+fromUserId+"' AND touserid = '"+toUserId+"'");
		db.batchExecute(sqlList, true);
	}
	
	private void setBizMessageListMc(String fromUserId, String toUserId) throws Throwable{
		List<String> toUserIdsList = getBizMessageListMc(fromUserId);
		if(toUserIdsList.size() >= XingyunCommonConstant.MESSAGE_BIZ_LIMTCOUNT)
			return;
		if(toUserIdsList.contains(toUserId))
			return;
		toUserIdsList.add(toUserId);
		MClient.getInstance().set(XingyunCommonConstant.MESSAGE_BIZ_MCKEY + fromUserId, getMcSaveTime(), toUserIdsList);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getBizMessageListMc(String userId) throws Throwable{
		Object value = MClient.getInstance().get(XingyunCommonConstant.MESSAGE_BIZ_MCKEY + userId);
		if(value == null)
			return new ArrayList<String>();
		return (List<String>) value;
		
	}
	
	public boolean checkUserInBizMessageListMc(String fromUserId, String toUserId) throws Throwable{
		List<String> toUserIdsList = getBizMessageListMc(fromUserId);
		if(toUserIdsList.size() == 0)
			return true;
		if(!toUserIdsList.contains(toUserId) && toUserIdsList.size() >= XingyunCommonConstant.MESSAGE_BIZ_LIMTCOUNT)
			return false;
		return true;
	}
	
	/**
	 * 返回现在时间到第二天零点时间差
	 */
	private int getMcSaveTime(){
		Calendar ca = Calendar.getInstance();
		long nowLong = ca.getTimeInMillis();
		ca.set(Calendar.HOUR_OF_DAY , 23);
		ca.set(Calendar.MINUTE, 59);
		long endLong = ca.getTimeInMillis();
		return (int) (endLong - nowLong) / 1000;
	}

}
