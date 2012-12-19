package com.xingyun.services.friend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunFaceConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.follow.FollowService;
import com.xingyun.services.header.UserHeaderService;
import com.xingyun.services.index.MyIndexService;
import com.xingyun.services.message.MessageService;
import com.xingyun.services.search.SearchService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.MessageUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.SpecialCharFilterUtil;
import com.xingyun.util.UploadPicUtil;

public class FriendService {
	private static final DBOperate db = new DBOperate();
	private static final FriendService friendService = new FriendService();
	private FriendService(){}
	public static FriendService getInstance(){
		return friendService;
	}

	/**
	 * 检测用户好友类型：0 无关系，1 双方都为好友， 2 一方已经删除了另一方，单方朋友关系
	 */
	public int checkFriendRelationType(String fromUserId, String toUserId) throws Throwable{
		if(StringUtils.EMPTY.equals(fromUserId))
			return XingyunCommonConstant.FRIEND_RELATION_NO;
		boolean isFriend = checkFriendRelation(fromUserId, toUserId);
		boolean isOtherFriend = checkFriendRelation(toUserId, fromUserId);
		//相互好友
		if(isFriend && isOtherFriend)
			return XingyunCommonConstant.FRIEND_RELATION_DOUBLE;
		//无好友关系
		if(!isFriend && !isOtherFriend)
			return XingyunCommonConstant.FRIEND_RELATION_NO;
		//单个好友，一方已删除另一方
		return XingyunCommonConstant.FRIEND_RELATION_SINGLE;
	}
	
	/**
	 * 检查是否可以发短信 
	 * 1、特殊账户不显示收发短信
	 * 2、商业付费用户给关注过ta的人 发短信不限制
	 * 3、双方好友关系不限制发短信
	 */
	public int checkFriendRelationTypeToMessage(String fromUserId, String toUserId) throws Throwable{
		if(StringUtils.EMPTY.equals(fromUserId))
			return XingyunCommonConstant.FRIEND_RELATION_NO;
		
		if(XingyunCommonConstant.USERID_MAYUE.equals(fromUserId) || XingyunCommonConstant.USERID_MAYUE.equals(toUserId)
				|| XingyunCommonConstant.USERID_FEIPENG.equals(fromUserId) || XingyunCommonConstant.USERID_FEIPENG.equals(toUserId))
			return XingyunCommonConstant.FRIEND_RELATION_DOUBLE; 
		
		//当前用户是否为商业付费会员
		if(UserHeaderService.getInstance().getIsPayUser(fromUserId)){
			//toUser是否已关注fromUser 
			if(PublicQueryUtil.getInstance().checkIsFollow(toUserId, fromUserId))
				return XingyunCommonConstant.FRIEND_RELATION_PAY_USER;
		}
		
		return checkFriendRelationType(fromUserId, toUserId);
	}
	
	/**
	 * 检测是不是好友
	 */
	public boolean checkFriendRelation(String fromUserId, String toUserId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM xy_friend_relation WHERE fromuserid = ? AND touserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
	/**
	 * 检测是否已经发过加好友请求
	 */
	public boolean checkIsExistFriendRequest(String fromUserId, String toUserId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM xy_friend_request WHERE fromuserid = ? AND touserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
	/**
	 * 添加好友请求信息
	 */
	public int addFriendRequestInfo(String fromUserId, String toUserId, String content) throws Throwable{
		int maxId = addFriendRequest(fromUserId, toUserId, content);
		if(maxId == 0)
			return 0;
		MessageUtil.addNewMessage(toUserId, "requestcount");
		return maxId;
	}
	/**
	 * 插入好友请求
	 */
	private int addFriendRequest(String fromUserId, String toUserId, String content) throws Throwable{
		String sql = "INSERT INTO xy_friend_request(fromuserid,touserid,content,systime) VALUES(?,?,?,?)";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(content, 255));
		valueList.add(new Date());
		return db.insertData(sql, valueList);
	}
	/**
	 * 通过星云号查询用户信息
	 */
	public Map<Object,Object> getUserInfoMapByXynumber(String xyNumber) throws Throwable{
		String userId = getUserIdByXynumber(xyNumber);
		if(StringUtils.EMPTY.equals(userId))
			return null;
		return setFriendInfoMap(userId, XingyunUploadFileConstant.LOGO_WIDTH_75);
		
	}
	
	/**
	 * 通过星云号查找用户id
	 */
	private String getUserIdByXynumber(String xyNumber) throws Throwable{
		String sql = "SELECT userid FROM user WHERE xynumber = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(xyNumber);
		return CommonUtil.getStringValue(sql, valueList, "userid");
	}
	
	/**
	 * 通过用户id查询用户信息
	 */
	public Map<Object,Object> setFriendInfoMap(String userId, int logoWidth) throws Throwable{
		Map<Object,Object> map = PublicQueryUtil.getInstance().findUserCommonMap(userId);
		if(map == null)
			return null;
		Map<Object,Object> tmpMap = FollowService.getInstance().getUserProfileMap(userId);
		if(tmpMap == null)
			return null;
		map.putAll(tmpMap);
		map.put("logourl", UploadPicUtil.getPicWebUrl(map.get("logourl").toString(), logoWidth));
		int isShowfollow = UserHeaderService.getInstance().checkIsShowfollow(userId);
		map.put("isShowfollow", isShowfollow == XingyunFaceConstant.FACE_MODULE_SHOW_YES);
		if(isShowfollow == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
			map.put("fanscount", FollowService.getInstance().getFansCount(userId));
		map.put("skillList", FollowService.getInstance().getSkillListByUserId(userId));
		map.put("recommendCount", UserHeaderService.getInstance().getRecommendUserCount(userId));
		return map;
	}
	
	/**
	 * 添加好友请求信息
	 */
	public void addFriendInfo(String fromUserId, String toUserId) throws Throwable{
		String dateTime = DateUtil.getSimpleDateFormat();
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("DELETE FROM xy_friend_relation WHERE fromuserid = '"+fromUserId+"' AND touserid = '"+toUserId+"'");
		sqlList.add("DELETE FROM xy_friend_relation WHERE fromuserid = '"+toUserId+"' AND touserid = '"+fromUserId+"'");
		sqlList.add("INSERT INTO xy_friend_relation(fromuserid,touserid,systime) VALUES('"+fromUserId+"','"+toUserId+"','"+dateTime+"')");
		sqlList.add("INSERT INTO xy_friend_relation(fromuserid,touserid,systime) VALUES('"+toUserId+"','"+fromUserId+"','"+dateTime+"')");
		sqlList.add("DELETE FROM xy_friend_request WHERE fromuserid = '"+toUserId+"' AND touserid = '"+fromUserId+"'");
		db.batchExecute(sqlList, true);
		MessageService.getInstance().addMessageInfo(fromUserId, toUserId, "我通过了你的好友验证请求，现在我们可以开始对话啦。", XingyunCommonConstant.MESSAGE_TYPE_DEFAULT);
	}
	/**
	 * 忽略好友请求
	 */
	public void ignoreFriendRequest(String fromUserId, String toUserId) throws Throwable{
		String sql = "DELETE FROM xy_friend_request WHERE fromuserid = ? AND touserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(toUserId);
		valueList.add(fromUserId);
		db.deleteData(sql, valueList);
	}
	/**
	 * 按页获取好友数据
	 */
	public List<Map<Object,Object>> getFriendList(String userId, int sort, int curPage) throws Throwable{
		List<Map<Object,Object>> requestIndexList = null;
		if(sort == XingyunCommonConstant.FRIEND_SORT_RECOMMEND)
			requestIndexList = getFriendIndexListByRecommend(userId, curPage);
		else if(sort == XingyunCommonConstant.FRIEND_SORT_FANS)
			requestIndexList = getFriendIndexListByFansCount(userId, curPage);
		else
			requestIndexList = getFriendIndexListByTime(userId, curPage);
		return setFriendListInfo(requestIndexList);
	}
	/**
	 * 取好友列表: 按时间
	 * 使用索引：index_friend_fromuserid
	 */
	private List<Map<Object,Object>> getFriendIndexListByTime(String userId, int curPage) throws Throwable{
		String sql = "SELECT touserid AS userid FROM xy_friend_relation WHERE fromuserid = ? ORDER BY id DESC LIMIT ?, ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add((curPage -1) * XingyunCommonConstant.FRIEND_PAGE_SIZE);
		valueList.add(XingyunCommonConstant.FRIEND_PAGE_SIZE);
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 取好友列表: 按推荐
	 * 使用索引：index_friend_fromuserid，index_user_counter_userid
	 */
	private List<Map<Object,Object>> getFriendIndexListByRecommend(String userId, int curPage) throws Throwable{
		String sql = "SELECT p.touserid AS userid ,pv.recommendcount FROM xy_friend_relation p, user_counter pv WHERE p.touserid = pv.userid AND p.fromuserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() > 0){
			String[] seqValue = {"recommendcount"};
			CommonUtil.compositor(list, seqValue, 1);
			list = CommonUtil.subList(list, curPage, XingyunCommonConstant.FRIEND_PAGE_SIZE);
		}
		return list;
	}
	
	/**
	 * 取好友列表: 按被收藏数
	 * 使用索引：index_friend_fromuserid，index_user_counter_userid
	 */
	private List<Map<Object,Object>> getFriendIndexListByFansCount(String userId, int curPage) throws Throwable{
		String sql = "SELECT fu.touserid AS userid, fc.fanscount FROM xy_friend_relation fu, user_counter fc WHERE fu.touserid = fc.userid AND fu.fromuserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() > 0){
			String[] seqValue = {"fanscount"};
			CommonUtil.compositor(list, seqValue, 1);
			list = CommonUtil.subList(list, curPage, XingyunCommonConstant.FRIEND_PAGE_SIZE);
		}
		return list;
	}

	/**
	 * 获取好友总数
	 * 使用索引：index_friend_fromuserid
	 */
	public int getFriendCount(String userId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM xy_friend_relation WHERE fromuserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return db.getRecordCountSQL(sql, valueList);
	}
	
	/**
	 * 按页获取好友请求数据
	 */
	public List<Map<Object,Object>> getFriendRequestList(String userId, int curPage) throws Throwable{
		List<Map<Object,Object>> requestIndexList = getFriendRequestIndexList(userId, curPage);
		return setFriendListInfo(requestIndexList);
	}
	/**
	 * 取用户加好友请求
	 */
	private List<Map<Object,Object>> getFriendRequestIndexList(String userId, int curPage) throws Throwable{
		String sql = "SELECT fromuserid AS userid, content FROM xy_friend_request WHERE touserid = ? ORDER BY id DESC LIMIT ?, ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add((curPage -1) * XingyunCommonConstant.FRIEND_PAGE_SIZE);
		valueList.add(XingyunCommonConstant.FRIEND_PAGE_SIZE);
		return db.retrieveSQL(sql, valueList);
	}
	/**
	 * 整理list列表中的用户信息
	 */
	private List<Map<Object,Object>> setFriendListInfo(List<Map<Object,Object>> list) throws Throwable{
		List<Map<Object,Object>> friendList = new ArrayList<Map<Object,Object>>();
		String userId;
		Map<Object,Object> tmpMap = null;
		for(Map<Object,Object> map : list){
			userId = map.get("userid").toString();
			tmpMap = setFriendInfoMap(userId, XingyunUploadFileConstant.LOGO_WIDTH_50);
			if(tmpMap == null)
				continue;
			map.putAll(tmpMap);
			friendList.add(map);
		}
		return friendList;
	}
	/**
	 * 获取加好友请求总数
	 */
	public int getFriendRequestCount(String userId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM xy_friend_request WHERE touserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return db.getRecordCountSQL(sql, valueList);
	}
	
	/**
	 * 删除好友
	 */
	public void delFriend(String fromUserId, String toUserId) throws Throwable{
		String sql = "DELETE FROM xy_friend_relation WHERE fromuserid = ? AND touserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		db.deleteData(sql, valueList);
	}
	
	/**
	 * 获取最近联系的人
	 */
	public List<Map<Object,Object>> getRecentContactUserList(String userId) throws Throwable{
		List<Map<Object,Object>> userIndexList = MessageService.getInstance().getRecentContactUserIndexList(userId, XingyunCommonConstant.MESSAGE_TYPE_DEFAULT);
		if(userIndexList.size() == 0)
			return null;
		return MyIndexService.getInstance().setNewUserList(userIndexList);
	}
	
	/**
	 * 根据昵称查询 好友索引信息
	 */
	public List<Map<Object, Object>> findFriendByNickNameIndexList(String userID, String nickName, int sort) throws Throwable{
		nickName = SearchService.getInstance().replaceSearchContent(nickName);	//替换搜索内容特殊字符
		if(StringUtils.isBlank(nickName))
			return null;
		
		boolean isChinese = CommonUtil.isChinese(nickName);
		String sql = "SELECT u.userid, x.id AS fid, uc.recommendcount, uc.fanscount FROM xy_friend_relation x, user u, user_counter uc WHERE x.touserid = u.userid AND x.touserid = uc.userid AND x.fromuserid = ?";
		if(isChinese)
			sql += " AND u.nickname LIKE '%" + nickName + "%'";
		else
			sql += " AND u.pinyinname LIKE '%" + nickName + "%'";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		List<Map<Object, Object>> indexList = db.retrieveSQL(sql, vList);
		
		String[] seqValue = {"fid"};
		if(sort == XingyunCommonConstant.FRIEND_SORT_RECOMMEND)
			seqValue[0] = "recommendcount";
		else if(sort == XingyunCommonConstant.FRIEND_SORT_FANS)
			seqValue[0] = "fanscount";
		CommonUtil.compositor(indexList, seqValue, 1);
		return indexList;
	}
	
	/**
	 * 按昵称搜索好友
	 */
	public List<Map<Object,Object>> getFriendListByNickname(List<Map<Object, Object>> friendIndexList, int pageIndex) throws Throwable{
		//整理分页索引数据
		friendIndexList = CommonUtil.subList(friendIndexList, pageIndex, XingyunCommonConstant.FRIEND_PAGE_SIZE);
		if(friendIndexList == null || friendIndexList.size() == 0)
			return null;
		return setFriendListInfo(friendIndexList);
	}
	
	/**
	 * 按好友userID  显示好友信息
	 */
	public List<Map<Object,Object>> getFriendByUserID(String fromUserID, String toUserID) throws Throwable{
		List<Map<Object,Object>> friendList = findFriendByUserID(fromUserID, toUserID);
		if(friendList == null || friendList.size() == 0)
			return null;
		return setFriendListInfo(friendList);
	}
	
	/**
	 * 按昵称搜索好友提示数据
	 */
	public String getFriendListByNicknameTishi(String userID, String nickName) throws Throwable{
		List<Map<Object, Object>> friendTiShiList = findFriendByNickNameTishiList(userID, nickName);
		return SearchService.getInstance().getSearchUserTishiJson(nickName, friendTiShiList);
	}
	
	/**
	 * 根据昵称查询 好友提示信息
	 */
	public List<Map<Object, Object>> findFriendByNickNameTishiList(String userID, String nickName) throws Throwable{
		nickName = SearchService.getInstance().replaceSearchContent(nickName);	//替换搜索内容特殊字符
		if(StringUtils.isBlank(nickName))
			return null;
		
		boolean isChinese = CommonUtil.isChinese(nickName);
		String sql = "SELECT u.userid, u.nickname, u.pinyinname FROM xy_friend_relation x, user u WHERE x.touserid = u.userid AND x.fromuserid = ? AND u.nickname LIKE '%" + nickName + "%'";
		if(!isChinese)
			sql = "SELECT u.userid, u.nickname, u.pinyinname FROM xy_friend_relation x, user u WHERE x.touserid = u.userid AND x.fromuserid = ? AND u.pinyinname LIKE '%" + nickName + "%'";
		
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 根据好友userID 查询好友信息
	 */
	private List<Map<Object, Object>> findFriendByUserID(String fromUserID, String toUserID) throws Throwable{
		String sql = "SELECT userid FROM xy_friend_relation x, user u WHERE x.touserid = u.userid AND x.fromuserid = ? AND x.touserid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(fromUserID);
		vList.add(toUserID);
		return db.retrieveSQL(sql, vList);
	}
}
