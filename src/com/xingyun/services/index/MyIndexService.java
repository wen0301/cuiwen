package com.xingyun.services.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xingyun.bean.DynamicBean;
import com.xingyun.bean.XingyuBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunFaceConstant;
import com.xingyun.constant.XingyunMyIndexConstant;
import com.xingyun.constant.XingyunPostConstant;
import com.xingyun.constant.XingyunSystemConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.follow.FollowService;
import com.xingyun.services.friend.FriendService;
import com.xingyun.services.header.UserHeaderService;
import com.xingyun.services.post.PostService;
import com.xingyun.services.profile.ProfileService;
import com.xingyun.services.recommend.RecommendService;
import com.xingyun.services.xingyu.XingyuService;
import com.xingyun.services.zan.ZanService;
import com.xingyun.util.AreaUtil;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.UploadPicUtil;

public class MyIndexService {

	private static final DBOperate db = new DBOperate();
	private static final MyIndexService myIndexService = new MyIndexService();
	private MyIndexService(){}
	public static MyIndexService getInstance() {
		return myIndexService;
	}
	
	/**
	 * 查询用户动态索引数据
	 * 使用索引：index_dynamic_userid
	 */
	public List<Map<Object, Object>> findUserDynamicIndex(int showType, String userID) throws Throwable{
		List<Map<Object, Object>> toUserIdList = new ArrayList<Map<Object,Object>>();
		if(showType == XingyunMyIndexConstant.MYINDEX_DYNAMIC_SHOW_TYPE_ALL){		//全部动态
			toUserIdList.addAll(getFriendUserIdList(userID));
			toUserIdList.addAll(getFollowUserIdList(userID));
			CommonUtil.setList(toUserIdList);
		}else if(showType == XingyunMyIndexConstant.MYINDEX_DYNAMIC_SHOW_TYPE_HY)	//好友动态
			toUserIdList = getFriendUserIdList(userID);
		else if(showType == XingyunMyIndexConstant.MYINDEX_DYNAMIC_SHOW_TYPE_SC)	//收藏人动态
			toUserIdList = getFollowUserIdList(userID);
		
		String toUserIDs = CommonUtil.getStringUserID(toUserIdList, "touserid");
		if(showType == XingyunMyIndexConstant.MYINDEX_DYNAMIC_SHOW_TYPE_ALL)
			toUserIDs += toUserIDs.length() == 0 ? "'" + userID + "' ": ",'" + userID + "'";
		if(showType == XingyunMyIndexConstant.MYINDEX_DYNAMIC_SHOW_TYPE_ALL || showType == XingyunMyIndexConstant.MYINDEX_DYNAMIC_SHOW_TYPE_XY){
			String xingyunID = XingyunSystemConstant.getSystemConstantValue(XingyunSystemConstant.XINGYUN_UID);
			toUserIDs += toUserIDs.length() == 0 ? "'" + xingyunID + "'" : ",'" + xingyunID + "'";
		}
		if(StringUtils.isBlank(toUserIDs))
			return null;
		String sql = "SELECT id, userid, type, topicid, systime FROM dynamic WHERE userid IN(" + toUserIDs + ") ORDER BY id DESC LIMIT ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(XingyunMyIndexConstant.MYINDEX_DYNAMIC_SELECT_MAX_SIZE);
		List<Map<Object, Object>> indexList = db.retrieveSQL(sql, valueList);
		if(indexList.size() == 0)
			return null;
		List<Map<Object, Object>> dynamicNotInDelList = getDynamicDelIndexList(userID, indexList);
		List<Map<Object, Object>> dynamicIndexList = new ArrayList<Map<Object,Object>>();
		for(Map<Object, Object> map : dynamicNotInDelList){
			if(XingyunCommonConstant.DYNAMIC_TYPE_XINGYU == Integer.parseInt(map.get("type").toString())){
				boolean tag = checkShowXingYuDynamic(Integer.parseInt(map.get("topicid").toString()), map.get("userid").toString(), userID);
				if(!tag)
					continue;
			}
			dynamicIndexList.add(map);
			if(dynamicIndexList.size() >= XingyunMyIndexConstant.MYINDEX_DYNAMIC_MAX_SIZE)
				break;
		}
		return CommonUtil.subList(dynamicIndexList, 0, XingyunMyIndexConstant.MYINDEX_DYNAMIC_MAX_SIZE);
	}
	
	/**
	 **整理用户动态显示数据
	 * @param userID		用户ID
	 * @dynamicIndexList	动态索引数据
	 * @param pageIndex		页码
	 */
	public List<DynamicBean> getUserDynamicData(String userID, List<Map<Object, Object>> dynamicIndexList, int pageIndex) throws Throwable{
		//整理动态分页索引数据
		List<Map<Object, Object>> indexList = CommonUtil.subList(dynamicIndexList, pageIndex, XingyunMyIndexConstant.MYINDEX_DYNAMIC_PAGE_SIZE);
		if(indexList == null || indexList.size() == 0)
			return null;
		
		//整理动态显示数据
		List<DynamicBean> dynamicList = new ArrayList<DynamicBean>();
		String fromUserID = "";
		XingyuBean xyBean = null;
		Map<Object, Object> userMap = null;
		int postId = 0;
		for(Map<Object, Object> map : indexList){
			fromUserID = map.get("userid").toString();
			userMap = PublicQueryUtil.getInstance().findUserCommonMap(fromUserID);
			if(userMap == null)
				continue;
			
			int dynamicType = Integer.parseInt(map.get("type").toString());
			DynamicBean dynamicBean = new DynamicBean();
			dynamicBean.setDynamicID(Integer.parseInt(map.get("id").toString()));
			dynamicBean.setDynamicType(dynamicType);
			dynamicBean.setSystime(DateUtil.getBlogSystime((Date)map.get("systime")));
			dynamicBean.setFromUserID(fromUserID);
			dynamicBean.setFromNickName(userMap.get("nickname").toString());
			dynamicBean.setFromWkey(userMap.get("wkey").toString());
			dynamicBean.setFromUserLevel(Integer.parseInt(userMap.get("lid").toString()));
			dynamicBean.setFromUserVerified(Integer.parseInt(userMap.get("verified").toString()));
			dynamicBean.setFromUserVerifiedReason(userMap.get("verified_reason").toString());
			dynamicBean.setFromUserHref(CommonUtil.getUserIndexHref(fromUserID, userMap.get("wkey")));
			dynamicBean.setFromUserLogo(UploadPicUtil.getPicWebUrl(userMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50));
			
			String topicid = map.get("topicid").toString();
			//发布作品动态 
			if(XingyunCommonConstant.DYNAMIC_TYPE_ZP_FB == dynamicType){
				postId = Integer.parseInt(topicid);
				Map<Object, Object> postMap = findDynamicPostData(postId);
				if(postMap == null)
					continue;
				dynamicBean.setPostID(postId);
				dynamicBean.setPostType(Integer.parseInt(postMap.get("posttype").toString()));
				dynamicBean.setPostTitle(postMap.get("title").toString());
				dynamicBean.setPostCoverPic(UploadPicUtil.getPicWebUrl(postMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_190));
				dynamicBean.setPostZanCount(ZanService.getInstance().getPostZanCount(postId, XingyunCommonConstant.ZAN_TYPE_POST));
				dynamicBean.setPostIsZan(ZanService.getInstance().checkZan(userID, postId, fromUserID, XingyunCommonConstant.ZAN_TYPE_POST));
			}else if(XingyunCommonConstant.DYNAMIC_TYPE_ZP_TJ == dynamicType){		//推荐作品动态
				Map<Object, Object> postMap = findDynamicRecommendPostData(Integer.parseInt(topicid));
				if(postMap == null)
					continue;
				dynamicBean.setRecommendContent(postMap.get("reason").toString());
				dynamicBean.setPostID(Integer.parseInt(postMap.get("id").toString()));
				dynamicBean.setPostType(Integer.parseInt(postMap.get("posttype").toString()));
				dynamicBean.setPostTitle(postMap.get("title").toString());
				dynamicBean.setPostCoverPic(UploadPicUtil.getPicWebUrl(postMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_190));
				
				Map<Object, Object> toUserMap = PublicQueryUtil.getInstance().findUserCommonMap(postMap.get("userid").toString());;
				if(toUserMap == null)
					continue;
				dynamicBean.setToUserID(toUserMap.get("userid").toString());
				dynamicBean.setToNickName(toUserMap.get("nickname").toString());
				dynamicBean.setToUserLevel(Integer.parseInt(toUserMap.get("lid").toString()));
				dynamicBean.setToUserVerified(Integer.parseInt(toUserMap.get("verified").toString()));
				dynamicBean.setToUserHref(CommonUtil.getUserIndexHref(toUserMap.get("userid").toString(), toUserMap.get("wkey").toString() ) );
				dynamicBean.setViewCount(PostService.getInstance().getPostViewCount(dynamicBean.getPostID()));
				dynamicBean.setRecommendCount(RecommendService.getInstance().getPostRecommendCount(dynamicBean.getPostID()));
			}else if(XingyunCommonConstant.DYNAMIC_TYPE_USER_TJ == dynamicType){		//推荐用户动态
				Map<Object, Object> recommendUserMap = findDynamicRecommendUserMap(topicid);
				if(recommendUserMap == null)
					continue;
				dynamicBean.setToUserID(recommendUserMap.get("userid").toString());
				dynamicBean.setRecommendContent(recommendUserMap.get("reason").toString());
				dynamicBean.setToNickName(recommendUserMap.get("nickname").toString());
				dynamicBean.setToWkey(recommendUserMap.get("wkey").toString());
				dynamicBean.setToUserLevel(Integer.parseInt(recommendUserMap.get("lid").toString()));
				dynamicBean.setToUserVerified(Integer.parseInt(recommendUserMap.get("verified").toString()));
				dynamicBean.setToUserHref(CommonUtil.getUserIndexHref(recommendUserMap.get("userid").toString(), recommendUserMap.get("wkey").toString() ) );
				dynamicBean.setToUserLogo(UploadPicUtil.getPicWebUrl(recommendUserMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_150));
				dynamicBean.setRecommendCount(UserHeaderService.getInstance().getRecommendUserCount(dynamicBean.getToUserID()));
				int isShowfollow = UserHeaderService.getInstance().checkIsShowfollow(dynamicBean.getToUserID());
				dynamicBean.setIsShowfollow(isShowfollow);
				if(isShowfollow == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
					dynamicBean.setFansCount(FollowService.getInstance().getFansCount(dynamicBean.getToUserID()));
			}else if(XingyunCommonConstant.DYNAMIC_TYPE_XINGYU == dynamicType){		//星语动态
				xyBean = XingyuService.getInstance().findXingYuData(Integer.parseInt(topicid), userID);
				if(xyBean != null)
					dynamicBean.setXyBean(xyBean);
			}else if(XingyunCommonConstant.DYNAMIC_TYPE_CONTACT == dynamicType){
				dynamicBean.setContactMap(setContactDynamciData(fromUserID, userMap));
			}else if(XingyunCommonConstant.DYNAMIC_TYPE_JOBSTATUS == dynamicType){
				dynamicBean.setJobStatus(setJobStatusDynamicData(fromUserID));
			}else if(XingyunCommonConstant.DYNAMIC_TYPE_SKILL == dynamicType){
				dynamicBean.setSkillList(FollowService.getInstance().getSkillListByUserId(fromUserID));
			}else if(XingyunCommonConstant.DYNAMIC_TYPE_RENCAI == dynamicType){
				dynamicBean.setRencaiList(setRencaiDynamicData(fromUserID));
			}else if(XingyunCommonConstant.DYNAMIC_TYPE_SLIDER == dynamicType){
				dynamicBean.setSliderList(ProfileService.getInstance().getUserFaceList(fromUserID));
			}else if(XingyunCommonConstant.DYNAMIC_TYPE_VIDEO == dynamicType){
				dynamicBean.setVideoList(ProfileService.getInstance().getVideoList(fromUserID));
			}else if(XingyunCommonConstant.DYNAMIC_TYPE_PROFILE == dynamicType){
				
			}else if(XingyunCommonConstant.DYNAMIC_TYPE_VERIFIED == dynamicType){
				
			}else{
				continue;
			}
			dynamicList.add(dynamicBean);
		}
		return dynamicList;
	}
	
	/**
	 * 获取动态 好友用户ID
	 */
	private List<Map<Object,Object>> getFriendUserIdList(String userId) throws Throwable{
		String sql = "SELECT touserid FROM xy_friend_relation WHERE fromuserid = ? AND touserid != ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(XingyunSystemConstant.getSystemConstantValue(XingyunSystemConstant.XINGYUN_UID));
		return db.retrieveSQL(sql, valueList);
	}
	/**
	 * 获取动态 关注人用户ID
	 */
	private List<Map<Object,Object>> getFollowUserIdList(String userId) throws Throwable{
		String sql = "SELECT touserid FROM follow_user WHERE fromuserid = ? AND touserid != ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(XingyunSystemConstant.getSystemConstantValue(XingyunSystemConstant.XINGYUN_UID));
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 查询发布作品动态数据
	 */
	private Map<Object, Object> findDynamicPostData(int postID) throws Throwable{
		String sql = "SELECT title, coverpath, posttype FROM post WHERE id = ? AND status = ? AND isdel = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(postID);
		vList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, vList);
		return list.size() == 0 ? null : list.get(0);
	}
	
	/**
	 * 查询 推荐作品动态数据
	 */
	private Map<Object, Object> findDynamicRecommendPostData(int recommendID) throws Throwable{
		String sql = "SELECT r.reason, p.id, p.posttype, p.title, p.coverpath, p.userid FROM recommend r, post p WHERE p.id = r.topicid AND r.id = ? AND r.type = ? AND p.status = ? AND p.isdel = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(recommendID);
		vList.add(XingyunCommonConstant.RECOMMEND_TYPE_POST);
		vList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, vList);
		return list.size() == 0 ? null : list.get(0);
	}
	
	/**
	 * 查询 推荐用户动态数据
	 */
	private Map<Object, Object> findDynamicRecommendUserMap(String recommendID) throws Throwable{
		String sql = "SELECT r.reason, u.userid, u.nickname, u.wkey, u.lid, u.logourl, u.verified FROM recommend r, user u WHERE r.id = ? AND r.type = ? AND r.topicid = u.userid";
		List<Object> vList = new ArrayList<Object>();
		vList.add(recommendID);
		vList.add(XingyunCommonConstant.RECOMMEND_TYPE_USER);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, vList);
		return list.size() == 0 ? null : list.get(0);
	}
	
	/**
	 * 获取系统消息总数
	 * 使用索引：index_message_system_userid
	 */
	public int getSysMessageCount(String userID) throws Throwable{
		String sql = "SELECT COUNT(*) FROM message_system WHERE userid = ?";
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(userID);
		return db.getRecordCountSQL(sql, paramList);
	}
	
	/**
	 * 获取系统消息数据
	 * 使用索引：index_message_system_userid
	 */
	public List<Map<Object, Object>> getSysMessageData(String userID, int pageIndex) throws Throwable{
		pageIndex = pageIndex <= 0 ? 1 : pageIndex > XingyunMyIndexConstant.MYINDEX_SYSMESSAGE_MAX_PAGE ? XingyunMyIndexConstant.MYINDEX_SYSMESSAGE_MAX_PAGE : pageIndex;
		String sql = "SELECT id, type, userid, postid, systime FROM message_system WHERE userid = ? ORDER BY id DESC LIMIT ?, ?";
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(userID);
		paramList.add((pageIndex - 1) * XingyunMyIndexConstant.MYINDEX_SYSMESSAGE_PAGE_SIZE );
		paramList.add(XingyunMyIndexConstant.MYINDEX_SYSMESSAGE_PAGE_SIZE);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, paramList);
		if (list.size() == 0) 
			return null;
		return setSysMessageData(list);
	}
	
	/**
	 * 设置系统消息数据
	 */
	private List<Map<Object, Object>> setSysMessageData(List<Map<Object, Object>> list) throws Throwable {
		List<Map<Object,Object>> sysList = new ArrayList<Map<Object,Object>>();
		Map<Object, Object> postInfoMap = null;
		int type = 0;
		int postId = 0;
		String xingyunUID = XingyunSystemConstant.getSystemConstantValue(XingyunSystemConstant.XINGYUN_UID);
		Map<Object,Object> xingyunUserMap = PublicQueryUtil.getInstance().findUserCommonMap(xingyunUID);
		for(Map<Object,Object> map : list){
			map.put("userHref", xingyunUserMap.get("userHref") );
			map.put("logourl", UploadPicUtil.getPicWebUrl(xingyunUserMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50) );
			map.put("nickname", xingyunUserMap.get("nickname"));
			map.put("lid", xingyunUserMap.get("lid"));
			map.put("verified", xingyunUserMap.get("verified"));
			map.put("systime", DateUtil.getBlogSystime((Date)map.get("systime") ) );   // 系统时间
			type = CommonUtil.getIntValue(map.get("type"));
			postId = CommonUtil.getIntValue(map.get("postid"));
			if(type == XingyunCommonConstant.MESSAGE_SYSTEM_TYPE_POST
					|| type == XingyunCommonConstant.MESSAGE_SYSTEM_TYPE_POST_INDEX
					|| type == XingyunCommonConstant.MESSAGE_SYSTEM_TYPE_POST_PINDAO){
				postInfoMap = getUserInfoByPostId(postId);
				if (postInfoMap == null)
					continue;
				map.put("content", postInfoMap.get("title"));
				map.put("tradeid", postInfoMap.get("tradeid"));
				map.put("tradeName", postInfoMap.get("name"));
				map.put("postUserHref", CommonUtil.getUserIndexHref(CommonUtil.getStringValue(postInfoMap.get("userid")), postInfoMap.get("wkey")));
			}
			if(type == XingyunCommonConstant.MESSAGE_SYSTEM_TYPE_NOTICE)
				map.put("content", getNoticeContentById(postId));
			sysList.add(map);
		}
		return sysList;
	}
	
	/**
	 * 通过作品ID获取作品作者的信息
	 */
	private Map<Object,Object> getUserInfoByPostId(int postId) throws Throwable{
		String sql = "SELECT p.title, p.tradeid, d.name, u.userid, u.wkey FROM post p, user u, dic_trade d WHERE p.userid = u.userid AND p.tradeid = d.id AND p.id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	
	/**
	 * 查找可能认识的人
	 */
	public List<Map<Object,Object>> setMayKnowUserInfoList(List<Map<Object,Object>> list) throws Throwable{
		if(list == null || list.size() == 0)
			return null;
		Collections.shuffle(list);
		if(list.size() > XingyunMyIndexConstant.MYINDEX_MAYKNOW_SIZE)
			list = CommonUtil.subList(list, 1, XingyunMyIndexConstant.MYINDEX_MAYKNOW_SIZE);
		return setMayKnowUserList(list);
	}
	
	/**
	 * 获取可能认识用户的集合
	 * 使用索引：index_weibo_gzlist_userid  index_weibo_profile_sinauserid
	 */
	public List<Map<Object,Object>> getMayKnowUserIndexList(String userId) throws Throwable{
		String sql = "SELECT st.userid FROM weibo_profile st, weibo_doublelist rsg, user u WHERE st.sinauserid = rsg.sinauserid AND st.userid = u.userid AND rsg.userid = ? AND u.lid != ? LIMIT ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(XingyunCommonConstant.USER_LEVEL_YOUKE);
		valueList.add(XingyunMyIndexConstant.MYINDEX_MAYKNOW_MAX_SIZE);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		List<Map<Object,Object>> gzList = getNoFollowIdList(userId, list);
		if(gzList.size() == 0)
			return null;
		return gzList;
	}
	/**
	 * 关闭可能认识的人
	 */
	public void closeMayKnowUser(String fromUserId, String toUserId) throws Throwable{
		if(checkUserInMayKnowClose(fromUserId, toUserId))
			return;
		String sql = "INSERT INTO weibo_doublelist_close(fromuserid,touserid,systime) VALUES(?,?,?)";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		valueList.add(new Date());
		db.insertData(sql, valueList);
	}
	/**
	 * 检测用户是否已在关闭的可能认识的人里
	 */
	private boolean checkUserInMayKnowClose(String fromUserId, String toUserId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM weibo_doublelist_close WHERE fromuserid = ? AND touserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(fromUserId);
		valueList.add(toUserId);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
	/**
	 * 查找最新加入的大侠信息
	 */
	public List<Map<Object, Object>> getNewDaXiaList() throws Throwable {
		String sql = "SELECT userid FROM cms_user_new_daxia ORDER BY id DESC LIMIT ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(XingyunMyIndexConstant.MYINDEX_NEW_JOINED_DAXIA_SIZE);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return setNewUserList(list);
	}
	
	/**
	 * 查找最新加入的精英信息
	 */
	public List<Map<Object, Object>> getNewJingYingList() throws Throwable {
		String sql = "SELECT userid FROM cms_user_new_jingying ORDER BY id DESC LIMIT ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(XingyunMyIndexConstant.MYINDEX_NEW_JOINED_JINGYING_SIZE);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return setNewUserList(list);
	}
	
	/**
	 * 设置最新加入的用户详细信息
	 */
	public List<Map<Object, Object>> setNewUserList(List<Map<Object, Object>> newUserlist) throws Throwable {
		String userId = "";
		Map<Object, Object> userInfoMap = null;
		Map<Object,Object> userProfile = null;
		List<Map<Object,Object>> startList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> map : newUserlist){
			userId = map.get("userid").toString();
			userInfoMap = PublicQueryUtil.getInstance().findUserCommonMap(userId);
			if (userInfoMap == null )
				continue;
			map.putAll(userInfoMap);
			userProfile = ProfileService.getInstance().getNewStarInfoList(userId);
			if (userProfile != null) {
				map.put("constellation", userProfile.get("constellation")); 								// 星座
				map.putAll(AreaUtil.getInstance().setUserLocationMap(CommonUtil.getIntValue(userProfile.get("provinceid")), CommonUtil.getIntValue(userProfile.get("cityid"))));
			}
			map.put("verifyContent", userInfoMap.get("verified_reason")); 	// 认证信息
			map.put("logourl", UploadPicUtil.getPicWebUrl(userInfoMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50));	// 图像URL
			startList.add(map);
		}
		return startList;
	}
	
	/**
	 * 获取星云推荐的作品信息
	 */
	public List<Map<Object, Object>> getRecommWorks() throws Throwable {
		String sql = "SELECT postid FROM cms_post_strong ORDER BY id DESC LIMIT ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(XingyunMyIndexConstant.MYINDEX_XINGYUN_RECOMMEND_POST_SIZE);
		List<Map<Object,Object>> worklist = db.retrieveSQL(sql, valueList);
		if (worklist.size() == 0)
			return null;
		return setRecommWorks(worklist);
	}
	
	/**
	 * 设置星云推荐的作品详细信息
	 */
	public List<Map<Object, Object>> setRecommWorks(List<Map<Object, Object>> worklist) throws Throwable {
		int postId = 0;
		String userId = "";
		Map<Object, Object> userInfoMap = null;
		Map<Object, Object> postContent = null;
		List<Map<Object,Object>> recommList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> postMap : worklist){
			postId = Integer.valueOf(postMap.get("postid").toString());
			postContent = findPostContentByID(postId);
			if (postContent == null)
				continue;
			postMap.putAll(postContent);
			postMap.put("coverpath", UploadPicUtil.getPicWebUrl(postMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_220));// 作品链接
			userId = CommonUtil.getStringValue(postContent.get("userid"));
			userInfoMap = PublicQueryUtil.getInstance().findUserCommonMap(userId);
			if (userInfoMap == null )
				continue;
			postMap.putAll(userInfoMap);
			postMap.put("trades", getVocationByUserId(userId));
			postMap.put("verifyContent", postMap.get("verified_reason"));// 认证信息
			recommList.add(postMap);
		}
		return recommList;
	}

	/**
	 * 获取特定用户所从事的行业 
	 * 使用索引:index_user_vocation_userid
	 */
	private List<Map<Object,Object>> getVocationByUserId(String userId) throws Throwable{
		String sql = "SELECT dv.name AS name FROM user_vocation uv, dic_trade dv WHERE uv.tradeid = dv.id AND uv.userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return new ArrayList<Map<Object,Object>>(new HashSet<Map<Object,Object>>(list));
	}
	
	/**
	 * 根据postID查找 作品信息
	 */
	public Map<Object, Object> findPostContentByID(int postID) throws Throwable{
		String sql = "SELECT id, userid, posttype, title, coverpath, displaytype, tradeid, classid, status, isindex, isdel, updatetime, systime " +
					"FROM post WHERE id = ? AND status = ? AND isdel = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postID);
		valueList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		valueList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, valueList);
		return list.size() == 0 ? null : list.get(0);
	}
	
	/**
	 * 获取新浪微博关注列表里且没关注的用户id集合
	 * 使用索引：index_follow_user_fromuserid
	 */
	private List<Map<Object,Object>> getNoFollowIdList(String userId, List<Map<Object,Object>> userIdList) throws Throwable{
		String userIds = CommonUtil.getStringUserID(userIdList, "userid");
		String sql = "SELECT touserid FROM follow_user WHERE fromuserid = '"+userId+"' AND touserid IN("+userIds+")";
		List<Map<Object,Object>> list = db.retrieveSQL(sql);
		userIds = CommonUtil.getStringUserID(list, "touserid"); 
		List<String> followList = Arrays.asList(userIds);
		List<String> closeList = getMayKnowUserCloseList(userId);
		List<String> followToUserIdList = getFollowIndexList(userId);
		List<Map<Object,Object>> gzList = new ArrayList<Map<Object,Object>>();
		String toUserId;
		for(Map<Object,Object> map : userIdList){
			toUserId = map.get("userid").toString();
			if(!followList.contains(toUserId) && !closeList.contains(toUserId) && !followToUserIdList.contains(toUserId))
				gzList.add(map);
		}
		return gzList;
	}
	/**
	 * 可能认识的人，已关闭用户
	 */
	private List<String> getMayKnowUserCloseList(String userId) throws Throwable{
		List<String> closeList = new ArrayList<String>();
		String sql = "SELECT touserid FROM weibo_doublelist_close WHERE fromuserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return closeList;
		for(Map<Object,Object> map : list){
			closeList.add(map.get("touserid").toString());
		}
		return closeList;
	}
	
	/**
	 * 取用户的关注用户
	 */
	private List<String> getFollowIndexList(String userId) throws Throwable{
		List<String> followList = new ArrayList<String>();
		String sql = "SELECT touserid FROM follow_user WHERE fromuserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return followList;
		for(Map<Object,Object> map : list){
			followList.add(map.get("touserid").toString());
		}
		return followList;
	}
	
	/**
	 * 设置可能认识的人
	 * @param list 可能认识的人的集合列表
	 */
	private List<Map<Object,Object>> setMayKnowUserList(List<Map<Object,Object>> list) throws Throwable{
		String userId = "";
		Map<Object,Object> tmpMap = null;
		List<Map<Object,Object>> userList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> map : list){
			userId = map.get("userid").toString();
			tmpMap = PublicQueryUtil.getInstance().findUserCommonMap(userId);
			if(tmpMap == null)
				continue;
			tmpMap.put("logourl", UploadPicUtil.getPicWebUrl(tmpMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50));
			tmpMap.put("userid", userId);
			userList.add(tmpMap);
		}
		return userList;
	}
	
	private String getNoticeContentById(int noticeId) throws Throwable{
		String sql = "SELECT content FROM cms_notice WHERE id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(noticeId);
		return CommonUtil.getStringValue(sql, valueList, "content");
	}
	
	/**
	 * 判断星语动态是否显示
	 */
	private boolean checkShowXingYuDynamic(int xingyuID, String xyUsreID, String usreID) throws Throwable{
		String sql = "SELECT showtype FROM xingyu WHERE id = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(xingyuID);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, vList, true);
		if(list == null || list.size() == 0)
			return false;
		
		if(xyUsreID.equals(usreID))
			return true;
		
		int showType = Integer.parseInt(list.get(0).get("showtype").toString());
		if(XingyunCommonConstant.XINGYU_SHOW_TYPE_PUB == showType)
			return true;
		
		//好友
		if(XingyunCommonConstant.XINGYU_SHOW_TYPE_HY == showType && FriendService.getInstance().checkFriendRelation(xyUsreID, usreID))
			return true;
				
		return false;
	}
	/**
	 * 排除已删除的动态
	 * 使用索引：index_dynamic_del_userid
	 */
	private List<Map<Object,Object>> getDynamicDelIndexList(String userId, List<Map<Object,Object>> dynamicIndexList) throws Throwable{
		String dynamicIds = CommonUtil.getStringUserID(dynamicIndexList, "id");
		String sql = "SELECT dynamicid FROM dynamic_del WHERE userid = ? AND dynamicid IN ("+dynamicIds+")";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return dynamicIndexList;
		List<Object> dynamicDelList = new ArrayList<Object>();
		for(Map<Object,Object> map : list){
			dynamicDelList.add(map.get("dynamicid"));
		}
		List<Map<Object,Object>> dynamicList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> map : dynamicIndexList){
			if(dynamicDelList.contains(map.get("id")))
				continue;
			dynamicList.add(map);
		}
		return dynamicList;
	}
	
	/**
	 * 删除动态
	 */
	public void delDynamicById(String userId, int dynamicId) throws Throwable{
		if(checkUserInDynamicDel(userId, dynamicId))
			return;
		String sql = "INSERT INTO dynamic_del(userid,dynamicid,systime) VALUES(?,?,?)";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(dynamicId);
		valueList.add(new Date());
		db.insertData(sql, valueList);
	}
	
	/**
	 * 检测用户是否已删除过该条动态
	 * 使用索引：index_dynamic_del_userid
	 */
	private boolean checkUserInDynamicDel(String userId, int dynamicId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM dynamic_del WHERE userid = ? AND dynamicid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(dynamicId);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
	/**
	 * 获取动态中的联络方式
	 */
	private Map<Object,Object> setContactDynamciData(String userId, Map<Object,Object> userMap) throws Throwable{
		String sql = "SELECT mobile,qq,msn,weixin,blogurl,brokertel FROM user_profile WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		Map<Object,Object> profileMap = list.get(0);
		Map<Object,Object> contactMap = new HashMap<Object, Object>();
		List<String> contactStatusList = ProfileService.getInstance().getContactStatusList(userId);
		for(String status : contactStatusList){
			if(XingyunCommonConstant.USER_PROFILE_STATUS_PHONE.equals(status))
				contactMap.put("mobile", profileMap.get("mobile"));
			else if(XingyunCommonConstant.USER_PROFILE_STATUS_QQ.equals(status))
				contactMap.put("qq", profileMap.get("qq"));
			else if(XingyunCommonConstant.USER_PROFILE_STATUS_WEIXIN.equals(status))
				contactMap.put("weixin", profileMap.get("weixin"));
			else if(XingyunCommonConstant.USER_PROFILE_STATUS_MSN.equals(status))
				contactMap.put("msn", profileMap.get("msn"));
			else if(XingyunCommonConstant.USER_PROFILE_STATUS_WEIBOURL.equals(status))
				contactMap.put("blogurl", profileMap.get("blogurl"));
			else if(XingyunCommonConstant.USER_PROFILE_STATUS_BROKERPHONE.equals(status))
				contactMap.put("brokertel", profileMap.get("brokertel"));
		}
		if(contactStatusList.contains("email"))
			return contactMap;
		contactMap.put("email", userMap.get("email"));
		return contactMap;
	}
	/**
	 * 获取动态中的目前职业状态
	 */
	private String setJobStatusDynamicData(String userId) throws Throwable{
		String sql = "SELECT job_status FROM user WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return UserHeaderService.getInstance().getJobStatusName(CommonUtil.getIntValue(sql, valueList, "job_status"));
	}
	
	private List<Map<Object,Object>> setRencaiDynamicData(String userId) throws Throwable{
		String sql = "SELECT tradeid0, tradeid1 FROM cms_user_rencai WHERE userid = ? AND istrade = 1";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		Map<Object,Object> map = list.get(0);
		sql = "SELECT id, name FROM dic_trade WHERE id IN("+map.get("tradeid0")+","+map.get("tradeid1")+")";
		return db.retrieveSQL(sql);
	}
}
