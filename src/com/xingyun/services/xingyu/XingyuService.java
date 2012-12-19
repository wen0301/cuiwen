package com.xingyun.services.xingyu;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xingyun.bean.ResPicBean;
import com.xingyun.bean.User;
import com.xingyun.bean.XingyuBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.friend.FriendService;
import com.xingyun.services.index.MyIndexService;
import com.xingyun.services.zan.ZanService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.SpecialCharFilterUtil;
import com.xingyun.util.UploadPicUtil;

public class XingyuService {

	private static final DBOperate db = new DBOperate();
	private static final XingyuService xingyuService = new XingyuService();
	private XingyuService(){}
	public static XingyuService getInstance() {
		return xingyuService;
	}
	
	/**
	 * 获取星语数量
	 */
	public List<Map<Object, Object>> findXingYuAllIndex(String xyUserID, User user) throws Throwable{
		//游客
		if(user == null)
			return findXingYuPubCount(xyUserID);
			
		//获取用户 所有星语数量
		if(xyUserID.equals(user.getUserId()))
			return findXingYuAllCount(xyUserID);
		
		//好友
		if(FriendService.getInstance().checkFriendRelation(xyUserID, user.getUserId()))
			return findXingYuAllCount(xyUserID);
		
		//非好友  
		return findXingYuPubCount(xyUserID);
	}
	
	/**
	 * 获取用户 所有星语数量
	 */
	private List<Map<Object, Object>> findXingYuAllCount(String userID) throws Throwable{
		String sql = "SELECT id FROM xingyu WHERE userid = ? ORDER BY id DESC";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 获取用户 公开星语数量
	 */
	private List<Map<Object, Object>> findXingYuPubCount(String userID) throws Throwable{
		String sql = "SELECT id FROM xingyu WHERE userid = ? AND showtype = ? ORDER BY id DESC";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunCommonConstant.XINGYU_SHOW_TYPE_PUB);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 整理作品列表显示数据
	 */
	public List<XingyuBean> findXingYuList(String fromUsreID, List<Map<Object, Object>> indexList, int pageIndex, int pageSize) throws Throwable{
		indexList = CommonUtil.subList(indexList, pageIndex, pageSize);
		String xingyuIDs = CommonUtil.getStringID(indexList, "id");
		if(StringUtils.isBlank(xingyuIDs))
			return null;
		
		List<Map<Object, Object>> list = findXingYuData(xingyuIDs);
		if(list == null || list.size() == 0)
			return null;
		
		List<XingyuBean> xingyunList = new ArrayList<XingyuBean>();
		XingyuBean xingyuBean = null;
		for(Map<Object, Object> map : list){
			xingyuBean = findXingYuData(Integer.parseInt(map.get("id").toString()), fromUsreID);
			if(xingyuBean != null)
				xingyunList.add(xingyuBean);
		}
		return xingyunList;
	}
	
	/**
	 * 查询星语数据
	 */
	private List<Map<Object, Object>> findXingYuData(String xingyuIDs) throws Throwable{
		String sql = "SELECT id, userid, content, showtype, commentcount, zancount, fromtype, systime FROM xingyu WHERE id IN(" + xingyuIDs + ") ORDER BY id DESC";
		return db.retrieveSQL(sql);
	}
	
	/**
	 * 根据用户ID查询 用户推荐过的用户数据
	 * 使用索引：index_recommend_userid_topicid
	 */
	public List<Map<Object, Object>> findRecommendFromUserList(String userID) throws Throwable{
		String sql = "SELECT r.id AS rid, u.userid FROM recommend r, user u WHERE r.userid = u.userid AND r.type = ? AND r.topicid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunCommonConstant.RECOMMEND_TYPE_USER);
		vList.add(userID);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, vList);
		if(list.size() == 0)
			return null;
		
		String[] seqValue = {"rid"};
		CommonUtil.compositor(list, seqValue, 1);
		list = CommonUtil.subList(list, 0, XingyunCommonConstant.NEW_RECOMMEND_MAXSIZE);
		return MyIndexService.getInstance().setNewUserList(list);
	}
	
	/**
	 * 保存星语数据
	 */
	public int saveXingYuData(String userID, String xyContent, int xyShowType, List<ResPicBean> xingyuResPicBeanList) throws Throwable{
		int xingyuID = 0;
		try {
			xyShowType = XingyunCommonConstant.XINGYU_SHOW_TYPE_HY == xyShowType ? XingyunCommonConstant.XINGYU_SHOW_TYPE_HY : XingyunCommonConstant.XINGYU_SHOW_TYPE_PUB;
			xingyuID = insertXingYuData(userID, xyContent, xyShowType, XingyunCommonConstant.FROM_TYPE_WEB);//添加星语主题数据 获取主键ID
			PublicQueryUtil.getInstance().addDynamicData(userID, xingyuID, XingyunCommonConstant.DYNAMIC_TYPE_XINGYU);								//添加星语动态数据
			addXingYuResPicData(xingyuID, xingyuResPicBeanList);											//添加星语图片资源
			return xingyuID;
		} catch (Throwable e) {
			if(xingyuID > 0){
				List<String> sqlList = new ArrayList<String>();
				sqlList.add("DELETE FROM xingyu_pic WHERE xingyuid = " + xingyuID);
				sqlList.add("DELETE FROM dynamic WHERE userid = '" + userID + "' AND type = " + XingyunCommonConstant.DYNAMIC_TYPE_XINGYU + " AND topicid = '" + xingyuID + "'");
				sqlList.add("DELETE FROM xingyu WHERE id = " + xingyuID + " AND userid = '" + userID + "'");
				db.batchExecute(sqlList, true);
			}
			throw new Throwable(e);
		}
	}
	
	/**
	 * 处理星语资源图片
	 */
	private void addXingYuResPicData(int xingyuID, List<ResPicBean> xingyuResPicBeanList) throws Throwable{
		if(xingyuResPicBeanList == null || xingyuResPicBeanList.size() == 0)
			return;
		
		List<String> sqlList = new ArrayList<String>();
		String resPicSql = "";
		for(ResPicBean resPic : xingyuResPicBeanList){
			resPicSql = CommonUtil.getInsertResPicSql(resPic);	
			if(StringUtils.isNotBlank(resPicSql)){
				sqlList.add(resPicSql);
				sqlList.add("INSERT INTO xingyu_pic(xingyuid, picid, systime) VALUES(" + xingyuID + ", " + resPic.getPicid() + ", NOW())");
			}
		}
		db.batchExecute(sqlList);
	}
	
	/**
	 * 添加星语主题数据
	 */
	private int insertXingYuData(String userID, String content, int showType, int fromType) throws Throwable{
		String sql = "INSERT INTO xingyu(userid, content, showtype, commentcount, zancount, fromtype, systime) VALUES(?, ?, ?, ?, ?, ?, ?)";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(content, 1000));
		vList.add(showType);
		vList.add(0);
		vList.add(0);
		vList.add(fromType);
		vList.add(new Date());
		return db.insertData(sql, vList);
	}
	
	/**
	 * 整理星语主题数据
	 */
	public XingyuBean findXingYuData(int xingyuID, String fromUserID) throws Throwable{
		Map<Object, Object> xyMap = findXingYuTopicDate(xingyuID);
		if(xyMap == null)
			return null;
		
		
		//好友可见
		if(com.xingyun.constant.XingyunCommonConstant.XINGYU_SHOW_TYPE_HY == Integer.parseInt(xyMap.get("showtype").toString())){
			if(StringUtils.isBlank(fromUserID))
				return null;
			
			if(!fromUserID.equals(xyMap.get("userid").toString())){
				//好友
				if(!FriendService.getInstance().checkFriendRelation(xyMap.get("userid").toString(), fromUserID))
					return null;
			}
		}
		
		XingyuBean xingyuBean = new XingyuBean();
		xingyuBean.setId(xingyuID);
		xingyuBean.setUserid(xyMap.get("userid").toString());
		xingyuBean.setContent(xyMap.get("content").toString());
		xingyuBean.setShowtype(Integer.parseInt(xyMap.get("showtype").toString()));
		xingyuBean.setCommentcount(Integer.parseInt(xyMap.get("commentcount").toString()));
		xingyuBean.setZancount(Integer.parseInt(xyMap.get("zancount").toString()));
		xingyuBean.setFromtype(CommonUtil.checkCommentFromType(CommonUtil.getIntValue(xyMap.get("fromtype"))));
		xingyuBean.setSystime(DateUtil.getBlogSystime((Date)xyMap.get("systime")));
		xingyuBean.setPicList(getXingYuPicList(xingyuID));
		xingyuBean.setIsZan(ZanService.getInstance().checkZan(fromUserID, xingyuID, xingyuBean.getUserid(), XingyunCommonConstant.ZAN_TYPE_XINGYU));
		return xingyuBean;
	}
	
	/**
	 * 查询星语数据
	 */
	private Map<Object, Object> findXingYuTopicDate(int xingyuID) throws Throwable{
		String sql = "SELECT id, userid, content, showtype, commentcount, zancount, fromtype, systime FROM xingyu WHERE id = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(xingyuID);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, vList);
		return list.size() == 0 ? null : list.get(0);
	}
	
	/**
	 * 查询星语图片数据
	 */
	private List<Map<Object, Object>> getXingYuPicList(int xingyuID) throws Throwable{
		List<Map<Object, Object>> picList = findXingYuPicList(xingyuID);
		if(picList != null && picList.size() > 0){
			for(Map<Object, Object> map : picList){
				map.put("src", UploadPicUtil.getPicWebUrl(map.get("picid").toString(), 0));
				map.put("mid", UploadPicUtil.getPicWebUrl(map.get("picid").toString(), XingyunUploadFileConstant.XINGYU_WIDTH_500));
				map.put("thumb", UploadPicUtil.getPicWebUrl(map.get("picid").toString(), XingyunUploadFileConstant.XINGYU_WIDTH_150));
				map.put("showBigPic", UploadPicUtil.checkShowBigPic(map.get("picid").toString(), XingyunUploadFileConstant.XINGYU_WIDTH_500));
				
				int[] picWH = UploadPicUtil.getPicWH(map.get("picid").toString(), XingyunUploadFileConstant.XINGYU_WIDTH_500);
				map.put("midWidth", picWH[0]);
				map.put("midHeight", picWH[1]);
			}
		}
		return picList;
	}
	
	/**
	 * 查询星语图片数据
	 */
	private List<Map<Object, Object>> findXingYuPicList(int xingyuID) throws Throwable{
		String sql = "SELECT picid FROM xingyu_pic WHERE xingyuid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(xingyuID);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 检查星语作者是否一致
	 */
	public boolean checkXingyu(int xingyuID, String userID) throws Throwable{
		String sql = "SELECT COUNT(*) FROM xingyu WHERE id = ? AND userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(xingyuID);
		vList.add(userID);
		return db.getRecordCountSQL(sql, vList) > 0;
	}
	
	/**
	 * 删除星语数据
	 */
	public void delXingYuData(int xingyuID, String userID) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("DELETE FROM xingyu WHERE id = " + xingyuID + " AND userid = '" + userID + "'");
		sqlList.add("DELETE FROM xy_zan WHERE topicid = " + xingyuID);
		sqlList.add("DELETE FROM xingyu_pic WHERE xingyuid = " + xingyuID);
		sqlList.add("DELETE FROM comment WHERE topicid = " + xingyuID + " AND topicuserid = '" + userID + "' AND type = " + XingyunCommonConstant.COMMENT_SOURCE_XINGYU);
		sqlList.add("DELETE FROM dynamic WHERE userid = '" + userID + "' AND type = " + XingyunCommonConstant.DYNAMIC_TYPE_XINGYU + " AND topicid = '" + xingyuID + "'");
		db.batchExecute(sqlList, true);
	}
	/**
	 * 检查是否在10分钟内发送相同的内容
	 */
	public boolean checkXingyuInterval(String userId, String content) throws Throwable {
		content = SpecialCharFilterUtil.filterEncodeAndForbidValue(content, 1000);
		String sql = "SELECT MAX(systime) AS systime FROM xingyu WHERE userid = ? AND content = ?";
		List<Object> valueList = new ArrayList<Object>();
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
	 * 通过星语id获取用户的userHref
	 */
	public String getUserHrefByXingyuId(int xingyuId) throws Throwable{
		String sql = "SELECT u.userid, u.wkey FROM user u, xingyu x WHERE u.userid = x.userid AND x.id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(xingyuId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return StringUtils.EMPTY;
		Map<Object,Object> map = list.get(0);
		return CommonUtil.getUserIndexHref(map.get("userid").toString(), map.get("wkey"));
	}
}
