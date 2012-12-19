package com.xingyun.actions.profile;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.XingyunBaseAction;
import com.xingyun.bean.CooperationPriceBean;
import com.xingyun.bean.ModuleBean;
import com.xingyun.bean.ProfileBean;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.bean.XingyuBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunFaceConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.services.profile.ProfileService;
import com.xingyun.services.xingyu.XingyuService;
import com.xingyun.util.AreaUtil;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.CounterUtil;

public class ProfileShowAction extends XingyunBaseAction{
	private static final long serialVersionUID = 5572236251971285752L;
	private static final Logger log = Logger.getLogger(ProfileShowAction.class);
	private UserHeaderBean userHeaderBean;
	private List<Map<Object,Object>> facePicList;
	private List<Map<Object,Object>> postList;
	private Map<Object,Object> faceInfoMap;
	private List<Map<Object,Object>> videoList;
	private ProfileBean profileBean;
	private List<ModuleBean> moduleList;
	private List<Map<Object,Object>> logoList;
	private int moduleID;
	private String moduleName;
	private List<Map<Object, Object>> profileOtherList;		//档案自定义模块列表
	private List<Map<Object, Object>> profileOtherItemList;	//档案自定义模块明细列表
	private boolean isFromIpad;
	private int provinceId;
	private List<CooperationPriceBean> cooperationPriceList;	//合作报价list
	private List<XingyuBean> xingyuList;						//首页星云数据
	private int xingyuBeanCount;								//用户星云总数数据
	private boolean isFromIos;
	
	public String execute(){
		try{
			if(CommonUtil.checkIsMobileDevice(servletRequest, XingyunCommonConstant.MOBILEDEVICE_TYPE_IPHONE))
				return showProfileByIphone();
			userHeaderBean = StringUtils.isBlank(userid) ? UserHeaderUtil.getUserHeaderByWKey(user, wKey) : UserHeaderUtil.getUserHeaderByUserID(user, userid);
			if(userHeaderBean == null)
				return "del";
			isFromIpad = CommonUtil.checkIsMobileDevice(servletRequest, XingyunCommonConstant.MOBILEDEVICE_TYPE_IPAD);
			CounterUtil.setHomeCount(userHeaderBean.getUserId());
			faceInfoMap = ProfileService.getInstance().getFaceInfo(userHeaderBean.getUserId());
			if(faceInfoMap == null)
				return "del";
			if(Integer.parseInt(faceInfoMap.get("isshowfacepic").toString()) == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
				facePicList = ProfileService.getInstance().getUserFaceList(userHeaderBean.getUserId());
			if(Integer.parseInt(faceInfoMap.get("isshowpost").toString()) == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
				postList = ProfileService.getInstance().getPostInfoList(userHeaderBean.getUserId());
			if(Integer.parseInt(faceInfoMap.get("isshowvideo").toString()) == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
				videoList = ProfileService.getInstance().getVideoList(userHeaderBean.getUserId());
			if(Integer.parseInt(faceInfoMap.get("isshowcooperation").toString()) == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
				cooperationPriceList = ProfileService.getInstance().getCooperationPriceList(userHeaderBean.getUserId());
			if(Integer.parseInt(faceInfoMap.get("isshowxingyu").toString()) == XingyunFaceConstant.FACE_MODULE_SHOW_YES){
				List<Map<Object, Object>> indexList = XingyuService.getInstance().findXingYuAllIndex(userHeaderBean.getUserId(), user); 
				if(indexList != null && indexList.size() > 0){
					xingyuBeanCount = indexList.size();
					xingyuList = XingyuService.getInstance().findXingYuList(user == null ? "" : user.getUserId(), indexList, 1, XingyunCommonConstant.XINGYU_LIST_PAGE_SIZE_INDEX);
				}
			}
			moduleList = ProfileService.getInstance().getModuleList(userHeaderBean.getUserId());
			profileBean = ProfileService.getInstance().getUserProfileBean(userHeaderBean.getUserId());
			return SUCCESS;
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 显示头像幻灯片层
	 */
	public String getUserLogoList(){
		try{
			logoList = ProfileService.getInstance().getUserLogoList(userid, XingyunUploadFileConstant.LOGO_WIDTH_640);
			return "showLogo";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 显示档案模块
	 */
	public String showProfileModule(){
		try{
			userHeaderBean = UserHeaderUtil.getUserHeaderByUserID(user, userid);
			if(userHeaderBean == null)
				return "del";
			CounterUtil.setProfileCount(userHeaderBean.getUserId());
			profileOtherList = ProfileService.getInstance().getProfileOtherList(userHeaderBean.getUserId(),XingyunCommonConstant.USER_EDIT);
			if(profileOtherList == null || profileOtherList.size() == 0)
				return "del";
			setProfileModuleInfo();
			if(XingyunCommonConstant.PROFILE_OTHER_TITLE_BASE.equals(moduleName))
				profileBean = ProfileService.getInstance().getUserProfileBean(userHeaderBean.getUserId());
			else
				profileOtherItemList = ProfileService.getInstance().getProfileOtherItemData(moduleID);
			isFromIos = CommonUtil.checkIsIOSDevice(servletRequest);
			return "showProfileModule";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 整理档案模块信息
	 */
	private void setProfileModuleInfo() throws Throwable{
		if(moduleID == 0){
			moduleID = CommonUtil.getIntValue(profileOtherList.get(0).get("id"));
			moduleName = CommonUtil.getStringValue(profileOtherList.get(0).get("name"));
			return;
		}
		for(Map<Object,Object> map : profileOtherList){
			if(CommonUtil.getIntValue(map.get("id")) == moduleID){
				moduleName = CommonUtil.getStringValue(map.get("name"));
				break;
			}
		}
	}
	
	public String showProfileByIphone(){
		try{
			if(StringUtils.isBlank(userid))
				userid = CommonUtil.getUserIdBywKey(wKey);
			userHeaderBean = UserHeaderUtil.getUserHeaderByIphoneUserID(userid, "profile");
			if(userHeaderBean == null)
				return "del";
			CounterUtil.setHomeCount(userHeaderBean.getUserId());
			faceInfoMap = ProfileService.getInstance().getFaceInfo(userHeaderBean.getUserId());
			if(faceInfoMap == null)
				return "del";
			if(Integer.parseInt(faceInfoMap.get("isshowfacepic").toString()) == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
				facePicList = ProfileService.getInstance().getUserFaceList(userHeaderBean.getUserId());
			if(Integer.parseInt(faceInfoMap.get("isshowvideo").toString()) == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
				videoList = ProfileService.getInstance().getVideoList(userHeaderBean.getUserId());
			postList = ProfileService.getInstance().getPostInfoListByIphone(userHeaderBean.getUserId());
			moduleList = ProfileService.getInstance().getModuleList(userHeaderBean.getUserId());
			profileBean = ProfileService.getInstance().getUserProfileBean(userHeaderBean.getUserId());
			return "showProfileByIphone";
		}catch(Throwable e){
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 通过省份id获取市级列表
	 */
	public void getCityListByProvinceId(){
		try{
			sendResponseMsg(AreaUtil.getInstance().findCityData(provinceId));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	public UserHeaderBean getUserHeaderBean() {
		return userHeaderBean;
	}
	public void setUserHeaderBean(UserHeaderBean userHeaderBean) {
		this.userHeaderBean = userHeaderBean;
	}
	public List<Map<Object, Object>> getFacePicList() {
		return facePicList;
	}
	public void setFacePicList(List<Map<Object, Object>> facePicList) {
		this.facePicList = facePicList;
	}
	public List<Map<Object, Object>> getPostList() {
		return postList;
	}
	public void setPostList(List<Map<Object, Object>> postList) {
		this.postList = postList;
	}
	public Map<Object, Object> getFaceInfoMap() {
		return faceInfoMap;
	}
	public void setFaceInfoMap(Map<Object, Object> faceInfoMap) {
		this.faceInfoMap = faceInfoMap;
	}
	public List<Map<Object, Object>> getVideoList() {
		return videoList;
	}
	public void setVideoList(List<Map<Object, Object>> videoList) {
		this.videoList = videoList;
	}
	public ProfileBean getProfileBean() {
		return profileBean;
	}
	public void setProfileBean(ProfileBean profileBean) {
		this.profileBean = profileBean;
	}
	public List<ModuleBean> getModuleList() {
		return moduleList;
	}
	public void setModuleList(List<ModuleBean> moduleList) {
		this.moduleList = moduleList;
	}
	public List<Map<Object, Object>> getLogoList() {
		return logoList;
	}
	public void setLogoList(List<Map<Object, Object>> logoList) {
		this.logoList = logoList;
	}
	public int getModuleID() {
		return moduleID;
	}
	public void setModuleID(int moduleID) {
		this.moduleID = moduleID;
	}
	public List<Map<Object, Object>> getProfileOtherItemList() {
		return profileOtherItemList;
	}
	public void setProfileOtherItemList(
			List<Map<Object, Object>> profileOtherItemList) {
		this.profileOtherItemList = profileOtherItemList;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	public List<Map<Object, Object>> getProfileOtherList() {
		return profileOtherList;
	}
	public void setProfileOtherList(List<Map<Object, Object>> profileOtherList) {
		this.profileOtherList = profileOtherList;
	}
	public boolean getIsFromIpad() {
		return isFromIpad;
	}
	public void setIsFromIpad(boolean isFromIpad) {
		this.isFromIpad = isFromIpad;
	}
	public int getProvinceId() {
		return provinceId;
	}
	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}
	public List<CooperationPriceBean> getCooperationPriceList() {
		return cooperationPriceList;
	}
	public void setCooperationPriceList(
			List<CooperationPriceBean> cooperationPriceList) {
		this.cooperationPriceList = cooperationPriceList;
	}
	public int getXingyuBeanCount() {
		return xingyuBeanCount;
	}
	public void setXingyuBeanCount(int xingyuBeanCount) {
		this.xingyuBeanCount = xingyuBeanCount;
	}
	public List<XingyuBean> getXingyuList() {
		return xingyuList;
	}
	public void setXingyuList(List<XingyuBean> xingyuList) {
		this.xingyuList = xingyuList;
	}
	public boolean getIsFromIos() {
		return isFromIos;
	}
	public void setIsFromIos(boolean isFromIos) {
		this.isFromIos = isFromIos;
	}
}
