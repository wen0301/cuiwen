package com.xingyun.services.follow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xingyun.bean.User;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunFaceConstant;
import com.xingyun.constant.XingyunSystemConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.header.UserHeaderService;
import com.xingyun.services.search.SearchService;
import com.xingyun.util.AreaUtil;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.MessageUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.UploadPicUtil;

public class FollowService {
	private static final DBOperate db = new DBOperate();
	private static final FollowService followService = new FollowService();
	private FollowService(){}
	public static FollowService getInstance(){
		return followService;
	}
	
	/**
	 * 处理添加关注
	 */
	public String addFollowInfo(String fromUserId, String toUserId) throws Throwable{
		if(StringUtils.EMPTY.equals(toUserId) || fromUserId.equals(toUserId))
			return XingyunCommonConstant.RESPONSE_ERR_STRING;
		if(!PublicQueryUtil.getInstance().checkUserIsExist(toUserId))
			return XingyunCommonConstant.RESPONSE_ERR_STRING;
		if(PublicQueryUtil.getInstance().checkIsFollow(fromUserId, toUserId))//已关注过了
			return XingyunCommonConstant.RESPONSE_ERR_STRING;
		List<String> sqlList = new ArrayList<String>();
		PublicQueryUtil.getInstance().addFollow(fromUserId, toUserId, sqlList);
		db.batchExecute(sqlList, true);
		MessageUtil.addNewMessage(toUserId, "fanscount");
		return XingyunCommonConstant.RESPONSE_SUCCESS_STRING;
	}
	
	

	/**
	 * 检测是否相互关注
	 * 使用索引： index_follow_double_fromuserid_touserid
	 */
	public boolean checkIsFollowDouble(String fromUserId,String toUserId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM follow_double WHERE fromuserid = ? AND touserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
	
	/**
	 * 取消关注
	 */
	public String cancleFollowInfo(String fromUserId,String toUserId) throws Throwable{
		if(!PublicQueryUtil.getInstance().checkIsFollow(fromUserId, toUserId))
			return XingyunCommonConstant.RESPONSE_ERR_STRING;
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("DELETE FROM follow_user WHERE fromuserid = '" + fromUserId + "' AND touserid = '" + toUserId +"'");
		sqlList.add("UPDATE user_counter SET followcount = followcount - 1 WHERE userid = '" + fromUserId +"'");
		sqlList.add("UPDATE user_counter SET fanscount = fanscount - 1 WHERE userid = '" + toUserId +"'");
		if(checkIsFollowDouble(fromUserId, toUserId)){
			sqlList.add("DELETE FROM follow_double WHERE fromuserid = '" + fromUserId + "' AND touserid = '" + toUserId +"'");
			sqlList.add("UPDATE user_counter SET bifollowcount = bifollowcount - 1 WHERE userid = '" + fromUserId +"'");
		}
		if(checkIsFollowDouble(toUserId, fromUserId)){
			sqlList.add("DELETE FROM follow_double WHERE fromuserid = '" + toUserId + "' AND touserid = '" + fromUserId + "'");
			sqlList.add("UPDATE user_counter SET bifollowcount = bifollowcount - 1 WHERE userid = '" + toUserId +"'");
		}
		db.batchExecute(sqlList, true);
		return XingyunCommonConstant.RESPONSE_SUCCESS_STRING;
	}
	
	/**
	 * 关注类型按钮 (没有关系,双向关注,已关注,我的粉丝)
	 */
	public int checkFollowType(String fromUserID, String toUserID) throws Throwable{
		if(StringUtils.EMPTY.equals(fromUserID))
			return XingyunCommonConstant.FOLLOW_TYPE_NO;	  //未关注
		if(checkIsFollowDouble(fromUserID, toUserID))
			return XingyunCommonConstant.FOLLOW_TYPE_DOUBLE;  //相互关注
		boolean fromUserIsFollow = PublicQueryUtil.getInstance().checkIsFollow(fromUserID, toUserID);
		if(fromUserIsFollow)
			return XingyunCommonConstant.FOLLOW_TYPE_SINGLE;  //已关注
		boolean toUserIsFollow = PublicQueryUtil.getInstance().checkIsFollow(toUserID, fromUserID);
		if(toUserIsFollow)
			return XingyunCommonConstant.FOLLOW_TYPE_FANS;    //我的粉丝
		return XingyunCommonConstant.FOLLOW_TYPE_NO;	      //未关注
	}
	
	/**
	 * 查找粉丝总数
	 */
	public int getFansCount(String userId) throws Throwable{
		String sql = "SELECT fanscount FROM user_counter WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return CommonUtil.getIntValue(sql, valueList, "fanscount");
	}
	
	/**
	 * 查找我关注的人总数
	 */
	public int getFollowCount(String userId) throws Throwable{
		String sql = "SELECT followcount FROM user_counter WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return CommonUtil.getIntValue(sql, valueList, "followcount");
	}
	
	/**
	 * 查找相互关注总数
	 */
	public int getBiFollowCount(String userId) throws Throwable{
		String sql = "SELECT bifollowcount FROM user_counter WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return CommonUtil.getIntValue(sql, valueList, "bifollowcount");
	}
	
	/**
	 * 查找我关注的人和粉丝总数
	 */
	public Map<Object,Object> getFollowCountMap(String userId) throws Throwable{
		String sql = "SELECT followcount, fanscount FROM user_counter WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0){
			Map<Object,Object> map = new HashMap<Object, Object>();
			map.put("followcount", 0);
			map.put("fanscount", 0);
			return map;
		}
		return list.get(0);
	}
	
	public List<Map<Object,Object>> getFollowUserList(User user, String fromUserId, int sort, int curPage) throws Throwable{
		List<Map<Object,Object>> followIndexList = getFollowUserIndexList(fromUserId, sort, curPage);
		return setFollowUserInfo(user, followIndexList, 0);
	}
	
	public List<Map<Object,Object>> getFollowDoubleList(User user, String fromUserId, int sort, int curPage) throws Throwable{
		List<Map<Object,Object>> followIndexList = getFollowDoubleIndexList(fromUserId, sort, curPage);
		return setFollowUserInfo(user, followIndexList, 3);
	}
	
	/**
	 * 使用索引：index_follow_user_fromuserid
	 */
	private List<Map<Object,Object>> getFollowUserIndexList(String userId, int sort, int curPage) throws Throwable{
		if(sort == XingyunCommonConstant.FOLLOW_SORT_FANS){
			String sql = "SELECT fu.id, fu.touserid, fc.fanscount FROM follow_user fu, user_counter fc WHERE fu.touserid = fc.userid AND fu.fromuserid = ?";
			List<Object> valueList = new ArrayList<Object>();
			valueList.add(userId);
			List<Map<Object, Object>> list = db.retrieveSQL(sql, valueList);
			if(list.size() > 0){
				String[] seqValue = {"fanscount"};
				CommonUtil.compositor(list, seqValue, 1);
				list = CommonUtil.subList(list, curPage, XingyunCommonConstant.FOLLOW_PAGE_SIZE);
			}
			return list;
		}
		String sql = "SELECT id, touserid FROM follow_user WHERE fromuserid = ? ORDER BY id DESC LIMIT ?,?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add((curPage -1) * XingyunCommonConstant.FOLLOW_PAGE_SIZE);
		valueList.add(XingyunCommonConstant.FOLLOW_PAGE_SIZE);
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 使用索引：index_follow_double_fromuserid
	 */
	private List<Map<Object,Object>> getFollowDoubleIndexList(String userId, int sort, int curPage) throws Throwable{
		if(sort == XingyunCommonConstant.FOLLOW_SORT_FANS){
			String sql = "SELECT fu.id, fu.touserid, fc.bifollowcount, fc.fanscount FROM follow_double fu, user_counter fc WHERE fu.touserid = fc.userid AND fu.fromuserid = ?";
			List<Object> valueList = new ArrayList<Object>();
			valueList.add(userId);
			List<Map<Object, Object>> list = db.retrieveSQL(sql, valueList);
			if(list.size() > 0){
				String[] seqValue = {"fanscount"};
				CommonUtil.compositor(list, seqValue, 1);
				list = CommonUtil.subList(list, curPage, XingyunCommonConstant.FOLLOW_PAGE_SIZE);
			}
			return list;
		}
		String sql = "SELECT id, touserid FROM follow_double WHERE fromuserid = ? ORDER BY id DESC LIMIT ?,?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add((curPage -1) * XingyunCommonConstant.FOLLOW_PAGE_SIZE);
		valueList.add(XingyunCommonConstant.FOLLOW_PAGE_SIZE);
		return db.retrieveSQL(sql, valueList);
	}
	
	public List<Map<Object,Object>> getFansList(User user, String followId, int sort, int curPage) throws Throwable{
		List<Map<Object,Object>> followIndexList = getFansIndexList(followId, sort, curPage);
		return setFollowUserInfo(user, followIndexList, 1);
	}
	
	/**
	 * 使用索引：index_follow_fans_touserid
	 */
	private List<Map<Object,Object>> getFansIndexList(String userId, int sort, int curPage) throws Throwable{
		if(sort == XingyunCommonConstant.FOLLOW_SORT_FANS){
			String sql = "SELECT fu.id, fu.fromuserid AS touserid, fc.fanscount FROM follow_user fu, user_counter fc WHERE fu.fromuserid = fc.userid AND fu.touserid = ?";
			List<Object> valueList = new ArrayList<Object>();
			valueList.add(userId);
			List<Map<Object, Object>> list = db.retrieveSQL(sql, valueList);
			if(list.size() > 0){
				String[] seqValue = {"fanscount"};
				CommonUtil.compositor(list, seqValue, 1);
				list = CommonUtil.subList(list, curPage, XingyunCommonConstant.FOLLOW_PAGE_SIZE);
			}
			return list;
		}else{
			String sql = "SELECT id, fromuserid AS touserid FROM follow_user WHERE touserid = ? ORDER BY id DESC LIMIT ?,?";
			List<Object> valueList = new ArrayList<Object>();
			valueList.add(userId);
			valueList.add((curPage -1) * XingyunCommonConstant.FOLLOW_PAGE_SIZE);
			valueList.add(XingyunCommonConstant.FOLLOW_PAGE_SIZE);
			return db.retrieveSQL(sql, valueList);
		}
	}
	/**
	 * 整理关注列表里的用户信息
	 * @param type    0:我关注的人 1：关注我的人 2: 相互关注
	 */
	private List<Map<Object,Object>> setFollowUserInfo(User user, List<Map<Object,Object>> list, int type) throws Throwable{
		String toUserId;
		int isShowfollow = 0;
		Map<Object,Object> tmpMap = null;
		List<Map<Object,Object>> followList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> map : list){
			toUserId = map.get("touserid").toString();
			if(XingyunSystemConstant.getSystemConstantValue(XingyunSystemConstant.XINGYUN_UID).equals(toUserId) && type == 0)
				continue;
			tmpMap = PublicQueryUtil.getInstance().findUserCommonMap(toUserId);
			if(tmpMap == null)
				continue;
			map.putAll(tmpMap);
			tmpMap = getUserProfileMap(toUserId);
			if(tmpMap == null)
				continue;
			map.putAll(tmpMap);
			map.put("logourl", UploadPicUtil.getPicWebUrl(map.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50));
			isShowfollow = UserHeaderService.getInstance().checkIsShowfollow(toUserId);
			map.put("isShowfollow", isShowfollow == XingyunFaceConstant.FACE_MODULE_SHOW_YES);
			if(isShowfollow == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
				map.put("fanscount", getFansCount(toUserId));
			map.put("skillList", getSkillListByUserId(toUserId));
			map.put("recommendCount", UserHeaderService.getInstance().getRecommendUserCount(toUserId));
			map.put("isXingyunUID", CommonUtil.checkIsXingyunUID(toUserId));
			map.put("lookState", CommonUtil.getLookState(user, toUserId));
			if(user != null && !user.getUserId().equals(toUserId))
				map.put("followType", checkFollowType(user.getUserId(), toUserId));
			followList.add(map);
		}
		return followList;
	}
	
	/**
	 * 
	 * 使用索引：index_user_profile_userid
	 */
	public Map<Object,Object> getUserProfileMap(String userId) throws Throwable{
		String sql = "SELECT gender,provinceid,cityid FROM user_profile WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList, true);
		if(list.size() == 0)
			return null;
		Map<Object,Object> map = list.get(0);
		map.put("provinceName",AreaUtil.getInstance().findAreaNameById(Integer.parseInt(map.get("provinceid").toString())));
		map.put("cityName",AreaUtil.getInstance().findAreaNameById(Integer.parseInt(map.get("cityid").toString())));
		return map;
	}
	
	/**
	 * 
	 * 使用索引：index_user_vocation_userid
	 */
	public List<Map<Object,Object>> getSkillListByUserId(String userId) throws Throwable{
		String sql = "SELECT dv2.name FROM user_vocation uv, dic_skill dv2 WHERE uv.skillid = dv2.id AND uv.userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return db.retrieveSQL(sql, valueList);
	}
	/**
	 * 检测用户是否能够查看关注人列表
	 */
	public boolean checkIsShowFollow(User user, String toUserId) throws Throwable{
		if(user != null && user.getUserId().equals(toUserId))
			return true;
		return UserHeaderService.getInstance().checkIsShowfollow(toUserId) == XingyunFaceConstant.FACE_MODULE_SHOW_YES;
	}
	
	/**
	 * 获取TA
	 */
	public String getTA(User user, String userId, int gender) throws Throwable{
		String TA = "他";
		if(user != null && user.getUserId().equals(userId))
			TA = "我";
		else if(gender == XingyunCommonConstant.GENDER_GIRL)
			TA = "她";
		return TA;
	}
	
	/**
	 * 搜索我关注的人索引数据
	 */
	public List<Map<Object, Object>> findFollowUserByNickNameIndexList(String userID, String searchNickName, int sort) throws Throwable{
		searchNickName = SearchService.getInstance().replaceSearchContent(searchNickName);	//替换搜索内容特殊字符
		if(StringUtils.isBlank(searchNickName))
			return null;

		boolean isChinese = CommonUtil.isChinese(searchNickName);
		String sql = "SELECT u.userid AS touserid, f.id AS fid, uc.followcount, uc.fanscount FROM follow_user f, user u, user_counter uc WHERE f.touserid = u.userid AND f.touserid = uc.userid AND f.fromuserid = ?";
		if(isChinese)
			sql += " AND u.nickname LIKE '%" + searchNickName + "%'";
		else
			sql += " AND u.pinyinname LIKE '%" + searchNickName + "%'";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		List<Map<Object, Object>> indexList = db.retrieveSQL(sql, vList);
		
		String[] seqValue = {"fid"};
		if(sort == XingyunCommonConstant.FOLLOW_SORT_FANS)
			seqValue[0] = "fanscount";
		CommonUtil.compositor(indexList, seqValue, 1);
		return indexList;
	}
	
	/**
	 * 搜索关注我的人索引数据
	 */
	public List<Map<Object, Object>> findFollowFansByNickNameIndexList(String userID, String searchNickName, int sort) throws Throwable{
		searchNickName = SearchService.getInstance().replaceSearchContent(searchNickName);	//替换搜索内容特殊字符
		if(StringUtils.isBlank(searchNickName))
			return null;
		
		boolean isChinese = CommonUtil.isChinese(searchNickName);
		String sql = "SELECT u.userid AS touserid, f.id AS fid, uc.followcount, uc.fanscount FROM follow_user f, user u, user_counter uc WHERE f.fromuserid = u.userid AND f.fromuserid = uc.userid AND f.touserid = ?";
		if(isChinese)
			sql += " AND u.nickname LIKE '%" + searchNickName + "%'";
		else
			sql += " AND u.pinyinname LIKE '%" + searchNickName + "%'";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		List<Map<Object, Object>> indexList = db.retrieveSQL(sql, vList);
		
		String[] seqValue = {"fid"};
		if(sort == XingyunCommonConstant.FOLLOW_SORT_FANS)
			seqValue[0] = "fanscount";
		CommonUtil.compositor(indexList, seqValue, 1);
		return indexList;
	}
	
	/**
	 * 搜索相互关注的人索引数据
	 */
	public List<Map<Object, Object>> findFollowDoubleByNickNameIndexList(String userID, String searchNickName, int sort) throws Throwable{
		searchNickName = SearchService.getInstance().replaceSearchContent(searchNickName);	//替换搜索内容特殊字符
		if(StringUtils.isBlank(searchNickName))
			return null;
		
		boolean isChinese = CommonUtil.isChinese(searchNickName);
		String sql = "SELECT u.userid AS touserid, f.id AS fid, uc.followcount, uc.fanscount FROM follow_double f, user u, user_counter uc WHERE f.touserid = u.userid AND f.touserid = uc.userid AND f.fromuserid = ?";
		if(isChinese)
			sql += " AND u.nickname LIKE '%" + searchNickName + "%'";
		else
			sql += " AND u.pinyinname LIKE '%" + searchNickName + "%'";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		List<Map<Object, Object>> indexList = db.retrieveSQL(sql, vList);
		
		String[] seqValue = {"fid"};
		if(sort == XingyunCommonConstant.FOLLOW_SORT_FANS)
			seqValue[0] = "fanscount";
		CommonUtil.compositor(indexList, seqValue, 1);
		return indexList;
	}
	
	/**
	 * 按昵称搜索 关注人 数据
	 */
	public List<Map<Object,Object>> getFollowListByNickname(User user, List<Map<Object, Object>> followIndexList, int pageIndex, int type) throws Throwable{
		//整理分页索引数据
		followIndexList = CommonUtil.subList(followIndexList, pageIndex, XingyunCommonConstant.FOLLOW_PAGE_SIZE);
		if(followIndexList == null || followIndexList.size() == 0)
			return null;
		return setFollowUserInfo(user, followIndexList, type);
	}
	
	/**
	 * 按昵称搜索 我关注的人提示信息
	 */
	public String getFollowUserByNicknameTishi(String userID, String searchNickName) throws Throwable{
		List<Map<Object, Object>> followTiShiList = getFollowUserByNicknameTishiList(userID, searchNickName);
		return SearchService.getInstance().getSearchUserTishiJson(searchNickName, followTiShiList);
	}
	
	/**
	 * 根据昵称查询 我关注的人提示信息
	 */
	public List<Map<Object, Object>> getFollowUserByNicknameTishiList(String userID, String nickName) throws Throwable{
		nickName = SearchService.getInstance().replaceSearchContent(nickName);	//替换搜索内容特殊字符
		if(StringUtils.isBlank(nickName))
			return null;
		
		boolean isChinese = CommonUtil.isChinese(nickName);
		String sql = "SELECT u.userid, u.nickname, u.pinyinname FROM follow_user f, user u WHERE f.touserid = u.userid AND f.fromuserid = ?";
		if(isChinese)
			sql += " AND u.nickname LIKE '%" + nickName + "%'";
		else
			sql += " AND u.pinyinname LIKE '%" + nickName + "%'";
		
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 按昵称搜索 关注我的人提示信息
	 */
	public String getFollowFansByNicknameTishi(String userID, String searchNickName) throws Throwable{
		List<Map<Object, Object>> followTiShiList = getFollowFansByNicknameTishiList(userID, searchNickName);
		return SearchService.getInstance().getSearchUserTishiJson(searchNickName, followTiShiList);
	}
	
	/**
	 * 根据昵称查询 关注我的人提示信息
	 */
	public List<Map<Object, Object>> getFollowFansByNicknameTishiList(String userID, String nickName) throws Throwable{
		nickName = SearchService.getInstance().replaceSearchContent(nickName);	//替换搜索内容特殊字符
		if(StringUtils.isBlank(nickName))
			return null;
		
		boolean isChinese = CommonUtil.isChinese(nickName);
		String sql = "SELECT u.userid, u.nickname, u.pinyinname FROM follow_user f, user u WHERE f.fromuserid = u.userid AND f.touserid = ?";
		if(isChinese)
			sql += " AND u.nickname LIKE '%" + nickName + "%'";
		else
			sql += " AND u.pinyinname LIKE '%" + nickName + "%'";
		
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 按昵称搜索 相互关注的人提示信息
	 */
	public String getFollowDoubleByNicknameTishi(String userID, String searchNickName) throws Throwable{
		List<Map<Object, Object>> followTiShiList = getFollowDoubleByNicknameTishiList(userID, searchNickName);
		return SearchService.getInstance().getSearchUserTishiJson(searchNickName, followTiShiList);
	}
	
	/**
	 * 根据昵称查询 相互关注的人提示信息
	 */
	public List<Map<Object, Object>> getFollowDoubleByNicknameTishiList(String userID, String nickName) throws Throwable{
		nickName = SearchService.getInstance().replaceSearchContent(nickName);	//替换搜索内容特殊字符
		if(StringUtils.isBlank(nickName))
			return null;
		
		boolean isChinese = CommonUtil.isChinese(nickName);
		String sql = "SELECT u.userid, u.nickname, u.pinyinname FROM follow_double f, user u WHERE f.touserid = u.userid AND f.fromuserid = ?";
		if(isChinese)
			sql += " AND u.nickname LIKE '%" + nickName + "%'";
		else
			sql += " AND u.pinyinname LIKE '%" + nickName + "%'";
		
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 按用户userID  显示关注用户信息
	 */
	public List<Map<Object,Object>> getFollowUserByUserID(User user, String fromUserID, String toUserID) throws Throwable{
		List<Map<Object,Object>> followList = findFollowUserByUserID(fromUserID, toUserID);
		if(followList == null || followList.size() == 0)
			return null;
		return setFollowUserInfo(user, followList, 0);
	}
	
	/**
	 * 根据用户userID 查询关注用户信息
	 */
	private List<Map<Object, Object>> findFollowUserByUserID(String fromUserID, String toUserID) throws Throwable{
		String sql = "SELECT u.userid AS touserid FROM follow_user f, user u WHERE f.touserid = u.userid AND f.fromuserid = ? AND f.touserid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(fromUserID);
		vList.add(toUserID);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 按用户userID  显示关注用户信息
	 */
	public List<Map<Object,Object>> getFollowFansByUserID(User user, String fromUserID, String toUserID) throws Throwable{
		List<Map<Object,Object>> followList = findFollowFansByUserID(fromUserID, toUserID);
		if(followList == null || followList.size() == 0)
			return null;
		return setFollowUserInfo(user, followList, 0);
	}
	
	/**
	 * 根据用户userID 查询关注用户信息
	 */
	private List<Map<Object, Object>> findFollowFansByUserID(String fromUserID, String toUserID) throws Throwable{
		String sql = "SELECT u.userid AS touserid FROM follow_user f, user u WHERE f.fromuserid = u.userid AND f.fromuserid = ? AND f.touserid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(fromUserID);
		vList.add(toUserID);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 按用户userID  显示相互关注用户信息
	 */
	public List<Map<Object,Object>> getFollowDoubleByUserID(User user, String fromUserID, String toUserID) throws Throwable{
		List<Map<Object,Object>> followList = findFollowDoubleByUserID(fromUserID, toUserID);
		if(followList == null || followList.size() == 0)
			return null;
		return setFollowUserInfo(user, followList, 0);
	}
	
	/**
	 * 根据用户userID 查询相互关注用户信息
	 */
	private List<Map<Object, Object>> findFollowDoubleByUserID(String fromUserID, String toUserID) throws Throwable{
		String sql = "SELECT u.userid AS touserid FROM follow_double f, user u WHERE f.touserid = u.userid AND f.fromuserid = ? AND f.touserid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(fromUserID);
		vList.add(toUserID);
		return db.retrieveSQL(sql, vList);
	}
}
