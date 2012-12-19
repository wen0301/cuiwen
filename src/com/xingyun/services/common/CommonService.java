package com.xingyun.services.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xingyun.bean.User;
import com.xingyun.constant.XingyunFaceConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.follow.FollowService;
import com.xingyun.services.friend.FriendService;
import com.xingyun.services.header.UserHeaderService;
import com.xingyun.util.AreaUtil;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.UploadPicUtil;

public class CommonService {
	private static final CommonService commonService = new CommonService();
	private CommonService(){}
	public static CommonService getInstance() {
		return commonService;
	}
	private static final DBOperate db = new DBOperate();
	
	public Map<Object,Object> getUserFloatLayerData(User user, String toUserId) throws Throwable{
		Map<Object,Object> userInfoMap = PublicQueryUtil.getInstance().findUserCommonMap(toUserId);
		if(userInfoMap == null)
			return null;
		String fromUserId = (user == null ? StringUtils.EMPTY : user.getUserId());
		userInfoMap.put("logourl", UploadPicUtil.getPicWebUrl(userInfoMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50));
		userInfoMap.put("verified", Integer.parseInt(userInfoMap.get("verified").toString()));
		int isShowFollow = UserHeaderService.getInstance().checkIsShowfollow(toUserId);
		userInfoMap.put("isShowfollow", isShowFollow);
		if(isShowFollow == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
			userInfoMap.put("fanscount", FollowService.getInstance().getFansCount(toUserId));
		userInfoMap.put("recommendCount", UserHeaderService.getInstance().getRecommendUserCount(toUserId));
		userInfoMap.put("followType", FollowService.getInstance().checkFollowType(fromUserId, toUserId));
		userInfoMap.put("lookState", CommonUtil.getLookState(user, toUserId));
		userInfoMap.put("isXingyunUID", CommonUtil.checkIsXingyunUID(toUserId));
		userInfoMap.put("friendRelationType", FriendService.getInstance().checkFriendRelationTypeToMessage(fromUserId, toUserId));
		userInfoMap.put("location", AreaUtil.getInstance().getUserAddressMap(toUserId, false)); //设置用户所在地
		userInfoMap.put("gender", UserHeaderService.getInstance().getGenderByUserId(toUserId)); //用户性别
		userInfoMap.put("xyProxy", UserHeaderService.getInstance().getXyProxyByUserID(toUserId));
		return userInfoMap;
	}
	
	/**
	 * 检测是否为后台用户
	 */
	public boolean checkIsAdminUser(String userName, String password) throws Throwable{
		String sql = "SELECT COUNT(*) FROM cms_admin_user WHERE username = ? AND password = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userName);
		valueList.add(password);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
}
