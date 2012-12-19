package com.xingyun.services.header;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.xingyun.bean.User;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunFaceConstant;
import com.xingyun.constant.XingyunPostConstant;
import com.xingyun.constant.XingyunSystemConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.comment.CommentService;
import com.xingyun.services.follow.FollowService;
import com.xingyun.services.friend.FriendService;
import com.xingyun.services.post.PostService;
import com.xingyun.services.profile.ProfileService;
import com.xingyun.services.recommend.RecommendService;
import com.xingyun.services.xingyu.XingyuService;
import com.xingyun.services.zan.ZanService;
import com.xingyun.util.AreaUtil;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.CounterUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.UploadPicUtil;

public class UserHeaderService {
	private static final DBOperate db = new DBOperate();
	private static final UserHeaderService userHeaderService = new UserHeaderService();
	private UserHeaderService(){}
	public static UserHeaderService getInstance(){
		return userHeaderService;
	}
	/**
	 * 整理页面头部信息   普通用户
	 * @param user		当前访问的用户信息
	 * @param userId		被访问用户userID
	 */
	public UserHeaderBean initUserHeaderData(User user, String userId) throws Throwable{		
		Map<Object,Object> toUserMsgMap = PublicQueryUtil.getInstance().findUserCommonMap(userId);
		if(toUserMsgMap == null || toUserMsgMap.size() == 0)
			return null;
		return setUserHeaderData(user, toUserMsgMap);	//整理头部数据
	}
	
	public UserHeaderBean initUserLeftData(User user, String userId) throws Throwable{
		Map<Object,Object> toUserMsgMap = PublicQueryUtil.getInstance().findUserCommonMap(userId);
		if(toUserMsgMap == null || toUserMsgMap.size() == 0)
			return null;
		return setUserLeftData(user, toUserMsgMap);
	}
	
	public UserHeaderBean initIphoneUserData(String userId, String fromType) throws Throwable{
		Map<Object,Object> toUserMsgMap = PublicQueryUtil.getInstance().findUserCommonMap(userId);
		if(toUserMsgMap == null || toUserMsgMap.size() == 0)
			return null;
		return setIphoneUserData(toUserMsgMap, fromType);
	}
	/**
	 * 整理用户头部数据  普通用户
	 */
	private UserHeaderBean setUserHeaderData(User user, Map<Object,Object> toUserMsgMap) throws Throwable{
		UserHeaderBean userHeaderBean = new UserHeaderBean();
		String toUserID = CommonUtil.getStringValue(toUserMsgMap.get("userid"));
		String fromUserID = user == null ? "" : user.getUserId();
		String lookState = CommonUtil.getLookState(user, toUserID);
		userHeaderBean.setUserId(toUserID); //用户userid
		userHeaderBean.setEmail(toUserMsgMap.get("email").toString());
		userHeaderBean.setLogoUrl(UploadPicUtil.getPicWebUrl(toUserMsgMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_200));	//用户头像图片访问路径
		userHeaderBean.setLogoUrl_640(UploadPicUtil.getPicWebUrl(toUserMsgMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_640));	// 用户头像原始大小图片访问路径
		userHeaderBean.setIsDefaultLogo(XingyunUploadFileConstant.LOGO_SYS_RESID.equals(toUserMsgMap.get("logourl").toString()));
		userHeaderBean.setNickName(toUserMsgMap.get("nickname").toString()); //用户nickname
		userHeaderBean.setLid(CommonUtil.getIntValue(toUserMsgMap.get("lid"))); //用户等级
		userHeaderBean.setJoinDate(DateUtil.getSimpleDate((Date)toUserMsgMap.get("joinDate")));
		userHeaderBean.setLookState(lookState); //整理当前用户的访问状态	
		userHeaderBean.setVerified(CommonUtil.getIntValue(toUserMsgMap.get("verified")));
		userHeaderBean.setVerified_reason(CommonUtil.getStringValue(toUserMsgMap.get("verified_reason")));	 //用户认证职务
		userHeaderBean.setVocationList(getUserVocationByUserId(toUserID)); //用户职业数据
		userHeaderBean.setIntroduction(CommonUtil.getStringValue(toUserMsgMap.get("introduction"))); //一句话简介
		userHeaderBean.setFollowType(FollowService.getInstance().checkFollowType(fromUserID, toUserID)); //收藏类型(收藏，未收藏)
		userHeaderBean.setLocation(AreaUtil.getInstance().getUserAddressMap(toUserID, false)); //设置用户所在地
		Map<Object,Object> followCountMap = FollowService.getInstance().getFollowCountMap(toUserID);
		userHeaderBean.setFansCount(Integer.parseInt(followCountMap.get("fanscount").toString()));
		int followCount = Integer.parseInt(followCountMap.get("followcount").toString());
		if(!XingyunSystemConstant.getSystemConstantValue(XingyunSystemConstant.XINGYUN_UID).equals(toUserID))
			followCount = followCount - 1;
		userHeaderBean.setFollowCount(followCount);
		userHeaderBean.setRecommendUserCount(getRecommendUserCount(toUserID));
		userHeaderBean.setRecommendPoint(getRecommendPoint(userHeaderBean.getRecommendUserCount()));
		userHeaderBean.setIsRecommendUser(false);
		if(XingyunCommonConstant.USER_SEE.equals(userHeaderBean.getLookState()))
			userHeaderBean.setIsRecommendUser(RecommendService.getInstance().checkIsRecommendOverByType(XingyunCommonConstant.RECOMMEND_TYPE_USER, fromUserID, toUserID) > 0);
		userHeaderBean.setVisitCount(getUserVisitCount(toUserID));
		userHeaderBean.setPicPostCount(PostService.getInstance().getUserTotalPost(toUserID, XingyunPostConstant.POST_TYPE_PIC, lookState));
		userHeaderBean.setVideoPostCount(PostService.getInstance().getUserTotalPost(toUserID, XingyunPostConstant.POST_TYPE_VIDEO, lookState));
		userHeaderBean.setRecommendAllCount(getRecommendAllCount(toUserID));
		userHeaderBean.setProfileCount(getProfileCount(toUserID));
		userHeaderBean.setXingyuCount(XingyuService.getInstance().findXingYuAllIndex(toUserID, user).size());	//星语数量
		userHeaderBean.setUserHref(CommonUtil.getUserIndexHref(toUserID, toUserMsgMap.get("wkey")));
		userHeaderBean.setIsXingyunUID(CommonUtil.checkIsXingyunUID(toUserID));
		userHeaderBean.setGender(getGenderByUserId(toUserID));
		userHeaderBean.setIsShowfollow(checkIsShowfollow(toUserID));
		userHeaderBean.setFriendRelationType(XingyunCommonConstant.FRIEND_RELATION_NO);
		if(!XingyunCommonConstant.USER_FREE.equals(lookState))
			userHeaderBean.setFriendRelationType(FriendService.getInstance().checkFriendRelationTypeToMessage(fromUserID, toUserID));
		userHeaderBean.setScreenName(PublicQueryUtil.getInstance().getScreenNameByUserId(toUserID));
		userHeaderBean.setXyProxy(getXyProxyByUserID(toUserID));
		userHeaderBean.setJobStatus(getJobStatusName(CommonUtil.getIntValue(toUserMsgMap.get("job_status"))));	//求职状态
		userHeaderBean.setIsRencai(getIsRencai(toUserID));	//是否星云人才
//		userHeaderBean.setIsPayUser(getIsPayUser(toUserID));
		// 联系方式
		setContactInfo(toUserID, userHeaderBean);
		return userHeaderBean;
	}
	
	/**
	 * 整理头部联系方式
	 */
	private void setContactInfo(String toUserID, UserHeaderBean userHeaderBean) throws Throwable{
		userHeaderBean.setContactStatusList(ProfileService.getInstance().getContactStatusList(toUserID) );
		Map<Object,Object> contactMap = ProfileService.getInstance().getUserContactInfoList(toUserID);
		if(userHeaderBean.getContactStatusList() != null && userHeaderBean.getContactStatusList().size() > 0 && contactMap != null){
			for(String key : userHeaderBean.getContactStatusList()){
				if(XingyunCommonConstant.USER_PROFILE_STATUS_PHONE.equals(key))
					userHeaderBean.setMobile(CommonUtil.getStringValue(contactMap.get("mobile") ) );
				else if(XingyunCommonConstant.USER_PROFILE_STATUS_QQ.equals(key))
					userHeaderBean.setQq(CommonUtil.getStringValue(contactMap.get("qq") ) );
				else if(XingyunCommonConstant.USER_PROFILE_STATUS_WEIXIN.equals(key))
					userHeaderBean.setWeixin(CommonUtil.getStringValue(contactMap.get("weixin") ) );
				else if(XingyunCommonConstant.USER_PROFILE_STATUS_MSN.equals(key))
					userHeaderBean.setMsn(CommonUtil.getStringValue(contactMap.get("msn") ) );
				else if(XingyunCommonConstant.USER_PROFILE_STATUS_WEIBOURL.equals(key))
					userHeaderBean.setWeibourl(CommonUtil.getStringValue(contactMap.get("blogurl") ) );
				else if(XingyunCommonConstant.USER_PROFILE_STATUS_BROKERPHONE.equals(key))
					userHeaderBean.setBrokertel(CommonUtil.getStringValue(contactMap.get("brokertel") ) );
			}
		}
	}
	
	private UserHeaderBean setUserLeftData(User user, Map<Object,Object> toUserMsgMap) throws Throwable{
		UserHeaderBean userHeaderBean = new UserHeaderBean();
		String toUserID = CommonUtil.getStringValue(toUserMsgMap.get("userid"));
		String lookState = CommonUtil.getLookState(user, toUserID);
		userHeaderBean.setUserId(toUserID); //用户userid
		userHeaderBean.setLogoUrl(UploadPicUtil.getPicWebUrl(toUserMsgMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_200));	//用户头像图片访问路径
		userHeaderBean.setNickName(toUserMsgMap.get("nickname").toString()); //用户nickname
		userHeaderBean.setLid(CommonUtil.getIntValue(toUserMsgMap.get("lid"))); //用户等级
		userHeaderBean.setLookState(lookState); //整理当前用户的访问状态	
		userHeaderBean.setUserHref(CommonUtil.getUserIndexHref(toUserID, toUserMsgMap.get("wkey")));
		userHeaderBean.setGender(getGenderByUserId(toUserID)); //用户性别
		userHeaderBean.setVerified(user != null ? user.getVerified() : CommonUtil.getIntValue(toUserMsgMap.get("verified")));	// 用户是否认证
		userHeaderBean.setXyNumber(CommonUtil.getStringValue(toUserMsgMap.get("xynumber")));
		userHeaderBean.setPicPostCount(PostService.getInstance().getUserTotalPost(toUserID, XingyunPostConstant.POST_TYPE_PIC, lookState));
		userHeaderBean.setVideoPostCount(PostService.getInstance().getUserTotalPost(toUserID, XingyunPostConstant.POST_TYPE_VIDEO, lookState));
		userHeaderBean.setRecommendUserCount(getRecommendUserCount(toUserID));
		userHeaderBean.setRecommendPoint(getRecommendPoint(userHeaderBean.getRecommendUserCount()));
		userHeaderBean.setRecommendAllCount(getRecommendAllCount(toUserID));
		Map<Object,Object> followCountMap = FollowService.getInstance().getFollowCountMap(toUserID);
		userHeaderBean.setFansCount(Integer.parseInt(followCountMap.get("fanscount").toString()));
		int followCount = Integer.parseInt(followCountMap.get("followcount").toString());
		if(!XingyunSystemConstant.getSystemConstantValue(XingyunSystemConstant.XINGYUN_UID).equals(toUserID))
			followCount = followCount - 1;
		userHeaderBean.setFollowCount(followCount);
		if(XingyunCommonConstant.USER_EDIT.equals(lookState)){
			userHeaderBean.setFriendCount(FriendService.getInstance().getFriendCount(toUserID));
			userHeaderBean.setCommentCount(CommentService.getInstance().getCommendCount(toUserID));
			userHeaderBean.setZanCount(ZanService.getInstance().getZanCount(toUserID, XingyunCommonConstant.COMMENT_TYPE_RECEIVE, XingyunCommonConstant.COMMENT_SOURCE_ALL));
			userHeaderBean.setXingyuCount(XingyuService.getInstance().findXingYuAllIndex(toUserID, user).size());	//星语数量
			userHeaderBean.setRecommentToUserAndPostCount(RecommendService.getInstance().findRecommendToUserCount(toUserID, 0) + RecommendService.getInstance().findRecommendToPostCount(toUserID));
			userHeaderBean.setProfileCount(getProfileCount(toUserID));
			userHeaderBean.setCollectionPostCount(PostService.getInstance().getCollectionPostCount(user.getUserId()));	//收藏作品数量
			userHeaderBean.setBiFollowCount(FollowService.getInstance().getBiFollowCount(toUserID));
		}
//		userHeaderBean.setIsPayUser(getIsPayUser(toUserID));
		return userHeaderBean;
	}
	
	private UserHeaderBean setIphoneUserData(Map<Object,Object> toUserMsgMap, String fromType) throws Throwable{
		UserHeaderBean userHeaderBean = new UserHeaderBean();
		String toUserID = CommonUtil.getStringValue(toUserMsgMap.get("userid"));
		userHeaderBean.setUserId(toUserID); //用户userid
		userHeaderBean.setLogoUrl(UploadPicUtil.getPicWebUrl(toUserMsgMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_150));	//用户头像图片访问路径
		userHeaderBean.setNickName(toUserMsgMap.get("nickname").toString()); //用户nickname
		userHeaderBean.setLid(CommonUtil.getIntValue(toUserMsgMap.get("lid"))); //用户等级
		userHeaderBean.setJoinDate(DateUtil.getSimpleDate((Date)toUserMsgMap.get("joinDate")));
		userHeaderBean.setVerified(CommonUtil.getIntValue(toUserMsgMap.get("verified")));
		userHeaderBean.setVerified_reason(CommonUtil.getStringValue(toUserMsgMap.get("verified_reason")));	 //用户认证职务
		userHeaderBean.setVocationList(getUserVocationByUserId(toUserID)); //用户职业数据
		userHeaderBean.setIntroduction(CommonUtil.getStringValue(toUserMsgMap.get("introduction"))); //一句话简介
		userHeaderBean.setUserHref(CommonUtil.getUserIndexHref(toUserID, toUserMsgMap.get("wkey")));
		if("profile".equals(fromType)){
			userHeaderBean.setIsShowfollow(checkIsShowfollow(toUserID));
			if(userHeaderBean.getIsShowfollow() == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
				userHeaderBean.setFansCount(FollowService.getInstance().getFansCount(toUserID));
			userHeaderBean.setRecommendUserCount(getRecommendUserCount(toUserID));
			userHeaderBean.setVisitCount(getUserVisitCount(toUserID));
		}
		return userHeaderBean;
	}
	public List<Map<Object,Object>> getUserVocationByUserId(String userId) throws Throwable{
		List<Map<Object,Object>> parentVocationList = getParentVocationByUserId(userId);
		if(parentVocationList == null)
			return null;
		int tradeId = 0, vocationId = 0;
		for(Map<Object,Object> map : parentVocationList){
			tradeId = Integer.parseInt(map.get("tradeid").toString());
			vocationId = Integer.parseInt(map.get("vocationid").toString());
			map.put("tradeName", getTradeName(tradeId));
			map.put("vocationName", getVocationName(vocationId));
			map.put("skillList", getSkillByUserId(userId, tradeId, vocationId));
		}
		return parentVocationList;
	}
	
	/**
	 *
	 * 使用索引：index_user_vocation_userid
	 */
	private List<Map<Object,Object>> getParentVocationByUserId(String userId) throws Throwable{
		String sql = "SELECT tradeid,vocationid FROM user_vocation WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return CommonUtil.setList(list);
	}
	
	/**
	 * 
	 * 使用索引：index_user_vocation_userid
	 */
	private List<Map<Object,Object>> getSkillByUserId(String userId, int tradeId, int vocationId) throws Throwable{
		String sql = "SELECT v2.id, v2.name FROM user_vocation uv,dic_skill v2 WHERE uv.skillid = v2.id AND uv.userid = ? AND uv.tradeid = ? AND uv.vocationid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(tradeId);
		valueList.add(vocationId);
		return db.retrieveSQL(sql, valueList);
	}
	/**
	 * 通过行业ID查询行业名
	 */
	public String getTradeName(int tradeId) throws Throwable{
		String sql = "SELECT name FROM dic_trade WHERE id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(tradeId);
		return CommonUtil.getStringValue(sql, valueList, "name");
	}
	/**
	 * 通过职业ID查询职业名
	 */
	private String getVocationName(int vocationId) throws Throwable{
		String sql = "SELECT name FROM dic_vocation WHERE id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(vocationId);
		return CommonUtil.getStringValue(sql, valueList, "name");
	}
	
	/**
	 * 查询性别
	 * 使用索引：index_user_profile_userid
	 */
	public int getGenderByUserId(String userId) throws Throwable{
		String sql = "SELECT gender FROM user_profile WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return CommonUtil.getIntValue(sql, valueList, "gender");
	}
	
	/**
	 * 使用索引：index_user_visit_counter_userid
	 */
	private int getUserVisitCount(String userId) throws Throwable{
		String sql = "SELECT homecount,profilecount,postcount,recommendcount,xingyucount FROM user_visit_counter WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return 0;
		Map<Object,Object> map = list.get(0);
		int allCount = CommonUtil.getIntValue(map.get("homecount")) + CounterUtil.getHomeCount(userId);
		allCount += CommonUtil.getIntValue(map.get("profilecount")) + CounterUtil.getProfileCount(userId);
		allCount += CommonUtil.getIntValue(map.get("postcount")) + CounterUtil.getPostCount(userId);
		allCount += CommonUtil.getIntValue(map.get("recommendcount")) + CounterUtil.getRecommendCount(userId);
		allCount += CommonUtil.getIntValue(map.get("xingyucount")) + CounterUtil.getXingyuCount(userId);
		return allCount;
	}
	
	/**
	 * 
	 * 使用索引：index_user_counter_userid
	 */
	public int getRecommendUserCount(String userId) throws Throwable{
		String sql = "SELECT recommendcount FROM user_counter WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return CommonUtil.getIntValue(sql, valueList, "recommendcount");
	}
	
	/**
	 * 查询用户推荐过的人和作品总数
	 */
	private static int getRecommendAllCount(String userId) throws Throwable{
		int recommendPostCount = RecommendService.getInstance().findRecommendPostCount(userId);
		int recommendFromUserCount = RecommendService.getInstance().findRecommendFromUserCount(userId, 0);
		int recommendToUserCount = RecommendService.getInstance().findRecommendToUserCount(userId, 0);
		return recommendPostCount + recommendFromUserCount + recommendToUserCount;
	}
	
	public int getRecommendPoint(int recommendCount) throws Throwable{
		if(recommendCount < 5)
			return 0;
		if(recommendCount >=5 && recommendCount < 10)
			return 1;
		if(recommendCount >=10 && recommendCount < 30)
			return 2;
		if(recommendCount >=30 && recommendCount < 120)
			return 3;
		if(recommendCount >=120 && recommendCount < 600)
			return 4;
		if(recommendCount >= 600)
			return 5;
		return 0;
	}
	
	/**
	 * 使用索引：index_face_userid
	 */
	public int checkIsShowfollow(String userId) throws Throwable{
		String sql = "SELECT isshowfollow FROM face WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return CommonUtil.getIntValue(sql, valueList, "isshowfollow");
	}
	
	private int getProfileCount(String userId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM user_profile_other WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return db.getRecordCountSQL(sql, valueList);
	}
	
	/**
	 * 根据用户ID判断用户是否加入星云商业代理
	 */
	public int getXyProxyByUserID(String userID) throws Throwable{
		String sql = "SELECT COUNT(*) FROM cms_user_proxy WHERE userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		int count = db.getRecordCountSQL(sql, vList);
		return count > 0 ? XingyunCommonConstant.USER_PROXY_YES : XingyunCommonConstant.USER_PROXY_NO;
	}
	
	/**
	 * 根据用户ID判断用户是否为商业付费会员
	 */
	public boolean getIsPayUser(String userID) throws Throwable{
		String sql = "SELECT COUNT(*) FROM pay_user WHERE userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		return db.getRecordCountSQL(sql, vList) > 0;
	}
	
	/**
	 * 	目前状态设置 
	 */
	public String getJobStatusName(int intValue) {
		if (intValue == XingyunCommonConstant.USER_JOB_STATUS_NONE) return "无";
		if (intValue == XingyunCommonConstant.USER_JOB_STATUS_ZAIZHI) return "在职";
		if (intValue == XingyunCommonConstant.USER_JOB_STATUS_JIANZHI) return "兼职";
		if (intValue == XingyunCommonConstant.USER_JOB_STATUS_CHUANGYE) return "创业";
		if (intValue == XingyunCommonConstant.USER_JOB_STATUS_QIUZHI) return "求职";
		if (intValue == XingyunCommonConstant.USER_JOB_STATUS_KEHEZUO) return "可合作";
		if (intValue == XingyunCommonConstant.USER_JOB_STATUS_ZIYOUZHIYE) return "自由职业";
		if (intValue == XingyunCommonConstant.USER_JOB_STATUS_SHIXI) return "实习";
		if (intValue == XingyunCommonConstant.USER_JOB_STATUS_ZAIXIAO) return "在校";
		return "无";
	}
	
	/**
	 * 查询用户是否为星云人才
	 */
	public boolean getIsRencai(String userID) throws Throwable{
		String sql = "SELECT COUNT(*) FROM cms_user_rencai_trade WHERE userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);		
		return db.getRecordCountSQL(sql, vList) > 0;
	}
}
