package com.xingyun.services.sinaoauth;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunOauthConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.db.DBgetNextID;
import com.xingyun.session.SessionCookie;
import com.xingyun.util.AreaUtil;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.PinyinUtil;
import com.xingyun.util.UserManageUtil;
import com.xingyun.weibo4j.Account;
import com.xingyun.weibo4j.Oauth;
import com.xingyun.weibo4j.Timeline;
import com.xingyun.weibo4j.Users;
import com.xingyun.weibo4j.Weibo;
import com.xingyun.weibo4j.http.AccessToken;
import com.xingyun.weibo4j.model.User;
import com.xingyun.weibo4j.org.json.JSONObject;

public class SinaOauthService {
	
	private static final Logger log = Logger.getLogger(SinaOauthService.class);
	
	private static final DBOperate db = new DBOperate();
	private static final SinaOauthService sinaOauthService = new SinaOauthService();
	private SinaOauthService(){}
	public static SinaOauthService getInstance() {
		return sinaOauthService;
	}
	
	/**
	 * 通过code获取新浪微博access_token
	 */
	public AccessToken getSinaAccessToken(String code) throws Throwable{
		return getAccessToken(code, 1);
	}
	
	private AccessToken getAccessToken(String code, int index) throws Throwable{
		if(index > XingyunOauthConstant.SINA_APPID_COUNT)
			return null;
		try{
			Oauth oauth = new Oauth();
			return oauth.getAccessTokenByCode(code);
		}catch(Throwable e){
			log.error(e.getMessage() + "----------------------------code:"+code+"---index:"+index, e);
			return getAccessToken(code, ++ index);
		}
	}

	/**
	 * 根据新浪用户id获取与xingyun绑定的用户id
	 * 使用索引：index_weibo_profile_sinauserid
	 */
	public String getSinaUserIdByUserId(String userId) throws Throwable{
		String sql = "SELECT sinauserid FROM weibo_profile WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return CommonUtil.getStringValue(sql, valueList, "sinauserid");
	}
	
	/**
	 * 根据新浪用户id获取与xingyun绑定的用户id
	 * 使用索引：index_weibo_profile_sinauserid
	 */
	public String getUserIdBySinaUserId(String sinaUserId) throws Throwable{
		String sql = "SELECT userid FROM weibo_profile WHERE sinauserid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(sinaUserId);
		return CommonUtil.getStringValue(sql, valueList, "userid");
	}
	
	/**
	 * 通过userid获取token信息
	 * 使用索引：index_weibo_profile_userid
	 */
	public Map<Object,Object> getUserTokenInfo(String userId) throws Throwable{
		String sql = "SELECT accesstoken,sinauserid FROM weibo_profile WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	
	/**
	 * 通过token及新浪微博uid获取新浪用户对象
	 */
	private User showSinaUserInfo(String accessToken, String sinaUserId) throws Throwable{
		return getSinaUser(accessToken, sinaUserId, 1);
	}
	
	private User getSinaUser(String accessToken, String sinaUserId, int index) throws Throwable{
		if(index > XingyunOauthConstant.SINA_APPID_COUNT)
			return null;
		try{
			Weibo weibo = new Weibo();
			weibo.setToken(accessToken);
			return new Users().showUserById(sinaUserId);
		}catch(Throwable e){
			log.error(e.getMessage() + "----------------------------accessToken:"+accessToken+"---index:"+index, e);
			return getSinaUser(accessToken, sinaUserId, ++ index);
		}
	}
	/**
	 * 通过新浪微博注册游客
	 */
	public String registerYouke(HttpServletRequest request, String email, String phone) throws Throwable{
		String accessToken = SessionCookie.getCookieValue(request, XingyunOauthConstant.XINGYUN_SINA_TOKENKEY);
		if(StringUtils.isBlank(accessToken))
			return StringUtils.EMPTY;
		String sinaUserId = SessionCookie.getCookieValue(request, XingyunOauthConstant.XINGYUN_SINA_USERIDKEY);
		if(StringUtils.isBlank(sinaUserId))
			return StringUtils.EMPTY;
		String expires = SessionCookie.getCookieValue(request, XingyunOauthConstant.XINGYUN_SINA_EXPIRES);
		if(StringUtils.isBlank(expires))
			return StringUtils.EMPTY;
		User user = showSinaUserInfo(accessToken, sinaUserId);
		if(user == null)
			return StringUtils.EMPTY;
		String userId = registerToYouke(email, phone, user, sinaUserId, accessToken, expires);
		clearCookie();
		if(StringUtils.EMPTY.equals(userId))
			return StringUtils.EMPTY;
		Map<Object,Object> inviteInfoMap = UserManageUtil.getInstance().getInviteInfoBySinaUid(sinaUserId);
		if(inviteInfoMap == null)
			return userId;
		String inviteUserId = CommonUtil.getStringValue(inviteInfoMap.get("userid"));
		String inviteCode = CommonUtil.getStringValue(inviteInfoMap.get("invitecode"));
		return UserManageUtil.getInstance().connectInviteUserInfo(userId, inviteUserId, sinaUserId, inviteCode);
	}
	
	/**
	 * 获取新浪微博用户域名
	 */
	private String getBlogUrl(String userId, String domain) throws Throwable{
		String blogUrl = XingyunOauthConstant.SINA_DOMAIN + "/u/" + userId;
		if(StringUtils.isNotBlank(domain))
			blogUrl = XingyunOauthConstant.SINA_DOMAIN + "/" + domain;
		return blogUrl;
	}

	/**
	 * 通过邀请码查找用户id
	 */
	public Map<Object,Object> getInviteMapByInviteCode(String inviteCode) throws Throwable{
		String sql = "SELECT userid, sinauserid FROM cms_invite WHERE invitecode = ? AND isactivate = 0";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(inviteCode);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	/**
	 * 检测邀请码是否存在
	 * 使用索引：index_cms_invite_invitecode
	 */
	public int checkInviteCodeIsActivate(String code) throws Throwable{
		String sql = "SELECT isactivate FROM cms_invite WHERE invitecode = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(code);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return -1;
		return Integer.parseInt(list.get(0).get("isactivate").toString());
	}
	
	/**
	 * 注册游客
	 */
	private String registerToYouke(String email, String phone, User user, String sinaUserId, String accessToken, String expires) throws Throwable{
		String userId = StringUtils.EMPTY;
		try{
			userId = addUserData(email);
			if(StringUtils.EMPTY.equals(userId))
				return StringUtils.EMPTY;
			boolean addTag = addYoukeOtherInfo(userId, phone, user, sinaUserId, accessToken, expires);
			if(!addTag){
				rollBackRegisterUser(userId);
				return StringUtils.EMPTY;
			}
			return userId;
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			rollBackRegisterUser(userId);
			return StringUtils.EMPTY;
		}
	}
	
	/**
	 * 更新新浪注册用户数据
	 * @param userId			用户ID
	 * @param user				新浪用户user
	 * @param type				请求授权来源
	 * @param logoResPicBean	头像资源bean
	 * @param sinaUserId		新浪用户ID
	 * @param accessToken		新浪token
	 */
	private boolean addYoukeOtherInfo(String userId, String phone, User user, String sinaUserId, String accessToken, String expires){
		try{
			String time = DateUtil.getSimpleDateFormat();
			Map<Object,Object> locationMap = setUserLocation(user.getLocation());
			int provinceId = Integer.parseInt(locationMap.get("provinceid").toString());
			int cityId = Integer.parseInt(locationMap.get("cityid").toString());
			int gender = user.getGender().equals("f") ? XingyunCommonConstant.GENDER_GIRL : XingyunCommonConstant.GENDER_BOY;
			String blogUrl = getBlogUrl(sinaUserId, user.getUserDomain());
			List<String> sqlList = new ArrayList<String>();
			sqlList.add("INSERT INTO user_profile(userid,gender,blogurl,mobile,provinceid,cityid,systime) VALUES('"+userId+"',"+gender+",'"+blogUrl+"','"+phone+"',"+provinceId+","+cityId+",'"+time+"')");
			sqlList.add("INSERT INTO weibo_profile(userid,sinauserid,accesstoken,expires,screen_name,verified,verified_reason,fscount,gzcount,doublecount,systime) VALUES('"+userId+"','"+sinaUserId+"','"+accessToken+"','"+expires+"','"+user.getScreenName()+"',"+(user.getVerifiedType() == -1 ? 0 : 1)+",'"+user.getVerifiedReason()+"',"+user.getFollowersCount()+","+user.getFriendsCount()+","+user.getBiFollowersCount()+",'"+time+"')");
			sqlList.add("INSERT INTO cms_weibo(userid,sinauserid,accesstoken,expires,gzcount,doublecount,systime) VALUES('"+userId+"','"+sinaUserId+"','"+accessToken+"','"+expires+"',"+user.getFriendsCount()+","+user.getBiFollowersCount()+",'"+time+"')");
			sqlList.add("INSERT INTO user_logo(userid,logourl,systime) VALUES('"+userId+"','"+XingyunUploadFileConstant.LOGO_SYS_RESID+"','"+time+"')");
			db.batchExecute(sqlList, true);
			return true;
		}catch (Throwable e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}
	/**
	 * 通过地址获取省份id及城市id
	 */
	private Map<Object,Object> setUserLocation(String location) throws Throwable{
		Map<Object,Object> map = new HashMap<Object, Object>();
		if(StringUtils.EMPTY.equals(location)){
			map.put("provinceid", 0);
			map.put("cityid", 0);
			return map;
		}
		String[] arrLocation = location.split(" ");
		if(arrLocation.length == 1){
			map.put("provinceid", AreaUtil.getInstance().findAreaIdByName(arrLocation[0]));
			map.put("cityid", 0);
			return map;
		}
		map.put("provinceid", AreaUtil.getInstance().findAreaIdByName(arrLocation[0]));
		map.put("cityid", AreaUtil.getInstance().findAreaIdByName(arrLocation[1]));
		return map;
	}
	/**
	 * 往user表中插入数据
	 */
	private String addUserData(String email){
		try{
			String userId = DBgetNextID.getInstance().getWebSinaNextUserID();
			String nickName = getYoukeNickName();
			String sql = "INSERT INTO user(userid,email,nickname,pinyinname,lid,logourl,systime) VALUES(?,?,?,?,?,?,?)";
			List<Object> valueList = new ArrayList<Object>();
			valueList.add(userId);
			valueList.add(email);
			valueList.add(nickName);
			valueList.add(PinyinUtil.getInstance().getSelling(nickName));
			valueList.add(XingyunCommonConstant.USER_LEVEL_YOUKE);
			valueList.add(XingyunUploadFileConstant.LOGO_SYS_RESID);
			valueList.add(new Date());
			db.insertData(sql, valueList);
			return userId;
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return StringUtils.EMPTY;
		}
	}
	/**
	 * 获取游客昵称
	 */
	private String getYoukeNickName() throws Throwable{
		String nickName = XingyunCommonConstant.YOUKE_NICKNAME_PREFIX + CommonUtil.getRandomNickName(XingyunCommonConstant.YOUKE_NICKNAME_SUFFIX_NUM);
		if(!XingyunCommonConstant.RESPONSE_SUCCESS_STRING.equals(CommonUtil.checkIsExistNickName(nickName)))
			return getYoukeNickName();
		return nickName;
	}
	/**
	 * 回滚user表中数据
	 */
	private void rollBackRegisterUser(String userId) throws Throwable{
		String sql = "DELETE FROM user WHERE userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userId);
		db.deleteData(sql, vList);
	}
	
	/**
	 * 将新浪的token保存在cookie中
	 */
	public void setCookie(AccessToken accessToken, String sinaUserId) throws Throwable{
		SessionCookie.setCookie(XingyunOauthConstant.XINGYUN_SINA_TOKENKEY, accessToken.getAccessToken(), XingyunOauthConstant.XINGYUN_TOKEN_SAVE_TIME);
		SessionCookie.setCookie(XingyunOauthConstant.XINGYUN_SINA_EXPIRES, accessToken.getExpireIn(), XingyunOauthConstant.XINGYUN_TOKEN_SAVE_TIME);
		SessionCookie.setCookie(XingyunOauthConstant.XINGYUN_SINA_USERIDKEY, sinaUserId, XingyunOauthConstant.XINGYUN_TOKEN_SAVE_TIME);
	}

	/**
	 * 将新浪的token从cookie中清除
	 */
	private void clearCookie(){
		SessionCookie.setCookie(XingyunOauthConstant.XINGYUN_SINA_TOKENKEY, null, 0);
		SessionCookie.setCookie(XingyunOauthConstant.XINGYUN_SINA_EXPIRES, null, 0);
		SessionCookie.setCookie(XingyunOauthConstant.XINGYUN_SINA_USERIDKEY, null, 0);
	}
	
	/**
	 * 检测token是否过了有效期
	 */
	public String checkTokenIsValid(Map<Object,Object> tokenInfoMap) throws Throwable{
		String token = tokenInfoMap.get("accesstoken").toString();
		String sinaUserId = tokenInfoMap.get("sinauserid").toString();
		return checkTokenIsValid(token, sinaUserId);
	}

	/**
	 * 检测token是否过了有效期
	 */
	private String checkTokenIsValid(String token, String sinaUserId) throws Throwable{
		Weibo weibo = new Weibo();
		weibo.setToken(token);
		try{
			Account am = new Account();
			JSONObject uid = am.getUid();
			if(uid == null || uid.get("uid") == null)
				return XingyunOauthConstant.TOKEN_F;
			if(uid.get("uid").toString().equals(sinaUserId))
				return XingyunOauthConstant.TOKEN_T;
			return XingyunOauthConstant.TOKEN_F;
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return XingyunOauthConstant.TOKEN_F;
		}
	}
	
	/**
	 * 检测用户的新浪微博token是否存在
	 * 使用索引：index_weibo_profile_userid
	 */
	private boolean checkTokenIsExist(String userId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM weibo_profile WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
	/**
	 * 修改新浪微博token信息
	 */
	public boolean updateWeiboToken(String userId, String sinaUserId, String accessToken, String expires, boolean isUpdateWeiboInfo){
		try{
			List<String> sqlList = new ArrayList<String>();
			if(!checkTokenIsExist(userId))
				sqlList.add("INSERT INTO weibo_profile(accesstoken, expires, userid, sinauserid, systime) VALUES('"+accessToken+"','"+expires+"','"+userId+"','"+sinaUserId+"',NOW())");
			else
				sqlList.add("UPDATE weibo_profile SET accesstoken = '"+accessToken+"', expires = '"+expires+"', systime = NOW() WHERE userid = '"+userId+"' AND sinauserid = '"+sinaUserId+"'");
			sqlList.add("UPDATE cms_weibo SET accesstoken = '"+accessToken+"', expires = '"+expires+"' WHERE userid = '"+userId+"' AND sinauserid = '"+sinaUserId+"'");
			db.batchExecute(sqlList, true);
			if(isUpdateWeiboInfo)
				updateWeiboUserInfo(userId, sinaUserId, accessToken);
			return true;
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * 将文本同步到授权好的新浪微博
	 */
	public void shareToWeibo(String userId, String content, String picUrl) throws Throwable{
		Map<Object,Object> tokenInfoMap = getUserTokenInfo(userId);
		if(tokenInfoMap == null)
			return;
		addToWeibo(userId, tokenInfoMap.get("accesstoken").toString(), content, picUrl, 1);
	}
	/**
	 * 添加新浪微博
	 */
	private void addToWeibo(String userId, String accessToken, String content, String picUrl,  int index) throws Throwable{
		if(index > XingyunOauthConstant.SINA_APPID_COUNT)
			return;
		try{
			Weibo weibo = new Weibo();
			weibo.setToken(accessToken);
			Timeline tm = new Timeline();
			tm.UpdatePicUrlStatus(content, picUrl);
		}catch(Throwable e){
			log.error(e.getMessage() + "---------------------userId:"+userId+"---content:"+content+"---index:"+index, e);
			addToWeibo(userId, accessToken, content, picUrl, ++ index);
		}
	}
	
	/**
	 * 修改用户的新浪微博信息
	 */
	private void updateWeiboUserInfo(String userId, String sinaUserId, String accessToken) throws Throwable{
		User user = showSinaUserInfo(accessToken, sinaUserId);
		if(user == null)
			return;
		String sql = "UPDATE weibo_profile SET screen_name = ?, verified = ?, verified_reason = ? WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(user.getScreenName());
		valueList.add(user.getVerifiedType() == -1 ? 0 : 1);
		valueList.add(user.getVerifiedReason());
		valueList.add(userId);
		db.updateData(sql, valueList);
	}
}
