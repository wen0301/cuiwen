package com.xingyun.actions.profile;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.AuthUserAction;
import com.xingyun.bean.CooperationPriceBean;
import com.xingyun.bean.ItemBean;
import com.xingyun.bean.ProfileBean;
import com.xingyun.bean.ProfileVideoItemBean;
import com.xingyun.bean.ResPicBean;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunLine;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.services.profile.ProfileService;
import com.xingyun.services.recommend.RecommendService;
import com.xingyun.services.sinaoauth.SinaOauthService;
import com.xingyun.upload.bean.UploadParamBean;
import com.xingyun.upload.services.UploadService;
import com.xingyun.util.AreaUtil;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.SpecialCharFilterUtil;
import com.xingyun.util.UploadPicUtil;

public class ProfileUpdateAction extends AuthUserAction {
	
	private static final long serialVersionUID = 8871906252308383138L;
	private static final Logger log = Logger.getLogger(ProfileUpdateAction.class);	
	
	private List<Map<Object, Object>> logoList;	//用户头像列表
	private List<UploadParamBean> p;			//用户上传头像信息		
	private List<Integer> logoIDs;				//用户头像ID集合	
	private int clearLogoID;					//要删除的用户头像ID	
	
	private List<Map<Object, Object>> faceList;	//用户封面列表
	
	private String content;
	private int isShowVideo;
	private int isShowFacePic;
	private int isShowPost;
	private int isShowfollow;
	private int isShowCooperation;
	private int isShowXingyu;
	private int moduleType;
	
	private int videoId;
	private ProfileVideoItemBean videoItemBean;
	private List<ProfileVideoItemBean> videoItemList;
	
	private UserHeaderBean userHeaderBean;	//头部数据bean
	private ProfileBean profileBean;
	private List<Map<Object,Object>> provinceList;
	private List<Map<Object,Object>> cityList;
	private List<Map<Object,Object>> cityList_born;
	private String userId;
	private Map<Object,Object> memberShipMap;
	
	private List<Map<Object, Object>> profileOtherList;		//档案自定义模块列表
	private List<Map<Object, Object>> profileOtherItemList;	//档案自定义模块明细列表
	private int moduleID; 									//档案自定义模块ID
	private List<Integer> otherModuleIDs; 					//档案自定义模块ID集合
	private String moduleName; 								//档案自定义模块名称
	private String moduleText;								//档案自定义模块文本内容
	private int moduleTextID;								//档案自定义模块文本内容ID
	private List<ItemBean> itemBeanList; 					//档案自定义模块明细集合
	private int isShowOtherModule;
	private String curLocation;
	private String cooperationContent;						//合作报价联络方式	
	private List<CooperationPriceBean> cooperationPriceList;//合作报价list
	private Map<Object,Object> payUserMap;					//商业付费会员信息
	
	private Map<Object,Object> systemSetMap;  //系统设置集合
	private int picwater;	   								//是否添加水印
	private int postcomment;   								//评论权限：个人作品
	private int xingyucomment; 								//评论权限：星语
	private int jobStatus;	   								//目前状态
	private int showContactInPost;							//在作品详细页显示联系方式
	private int isShareWeibo;                               //是否分享到新浪微博
	private boolean isFromIos;
	
	/**
	 * 查看头像列表
	 */
	public String showLogoList(){
		try {
			logoList = ProfileService.getInstance().getUserLogoList(user.getUserId(), XingyunUploadFileConstant.LOGO_WIDTH_150);	//查询用户作品列表
			return "showLogoList";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 保存用户上传头像
	 */
	public void saveUploadLogoAjax(){
		try {
			if(p == null || p.size() == 0 || (clearLogoID == 0 && !ProfileService.getInstance().checkUserUploadLogo(user.getUserId()))){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return ;
			}
			
			ResPicBean logoResPicBean = UploadService.getInstance().logoUploadSave(user.getUserId(), p.get(0));	//保存用户上传头像
			ProfileService.getInstance().saveUserUploadLogoData(clearLogoID, user.getUserId(), logoResPicBean);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 保存用户头像数据
	 */
	public void saveUserLogoAjax(){
		try {
			if(logoIDs == null || logoIDs.size() == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			ProfileService.getInstance().saveUserLogoData(user.getUserId(), logoIDs);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 删除用户头像数据
	 */
	public void clearUserLogoAjax(){
		try {
			boolean clearTag = ProfileService.getInstance().clearUserLogoData(user.getUserId(), clearLogoID);
			sendResponseMsg(clearTag ? XingyunCommonConstant.RESPONSE_SUCCESS_STRING : XingyunCommonConstant.RESPONSE_ERR_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 查看用户封面图片编辑页面
	 */
	public String showFaceList(){
		try {
			faceList = ProfileService.getInstance().getUserFaceList(user.getUserId());	//获取用户封面图片列表
			return "showFaceEditPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 保存动态封面数据
	 */
	public void saveFaceDynamicAjax(){
		try {			
			ProfileService.getInstance().saveFaceDynamicData(user.getUserId(), p);
			PublicQueryUtil.getInstance().addDynamicDataByType(user.getUserId(), XingyunCommonConstant.DYNAMIC_TYPE_SLIDER);
			CommonUtil.stopTime();
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 编辑一句话简介
	 */
	public void editIntroduction(){
		try {
			ProfileService.getInstance().updateIntroduction(user.getUserId(), content);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}

	/**
	 * 更新目前状态信息
	 */
	public void setJobStatus() {
		try {
			ProfileService.getInstance().updateJobStatus(user.getUserId(), jobStatus);
			if(jobStatus != XingyunCommonConstant.USER_JOB_STATUS_NONE)
				PublicQueryUtil.getInstance().addDynamicDataByType(user.getUserId(), XingyunCommonConstant.DYNAMIC_TYPE_JOBSTATUS);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 管理首页模块显示
	 */
	public void updateModule(){
		try{
			ProfileService.getInstance().saveModule(user.getUserId(), isShowVideo, isShowFacePic, isShowPost, isShowfollow, isShowCooperation, isShowXingyu);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 隐藏个人首页幻灯片，视频简历模块, 合作报价
	 */
	public void hideFaceModule(){
		try{
			ProfileService.getInstance().hideFaceModule(user.getUserId(), moduleType);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 隐藏基础资料及自定义模块
	 */
	public void hideOtherModule(){
		try{
			ProfileService.getInstance().hideOtherModule(moduleID, user.getUserId());
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 添加视频简历
	 */
	public void addVideo(){
		try {
			if(ProfileService.getInstance().getVideoCountByUserId(user.getUserId()) >= XingyunCommonConstant.PROFILE_VIDEO_MAXNUM){
				sendResponseMsg("exceed");
				return;
			}
			if(videoItemBean == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(p == null || p.get(0) == null || p.get(0).getSrc() == null || p.get(0).getSrc().size() == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			
			ResPicBean videoResPicBean = UploadService.getInstance().videoCoverSave(user.getUserId(), p.get(0));
			if(videoResPicBean == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			videoItemBean.setCoverpath(videoResPicBean.getPicid());
			int id = ProfileService.getInstance().addVideoInfo(user.getUserId(), videoItemBean, videoResPicBean);
			sendResponseMsg(id == 0 ? XingyunCommonConstant.RESPONSE_SUCCESS_STRING : id+"");
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 删除视频简历
	 */
	public void delVideo(){
		try{
			ProfileService.getInstance().delVideo(user.getUserId(), videoId);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 保存视频简历排序
	 */
	public void updateVideoSort(){
		try{
			ProfileService.getInstance().updateVideoSort(user.getUserId(), videoItemList);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 打开编辑基础资料页
	 */
	public String editUserProfile(){
		try{
			userHeaderBean = UserHeaderUtil.getUserHeaderByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			profileBean = ProfileService.getInstance().getUserProfileBean(user.getUserId());
			provinceList = AreaUtil.getInstance().findAreaInfo(0);
			if(profileBean.getProvinceid() != 0)
				cityList = AreaUtil.getInstance().findAreaInfo(profileBean.getProvinceid());
			if(profileBean.getProvinceid_born() != 0)
				cityList_born = AreaUtil.getInstance().findAreaInfo(profileBean.getProvinceid_born());
			//整理用户自定义模块
			profileOtherList = ProfileService.getInstance().getProfileOtherList(user.getUserId(), XingyunCommonConstant.USER_EDIT);
			// 获取基础资料模块ID
			moduleID = ProfileService.getInstance().getDefaultModulID(profileOtherList);
			return "showProfileEditPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 编辑基础资料
	 */
	public void updateUserProfile(){
		try {
			ProfileService.getInstance().updateProfileInfo(user.getUserId(), profileBean);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 帐号设置：昵称/域名
	 */
	public String showEidtAccount(){
		try {
			profileBean = ProfileService.getInstance().getUserBaseInfo(user);
			provinceList = AreaUtil.getInstance().findAreaInfo(0);
			if(profileBean.getProvinceid() != 0)
				cityList = AreaUtil.getInstance().findAreaInfo(profileBean.getProvinceid());
			userId = user.getUserId();
			return "showSetAccount";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 设置：系统设置
	 */
	public String showEditSystem(){
		try {
			systemSetMap = ProfileService.getInstance().getUserSystemSetMap(user.getUserId());
			return "showSetSystem";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 设置：系统设置 保存
	 */
	public void saveEditSystemAjax(){
		try {
			ProfileService.getInstance().updateUserSystem(user.getUserId(), picwater, postcomment, xingyucomment);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 ** 帐号设置：昵称/域名
	 */
	public void updateAccountInfo(){
		try {
			if(profileBean == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(!profileBean.getNickName_Original().equals(profileBean.getNickName())){
				String checkNickname = CommonUtil.checkIsExistNickName(profileBean.getNickName());
				if(!XingyunCommonConstant.RESPONSE_SUCCESS_STRING.equals(checkNickname)){
					sendResponseMsg("nickname:" + checkNickname);
					return;
				}
			}
			if(StringUtils.isNotBlank(profileBean.getWkey())){
				String checkWkey = CommonUtil.checkIsExistWkey(profileBean.getWkey());
				if(!XingyunCommonConstant.RESPONSE_SUCCESS_STRING.equals(checkWkey)){
					sendResponseMsg("wkey:" + checkWkey);
					return;
				}
			}
			ProfileService.getInstance().updateProfileAccountInfo(user.getUserId(), profileBean);
			if(isShareWeibo == XingyunCommonConstant.XINGYUN_SHAREWEIBO_YES){
				String content = "我已通过审核成为星云人才！ 我的星云人才个人网站地址：>> "+ XingyunLine.XINGYUN_CN + CommonUtil.getUserIndexHref(user.getUserId(), user.getWkey()) +" 申请成为星云人才真心很难！  #星云网# " + XingyunLine.XINGYUN_CN;
				SinaOauthService.getInstance().shareToWeibo(user.getUserId(), content, RecommendService.getInstance().getLogoUrlByUserId(user.getUserId()));
			}
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	/**
	 * 帐号设置：会员身份
	 */
	public String showMemberShip(){
		try {
			memberShipMap = ProfileService.getInstance().getMemberShipInfoMap(user);
			return "showMemberShip";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 帐号设置：商业付费会员
	 */
	public String showPayUserPage(){
		try {
			payUserMap = ProfileService.getInstance().getPayUserMap(user.getUserId());
			return "showPayUserPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 帐号设置：联系方式
	 */
	public String showEditContact(){
		try {
			profileBean = ProfileService.getInstance().getUserContactInfo(user);
			setShowContactInPost(ProfileService.getInstance().checkShowContact(user.getUserId()));
			return "showSetContact";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 ** 帐号设置：保存联系方式
	 */
	public void updateContactInfo(){
		try {
			if(profileBean == null){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(!profileBean.getEmail().equals(profileBean.getEmail_old()) && !XingyunCommonConstant.RESPONSE_SUCCESS_STRING.equals(CommonUtil.checkIsExistEmail(profileBean.getEmail()))){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			ProfileService.getInstance().updateContactInfo(user.getUserId(), profileBean, showContactInPost);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 根据自定义模块id显示编辑数据
	 */
	public String showProfileOtherByMoudle(){
		try{
			moduleName = ProfileService.getInstance().getProfileOtherTitle(user.getUserId(), moduleID);	//查询自定义模块标题
			if(StringUtils.isBlank(moduleName))
				return "del";
			userHeaderBean = UserHeaderUtil.getUserHeaderByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			profileOtherList = ProfileService.getInstance().getProfileOtherList(user.getUserId(), XingyunCommonConstant.USER_EDIT);
			profileOtherItemList = ProfileService.getInstance().getProfileOtherItemData(moduleID);
			isFromIos = CommonUtil.checkIsIOSDevice(servletRequest);
			return "showProfileOtherMoudle";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 保存自定义模块数据
	 */
	public String saveProfileOtherAjax(){
		try {
			moduleName = SpecialCharFilterUtil.filterEncodeAndForbidValue(moduleName, XingyunCommonConstant.PROFILE_OTHER_TITLE_MAX_LENGTH);
			if(StringUtils.isBlank(moduleName)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			if(itemBeanList != null && itemBeanList.size() > XingyunCommonConstant.PROFILE_OTHER_PICITEM_MAXSIZE + 1){
				sendResponseMsg("exceed");
				return null;
			}
			List<ResPicBean> resPicBeanList = null;
			if(p != null && p.get(0) != null && p.get(0).getSrc() != null && p.get(0).getSrc().size() > 0 && itemBeanList.size() != 0){
				resPicBeanList = UploadService.getInstance().profileOtherItemSave(user.getUserId(), user.getNickName(), CommonUtil.getUserIndexHref(user.getUserId(), user.getWkey()), p.get(0));
				CommonUtil.operateItemBeanListPic(itemBeanList, resPicBeanList);
			}
			moduleID = ProfileService.getInstance().addModuleData(user.getUserId(), moduleName, moduleText, itemBeanList, resPicBeanList);	//保存自定义模块数据
			if("show".equals(curLocation)){
				sendResponseMsg(String.valueOf(moduleID));
				return null;
			}
			//整理用户自定义模块
			profileOtherList = ProfileService.getInstance().getProfileOtherList(user.getUserId(), XingyunCommonConstant.USER_EDIT);
			isFromIos = CommonUtil.checkIsIOSDevice(servletRequest);
			return "showProfileEditLeftPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 保存自定义模块修改数据
	 */
	public String updateProfileOtherAjax(){
		try {
			boolean checkTag = ProfileService.getInstance().checkDelProfileOther(user.getUserId(), moduleID);	//检查自定义模块是否可以编辑
			if(!checkTag){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			
			moduleName = SpecialCharFilterUtil.filterEncodeAndForbidValue(moduleName, XingyunCommonConstant.PROFILE_OTHER_TITLE_MAX_LENGTH);
			if(StringUtils.isBlank(moduleName) || ((p == null || p.get(0) == null) && StringUtils.isBlank(moduleText))){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			
			List<ResPicBean> resPicBeanList = null;
			if(p.get(0).getSrc() != null && p.get(0).getSrc().size() > 0 && itemBeanList.size() != 0){
				resPicBeanList = UploadService.getInstance().profileOtherItemSave(user.getUserId(), user.getNickName(), CommonUtil.getUserIndexHref(user.getUserId(), user.getWkey()), p.get(0));
				CommonUtil.operateItemBeanListPic(itemBeanList, resPicBeanList);
			}
			ProfileService.getInstance().updateModuleData(user.getUserId(), moduleID, moduleName, moduleTextID, moduleText, itemBeanList, resPicBeanList);	//保存自定义模块修改数据
			//整理用户自定义模块
			profileOtherList = ProfileService.getInstance().getProfileOtherList(user.getUserId(), XingyunCommonConstant.USER_EDIT);
			return "showProfileEditLeftPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 查看自定义模块排序数据
	 */
	public String showProfileOtherNameList(){
		try {
			profileOtherList = ProfileService.getInstance().getProfileOtherList(user.getUserId(), XingyunCommonConstant.USER_EDIT);
			return "showProfileOtherSortPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 自定义模块显示隐藏操作
	 */
	public void updateOtherModuleStatus(){
		try {
			ProfileService.getInstance().updateOtherModuleStatus(user.getUserId(), moduleID, isShowOtherModule);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 保存自定义模块排序数据
	 */
	public void saveOtherModuleSortAjax(){
		try {
			ProfileService.getInstance().saveOtherModuleSort(user.getUserId(), otherModuleIDs);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	
	/**
	 * 编辑自定义模块数据
	 */
	public String showProfileOtherEditAjax(){
		try {
			moduleName = ProfileService.getInstance().getProfileOtherTitle(user.getUserId(), moduleID);	//查询自定义模块标题
			if(StringUtils.isBlank(moduleName)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			profileOtherItemList = ProfileService.getInstance().getProfileOtherItemData(moduleID);			//查询自定义模块明细数据
			isFromIos = CommonUtil.checkIsIOSDevice(servletRequest);
			return "showProfileOtherEditPop";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 删除自定义模块数据
	 */
	public void delProfileOtherAjax() throws Throwable {
		try {
			boolean checkTag = ProfileService.getInstance().checkDelProfileOther(user.getUserId(), moduleID);	//检查自定义模块是否可以删除
			if(!checkTag){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			ProfileService.getInstance().delProfileOther(user.getUserId(), moduleID);	//删除自定义模块数据
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 保存上传全身照
	 */
	public void saveUploadWholeBodyAjax(){
		try {
			if(p == null || p.size() == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return ;
			}
			ResPicBean wholebodyResPicBean = UploadService.getInstance().wholeBodyUploadSave(user.getUserId(), p.get(0));	//保存用户上传头像
			ProfileService.getInstance().saveWholeBodyData(user.getUserId(), wholebodyResPicBean);
			CommonUtil.stopTime();
			sendResponseMsg(UploadPicUtil.getPicWebUrl(wholebodyResPicBean.getPicid(), XingyunUploadFileConstant.PROFILE_WHOLEBODY_WIDTH_300));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 打开星云商业代理页面
	 */
	public String showXyProxy(){
		try{
			userHeaderBean = UserHeaderUtil.getUserHeaderByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			return "showXyProxy";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 打开编辑合作报价页面
	 */
	public String showEditCooperationPrice(){
		try{
			cooperationPriceList = ProfileService.getInstance().getCooperationPriceList(user.getUserId());
			return "showEditCooperationPrice";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 保存编辑合作报价数据
	 */
	public void saveEditCooperationPriceAjax(){
		try {
			ProfileService.getInstance().saveUserCooperationPriceData(user.getUserId(), cooperationPriceList, cooperationContent);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	public List<Map<Object, Object>> getLogoList() {
		return logoList;
	}
	public void setLogoList(List<Map<Object, Object>> logoList) {
		this.logoList = logoList;
	}
	public List<UploadParamBean> getP() {
		return p;
	}
	public void setP(List<UploadParamBean> p) {
		this.p = p;
	}
	public List<Integer> getLogoIDs() {
		return logoIDs;
	}
	public void setLogoIDs(List<Integer> logoIDs) {
		this.logoIDs = logoIDs;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public List<Map<Object, Object>> getFaceList() {
		return faceList;
	}

	public void setFaceList(List<Map<Object, Object>> faceList) {
		this.faceList = faceList;
	}

	public int getIsShowVideo() {
		return isShowVideo;
	}

	public void setIsShowVideo(int isShowVideo) {
		this.isShowVideo = isShowVideo;
	}

	public int getIsShowFacePic() {
		return isShowFacePic;
	}

	public void setIsShowFacePic(int isShowFacePic) {
		this.isShowFacePic = isShowFacePic;
	}

	public int getIsShowPost() {
		return isShowPost;
	}

	public void setIsShowPost(int isShowPost) {
		this.isShowPost = isShowPost;
	}

	public List<ProfileVideoItemBean> getVideoItemList() {
		return videoItemList;
	}

	public void setVideoItemList(List<ProfileVideoItemBean> videoItemList) {
		this.videoItemList = videoItemList;
	}
	
	public UserHeaderBean getUserHeaderBean() {
		return userHeaderBean;
	}

	public void setUserHeaderBean(UserHeaderBean userHeaderBean) {
		this.userHeaderBean = userHeaderBean;
	}

	public ProfileBean getProfileBean() {
		return profileBean;
	}

	public void setProfileBean(ProfileBean profileBean) {
		this.profileBean = profileBean;
	}

	public List<Map<Object, Object>> getProvinceList() {
		return provinceList;
	}

	public void setProvinceList(List<Map<Object, Object>> provinceList) {
		this.provinceList = provinceList;
	}

	public List<Map<Object, Object>> getCityList() {
		return cityList;
	}

	public void setCityList(List<Map<Object, Object>> cityList) {
		this.cityList = cityList;
	}

	public Map<Object, Object> getMemberShipMap() {
		return memberShipMap;
	}

	public void setMemberShipMap(Map<Object, Object> memberShipMap) {
		this.memberShipMap = memberShipMap;
	}

	public int getClearLogoID() {
		return clearLogoID;
	}

	public void setClearLogoID(int clearLogoID) {
		this.clearLogoID = clearLogoID;
	}

	public List<Map<Object, Object>> getProfileOtherList() {
		return profileOtherList;
	}

	public void setProfileOtherList(List<Map<Object, Object>> profileOtherList) {
		this.profileOtherList = profileOtherList;
	}

	public List<Map<Object, Object>> getProfileOtherItemList() {
		return profileOtherItemList;
	}

	public void setProfileOtherItemList(
			List<Map<Object, Object>> profileOtherItemList) {
		this.profileOtherItemList = profileOtherItemList;
	}

	public int getModuleID() {
		return moduleID;
	}

	public void setModuleID(int moduleID) {
		this.moduleID = moduleID;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleText() {
		return moduleText;
	}

	public void setModuleText(String moduleText) {
		this.moduleText = moduleText;
	}

	public List<ItemBean> getItemBeanList() {
		return itemBeanList;
	}

	public void setItemBeanList(List<ItemBean> itemBeanList) {
		this.itemBeanList = itemBeanList;
	}

	public int getModuleTextID() {
		return moduleTextID;
	}

	public void setModuleTextID(int moduleTextID) {
		this.moduleTextID = moduleTextID;
	}

	public List<Integer> getOtherModuleIDs() {
		return otherModuleIDs;
	}

	public void setOtherModuleIDs(List<Integer> otherModuleIDs) {
		this.otherModuleIDs = otherModuleIDs;
	}

	public ProfileVideoItemBean getVideoItemBean() {
		return videoItemBean;
	}

	public void setVideoItemBean(ProfileVideoItemBean videoItemBean) {
		this.videoItemBean = videoItemBean;
	}

	public int getVideoId() {
		return videoId;
	}

	public void setVideoId(int videoId) {
		this.videoId = videoId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getModuleType() {
		return moduleType;
	}

	public void setModuleType(int moduleType) {
		this.moduleType = moduleType;
	}

	public List<Map<Object, Object>> getCityList_born() {
		return cityList_born;
	}

	public void setCityList_born(List<Map<Object, Object>> cityList_born) {
		this.cityList_born = cityList_born;
	}

	public int getIsShowOtherModule() {
		return isShowOtherModule;
	}

	public void setIsShowOtherModule(int isShowOtherModule) {
		this.isShowOtherModule = isShowOtherModule;
	}

	public String getCurLocation() {
		return curLocation;
	}

	public void setCurLocation(String curLocation) {
		this.curLocation = curLocation;
	}

	public int getIsShowCooperation() {
		return isShowCooperation;
	}

	public void setIsShowCooperation(int isShowCooperation) {
		this.isShowCooperation = isShowCooperation;
	}

	public List<CooperationPriceBean> getCooperationPriceList() {
		return cooperationPriceList;
	}

	public void setCooperationPriceList(
			List<CooperationPriceBean> cooperationPriceList) {
		this.cooperationPriceList = cooperationPriceList;
	}

	public String getCooperationContent() {
		return cooperationContent;
	}

	public void setCooperationContent(String cooperationContent) {
		this.cooperationContent = cooperationContent;
	}

	public Map<Object, Object> getPayUserMap() {
		return payUserMap;
	}

	public void setPayUserMap(Map<Object, Object> payUserMap) {
		this.payUserMap = payUserMap;
	}

	public int getPicwater() {
		return picwater;
	}

	public void setPicwater(int picwater) {
		this.picwater = picwater;
	}

	public int getPostcomment() {
		return postcomment;
	}

	public void setPostcomment(int postcomment) {
		this.postcomment = postcomment;
	}

	public int getXingyucomment() {
		return xingyucomment;
	}

	public void setXingyucomment(int xingyucomment) {
		this.xingyucomment = xingyucomment;
	}

	public Map<Object, Object> getSystemSetMap() {
		return systemSetMap;
	}

	public void setSystemSetMap(Map<Object, Object> systemSetMap) {
		this.systemSetMap = systemSetMap;
	}

	public int getIsShowXingyu() {
		return isShowXingyu;
	}

	public void setIsShowXingyu(int isShowXingyu) {
		this.isShowXingyu = isShowXingyu;
	}

	public int getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(int jobStatus) {
		this.jobStatus = jobStatus;
	}

	public int getShowContactInPost() {
		return showContactInPost;
	}

	public void setShowContactInPost(int showContactInPost) {
		this.showContactInPost = showContactInPost;
	}

	public int getIsShareWeibo() {
		return isShareWeibo;
	}

	public void setIsShareWeibo(int isShareWeibo) {
		this.isShareWeibo = isShareWeibo;
	}

	public boolean getIsFromIos() {
		return isFromIos;
	}

	public void setIsFromIos(boolean isFromIos) {
		this.isFromIos = isFromIos;
	}

	public int getIsShowfollow() {
		return isShowfollow;
	}

	public void setIsShowfollow(int isShowfollow) {
		this.isShowfollow = isShowfollow;
	}
}
