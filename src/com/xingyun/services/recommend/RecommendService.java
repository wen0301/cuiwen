package com.xingyun.services.recommend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunPostConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.post.PostService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.MessageUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.SpecialCharFilterUtil;
import com.xingyun.util.UploadPicUtil;

public class RecommendService {

	private static final DBOperate db = new DBOperate();
	private static final RecommendService recommendService = new RecommendService();
	private RecommendService(){}
	public static RecommendService getInstance() {
		return recommendService;
	}
	
	/**
	 * 根据用户ID查询用户推荐过的作品总数
	 * 使用索引：index_recommend_userid_topicid
	 */
	public int findRecommendPostCount(String userID) throws Throwable{
		String sql = "SELECT COUNT(*) FROM recommend r, post p WHERE r.topicid = p.id AND r.type = ? AND r.userid = ? AND p.isdel = ? AND p.status = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunCommonConstant.RECOMMEND_TYPE_POST);
		vList.add(userID);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		vList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		return db.getRecordCountSQL(sql, vList);
	}
	
	/**
	 * 获取用户推荐作品数据
	 */
	public List<Map<Object, Object>> getShowRecommendPostList(String userID, int pageIndex, int pageSize) throws Throwable{
		List<Map<Object, Object>> postList = findRecommendPostList(userID, pageIndex, pageSize);
		if(postList.size() == 0)
			return null;
		String postUserID = "";
		int postId;
		Map<Object, Object> userMap = null;
		for(Map<Object, Object> postMap : postList){
			postUserID = postMap.get("userid").toString();
			postId = Integer.parseInt(postMap.get("id").toString() );
			userMap = PublicQueryUtil.getInstance().findUserCommonMap(postUserID);
			if(userMap == null)
				continue;
			
			postMap.put("coverPic", UploadPicUtil.getPicWebUrl(postMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_190));
			postMap.put("nickName", userMap.get("nickname").toString());
			postMap.put("userHref", userMap.get("userHref").toString());
			postMap.put("level", Integer.parseInt(userMap.get("lid").toString()));
			postMap.put("verified", userMap.get("verified"));
			postMap.put("zpViewcount", PostService.getInstance().getPostViewCount(postId ) );
			postMap.put("zpRecommendCount", RecommendService.getInstance().getPostRecommendCount(postId ) );
		}
		return postList;
	}
	
	/**
	 * 查询用户推荐作品数据
	 * @param userID		用户ID
	 * @param pageIndex		页码
	 * @param pageSize		每页显示数量
	 * 使用索引：index_recommend_userid_topicid
	 */
	private List<Map<Object, Object>> findRecommendPostList(String userID, int pageIndex, int pageSize) throws Throwable{
		String sql = "SELECT r.id AS rid, r.userid AS recommenduserid, r.reason AS reason, p.id, p.userid, p.posttype, p.title, p.coverpath FROM recommend r, post p WHERE r.topicid = p.id AND r.type = ? AND r.userid = ? AND p.isdel = ? AND p.status = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunCommonConstant.RECOMMEND_TYPE_POST);
		vList.add(userID);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		vList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, vList);
		if(list.size() > 0){
			String[] seqValue = {"rid"};
			CommonUtil.compositor(list, seqValue, 1);
			list = CommonUtil.subList(list, pageIndex, pageSize);
		}
		return list;
	}
	
	/**
	 * 根据用户ID 推荐类型 主题ID 查询推荐表ID
	 * 使用索引：index_recommend_userid_topicid
	 */
	private int findRecommendID(String userID, String topicID, int type) throws Throwable{
		String sql = "SELECT id FROM recommend WHERE userid = ? AND topicid = ? AND type = ? ";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(topicID);
		vList.add(type);
		return CommonUtil.getIntValue(sql, vList, "id");
	}
	
	/**
	 * 根据用户ID查询 用户推荐过的用户总数
	 * 使用索引：index_recommend_userid_topicid
	 */
	public int findRecommendFromUserCount(String userID, int levelType) throws Throwable{
		String sql = "SELECT COUNT(r.id) FROM recommend r, user u WHERE r.userid = ? AND r.type = ? AND r.topicid = u.userid";
		if(levelType == XingyunCommonConstant.USER_LEVEL_MINGXING || levelType == XingyunCommonConstant.USER_LEVEL_JINGYING)
			sql = "SELECT COUNT(r.id) FROM recommend r, user u WHERE r.userid = ? AND r.type = ? AND r.topicid = u.userid AND u.lid = " + levelType;
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunCommonConstant.RECOMMEND_TYPE_USER);
		return db.getRecordCountSQL(sql, vList);
	}
	
	/**
	 * 获取用户推荐用户数据
	 */
	public List<Map<Object, Object>> getShowRecommendFromUserList(String userID, int levelType, int pageIndex, int pageSize) throws Throwable{
		List<Map<Object, Object>> userList = findRecommendFromUserList(userID, levelType, pageIndex, pageSize);
		if(userList.size() == 0)
			return null;
		for(Map<Object, Object> userMap : userList){
			userMap.put("level", levelType > 0 ? levelType : Integer.parseInt(userMap.get("lid").toString()));
			userMap.put("userHref", CommonUtil.getUserIndexHref(userMap.get("userid").toString(), userMap.get("wkey")));
			userMap.put("logoUrl", UploadPicUtil.getPicWebUrl(userMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_150));
		}
		return userList;
	}
	
	/**
	 * 根据用户ID查询 用户推荐过的用户数据
	 * @param userID		用户ID
	 * @param levelType		用户等级
	 * @param pageIndex		页码
	 * @param pageSize		每页显示数量
	 * 使用索引：index_recommend_userid_topicid
	 */
	private List<Map<Object, Object>> findRecommendFromUserList(String userID, int levelType, int pageIndex, int pageSize) throws Throwable{
		String sql = "SELECT r.id AS rid, r.userid AS recommenduserid, r.reason AS reason, u.userid, u.nickname, u.wkey, u.lid, u.logourl, u.verified , u.verified_reason FROM recommend r, user u WHERE r.userid = ? AND r.type = ? AND r.topicid = u.userid";
		if(levelType == XingyunCommonConstant.USER_LEVEL_MINGXING || levelType == XingyunCommonConstant.USER_LEVEL_JINGYING)
			sql = "SELECT r.id AS rid, r.userid AS recommenduserid, r.reason AS reason, u.userid, u.nickname, u.wkey, u.lid, u.logourl, u.verified , u.verified_reason FROM recommend r, user u WHERE r.userid = ? AND r.type = ? AND r.topicid = u.userid AND u.lid = " + levelType;
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunCommonConstant.RECOMMEND_TYPE_USER);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, vList);
		if(list.size() > 0){
			String[] seqValue = {"rid"};
			CommonUtil.compositor(list, seqValue, 1);
			list = CommonUtil.subList(list, pageIndex, pageSize);
		}
		return list;
	}
	
	/**
	 * 根据用户ID查询 推荐过他的用户总数
	 * 使用索引：index_recommend_topicid
	 */
	public int findRecommendToUserCount(String userID, int levelType) throws Throwable{
		String sql = "SELECT COUNT(r.id) FROM recommend r, user u WHERE r.userid = u.userid AND r.type = ? AND r.topicid = ?";
		if(levelType == XingyunCommonConstant.USER_LEVEL_MINGXING || levelType == XingyunCommonConstant.USER_LEVEL_JINGYING)
			sql = "SELECT COUNT(r.id) FROM recommend r, user u WHERE r.userid = u.userid AND r.type = ? AND r.topicid = ? AND u.lid = " + levelType;
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunCommonConstant.RECOMMEND_TYPE_USER);
		vList.add(userID);
		return db.getRecordCountSQL(sql, vList);
	}
	
	/**
	 * 根据用户ID查询 推荐过他作品的用户总数
	 * 使用索引：index_recommend_topicid
	 */
	public int findRecommendToPostCount(String userID) throws Throwable{
		String sql = "SELECT id FROM post WHERE userid = ? AND isdel = ? AND status = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userID);
		valueList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		valueList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		List<Map<Object, Object>> postList = db.retrieveSQL(sql, valueList);
		if(postList.size() == 0)
			return 0;
		String topicIDs = CommonUtil.getStringUserID(postList, "id");
		sql = "SELECT COUNT(*) FROM recommend r, user u WHERE r.userid = u.userid AND r.topicid IN (" + topicIDs + ") AND r.type = ?";
		valueList.clear();
		valueList.add(XingyunCommonConstant.RECOMMEND_TYPE_POST);
		return db.getRecordCountSQL(sql, valueList);
	}
	
	/**
	 * 获取推荐过他数据
	 */
	public List<Map<Object, Object>> getShowRecommendToUserList(String userID, int levelType, int pageIndex, int pageSize) throws Throwable{
		List<Map<Object, Object>> userList = findRecommendToUserList(userID, levelType, pageIndex, pageSize);
		if(userList.size() == 0)
			return null;
		for(Map<Object, Object> userMap : userList){
			userMap.put("level", levelType > 0 ? levelType : Integer.parseInt(userMap.get("lid").toString()));
			userMap.put("userHref", CommonUtil.getUserIndexHref(userMap.get("userid").toString(), userMap.get("wkey")));
			userMap.put("logoUrl", UploadPicUtil.getPicWebUrl(userMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_150));
		}
		return userList;
	}
	
	/**
	 * 根据用户ID查询 推荐过他的用户数据
	 * @param userID		用户ID
	 * @param levelType		用户等级
	 * @param pageIndex		页码
	 * @param pageSize		每页显示数量
	 * 使用索引：index_recommend_topicid
	 */
	private List<Map<Object, Object>> findRecommendToUserList(String userID, int levelType, int pageIndex, int pageSize) throws Throwable{
		String sql = "SELECT r.id AS rid, r.reason AS reason, u.userid, u.nickname, u.wkey, u.lid, u.logourl, u.verified , u.verified_reason FROM recommend r, user u WHERE r.userid = u.userid AND r.type = ? AND r.topicid = ?";
		if(levelType == XingyunCommonConstant.USER_LEVEL_MINGXING || levelType == XingyunCommonConstant.USER_LEVEL_JINGYING)
			sql = "SELECT r.id AS rid, r.reason AS reason, u.userid, u.nickname, u.wkey, u.lid, u.logourl, u.verified , u.verified_reason FROM recommend r, user u WHERE r.userid = u.userid AND r.type = ? AND r.topicid = ? AND u.lid = " + levelType;
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunCommonConstant.RECOMMEND_TYPE_USER);
		vList.add(userID);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, vList);
		if(list.size() > 0){
			String[] seqValue = {"rid"};
			CommonUtil.compositor(list, seqValue, 1);
			list = CommonUtil.subList(list, pageIndex, pageSize);
		}
		return list;
	}
	
	/**
	 * 根据用户ID和筛选类型 查询用户 被推荐总数
	 */
	public List<Map<Object, Object>> findRecommendToUserIndexList(String userID, int recommendType) throws Throwable{
		List<Map<Object, Object>> recommendList = new ArrayList<Map<Object,Object>>();
		if(recommendType == XingyunCommonConstant.RECOMMEND_TYPE_USER_AND_POST) {
			recommendList.addAll(findRecommendToUserData(userID));
			recommendList.addAll(findRecommendToPostData(userID));
		} else if (recommendType == XingyunCommonConstant.RECOMMEND_TYPE_USER) {
			recommendList.addAll(findRecommendToUserData(userID));
		} else if(recommendType == XingyunCommonConstant.RECOMMEND_TYPE_POST) {
			recommendList.addAll(findRecommendToPostData(userID));
		}

		if(recommendList.size() > 0){
			String[] seqValue = {"rid"};
			CommonUtil.compositor(recommendList, seqValue, 1);
		}
		return recommendList;
	}
	
	/**
	 * 查询在推荐用户类型中 推荐我的信息 
	 * 使用索引：index_recommend_topicid
	 */
	private List<Map<Object, Object>> findRecommendToUserData(String userID) throws Throwable{
		String sql = "SELECT r.id AS rid, r.userid, r.type, r.reason, r.systime, u.nickname, u.wkey, u.lid, u.logourl, u.verified FROM recommend r, user u WHERE r.userid = u.userid AND r.type = ? AND r.topicid = ? ";
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunCommonConstant.RECOMMEND_TYPE_USER);
		vList.add(userID);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 查询在推荐作品类型中 推荐我作品的信息 
	 * 使用索引：index_recommend_topicid
	 */
	private List<Map<Object, Object>> findRecommendToPostData(String userID) throws Throwable{
		String sql = "SELECT id FROM post WHERE userid = ? AND isdel = ? AND status = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		vList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		List<Map<Object, Object>> postList = db.retrieveSQL(sql, vList);
		if(postList.size() == 0)
			return postList;
		String topicIDs = CommonUtil.getStringUserID(postList, "id");
		sql = "SELECT r.id AS rid, r.userid, r.type, r.reason, r.systime, u.nickname, u.wkey, u.lid, u.logourl, u.verified, p.id AS postid, p.posttype, p.title, p.coverpath FROM recommend r, user u, post p WHERE r.userid = u.userid AND r.topicid = p.id AND r.topicid IN (" + topicIDs + ") AND r.type = ?";
		vList.clear();
		vList.add(XingyunCommonConstant.RECOMMEND_TYPE_POST);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 整理用户被动推荐显示数据
	 */
	public List<Map<Object, Object>> setRecommendToUserDataList(List<Map<Object, Object>> recommendIndexList, int pageIndex, int pageSize) throws Throwable{
		List<Map<Object, Object>> showList = CommonUtil.subList(recommendIndexList, pageIndex, pageSize);
		if(showList == null || showList.size() == 0)
			return null;
		
		String fromUserID = "";
		Map<Object,Object> tmpMap = null;
		for(Map<Object, Object> map : showList){
			fromUserID = map.get("userid").toString();
			map.put("logoUrl", UploadPicUtil.getPicWebUrl(map.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50));
			map.put("userHerf", CommonUtil.getUserIndexHref(fromUserID, map.get("wkey")));
			map.put("level", Integer.parseInt(map.get("lid").toString()));
			map.put("verified", Integer.parseInt(map.get("verified").toString()));
			map.put("systime", DateUtil.getBlogSystime((Date)map.get("systime")));
			if (map.get("coverpath") != null) {
				tmpMap = getUserInfoByPostId(CommonUtil.getIntValue(map.get("postid")));
				if(tmpMap == null)
					continue;
				map.put("coverpath", UploadPicUtil.getPicWebUrl(map.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_190));
				map.put("postUserHref", CommonUtil.getUserIndexHref(CommonUtil.getStringValue(tmpMap.get("userid")), tmpMap.get("wkey")));
			}
		}
		return showList;
	}
	
	/**
	 * 通过作品ID获取作品作者的信息
	 */
	private Map<Object,Object> getUserInfoByPostId(int postId) throws Throwable{
		String sql = "SELECT u.userid, u.wkey FROM post p, user u WHERE p.userid = u.userid AND p.id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	
	/**
	 * 添加推荐用户数据
	 * @param fromUserId	当前用户ID
	 * @param toUserId		被推荐用户ID
	 * @param reason		推荐内容
	 */
	public void recommendUserInfo(String fromUserId, String toUserId, String reason, int isShareDynamic) throws Throwable{
		int recommendID = 0;
		try{
			String time = DateUtil.getSimpleDateFormat();
			List<String> sqlList = new ArrayList<String>();
			recommendID = addRecommendData(XingyunCommonConstant.RECOMMEND_TYPE_USER, fromUserId, toUserId, reason);
			if(isShareDynamic == XingyunCommonConstant.XINGYUN_SHAREDYNAMIC_YES)
				sqlList.add("INSERT INTO dynamic(userid, type, topicid, systime) VALUES('" + fromUserId + "'," + XingyunCommonConstant.DYNAMIC_TYPE_USER_TJ + "," + recommendID + ", '" + time + "')");
			sqlList.add("UPDATE user_counter SET recommendcount = recommendcount + 1 WHERE userid = '" + toUserId + "'");
			db.batchExecute(sqlList, true);
			MessageUtil.addNewMessage(toUserId, "recommendcount");
		} catch (Throwable e) {
			if(recommendID > 0)
				db.deleteData("DELETE from recommend WHERE id = " + recommendID);
			throw new Throwable(e);
		}
	}
	
	/**
	 * 添加推荐表数据
	 * @param type
	 * @param userId
	 * @param topicId
	 * @param reason
	 */
	private int addRecommendData(int type, String userId, String topicId, String reason) throws Throwable{
		String sql = "INSERT INTO recommend(type,userid,topicid,reason,systime) VALUES(?, ?, ?, ?, ?)";
		List<Object> vList = new ArrayList<Object>();
		vList.add(type);
		vList.add(userId);
		vList.add(topicId);
		vList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(reason, 500));
		vList.add(new Date());
		return db.insertData(sql, vList);
	}
	
	/**
	 * 检查是否推荐过
	 * 使用索引：index_recommend_userid_topicid
	 */
	public int checkIsRecommendOverByType(int type, String userId, String topicId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM recommend WHERE userid = ? AND topicid = ? AND type = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(topicId);
		valueList.add(type);
		return db.getRecordCountSQL(sql, valueList);
	}
	
	/**
	 * 添加推荐作品数据
	 */
	public boolean addRecommendPostData(String fromUserId, int postID, String postUserID, String recommendContent, int isShareDynamic) throws Throwable{
		int recommendID = 0;
		try{
			String sysTime = DateUtil.getSimpleDateFormat();
			List<String> sqlList = new ArrayList<String>();
			recommendID = addRecommendData(XingyunCommonConstant.RECOMMEND_TYPE_POST, fromUserId, "" + postID, recommendContent);
			if(isShareDynamic == XingyunCommonConstant.XINGYUN_SHAREDYNAMIC_YES)
				sqlList.add("INSERT INTO dynamic(userid, type, topicid, systime) VALUES('" + fromUserId + "'," + XingyunCommonConstant.DYNAMIC_TYPE_ZP_TJ + "," + recommendID + ", '" + sysTime + "')");
			sqlList.add("UPDATE post_counter SET recommendcount = recommendcount + 1 WHERE postid = " + postID);
			db.batchExecute(sqlList, true);
			MessageUtil.addNewMessage(postUserID, "recommendcount");
			return true;
		} catch (Throwable e) {
			if(recommendID > 0)
				db.deleteData("DELETE from recommend WHERE id = " + recommendID);
			throw new Throwable(e);
		}
	}
	
	/**
	 * 根据作品ID 查询作品被推荐数量
	 * 使用索引：index_post_counter_postid
	 */
	public int getPostRecommendCount(int postID) throws Throwable{
		String sql = "SELECT recommendcount FROM post_counter WHERE postid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postID);
		return CommonUtil.getIntValue(sql, valueList, "recommendcount");
	}
	
	/**
	 * 检查用户是否可以推荐过该作品
	 * @param fromUser		当前用户
	 * @param postID		作品ID
	 * @param postUserID	作品作者ID
	 */
	public boolean checkRecommendPost(String fromUserId, int postID, String postUserID) throws Throwable{
		Map<Object, Object> postMap = PostService.getInstance().findPostMap(postID, postUserID);
		if(postMap == null)
			return false;
		
		if(fromUserId.equals(postMap.get("userid").toString()))
			return false;
		
		if(Integer.parseInt(postMap.get("isdel").toString()) == XingyunPostConstant.ZP_DEL_TYPE_YES)
			return false;
		
		if(Integer.parseInt(postMap.get("status").toString()) != XingyunPostConstant.ZP_STATUS_TYPE_FB)
			return false;
		
		return checkIsRecommendOverByType(XingyunCommonConstant.RECOMMEND_TYPE_POST, fromUserId, "" + postID) == 0;
	}
	
	
	/**
	 * 取消推荐作品
	 * @param userID	用户ID
	 * @param postID	作品ID
	 */
	public boolean delRecommendPost(String userID, int postID) throws Throwable{
		int recommendID = findRecommendID(userID, "" + postID, XingyunCommonConstant.RECOMMEND_TYPE_POST);
		if(recommendID == 0)
			return false;
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("DELETE FROM recommend WHERE id = " + recommendID); 
		sqlList.add("UPDATE post_counter SET recommendcount = recommendcount - 1 WHERE postid = " + postID);
		sqlList.add("DELETE FROM dynamic WHERE userid = '" + userID + "' AND type = " + XingyunCommonConstant.DYNAMIC_TYPE_ZP_TJ + " AND topicid = '" + postID + "'");
		db.batchExecute(sqlList, true);
		return true;
	}
	
	/**
	 * 取消推荐用户
	 * @param fromUserId	用户ID
	 * @param toUserId		推荐用户ID
	 */
	public boolean cancelRecommendUser(String fromUserId, String toUserId) throws Throwable{
		int recommendID = findRecommendID(fromUserId, toUserId, XingyunCommonConstant.RECOMMEND_TYPE_USER);
		if(recommendID == 0)
			return false;
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("DELETE FROM recommend WHERE id = " + recommendID); 
		sqlList.add("UPDATE user_counter SET recommendcount = recommendcount - 1 WHERE userid = '"+toUserId+"'");
		sqlList.add("DELETE FROM dynamic WHERE userid = '" + fromUserId + "' AND type = " + XingyunCommonConstant.DYNAMIC_TYPE_USER_TJ + " AND topicid = '" + toUserId + "'");
		db.batchExecute(sqlList, true);
		return true;
	}
	
	/**
	 * 通过用户id获取头像
	 */
	public String getLogoUrlByUserId(String userId) throws Throwable{
		String sql = "SELECT logourl FROM user WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		String logoUrl = CommonUtil.getStringValue(sql, valueList, "logourl");
		if(StringUtils.EMPTY.equals(logoUrl))
			return StringUtils.EMPTY;
		return UploadPicUtil.getPicWebUrl(logoUrl, XingyunUploadFileConstant.LOGO_WIDTH_640);
	}
	
	/**
	 * 通过作品id获取作品封面
	 */
	public String getCoverPathByPostId(int postId) throws Throwable{
		String sql = "SELECT coverpath FROM post WHERE id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postId);
		String coverPath = CommonUtil.getStringValue(sql, valueList, "coverpath");
		if(StringUtils.EMPTY.equals(coverPath))
			return StringUtils.EMPTY;
		return UploadPicUtil.getPicWebUrl(coverPath, XingyunUploadFileConstant.POST_COVER_WIDTH_250);
	}
}
