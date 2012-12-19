package com.xingyun.services.profile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xingyun.bean.CooperationPriceBean;
import com.xingyun.bean.ItemBean;
import com.xingyun.bean.ModuleBean;
import com.xingyun.bean.ProfileBean;
import com.xingyun.bean.ProfileVideoItemBean;
import com.xingyun.bean.ResPicBean;
import com.xingyun.bean.User;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunFaceConstant;
import com.xingyun.constant.XingyunPostConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.post.PostService;
import com.xingyun.services.recommend.RecommendService;
import com.xingyun.upload.bean.UploadParamBean;
import com.xingyun.upload.services.UploadService;
import com.xingyun.util.AreaUtil;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.PinyinUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.SpecialCharFilterUtil;
import com.xingyun.util.UploadPicUtil;

public class ProfileService {
	private static final DBOperate db = new DBOperate();
	private static final ProfileService profileService = new ProfileService();
	private ProfileService(){}
	public static ProfileService getInstance() {
		return profileService;
	}
	
	/**
	 * 根据用户ID整理 头像列表数据
	 */
	public List<Map<Object, Object>> getUserLogoList(String userId, int logoWidth) throws Throwable{
		List<Map<Object, Object>> logoList = PublicQueryUtil.getInstance().findUserLogoListData(userId, false);
		if(logoList == null || logoList.size() == 0)
			return null;
		for(Map<Object, Object> map : logoList)
			map.put("logoWebUrl", UploadPicUtil.getPicWebUrl(map.get("logourl").toString(), logoWidth));
		return logoList;
	}
	
	/**
	 * 检查用户是否可以上传头像
	 */
	public boolean checkUserUploadLogo(String userID) throws Throwable{
		int logoCount = findUserLogoCount(userID);
		return logoCount < XingyunCommonConstant.USER_UPLOAD_LOG_MAX_SIZE;
	}
	
	/**
	 * 根据用户ID 查询用户头像数量
	 * 使用索引：index_user_logo_userid
	 */
	private int findUserLogoCount(String userID) throws Throwable{
		String sql = "SELECT COUNT(*) FROM user_logo WHERE userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		return db.getRecordCountSQL(sql, vList);
	}
	
	/**
	 * 根据用户ID 查询用户头像是否是默认头像
	 */
	private boolean findUserLogoDefaultCount(String userID) throws Throwable{
		String sql = "SELECT COUNT(*) FROM user WHERE userid = ? AND logourl = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunUploadFileConstant.LOGO_SYS_RESID);
		return db.getRecordCountSQL(sql, vList) == 1 ? true : false;
	}
	
	private String getUserLogoUrl(int logoId) throws Throwable{
		String sql = "SELECT logourl FROM user_logo WHERE id = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(logoId);
		return CommonUtil.getStringValue(sql, vList, "logourl");
	}
	
	/**
	 * 保存用户上传头像
	 */
	public void saveUserUploadLogoData(int logoID, String userID, ResPicBean logoResPicBean) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		if(logoID > 0){
			sqlList.add("UPDATE user_logo SET logourl = '"+logoResPicBean.getPicid()+"' WHERE id = "+logoID+" AND userid = '"+userID+"'");
			if(getLogoSeqById(logoID, userID) == 0)
				sqlList.add("UPDATE user SET logourl = '"+logoResPicBean.getPicid()+"' WHERE userid = '"+userID+"'");
		}else{
			// 如果用户现在使用的头像是默认头像，则更新默认头像
			if (findUserLogoDefaultCount(userID)) {
				sqlList.add("UPDATE user_logo SET logourl = '"+logoResPicBean.getPicid()+"' WHERE logourl = "+XingyunUploadFileConstant.LOGO_SYS_RESID+" AND userid = '"+userID+"'");
				sqlList.add("UPDATE user SET logourl = '"+logoResPicBean.getPicid()+"' WHERE userid = '"+userID+"'");
			} 
			// 如果不是，则仅插入新头像
			else 
			{
				int logoCount = findUserLogoCount(userID);
				sqlList.add("INSERT INTO user_logo(userid, logourl, seq, systime) VALUES('"+userID+"', '"+logoResPicBean.getPicid()+"', "+logoCount+",'"+DateUtil.getSimpleDateFormat()+"')");
			}
		}
		
		String resPicSql = CommonUtil.getInsertResPicSql(logoResPicBean);
		if(StringUtils.isNotBlank(resPicSql))
			sqlList.add(resPicSql);
		db.batchExecute(sqlList, true);
	}
	/**
	 * 查找头像的顺序
	 */
	private int getLogoSeqById(int logoID, String userID) throws Throwable{
		String sql = "SELECT seq FROM user_logo WHERE id = ? AND userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(logoID);
		valueList.add(userID);
		return CommonUtil.getIntValue(sql, valueList, "seq");
	}
	/**
	 * 保存用户头像数据
	 */
	public void saveUserLogoData(String userID, List<Integer> logoIDs) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder();
		int seq = 0;
		int logoId_first = 0;
		for(Integer id : logoIDs){
			if(seq == 0)
				logoId_first = id;
			sqlList.add("UPDATE user_logo SET seq = " + seq + " WHERE id = " + id + " AND userid = '" + userID + "'");
			sb.append(id).append(",");
			seq++;
			if(seq >= XingyunCommonConstant.USER_UPLOAD_LOG_MAX_SIZE)
				break;
		}
		sqlList.add("UPDATE user SET logourl = '"+getUserLogoUrl(logoId_first)+"' WHERE userid = '" + userID + "'");
		if(sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		sqlList.add(0, "DELETE FROM user_logo WHERE userid = '" + userID + "' AND id NOT IN(" + sb.toString() + ")");
		db.batchExecute(sqlList, true);
	}
	
	/**
	 * 删除用户头像数据
	 */
	public boolean clearUserLogoData(String userID, int logoID) throws Throwable{
		String sql = "SELECT id FROM user_logo WHERE id = ? AND userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(logoID);
		vList.add(userID);
		int id = CommonUtil.getIntValue(sql, vList, "id");
		if(id == 0)
			return false;
		
		sql = "DELETE FROM user_logo WHERE id = ?";
		vList.clear();
		vList.add(id);
		db.deleteData(sql, vList);
		return true;
	}
	
	/**
	 * 获取用户封面图片集合
	 */
	public List<Map<Object, Object>> getUserFaceList(String userID) throws Throwable{
		int templetID = fingFaceTempletID(userID, XingyunFaceConstant.FACE_TEMPLET_DYNAMIC);
		List<Map<Object, Object>> picList = fingFaceTempletPic(templetID);
		for(Map<Object, Object> map : picList){
			map.put("picWebUrl", UploadPicUtil.getPicWebUrl(map.get("picpath").toString(), XingyunUploadFileConstant.FACE_DYNAMIC_WIDTH_1020));
			map.put("picWebUrl_619", UploadPicUtil.getPicWebUrl(map.get("picpath").toString(), XingyunUploadFileConstant.FACE_PHONE_WIDTH_619));
			map.put("picWebUrl_150", UploadPicUtil.getPicWebUrl(map.get("picpath").toString(), XingyunUploadFileConstant.FACE_DYNAMIC_WIDTH_150));
		}
		return picList;
	}
	
	/**
	 * 查询用户封面图片数据
	 * 使用索引：index_face_templet_userid_type
	 */
	private int fingFaceTempletID(String userID, int templet) throws Throwable{
		String sql = "SELECT id FROM face_templet WHERE userid = ? AND type = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(templet);
		int id = CommonUtil.getIntValue(sql, vList, "id");
		if(id > 0)
			return id;
		sql = "INSERT INTO face_templet(userid, type, systime) VALUES(?, ?, ?)";
		vList.add(new Date());
		return db.insertData(sql, vList);
	}
	
	/**
	 * 查询用户封面图片数据
	 * 使用索引：index_face_templet_item_templetid
	 */
	private List<Map<Object, Object>> fingFaceTempletPic(int templetID) throws Throwable{
		String sql = "SELECT id, templetid, picpath FROM face_templet_item WHERE templetid = ? ORDER BY id";
		List<Object> vList = new ArrayList<Object>();
		vList.add(templetID);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 保存动态封面数据
	 */
	public void saveFaceDynamicData(String userID, List<UploadParamBean> pList) throws Throwable{
		List<ResPicBean> resPicBeanList = UploadService.getInstance().faceDynamicSave(userID, pList);		//处理动态封面图片
		int templetID = fingFaceTempletID(userID, XingyunFaceConstant.FACE_TEMPLET_DYNAMIC);	//获取模板ID
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("DELETE FROM face_templet_item WHERE templetid = " + templetID);
		String resPicSql = "";
		for(ResPicBean picBean : resPicBeanList){
			sqlList.add("INSERT INTO face_templet_item(templetid, picpath, systime) VALUES(" + templetID + ", '" + picBean.getPicid() + "', '" + DateUtil.getSimpleDateFormat() + "')");
			resPicSql = CommonUtil.getInsertResPicSql(picBean);
			if(StringUtils.isNotBlank(resPicSql))
				sqlList.add(resPicSql);
		}
		db.batchExecute(sqlList, true);
	}
	
	public void updateIntroduction(String userId, String content) throws Throwable{
		content = StringUtils.isBlank(content) ? "" : SpecialCharFilterUtil.filterEncodeAndForbidValue(content, 255);
		String sql = "UPDATE user SET introduction = ? WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(content);
		valueList.add(userId);
		db.updateData(sql, valueList);
	}
	
	public List<Map<Object,Object>> getPostInfoList(String userId) throws Throwable{
		List<Map<Object,Object>> postList = getPostIndexList(userId);
		if(postList.size() == 0)
			return postList;
		String[] seqValue = {"id"};
		CommonUtil.compositor(postList, seqValue, 1);
		setPostInfoList(postList);
		return postList;
	}
	
	public List<Map<Object,Object>> getPostInfoListByIphone(String userId) throws Throwable{
		List<Map<Object,Object>> postList = getPostIndexListByInphone(userId);
		if(postList.size() == 0)
			return postList;
		setPostInfoList(postList);
		return postList;
	}
	
	/**
	 * 
	 * 使用索引：index_post_userid
	 */
	private List<Map<Object,Object>> getPostIndexList(String userId) throws Throwable{
		String sql = "SELECT p.id,p.posttype,p.title,p.coverpath,p.systime,p.userid,u.wkey FROM post p, user u WHERE p.userid = u.userid AND p.userid = ? AND p.isindex = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(XingyunPostConstant.ZP_INDEX_TYPE_YES);
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 
	 * 使用索引：index_post_userid
	 */
	private List<Map<Object,Object>> getPostIndexListByInphone(String userId) throws Throwable{
		String sql = "SELECT p.id,p.posttype,p.title,p.coverpath,p.systime,p.userid,u.wkey FROM post p, user u WHERE p.userid = u.userid AND p.userid = ? AND p.isdel = ? AND p.status = ? ORDER BY p.id DESC";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		valueList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		valueList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		return db.retrieveSQL(sql, valueList);
	}
	
	private void setPostInfoList(List<Map<Object,Object>> list) throws Throwable{
		int postId;
		for(Map<Object,Object> postMap : list){
			postId = Integer.parseInt(postMap.get("id").toString());
			postMap.put("coverpath", UploadPicUtil.getPicWebUrl(postMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_250));
			postMap.put("zpViewcount", PostService.getInstance().getPostViewCount(postId));
			postMap.put("zpRecommendCount", RecommendService.getInstance().getPostRecommendCount(postId));
			postMap.put("isNew", CommonUtil.checkPostIsNew((Date)postMap.get("systime")));
			postMap.put("userHref", CommonUtil.getUserIndexHref(CommonUtil.getStringValue(postMap.get("userid")), postMap.get("wkey")));
		}
	}
	
	public void saveModule(String userId, int isShowVideo, int isShowFacePic, int isShowPost, int isShowfollow, int isShowCooperation, int isShowXingyu) throws Throwable{
		String sql = "UPDATE face SET isshowvideo = ?, isshowfacepic = ?, isshowpost = ?, isshowfollow = ?, isshowcooperation = ? , isshowxingyu = ? WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(isShowVideo);
		valueList.add(isShowFacePic);
		valueList.add(isShowPost);
		valueList.add(isShowfollow);
		valueList.add(isShowCooperation);
		valueList.add(isShowXingyu);
		valueList.add(userId);
		db.updateData(sql, valueList);
	}
	/**
	 * 隐藏个人首页幻灯片，视频简历模块
	 */
	public void hideFaceModule(String userId, int moduleType) throws Throwable{
		String sql = StringUtils.EMPTY;
		if(moduleType == XingyunFaceConstant.FACE_MODULE_FACEPIC)
			sql = "UPDATE face SET isshowfacepic = ? WHERE userid = ?";
		else if(moduleType == XingyunFaceConstant.FACE_MODULE_VIDEO)
			sql = "UPDATE face SET isshowvideo = ? WHERE userid = ?";
		else if(moduleType == XingyunFaceConstant.FACE_MODULE_COOPERATION_PRICE)
			sql = "UPDATE face SET isshowcooperation = ? WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(XingyunFaceConstant.FACE_MODULE_SHOW_NO);
		valueList.add(userId);
		db.updateData(sql, valueList);
	}
	/**
	 * 隐藏基础资料及自定义模块
	 */
	public void hideOtherModule(int moduleId, String userId) throws Throwable{
		String sql = "UPDATE user_profile_other SET isshow = ? WHERE id = ? AND userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(XingyunFaceConstant.FACE_MODULE_SHOW_NO);
		valueList.add(moduleId);
		valueList.add(userId);
		db.updateData(sql, valueList);
	}
	
	/**
	 * 使用索引：index_face_userid
	 */
	public Map<Object,Object> getFaceInfo(String userId) throws Throwable{
		String sql = "SELECT isshowvideo,isshowfacepic,isshowpost,isshowcooperation,isshowxingyu FROM face WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	
	/**
	 * 查询用户的视频简历
	 * 使用索引：index_user_profile_video_userid
	 */
	public List<Map<Object,Object>> getVideoList(String userId) throws Throwable{
		String sql = "SELECT id,url,url_ipad,href,vid,coverpath,seq FROM user_profile_video WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		CommonUtil.compositor(list, new String[]{"seq"} , 0);
		for(Map<Object,Object> map : list){
			map.put("coverpath_web", UploadPicUtil.getPicWebUrl(map.get("coverpath").toString(), XingyunUploadFileConstant.VIDEO_WIDTH_150));
		}
		return list;
	}
	
	/**
	 * 查询用户视频简历总数
	 * 使用索引：index_user_profile_video_userid
	 */
	public int getVideoCountByUserId(String userId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM user_profile_video WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return db.getRecordCountSQL(sql, valueList);
	}
	
	public void delVideoById(String userId, int videoId) throws Throwable{
		String sql = "DELETE FROM user_profile_video WHERE id = ? AND userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(videoId);
		valueList.add(userId);
		db.deleteData(sql, valueList);
	}
	
	/**
	 * 保存视频简历信息
	 */
	public int addVideoInfo(String userId, ProfileVideoItemBean videoItemBean, ResPicBean videoResPicBean) throws Throwable{
		int videoID = 0;
		try {
			String sql = "INSERT INTO user_profile_video(userid,url,url_ipad,href,vid,coverpath,seq,systime) VALUES(?,?,?,?,?,?,?,?)";
			List<Object> valueList = new ArrayList<Object>();
			valueList.add(userId);
			valueList.add(videoItemBean.getVideourl());
			valueList.add(videoItemBean.getIpadurl());
			valueList.add(videoItemBean.getVideohref());
			valueList.add(videoItemBean.getVid());
			valueList.add(videoItemBean.getCoverpath());
			valueList.add(videoItemBean.getSeq());
			valueList.add(new Date());
			videoID = db.insertData(sql, valueList);
			
			String resPicSql = CommonUtil.getInsertResPicSql(videoResPicBean);
			if(StringUtils.isNotBlank(resPicSql)){
				db.insertData(resPicSql);
				PublicQueryUtil.getInstance().addDynamicDataByType(userId, XingyunCommonConstant.DYNAMIC_TYPE_VIDEO);
			}
		} catch (Throwable e) {
			if(videoID > 0){
				db.deleteData("DELETE FROM user_profile_video WHERE id = " + videoID);
			}
			throw new Throwable(e);
		}
		return videoID;
	}
	
	public void delVideo(String userId, int videoId) throws Throwable{
		String sql = "DELETE FROM user_profile_video WHERE id = ? AND userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(videoId);
		valueList.add(userId);
		db.deleteData(sql, valueList);
	}
	
	public void updateVideoSort(String userId, List<ProfileVideoItemBean> videoItemBeanList) throws Throwable{
		if(videoItemBeanList == null || videoItemBeanList.size() == 0)
			return;
		int seq = 0;
		List<String> sqlList = new ArrayList<String>();
		for(ProfileVideoItemBean videoItemBean : videoItemBeanList){
			sqlList.add("UPDATE user_profile_video SET seq = "+ seq +" WHERE id = " +videoItemBean.getId() + " AND userid = '" + userId + "'");
			seq ++;
		}
		db.batchExecute(sqlList, true);
	}
	
	/**
	 * 获取用户明细
	 */
	public ProfileBean getUserProfileBean(String userId) throws Throwable{
		Map<Object,Object> userProfileMap = getUserProfileMap(userId);
		if(userProfileMap == null)
			return null;
		return setUserProfileBean(userProfileMap);
	}
	
	/**
	 * 
	 * 使用索引：index_user_profile_userid
	 */
	private Map<Object, Object> getUserProfileMap(String userId) throws Throwable {
		String sql = "SELECT realname,englishname,nation,birthday,birthday_status,constellation,constellation_up,height,weight,shape1,shape2,shape3,blood," +
				"provinceid,cityid,provinceid_born,cityid_born,school,school_status,language,delegate,company,broker,interest,agency,gender,wholebodypic" +
				" FROM user_profile WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId );
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	private ProfileBean setUserProfileBean(Map<Object,Object> map) throws Throwable{
		ProfileBean profileBean = new ProfileBean();
		profileBean.setRealname(CommonUtil.getStringValue(map.get("realname")));
		profileBean.setEnglishname(CommonUtil.getStringValue(map.get("englishname")));
		profileBean.setNation(CommonUtil.getStringValue(map.get("nation")));
		profileBean.setBirthday(CommonUtil.getStringValue(map.get("birthday")));
		profileBean.setBirthday_status(CommonUtil.getIntValue(map.get("birthday_status")));
		profileBean.setBirthday_profile(profileBean.getBirthday());
		if(profileBean.getBirthday_status() == XingyunCommonConstant.PROFILE_PRIVATE_BIRTHDAY_NO && !StringUtils.EMPTY.equals(map.get("birthday").toString()) && map.get("birthday").toString().length() > 5)
			profileBean.setBirthday_profile(map.get("birthday").toString().substring(5));
		profileBean.setConstellation(CommonUtil.getStringValue(map.get("constellation")));
		profileBean.setConstellation_up(CommonUtil.getStringValue(map.get("constellation_up")));
		profileBean.setHeight(CommonUtil.getStringValue(map.get("height")));
		profileBean.setWeight(CommonUtil.getStringValue(map.get("weight")));
		profileBean.setShape1(CommonUtil.getIntValue(map.get("shape1")));
		profileBean.setShape2(CommonUtil.getIntValue(map.get("shape2")));
		profileBean.setShape3(CommonUtil.getIntValue(map.get("shape3")));
		profileBean.setBlood(CommonUtil.getStringValue(map.get("blood")));
		profileBean.setLocationMap(AreaUtil.getInstance().setUserLocationMap(CommonUtil.getIntValue(map.get("provinceid")), CommonUtil.getIntValue(map.get("cityid"))));
		profileBean.setProvinceid(CommonUtil.getIntValue(map.get("provinceid")));
		profileBean.setCityid(CommonUtil.getIntValue(map.get("cityid")));
		profileBean.setLocationMap_born(AreaUtil.getInstance().setUserLocationMap(CommonUtil.getIntValue(map.get("provinceid_born")), CommonUtil.getIntValue(map.get("cityid_born"))));
		profileBean.setProvinceid_born(CommonUtil.getIntValue(map.get("provinceid_born")));
		profileBean.setCityid_born(CommonUtil.getIntValue(map.get("cityid_born")));
		profileBean.setSchool(CommonUtil.getStringValue(map.get("school")));
		profileBean.setSchool_status(CommonUtil.getIntValue(map.get("school_status")));
		profileBean.setLanguage(CommonUtil.getStringValue(map.get("language")));
		profileBean.setDelegate(CommonUtil.getStringValue(map.get("delegate")));
		profileBean.setCompany(CommonUtil.getStringValue(map.get("company")));
		profileBean.setBroker(CommonUtil.getStringValue(map.get("broker")));
		profileBean.setInterest(CommonUtil.getStringValue(map.get("interest")));
		profileBean.setAgency(CommonUtil.getStringValue(map.get("agency")));
		profileBean.setGender(CommonUtil.getIntValue(map.get("gender")));
		profileBean.setWholebodypic(CommonUtil.getStringValue(map.get("wholebodypic")));
		if(StringUtils.isNotEmpty(profileBean.getWholebodypic()))
			profileBean.setWholebodypic(UploadPicUtil.getPicWebUrl(profileBean.getWholebodypic(), XingyunUploadFileConstant.PROFILE_WHOLEBODY_WIDTH_300));
		return profileBean;
	}
	
	public String updateProfileInfo(String userId, ProfileBean profileBean) throws Throwable{
		if(profileBean.getProvinceid() != 0 && !AreaUtil.getInstance().checkAddressIsExist(profileBean.getProvinceid(), profileBean.getCityid()))
			return XingyunCommonConstant.RESPONSE_ERR_STRING;
		String sql = "UPDATE user_profile" +
				" SET realname = ?,englishname = ?, nation = ?, birthday = ?,birthday_status = ?, constellation = ?,constellation_up = ?,height = ?, weight = ?," +
				" shape1 = ?, shape2 = ?, shape3 = ?,blood = ?, provinceid = ?, cityid = ?, provinceid_born = ?, cityid_born = ?, school = ?, school_status = ?, language = ?, delegate = ?," +
				" company = ?, broker = ?, interest = ?, agency = ?, gender = ?" +
				" WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getRealname(), 30)); 
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getEnglishname(), 50)); 
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getNation(), 50)); 
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getBirthday(), 10)); 
		valueList.add(profileBean.getBirthday_status()); 
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getConstellation(), 50)); 
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getConstellation_up(), 50)); 
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getHeight(), 10)); 
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getWeight(), 10)); 
		valueList.add(profileBean.getShape1()); 
		valueList.add(profileBean.getShape2()); 
		valueList.add(profileBean.getShape3()); 
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getBlood(), 10)); 
		valueList.add(profileBean.getProvinceid()); 
		valueList.add(profileBean.getCityid());
		valueList.add(profileBean.getProvinceid_born()); 
		valueList.add(profileBean.getCityid_born());
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getSchool(), 50));
		valueList.add(profileBean.getSchool_status());
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getLanguage(), 50)); 
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getDelegate(), 255)); 
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getCompany(), 50)); 
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getBroker(), 50)); 
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getInterest(), 255));
		valueList.add(SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getAgency(), 50));
		valueList.add(profileBean.getGender());
		valueList.add(userId);
		db.updateData(sql, valueList);
		return XingyunCommonConstant.RESPONSE_ERR_STRING;
	}

	/**
	 * 获取帐号设置中基础信息
	 */
	public ProfileBean getUserBaseInfo(User user) throws Throwable{
		Map<Object,Object> map = getUserBaseInfoList(user.getUserId());
		return setUserBaserInfo(user, map);
	}
	/**
	 * 从表user_profile中获取相关资料
	 * 使用索引：index_user_profile_userid
	 */
	private Map<Object,Object> getUserBaseInfoList(String userId) throws Throwable{
		String sql = "SELECT agency, gender, provinceid, cityid FROM user_profile WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	
	/**
	 * 将基础信息整理成profileBean
	 */
	private ProfileBean setUserBaserInfo(User user, Map<Object,Object> map) throws Throwable{
		ProfileBean profileBean = new ProfileBean();
		profileBean.setNickName(user.getNickName());
		profileBean.setWkey(user.getWkey());
		if(map == null)
			return profileBean;
		profileBean.setAgency(CommonUtil.getStringValue(map.get("agency")));
		profileBean.setGender(CommonUtil.getIntValue(map.get("gender")));
		profileBean.setProvinceid(CommonUtil.getIntValue(map.get("provinceid")));
		profileBean.setCityid(CommonUtil.getIntValue(map.get("cityid")));
		return profileBean;
	}
	/**
	 * 保存昵称/域名
	 */
	public void updateProfileAccountInfo(String userId, ProfileBean profileBean) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		String pinyinName = PinyinUtil.getInstance().getSelling(profileBean.getNickName() );
		sqlList.add("UPDATE user SET nickname = '"+profileBean.getNickName().trim()+"' , pinyinname = '" + pinyinName + "' WHERE userid = '" + userId + "'");
		if(StringUtils.isNotBlank(profileBean.getWkey())){
			sqlList.add("UPDATE user SET wkey = '"+profileBean.getWkey().trim()+"' WHERE userid = '" + userId + "'");
		}
		db.batchExecute(sqlList, true);
	}
	/**
	 * 获取帐号设置中联系方式
	 */
	public ProfileBean getUserContactInfo(User user) throws Throwable{
		Map<Object,Object> map = getUserContactInfoList(user.getUserId());
		return setUserContactInfo(user, map);
	}
	/**
	 * 从表user_profile中获取联系方式
	 * 使用索引：index_user_profile_userid
	 */
	public Map<Object,Object> getUserContactInfoList(String userId) throws Throwable{
		String sql = "SELECT mobile, qq, weixin, msn, brokertel, assistanttel, others, express, blogurl FROM user_profile WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	
	/**
	 * 将联系方式整理成profileBean
	 */
	private ProfileBean setUserContactInfo(User user, Map<Object,Object> map) throws Throwable{
		ProfileBean profileBean = new ProfileBean();
		profileBean.setEmail(user.getEmail());
		if(map == null)
			return profileBean;
		profileBean.setMobile(CommonUtil.getStringValue(map.get("mobile")));
		profileBean.setQq(CommonUtil.getStringValue(map.get("qq")));
		profileBean.setWeixin(CommonUtil.getStringValue(map.get("weixin")));
		profileBean.setMsn(CommonUtil.getStringValue(map.get("msn")));
		profileBean.setBrokertel(CommonUtil.getStringValue(map.get("brokertel")));
		profileBean.setAssistanttel(CommonUtil.getStringValue(map.get("assistanttel")));
		profileBean.setOthers(CommonUtil.getStringValue(map.get("others")));
		profileBean.setExpress(CommonUtil.getStringValue(map.get("express")));
		profileBean.setBlogurl(CommonUtil.getStringValue(map.get("blogurl")));
		profileBean.setContactStatusList(getContactStatusList(user.getUserId()));
		return profileBean;
	}
	
	/**
	 * 获取用户资料显示与否信息 
	 * 使用索引：index_user_profile_status_userid
	 */
	public List<String> getContactStatusList(String userId) throws Throwable {
		String sql = "SELECT type FROM user_profile_status WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<String> contactStatusList = new ArrayList<String>();
		for (Map<Object, Object> tempMap : db.retrieveSQL(sql, valueList)) {
			contactStatusList.add(CommonUtil.getStringValue(tempMap.get("type") ) );
		}
		return contactStatusList;
	}
	/**
	 * 保存联系方式
	 * @param showContactInPost 
	 */
	public void updateContactInfo(String userId, ProfileBean profileBean, int showContactInPost) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		String sysTime = DateUtil.getSimpleDateFormat();
		sqlList.add("UPDATE user_profile SET mobile = '"+SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getMobile(), 50)+"'," +
					" qq = '"+SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getQq(), 50)+"'," +
					" weixin = '"+SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getWeixin(), 50)+"'," +
					" msn = '"+SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getMsn(), 100)+"'," +
					" brokertel = '"+SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getBrokertel(), 50)+"'," +
					" assistanttel = '"+SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getAssistanttel(), 50)+"'," +
					" express = '"+SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getExpress(), 255)+"'," +
					" blogurl = '"+SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getBlogurl(), 50)+"'," +
					" others = '"+SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getOthers(), 255)+"' WHERE userid = " + userId);
		sqlList.add("UPDATE user SET email = '"+SpecialCharFilterUtil.filterEncodeAndForbidValue(profileBean.getEmail(),255)+"' WHERE userid = " + userId);
		
		// 更新联络方式在用户头部的显示状态
		sqlList.add("DELETE FROM user_profile_status WHERE userid = " + userId);
		boolean isAddDynamic = false;
		if (profileBean.getContactStatusList() != null) {
			for (String type : profileBean.getContactStatusList()) {
				if (XingyunCommonConstant.USER_PROFILE_STATUS_PHONE.equals(type) 
					|| XingyunCommonConstant.USER_PROFILE_STATUS_EMAIL.equals(type)
					|| XingyunCommonConstant.USER_PROFILE_STATUS_QQ.equals(type)
					|| XingyunCommonConstant.USER_PROFILE_STATUS_WEIXIN.equals(type)
					|| XingyunCommonConstant.USER_PROFILE_STATUS_MSN.equals(type)
					|| XingyunCommonConstant.USER_PROFILE_STATUS_WEIBOURL.equals(type)
					|| XingyunCommonConstant.USER_PROFILE_STATUS_BROKERPHONE.equals(type) ) {
					sqlList.add("INSERT INTO user_profile_status (userid, type, systime) VALUES('" + userId + "','" + type + "', '" + sysTime + "')");
					isAddDynamic = true;
				}
			}
		}
		sqlList.add("UPDATE user_control SET contact = " + showContactInPost + " WHERE userid = " + userId);
		db.batchExecute(sqlList, true);
		if(isAddDynamic)
			PublicQueryUtil.getInstance().addDynamicDataByType(userId, XingyunCommonConstant.DYNAMIC_TYPE_CONTACT);
	}
	
	/**
	 * 从表user_profile中获取最新加入明星的相关资料
	 * 使用索引：index_user_profile_userid
	 */
	public Map<Object,Object> getNewStarInfoList(String userId) throws Throwable{
		String sql = "SELECT constellation, provinceid, cityid FROM user_profile WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	
	public Map<Object,Object> getMemberShipInfoMap(User user) throws Throwable{
		Map<Object,Object> map = new HashMap<Object, Object>();
		map.put("verified", user.getVerified());
		map.put("verified_reason", user.getVerified_reason());
		map.put("realName", getRealNameByUserId(user.getUserId()));
		return map;
	}
	
	/**
	 * 
	 * 使用索引：index_user_profile_userid
	 */
	public String getRealNameByUserId(String userId) throws Throwable{
		String sql = "SELECT realname FROM user_profile WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return CommonUtil.getStringValue(sql, valueList, "realname");
	}
	
	/**
	 * 获取用户模块明细信息
	 */
	public List<ModuleBean> getModuleList(String userId) throws Throwable {
		List<Map<Object, Object>> otherList = getProfileOtherList(userId, XingyunCommonConstant.USER_SEE);
		List<ModuleBean> moduleList = new ArrayList<ModuleBean>();
		ModuleBean moduleBean = null;
		for (int i = 0; i < otherList.size(); i++) {
			moduleBean = new ModuleBean();
			moduleBean.setId(Integer.valueOf(otherList.get(i).get("id").toString()));
			moduleBean.setName(otherList.get(i).get("name").toString() );
			moduleBean.setSeq(Integer.valueOf(otherList.get(i).get("seq").toString()));
			moduleBean.setVisible(Integer.parseInt(otherList.get(i).get("isshow").toString()));
			if(!XingyunCommonConstant.PROFILE_OTHER_TITLE_BASE.equals(moduleBean.getName()))
				moduleBean.setModuleDetailList(getModuleDetailList(Integer.valueOf(otherList.get(i).get("id").toString())));
			moduleList.add(moduleBean);
		}
		return moduleList;
	}
	
	/**
	 * 获取用户自定义信息
	 * 使用索引：index_user_profile_other_userid
	 */
	public List<Map<Object, Object>> getProfileOtherList(String userID, String lookState) throws Throwable{
		String sql = "SELECT id, name, seq, isshow FROM user_profile_other WHERE userid = ?";
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(userID);
		if(!XingyunCommonConstant.USER_EDIT.equals(lookState)){
			sql += " AND isshow = ?";
			paramList.add(XingyunFaceConstant.FACE_MODULE_SHOW_YES);
		}
		List<Map<Object, Object>> list = db.retrieveSQL(sql, paramList);
		if(list.size() > 0){
			String[] seqValue = {"seq"};
			CommonUtil.compositor(list, seqValue, 0);
		}
		return list;
	}
	
	/**
	 * 获取基础资料模块ID
	 */
	public int getDefaultModulID(List<Map<Object, Object>> moduleList){
		int defaultModulid = 0;
		for(Map<Object, Object> map : moduleList){
			if(XingyunCommonConstant.PROFILE_OTHER_TITLE_BASE.equals(map.get("name").toString())){
				defaultModulid = Integer.parseInt(map.get("id").toString());
				break;
			}
		}
		return defaultModulid;
	}
	
	/**
	 * 保存自定义模块排序
	 */
	public void saveOtherModuleSort(String userID, List<Integer> otherModuleIDs) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		int seq = 0;
		for(Integer mID : otherModuleIDs){
			sqlList.add("UPDATE user_profile_other SET seq = " + seq + " WHERE id = " + mID + " AND userid = '" + userID + "'");
			seq++;
		}
		if(sqlList.size() > 0)
			db.batchExecute(sqlList, true);
	}
	/**
	 * 自定义模块隐藏或隐藏操作
	 */
	public void updateOtherModuleStatus(String userID, int moduleID, int isShow) throws Throwable{
		String sql = "UPDATE user_profile_other SET isshow = ? WHERE id = ? AND userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(isShow);
		valueList.add(moduleID);
		valueList.add(userID);
		db.updateData(sql, valueList);
	}
	
	/**
	 * 保存新添加的自定义模块
	 */
	public int addModuleData(String userID, String moduleName, String moduleText, List<ItemBean> itemBeanList, List<ResPicBean> resPicBeanList) throws Throwable{
		int moduleID = 0;
		try {
			int maxSeq = getProfileOtherMaxSeq(userID);
			moduleID = addProfileOtherData(userID, moduleName, maxSeq + 1);		//添加自定义模块主题表数据
			// 添加自定义模块文本
			if(StringUtils.isNotBlank(moduleText)){
				itemBeanList = itemBeanList == null ? new ArrayList<ItemBean>() : itemBeanList;
				ItemBean itemBean = new ItemBean();
				itemBean.setC1(moduleText);
				itemBean.setItemType("text");
				itemBeanList.add(0, itemBean );	
			}
			updateModuleItem(0, moduleID, itemBeanList, resPicBeanList, true);	// 更新模块明细信息	
			PublicQueryUtil.getInstance().addDynamicDataByType(userID, XingyunCommonConstant.DYNAMIC_TYPE_PROFILE);
			return moduleID;
		} catch (Throwable e) {
			if(moduleID > 0){
				List<String> sqlList = new ArrayList<String>();
				sqlList.add("DELETE FROM user_profile_other_item WHERE otherid = " + moduleID);
				sqlList.add("DELETE FROM user_profile_other WHERE id = " + moduleID + " AND userid = '" + userID + "'");
				db.batchExecute(sqlList, true);
			}
			throw new Throwable(e);
		}
	}
	
	/**
	 * 保存自定义模块修改数据
	 */
	public void updateModuleData(String userID, int moduleID, String moduleName, int moduleTextID, String moduleText, List<ItemBean> itemBeanList, List<ResPicBean> resPicBeanList) throws Throwable{
		//添加自定义模块文本
		if(StringUtils.isNotBlank(moduleText)){
			itemBeanList = itemBeanList == null ? new ArrayList<ItemBean>() : itemBeanList;
			ItemBean itemBean = new ItemBean();
			itemBean.setItemId(moduleTextID);
			itemBean.setC1(moduleText);
			itemBean.setItemType("text");
			itemBeanList.add(0, itemBean );	
		}
		List<String> updateOtherSqlList = new ArrayList<String>();
		String noDelItemIds = CommonUtil.getNotDelItemIds(itemBeanList);
		String delItemSql = "DELETE FROM user_profile_other_item WHERE otherid = " + moduleID;
		if(StringUtils.isNotBlank(noDelItemIds))
			delItemSql += " AND id NOT IN(" + noDelItemIds + ")";
		updateOtherSqlList.add(delItemSql);
		
		List<String> updateSql = updateModuleItem(1, moduleID, itemBeanList, resPicBeanList, false);			// 更新模块明细信息
		updateOtherSqlList.add("UPDATE user_profile_other SET name = '" + moduleName + "' WHERE id = " + moduleID +" AND userid = '" + userID + "'");
		if(updateSql != null && updateSql.size() > 0)
			updateOtherSqlList.addAll(updateSql);
		if(updateOtherSqlList.size() > 0){
			db.batchExecute(updateOtherSqlList, true);
			PublicQueryUtil.getInstance().addDynamicDataByType(userID, XingyunCommonConstant.DYNAMIC_TYPE_PROFILE);
		}
	}
	
	/**
	 * 添加自定义模块数据
	 */
	private int addProfileOtherData(String userID, String moduleName, int seq) throws NumberFormatException, Throwable {
		String sql = "INSERT INTO user_profile_other(userid, name, seq, isshow, systime) VALUES(?, ?, ?, ?, ?)";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(moduleName);
		vList.add(seq);
		vList.add(XingyunFaceConstant.FACE_MODULE_SHOW_YES);
		vList.add(new Date());
		return db.insertData(sql, vList);
	}
	
	/**
	 * 获取自定义模块排序最大ID
	 * 使用索引：index_user_profile_other_userid
	 */
	private int getProfileOtherMaxSeq(String userID) throws Throwable {
		String sql = "SELECT MAX(seq) AS seq FROM user_profile_other WHERE userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		return CommonUtil.getIntValue(sql, vList, "seq");
	}
	
	/**
	 * 插入或更新模块明细数据
	 * @param tag	        标识符 0为插入 1为更新
	 * @param moduleID	    自定义模块Id
	 * @param itemBeanList	明细集合
	 */
	private List<String> updateModuleItem(int tag, int moduleID, List<ItemBean> itemBeanList, List<ResPicBean> resPicBeanList, boolean opType) throws Throwable{
		if(itemBeanList == null || itemBeanList.size() <= 0)
			return null;
		
		String sysTime = DateUtil.getSimpleDateFormat();
		String itemType = "";
		int type = 0;
		int seq = 1;
		String c1 = "";
		String c2 = "";
		int itemID = 0;
		List<String> sqlList = new ArrayList<String>();
		for(ItemBean item : itemBeanList){
			itemType = item.getItemType();
			if("text".equals(itemType)){
				type = XingyunPostConstant.XINGYUN_MODUAL_TEXT;
				c1 = StringUtils.isBlank(item.getC1()) ? "" : SpecialCharFilterUtil.filterEncodeAndForbidValue0(item.getC1(), XingyunPostConstant.TEXT_CONTENT_SIZE);
			}else if("pic".equals(itemType)){
				type = XingyunPostConstant.XINGYUN_MODUAL_PIC;
				c1 = item.getC1();
				c2 = StringUtils.isBlank(item.getC2()) ? "" : SpecialCharFilterUtil.filterEncodeAndForbidValue0(item.getC2(), XingyunPostConstant.PIC_TITLE_MAXSIZE);
			}
			
			if(type > 0){
				if(XingyunPostConstant.XINGYUN_MODUAL_TEXT == type || XingyunPostConstant.XINGYUN_MODUAL_PIC == type){
					if(tag == 1)
						itemID = item.getItemId();					
					if(tag == 0 || (tag == 1 && itemID == 0)){
						sqlList.add("INSERT INTO user_profile_other_item(otherid, type, c1, c2, seq, systime) VALUES(" + moduleID + ", " + type + ", '" + c1 + "', '" + c2 + "', " + seq + ", '" + sysTime + "')");
					}else if(tag == 1 && itemID > 0){			
						sqlList.add("UPDATE user_profile_other_item SET c1 = '" + c1 + "', c2 = '" + c2 + "', seq = " + seq + ", systime = '" + sysTime + "' WHERE id = " + itemID + " AND otherid = " + moduleID);	
					}
				}
			}
			seq++;				
			type = 0;
			c1 = "";
			c2 = "";
			itemID = -1;
		}
		if(resPicBeanList != null && resPicBeanList.size() > 0){
			String resPicSql = "";
			for(ResPicBean resPicBean : resPicBeanList){
				resPicSql = CommonUtil.getInsertResPicSql(resPicBean);
				if(StringUtils.isNotBlank(resPicSql))
					sqlList.add(resPicSql);
			}
		}
		if(opType)
			db.batchExecute(sqlList, true);
		return sqlList;
	}
	
	/**
	 * 获取自定义模块标题
	 */
	public String getProfileOtherTitle(String userID, int moduleID) throws Throwable {
		String sql = "SELECT name FROM user_profile_other WHERE id = ? AND userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(moduleID);
		vList.add(userID);
		return CommonUtil.getStringValue(sql, vList, "name");
	}
	
	/**
	 * 检查自定义模块是否可以删除
	 */
	public boolean checkDelProfileOther(String userID, int moduleID) throws Throwable{
		String name = getProfileOtherTitle(userID, moduleID);
		return StringUtils.isNotBlank(name);
	}
	
	/**
	 * 根据自定义模块ID整理编辑详情数据
	 */
	public List<Map<Object, Object>> getProfileOtherItemData(int moduleID) throws Throwable{
		List<Map<Object, Object>> list = findProfileOtherItemData(moduleID);
		if(list == null || list.size() == 0)
			return null;
		for(Map<Object, Object> map : list){
			String valueStr = map.get("c1").toString();
			if(XingyunPostConstant.XINGYUN_MODUAL_PIC != Integer.parseInt(map.get("type").toString()))
				continue;
			map.put("picThumb", UploadPicUtil.getPicWebUrl(valueStr, XingyunUploadFileConstant.PROFILE_OTHER_WIDTH_150));
			map.put("picSrc", UploadPicUtil.getPicWebUrl(valueStr, XingyunUploadFileConstant.PROFILE_OTHER_WIDTH_740));
			map.put("picSrcHeight", UploadPicUtil.getPicHeight(valueStr, XingyunUploadFileConstant.PROFILE_OTHER_WIDTH_740));
			map.put("picName", valueStr);
		}
		return list;
	}
	
	/**
	 * 根据自定义模块ID 查询自定义模块明细信息
	 * 使用索引：index_user_profile_other_item_otherid
	 */
	private List<Map<Object, Object>> findProfileOtherItemData(int moduleID) throws Throwable{
		String sql = "SELECT id, otherid, type, c1, c2, seq FROM user_profile_other_item WHERE otherid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(moduleID);
		List<Map<Object, Object>> list =  db.retrieveSQL(sql, valueList);
		if(list.size() > 0)
			CommonUtil.compositor(list, new String[]{"seq"}, 0);
		return list;
	}
	
	/**
	 * 获取用户自定义模块数据 
	 */
	private List<Map<Object, Object>> getModuleDetailList(int otherId) throws Throwable {
		List<Map<Object, Object>> list = findProfileOtherItemData(otherId);
		if(list == null || list.size() <= 0)
			return null;
		
		for(Map<Object, Object> map : list){
			if(Integer.parseInt(map.get("type").toString()) == XingyunPostConstant.XINGYUN_MODUAL_PIC){
				map.put("picMid", UploadPicUtil.getPicWebUrl(map.get("c1").toString(), XingyunUploadFileConstant.PROFILE_OTHER_WIDTH_500));
				map.put("picMidHeight", UploadPicUtil.getPicHeight(map.get("c1").toString(), XingyunUploadFileConstant.PROFILE_OTHER_WIDTH_500));
			}
		}
		return list;
	}
	
	/**
	 * 自定义模块删除
	 */
	public void delProfileOther(String userID, int moduleID) throws Throwable{
		String delOtherSql = "DELETE FROM user_profile_other WHERE id = " + moduleID + " AND userid = '" + userID + "'";
		String delOtherItemSql = "DELETE FROM user_profile_other_item WHERE otherid = " + moduleID;
		List<String> updatePostSqlList = new ArrayList<String>();
		updatePostSqlList.add(delOtherSql);
		updatePostSqlList.add(delOtherItemSql);
		db.batchExecute(updatePostSqlList, true);
	}
	
	/**
	 * 保存用户全身照
	 */
	public void saveWholeBodyData(String userID, ResPicBean wholeBodyResPicBean) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("UPDATE user_profile SET wholebodypic = '"+wholeBodyResPicBean.getPicid()+"' WHERE userid = '"+userID+"'");
		String resPicSql = CommonUtil.getInsertResPicSql(wholeBodyResPicBean);
		if(StringUtils.isNotBlank(resPicSql))
			sqlList.add(resPicSql);
		db.batchExecute(sqlList, true);
	}
	
	/**
	 * 获取帐号设置中 系统设置信息
	 */
	public Map<Object,Object> getUserSystemSetMap(String userID) throws Throwable{
		String sql = "SELECT picwater, postcomment, xingyucomment FROM user_control WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userID);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	
	/**
	 * 保存用户系统设置信息
	 */
	public void updateUserSystem(String userId, int picwater, int postcomment, int xingyucomment) throws Throwable{
		String sql = "UPDATE user_control SET picwater = ?, postcomment = ?, xingyucomment = ? WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(picwater);
		valueList.add(postcomment);
		valueList.add(xingyucomment);
		valueList.add(userId);
		db.updateData(sql, valueList);
	}
	/**
	 * 获取用户图片水印设置
	 */
	public int getUserPicWater(String userID) throws Throwable{
		String sql = "SELECT picwater FROM user_control WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userID);
		return CommonUtil.getIntValue(sql, valueList, "picwater");
	}
	
	/**
	 * 查询用户合作报价数据
	 */
	public List<CooperationPriceBean> getCooperationPriceList(String userId) throws Throwable{
		String sql = "SELECT id, name, price_min, price_max, price_type, content FROM user_cooperation_price WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		
		List<CooperationPriceBean> beanList = new ArrayList<CooperationPriceBean>();
		CooperationPriceBean bean = null;
		for(Map<Object,Object> map : list){
			bean = new CooperationPriceBean();
			bean.setId(Integer.parseInt(map.get("id").toString()));
			bean.setName(map.get("name").toString());
			bean.setPrice_min(map.get("price_min").toString());
			bean.setPrice_max(map.get("price_max").toString());
			bean.setFmt_price_min(CommonUtil.formatPrice(Integer.parseInt(map.get("price_min").toString())));
			bean.setFmt_price_max(CommonUtil.formatPrice(Integer.parseInt(map.get("price_max").toString())));
			bean.setPrice_type(Integer.parseInt(map.get("price_type").toString()));
			bean.setContent(map.get("content").toString());
			beanList.add(bean);
		}
		return beanList;
	}
	
	/**
	 * 保存用户合作报价数据
	 */
	public void saveUserCooperationPriceData(String userId, List<CooperationPriceBean> cooperationPriceList, String cooperationContent) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("DELETE FROM user_cooperation_price WHERE userid = '" + userId + "'");
		if(cooperationPriceList != null && cooperationPriceList.size() > 0){
			String systime = DateUtil.getSimpleDateFormat();
			StringBuilder sb = new StringBuilder();
			String name = "";
			int price_min = 0;
			int price_max = 0;
			cooperationContent = SpecialCharFilterUtil.filterEncodeAndForbidValue0(cooperationContent, 150);
			int count = 0;
			for(CooperationPriceBean bean : cooperationPriceList){
				name = SpecialCharFilterUtil.filterEncodeAndForbidValue(bean.getName(), 50);
				if(StringUtils.isBlank(name))
					continue;
				price_min = CommonUtil.checkInteger(bean.getPrice_min()) ? Integer.parseInt(bean.getPrice_min().trim()) : 0;
				price_max = CommonUtil.checkInteger(bean.getPrice_max()) ? Integer.parseInt(bean.getPrice_max().trim()) : 0;
				sb.append("INSERT INTO user_cooperation_price(userid, name, price_min, price_max, price_type, content, systime) VALUES(");
				sb.append("'").append(userId).append("',");
				sb.append("'").append(name).append("',");
				sb.append(price_min < 0 ? "0" : "" + price_min).append(",");
				sb.append(price_max < 0 ? "0" : "" + price_max).append(",");
				sb.append(bean.getPrice_type() == XingyunFaceConstant.COOPERATION_PRICE_TYPE_0 ? XingyunFaceConstant.COOPERATION_PRICE_TYPE_0 : XingyunFaceConstant.COOPERATION_PRICE_TYPE_1).append(",");
				sb.append("'").append(cooperationContent).append("',");
				sb.append("'").append(systime).append("')");
				sqlList.add(sb.toString());
				sb.setLength(0);
				count++;
				if(count >= XingyunFaceConstant.COOPERATION_PRICE_MAX_SIZE)
					break;
			}
		}
		db.batchExecute(sqlList, true);
	}
	
	/**
	 * 获取付费用户信息
	 */
	public Map<Object,Object> getPayUserMap(String userID) throws Throwable{
		Map<Object,Object> payUserMap = getPayUserInfoMap(userID);
		if(payUserMap == null)
			return null;
		
		payUserMap.put("endTime", DateUtil.getSimpleDate((Date)payUserMap.get("endtime")));
		return payUserMap;
	}
	
	/**
	 * 获取付费用户信息
	 */
	private Map<Object,Object> getPayUserInfoMap(String userId) throws Throwable{
		String sql = "SELECT id, month, endtime, systime FROM pay_user WHERE userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, vList);
		return list.size() == 0 ? null : list.get(0);
	}
	
	/**
	 * 更新用户目前状态（求职状态）
	 */
	public void updateJobStatus(String userId, int jobStatus) throws Throwable {
		String sql = "UPDATE user SET job_status = ? WHERE userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(jobStatus);
		vList.add(userId);
		db.updateData(sql, vList);
	}
	
	/**
	 * 检查是否显示联系方式
	 */
	public int checkShowContact(String userid) throws Throwable {
		String sql = "SELECT contact FROM user_control WHERE userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userid);
		return CommonUtil.getIntValue(sql, vList, "contact");
	}
}
