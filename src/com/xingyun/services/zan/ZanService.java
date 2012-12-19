package com.xingyun.services.zan;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.comment.CommentService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.UploadPicUtil;

public class ZanService {

	private static final DBOperate db = new DBOperate();
	private static final ZanService zanService = new ZanService();
	private ZanService(){}
	public static ZanService getInstance() {
		return zanService;
	}
	
	/**
	 * 添加赞数据
	 */
	public void addZan(int topicID, String userID, int type) throws Throwable{
		String sql = "INSERT INTO xy_zan(topicid, type, userid, systime) VALUES(?, ?, ?, ?)";
		List<Object> vList = new ArrayList<Object>();
		vList.add(topicID);
		vList.add(type);
		vList.add(userID);
		vList.add(new Date());
		db.insertData(sql, vList);
	}
	
	/**
	 * 获取赞数据
	 */
	public List<Map<Object, Object>> getZanListData(int topicID, int type) throws Throwable{
		List<Map<Object, Object>> zanList = findZanData(topicID, type);
		if(zanList == null || zanList.size() == 0)
			return zanList;
		for(Map<Object, Object> map : zanList){
			map.put("logoUrl", UploadPicUtil.getPicWebUrl(map.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50));
			map.put("userHref", CommonUtil.getUserIndexHref(map.get("userid").toString(), map.get("wkey").toString()));
		}
		return zanList;
	}
	
	/**
	 * 查询赞数据
	 * @param topicID	主题ID
	 * @param type		赞类型
	 */
	private List<Map<Object, Object>> findZanData(int topicID, int type) throws Throwable{
		String sql = "SELECT u.userid, u.nickname, u.wkey, u.logourl FROM xy_zan z, user u WHERE z.userid = u.userid AND z.topicid = ? AND z.type = ? ORDER BY z.id DESC";
		List<Object> vList = new ArrayList<Object>();
		vList.add(topicID);
		vList.add(type);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 添加星语赞数据
	 */
	public boolean addZanData(int topicID, String userID, int type, int fromType) throws Throwable{
		if(checkZan(topicID, userID, type))
			return false;
		boolean isSuccess = false;
		if(type == XingyunCommonConstant.ZAN_TYPE_XINGYU)
			isSuccess = addXingyuZan(topicID, userID, type, fromType);
		else
			isSuccess = addPostZan(topicID, userID, type, fromType);
		return isSuccess;
	}
	
	private boolean addPostZan(int topicID, String userID, int type, int fromType) throws Throwable{
		Map<Object,Object> postInfoMap = CommentService.getInstance().getPostInfoMap(topicID);
		if(postInfoMap == null)
			return false;
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("INSERT INTO xy_zan(topicid, topicuserid, type, userid, fromtype, content, systime) VALUES(" + topicID + ", '" + postInfoMap.get("userid") + "', " + type + ", '" + userID + "', " + fromType + ", '"+postInfoMap.get("title")+"', '" + DateUtil.getSimpleDateFormat() + "')");
		sqlList.add("UPDATE post_counter SET zancount = zancount + 1 WHERE postid = " + topicID);
		db.batchExecute(sqlList, true);
		return true;
	}
	
	private boolean addXingyuZan(int topicID, String userID, int type, int fromType) throws Throwable{
		Map<Object,Object> xingyuInfoMap = CommentService.getInstance().getXingyuInfoMap(topicID);
		if(xingyuInfoMap == null)
			return false;
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("INSERT INTO xy_zan(topicid, topicuserid, type, userid, fromtype, content, systime) VALUES(" + topicID + ", '" + xingyuInfoMap.get("userid") + "', " + type + ", '" + userID + "', " + fromType + ", '"+xingyuInfoMap.get("content")+"', '" + DateUtil.getSimpleDateFormat() + "')");
		sqlList.add("UPDATE xingyu SET zancount = zancount + 1 WHERE id = " + topicID);
		db.batchExecute(sqlList, true);
		return true;
	}
	
	/**
	 * 根据类型检测是否赞过
	 */
	public boolean checkZan(String fromUserID, int topicID, String topicUserID, int type) throws Throwable{
		if(fromUserID.equals(topicUserID))
			return true;
		return checkZan(topicID, fromUserID, type);
	}
	
	/**
	 * 检查是否赞过
	 */
	private boolean checkZan(int topicID, String userID, int type) throws Throwable{
		String sql = "SELECT COUNT(*) FROM xy_zan WHERE topicid = ? AND userid = ? AND type = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(topicID);
		vList.add(userID);
		vList.add(type);
		return db.getRecordCountSQL(sql, vList) > 0;
	}
	
	/**
	 * 获取发布作品的被赞数量
	 */
	public int getPostZanCount(int topicID, int type) throws Throwable {
		String sql = "SELECT COUNT(*) FROM xy_zan WHERE topicid = ? AND type = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(topicID);
		vList.add(type);
		return db.getRecordCountSQL(sql, vList);
	}
	
	/**
	 * 查询赞列表
	 * @param source   接收或发送
	 * @param type     全部或星语或作品
	 */
	public List<Map<Object,Object>> getZanList(String userId, int source, int type, int curPage) throws Throwable{
		List<Map<Object,Object>> zanIndexList = getZanIndexList(userId, source, type, curPage);
		if(zanIndexList.size() == 0)
			return null;
		return setZanList(zanIndexList);
	}
	/**
	 * 根据接收或发送，全部或星语或作品查询赞索引列表
	 */
	private List<Map<Object,Object>> getZanIndexList(String userId, int source, int type, int curPage) throws Throwable{
		String sql = "SELECT id, topicid, topicuserid, type, userid, fromtype, content, systime FROM xy_zan WHERE ";
		if(source == XingyunCommonConstant.COMMENT_TYPE_RECEIVE)
			sql += "topicuserid = ?";
		else
			sql += "userid = ?";
		if(type != XingyunCommonConstant.COMMENT_SOURCE_ALL)
			sql += "AND type = " + type;
		sql += " ORDER BY id DESC LIMIT ?, ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add((curPage -1) * XingyunCommonConstant.ZAN_PREPAGE_SIZE);
		valueList.add(XingyunCommonConstant.ZAN_PREPAGE_SIZE);
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 查询赞列表总数
	 * @param source   接收或发送
	 * @param type     全部或星语或作品
	 */
	public int getZanCount(String userId, int source, int type) throws Throwable{
		String sql = "SELECT COUNT(*) FROM xy_zan WHERE ";
		if(source == XingyunCommonConstant.COMMENT_TYPE_RECEIVE)
			sql += "topicuserid = ?";
		else
			sql += "userid = ?";
		if(type != XingyunCommonConstant.COMMENT_SOURCE_ALL)
			sql += "AND type = " + type;
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return db.getRecordCountSQL(sql, valueList);
	}
	/**
	 * 整理赞列表
	 */
	@SuppressWarnings("unchecked")
	private List<Map<Object, Object>> setZanList(List<Map<Object,Object>> zanIndexList) throws Throwable{
		List<Map<Object,Object>> zanList = new ArrayList<Map<Object,Object>>();
		String userIds = CommonUtil.getStringUserID(zanIndexList, "userid");
		Map<Object, Map<Object,Object>> userInfoMap = PublicQueryUtil.getInstance().getUserInfoMapByUserIds(userIds);
		String topicUserIds = CommonUtil.getStringUserID(zanIndexList, "topicuserid");
		Map<Object, Map<Object,Object>> topicUserInfoMap = PublicQueryUtil.getInstance().getUserInfoMapByUserIds(topicUserIds);
		Map<Object, Object> tmpMap = null;
		for(Map<Object,Object> map : zanIndexList){
			tmpMap = userInfoMap.get(map.get("userid"));
			if(tmpMap == null)
				continue;
			tmpMap.put("userHref", CommonUtil.getUserIndexHref(CommonUtil.getStringValue(tmpMap.get("userid")), tmpMap.get("wkey")));
			map.putAll(tmpMap);
			tmpMap = (Map<Object,Object>)topicUserInfoMap.get(map.get("topicuserid"));
			if(tmpMap == null)
				continue;
			map.put("topicNickName", CommonUtil.getStringValue(tmpMap.get("nickname")));
			map.put("topicUserId", CommonUtil.getStringValue(tmpMap.get("userid")));
			map.put("topicUserHref", CommonUtil.getUserIndexHref(CommonUtil.getStringValue(tmpMap.get("userid")), tmpMap.get("wkey")));
			map.put("topicLid", CommonUtil.getStringValue(tmpMap.get("lid")));
			map.put("topicVerified", CommonUtil.getStringValue(tmpMap.get("verified")));
			map.put("fromtype", CommonUtil.checkCommentFromType(CommonUtil.getIntValue(map.get("fromtype"))));
			map.put("systime", DateUtil.getBlogSystime((Date) map.get("systime")));
			zanList.add(map);
		}
		return zanList;
	}
}
