package com.xingyun.upload.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.xingyun.bean.ResPicBean;
import com.xingyun.constant.UploadResultCode;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunFirstDirConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBgetNextID;
import com.xingyun.services.profile.ProfileService;
import com.xingyun.upload.bean.UploadParamBean;
import com.xingyun.util.DateUtil;
import com.xingyun.util.JsonObjectUtil;
import com.xingyun.util.UploadDBUtil;
import com.xingyun.util.UploadPicUtil;

public class UploadService {

	private static final UploadService uploadSerivce = new UploadService();
	private UploadService(){}
	
	public static UploadService getInstance(){
		return uploadSerivce;
	}
	
	/**
	 * 头像预览
	 */
	public String userLogoPre(UploadParamBean paramBean) throws Throwable{
		String dest = UploadPicUtil.getPreTempPicPath(paramBean.getSrc().get(0));
		paramBean.setDest(Arrays.asList(dest));
		//处理图片
		String responseJson = UploadDeal.UPLOAD_TYPE_LOGO.pre(paramBean, null);
		//检查上传图片信息
		String code = getUploadPicCode(responseJson);
		if(!UploadResultCode.UPLOAD_PROCESS_SUCC.equals(code))
			throw new Throwable("upload error responseJson = " + responseJson);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", code);
		map.put("visitPath", "/" + dest);
		return JsonObjectUtil.getJsonStr(map);
	}
	
	/**
	 * 用户上传头像图片 保存
	 */
	public ResPicBean logoUploadSave(String userID, UploadParamBean paramBean) throws Throwable{
		String newPicID = DBgetNextID.getInstance().getUserNewPicID();
		String newPicPath = UploadPicUtil.getUserNewPicPath(userID, XingyunUploadFileConstant.UPLOAD_TYPE_LOGO, newPicID);
		Multimap<String, String> destMulMap = ArrayListMultimap.<String, String>create();
		destMulMap.put("dest640", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.LOGO_WIDTH_640));
		destMulMap.put("dest250", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.LOGO_WIDTH_250));
		destMulMap.put("dest200", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.LOGO_WIDTH_200));
		destMulMap.put("dest150", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.LOGO_WIDTH_150));
		destMulMap.put("dest100", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.LOGO_WIDTH_100));
		destMulMap.put("dest75", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.LOGO_WIDTH_75));
		destMulMap.put("dest50", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.LOGO_WIDTH_50));
		
		String responseJson = UploadDeal.UPLOAD_TYPE_LOGO.save(paramBean, destMulMap);
		return getUploadResPicBean(responseJson, newPicID, userID, paramBean.getUploadServer(), XingyunUploadFileConstant.UPLOAD_TYPE_LOGO);
	}
	
	/**
	 * 作品明细预览
	 */
	public String postItemPre(UploadParamBean paramBean) throws Throwable{
		List<Map<String, String>> picList = new ArrayList<Map<String, String>>();
		Multimap<String, String> destMulMap = ArrayListMultimap.<String, String> create();
		String thumbPath = "";
		Map<String, String> picMap = null;
		List<String> srcPathList = paramBean.getSrc();
		for (String srcPath : srcPathList){
			thumbPath = UploadPicUtil.getPreTempPicPath(srcPath);
			picMap = new HashMap<String, String>();
			picMap.put("visitPath", "/" + thumbPath);
			picMap.put("yuantu", srcPath);
			picList.add(picMap);
			destMulMap.put("thumb", thumbPath);
		}
		
		//处理图片
		String responseJson = UploadDeal.UPLOAD_TYPE_POST_ITEM.pre(paramBean, destMulMap);
		//检查上传图片信息
		String code = getUploadPicCode(responseJson);
		if(!UploadResultCode.UPLOAD_PROCESS_SUCC.equals(code))
			throw new Throwable("upload error responseJson = " + responseJson);
		Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put("code", code);
		codeMap.put("pic", picList);
		return JsonObjectUtil.getJsonStr(codeMap);
	}
	
	/**
	 * 作品封面预览
	 */
	public String postCoverPre(UploadParamBean paramBean) throws Throwable{
		String destPath = UploadPicUtil.getPreTempPicPath(paramBean.getSrc().get(0));
		paramBean.setDest(Arrays.asList(destPath));
		//处理图片
		String responseJson = UploadDeal.UPLOAD_TYPE_POST_COVER.pre(paramBean, null);
		//检查上传图片信息
		String code = getUploadPicCode(responseJson);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", code);
		if(UploadResultCode.UPLOAD_PROCESS_SUCC.equals(code))
			map.put("visitPath", destPath.startsWith("/") ? destPath : ("/" + destPath));
		return JsonObjectUtil.getJsonStr(map);
	}
	
	/**
	 * 作品明细保存
	 */
	public List<ResPicBean> postItemSave(String userID, String nickName, String watermarkContent, UploadParamBean paramBean) throws Throwable{
		List<ResPicBean> postResPicList = new ArrayList<ResPicBean>();
		if(paramBean.getSrc() == null || paramBean.getSrc().size() == 0)
			return postResPicList;
		
		ResPicBean resPicBean = null;
		String newPicID = "";
		String newPicPath = "";
		Multimap<String, String> destMulMap = ArrayListMultimap.<String, String> create();
		for (String srcPicPath : paramBean.getSrc()){
			newPicID = DBgetNextID.getInstance().getUserNewPicID();
			newPicPath = UploadPicUtil.getUserNewPicPath(userID, XingyunUploadFileConstant.UPLOAD_TYPE_POST_ITEM, newPicID);
			
			destMulMap.put("src", newPicPath);
			destMulMap.put("mid", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_ITEM_WIDTH_1010));
			destMulMap.put("mid_640", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_ITEM_WIDTH_640));
			destMulMap.put("thumb", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_ITEM_WIDTH_150));
			
			resPicBean = new ResPicBean();
			resPicBean.setSrcPicPath(srcPicPath);
			resPicBean.setPicid(newPicID);
			postResPicList.add(resPicBean);
		}
		paramBean.setWatermarkNickName(nickName);
		paramBean.setWatermarkWkey(watermarkContent);	//设置水印内容
		paramBean.setIsWater(ProfileService.getInstance().getUserPicWater(userID) == XingyunCommonConstant.PICWATER_YES);	//是否添加水印
		String responseJson = UploadDeal.UPLOAD_TYPE_POST_ITEM.save(paramBean, destMulMap);
		return getUploadMsgResPicBeanList(responseJson, postResPicList, userID, paramBean.getUploadServer(), XingyunUploadFileConstant.UPLOAD_TYPE_POST_ITEM);
	}
	
	/**
	 * 作品封面保存
	 */
	public ResPicBean postCoverSave(String userID, UploadParamBean paramBean) throws Throwable{
		String newPicID = DBgetNextID.getInstance().getUserNewPicID();
		String newPicPath = UploadPicUtil.getUserNewPicPath(userID, XingyunUploadFileConstant.UPLOAD_TYPE_POST_COVER, newPicID);
		Multimap<String, String> destMulMap = ArrayListMultimap.<String, String>create();
		destMulMap.put("dest250", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_COVER_WIDTH_250));
		destMulMap.put("dest220", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_COVER_WIDTH_220));
		destMulMap.put("dest200", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_COVER_WIDTH_200));
		destMulMap.put("dest190", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_COVER_WIDTH_190));
		destMulMap.put("dest165", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_COVER_WIDTH_165));
		destMulMap.put("dest150", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_COVER_WIDTH_150));

		String responseJson = UploadDeal.UPLOAD_TYPE_POST_COVER.save(paramBean, destMulMap);
		return getUploadResPicBean(responseJson, newPicID, userID, paramBean.getUploadServer(), XingyunUploadFileConstant.UPLOAD_TYPE_POST_COVER);
	}
	
	/**
	 * 作品封面保存(作品第一张图片做封面)
	 */
	public ResPicBean postCoverSave(String userID, UploadParamBean paramBean, String firstPicPath) throws Throwable{
		String newPicID = DBgetNextID.getInstance().getUserNewPicID();
		String newPicPath = UploadPicUtil.getUserNewPicPath(userID, XingyunUploadFileConstant.UPLOAD_TYPE_POST_COVER, newPicID);
		Multimap<String, String> destMulMap = ArrayListMultimap.<String, String>create();
		destMulMap.put("postCoverType", "item_cover");
		destMulMap.put("dest250", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_COVER_WIDTH_250));
		destMulMap.put("dest220", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_COVER_WIDTH_220));
		destMulMap.put("dest200", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_COVER_WIDTH_200));
		destMulMap.put("dest190", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_COVER_WIDTH_190));
		destMulMap.put("dest165", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_COVER_WIDTH_165));
		destMulMap.put("dest150", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.POST_COVER_WIDTH_150));
		if(StringUtils.isNotBlank(UploadPicUtil.getPicPath(firstPicPath, 0)))
			paramBean.setSrc(Arrays.asList(UploadPicUtil.getPicPath(firstPicPath, 0)));
		else
			paramBean.setSrc(Arrays.asList(firstPicPath));
		String responseJson = UploadDeal.UPLOAD_TYPE_POST_COVER.save(paramBean, destMulMap);
		return getUploadResPicBean(responseJson, newPicID, userID, paramBean.getUploadServer(), XingyunUploadFileConstant.UPLOAD_TYPE_POST_COVER);
	}
	
	/**
	 * 动态封面 裁剪预览
	 */
	public String faceDynamicPre(UploadParamBean paramBean) throws Throwable{
		String destPath = UploadPicUtil.getPreTempPicPath(paramBean.getSrc().get(0));
		paramBean.setDest(Arrays.asList(destPath));
		//处理图片
		String responseJson = UploadDeal.UPLOAD_TYPE_FACE_DYNAMIC.pre(paramBean, null);
		//检查上传图片信息
		String code = getUploadPicCode(responseJson);
		if(!UploadResultCode.UPLOAD_PROCESS_SUCC.equals(code))
			throw new Throwable("upload error responseJson = " + responseJson);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", code);
		map.put("visitPath", "/" + destPath);
		return JsonObjectUtil.getJsonStr(map);
	}
	
	/**
	 * 动态封面图片保存
	 */
	public List<ResPicBean> faceDynamicSave(String userID, List<UploadParamBean> pList) throws Throwable{
		List<ResPicBean> faceResPicList = new ArrayList<ResPicBean>();
		if(pList == null || pList.size() == 0)
			return faceResPicList;
		
		Multimap<String, String> destMulMap = ArrayListMultimap.<String, String>create();
		List<String> uploadSrcList = new ArrayList<String>();
		ResPicBean resPicBean = null;
		String newPicID = "";
		String newPicPath = "";
		String srcPicPath = "";
		for(UploadParamBean bean : pList){
			if(bean.getSrc() == null || bean.getSrc().size() == 0)
				continue;	
			
			srcPicPath = bean.getSrc().get(0);
			resPicBean = new ResPicBean();
			//新图片 需要处理
			if(srcPicPath.startsWith(XingyunFirstDirConstant.UPLOAD_TMP_DIR)){
				uploadSrcList.add(srcPicPath);
				newPicID = DBgetNextID.getInstance().getUserNewPicID();
				newPicPath = UploadPicUtil.getUserNewPicPath(userID, XingyunUploadFileConstant.UPLOAD_TYPE_FACE_DYNAMIC, newPicID);
				destMulMap.put("dest", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.FACE_DYNAMIC_WIDTH_1020));
				destMulMap.put("thumb", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.FACE_DYNAMIC_WIDTH_150));
				destMulMap.put("phone", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.FACE_PHONE_WIDTH_619));
				resPicBean.setPicid(newPicID);
			}else if(UploadPicUtil.findPicMap(srcPicPath, userID) != null){
				resPicBean.setPicid(srcPicPath);
			}else{
				continue;
			}
			faceResPicList.add(resPicBean);
		}
		if(destMulMap.size() == 0)
			return faceResPicList;
		
		String uploadServer = pList.get(0).getUploadServer();
		UploadParamBean paramBean = new UploadParamBean();
		paramBean.setUploadServer(uploadServer);
		paramBean.setSrc(uploadSrcList);
		String responseJson = UploadDeal.UPLOAD_TYPE_FACE_DYNAMIC.save(paramBean, destMulMap);
		return getUploadResPicBeanList(responseJson, faceResPicList, userID, uploadServer, XingyunUploadFileConstant.UPLOAD_TYPE_FACE_DYNAMIC);
	}
	
	/**
	 * 视频封面图片预览
	 */
	public String videoCoverPre(UploadParamBean paramBean) throws Throwable{
		String dest = UploadPicUtil.getPreTempPicPath(paramBean.getSrc().get(0));
		paramBean.setDest(Arrays.asList(dest));
		//处理图片
		String responseJson = UploadDeal.UPLOAD_TYPE_VIDEO.pre(paramBean, null);
		//检查上传图片信息
		String code = getUploadPicCode(responseJson);
		if(!UploadResultCode.UPLOAD_PROCESS_SUCC.equals(code))
			throw new Throwable("upload error responseJson = " + responseJson);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", code);
		map.put("visitPath", "/" + dest);
		return JsonObjectUtil.getJsonStr(map);
	}
	
	/**
	 * 视频封面图片保存
	 */
	public ResPicBean videoCoverSave(String userID, UploadParamBean paramBean) throws Throwable{
		String newPicID = DBgetNextID.getInstance().getUserNewPicID();
		String newPicPath = UploadPicUtil.getUserNewPicPath(userID, XingyunUploadFileConstant.UPLOAD_TYPE_VIDEO, newPicID);
		Multimap<String, String> destMulMap = ArrayListMultimap.<String, String>create();
		destMulMap.put("dest250", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.VIDEO_WIDTH_250));
		destMulMap.put("dest150", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.VIDEO_WIDTH_150));
		
		String responseJson = UploadDeal.UPLOAD_TYPE_VIDEO.save(paramBean, destMulMap);
		return getUploadResPicBean(responseJson, newPicID, userID, paramBean.getUploadServer(), XingyunUploadFileConstant.UPLOAD_TYPE_VIDEO);
	}
	
	/**
	 * 档案自定义模块明细图片预览
	 */
	public String profileOtherItemPrepare(UploadParamBean paramBean) throws Throwable{
		List<Map<String, String>> picList = new ArrayList<Map<String, String>>();
		Multimap<String, String> destMulMap = ArrayListMultimap.<String, String> create();
		String thumbPath = "";
		Map<String, String> picMap = null;
		List<String> srcPathList = paramBean.getSrc();
		for (String srcPath : srcPathList){
			thumbPath = UploadPicUtil.getPreTempPicPath(srcPath);
			picMap = new HashMap<String, String>();
			picMap.put("visitPath", "/" + thumbPath);
			picMap.put("yuantu", srcPath);
			picList.add(picMap);
			destMulMap.put("thumb", thumbPath);
		}
		
		//处理图片
		String responseJson = UploadDeal.UPLOAD_TYPE_PROFILE_OTHER_ITEM.pre(paramBean, destMulMap);
		//检查上传图片信息
		String code = getUploadPicCode(responseJson);
		if(!UploadResultCode.UPLOAD_PROCESS_SUCC.equals(code))
			throw new Throwable("upload error responseJson = " + responseJson);
		Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put("code", code);
		codeMap.put("pic", picList);
		return JsonObjectUtil.getJsonStr(codeMap);
	}
	
	/**
	 * 档案自定义模块明细图片保存
	 */
	public List<ResPicBean> profileOtherItemSave(String userID, String nickName, String watermarkContent, UploadParamBean paramBean) throws Throwable{
		List<ResPicBean> profileResPicList = new ArrayList<ResPicBean>();
		if(paramBean.getSrc() == null || paramBean.getSrc().size() == 0)
			return profileResPicList;
		
		ResPicBean resPicBean = null;
		String newPicID = "";
		String newPicPath = "";
		Multimap<String, String> destMulMap = ArrayListMultimap.<String, String> create();
		for (String srcPicPath : paramBean.getSrc()){
			newPicID = DBgetNextID.getInstance().getUserNewPicID();
			newPicPath = UploadPicUtil.getUserNewPicPath(userID, XingyunUploadFileConstant.UPLOAD_TYPE_PROFILE_OTHER_ITEM, newPicID);
			destMulMap.put("src", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.PROFILE_OTHER_WIDTH_740));
			destMulMap.put("mid", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.PROFILE_OTHER_WIDTH_500));
			destMulMap.put("thumb", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.PROFILE_OTHER_WIDTH_150));
			
			resPicBean = new ResPicBean();
			resPicBean.setSrcPicPath(srcPicPath);
			resPicBean.setPicid(newPicID);
			profileResPicList.add(resPicBean);
		}
		paramBean.setWatermarkNickName(nickName);
		paramBean.setWatermarkWkey(watermarkContent);	//设置水印内容
		paramBean.setIsWater(ProfileService.getInstance().getUserPicWater(userID) == XingyunCommonConstant.PICWATER_YES);	//是否添加水印
		String responseJson = UploadDeal.UPLOAD_TYPE_PROFILE_OTHER_ITEM.save(paramBean, destMulMap);
		return getUploadMsgResPicBeanList(responseJson, profileResPicList, userID, paramBean.getUploadServer(), XingyunUploadFileConstant.UPLOAD_TYPE_PROFILE_OTHER_ITEM);
	}
	
	/**
	 * 全身照预览
	 */
	public String wholeBodyPre(UploadParamBean paramBean) throws Throwable{
		String dest = UploadPicUtil.getPreTempPicPath(paramBean.getSrc().get(0));
		paramBean.setDest(Arrays.asList(dest));
		//处理图片
		String responseJson = UploadDeal.UPLOAD_TYPE_PROFILE_WHOLEBODY.pre(paramBean, null);
		//检查上传图片信息
		String code = getUploadPicCode(responseJson);
		if(!UploadResultCode.UPLOAD_PROCESS_SUCC.equals(code))
			throw new Throwable("upload error responseJson = " + responseJson);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", code);
		map.put("visitPath", "/" + dest);
		return JsonObjectUtil.getJsonStr(map);
	}
	
	/**
	 * 用户上传全身照 保存
	 */
	public ResPicBean wholeBodyUploadSave(String userID, UploadParamBean paramBean) throws Throwable{
		String newPicID = DBgetNextID.getInstance().getUserNewPicID();
		String newPicPath = UploadPicUtil.getUserNewPicPath(userID, XingyunUploadFileConstant.UPLOAD_TYPE_PROFILE_WHOLEBODY, newPicID);
		Multimap<String, String> destMulMap = ArrayListMultimap.<String, String>create();
		destMulMap.put("dest300", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.PROFILE_WHOLEBODY_WIDTH_300));
		String responseJson = UploadDeal.UPLOAD_TYPE_PROFILE_WHOLEBODY.save(paramBean, destMulMap);
		return getUploadResPicBean(responseJson, newPicID, userID, paramBean.getUploadServer(), XingyunUploadFileConstant.UPLOAD_TYPE_PROFILE_WHOLEBODY);
	}
	
	/**
	 * 星语上传图片预览
	 */
	public String xingyuUploadPre(UploadParamBean paramBean) throws Throwable{
		List<Map<String, String>> picList = new ArrayList<Map<String, String>>();
		Multimap<String, String> destMulMap = ArrayListMultimap.<String, String> create();
		String thumbPath = "";
		Map<String, String> picMap = null;
		List<String> srcPathList = paramBean.getSrc();
		for (String srcPath : srcPathList){
			thumbPath = UploadPicUtil.getPreTempPicPath(srcPath);
			picMap = new HashMap<String, String>();
			picMap.put("visitPath", "/" + thumbPath);
			picMap.put("yuantu", srcPath);
			picList.add(picMap);
			destMulMap.put("thumb", thumbPath);
		}
		
		//处理图片
		String responseJson = UploadDeal.UPLOAD_TYPE_XINGYU.pre(paramBean, destMulMap);
		//检查上传图片信息
		String code = getUploadPicCode(responseJson);
		if(!UploadResultCode.UPLOAD_PROCESS_SUCC.equals(code))
			throw new Throwable("upload error responseJson = " + responseJson);
		Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put("code", code);
		codeMap.put("pic", picList);
		return JsonObjectUtil.getJsonStr(codeMap);
	}
	
	/**
	 * 星语上传图片保存
	 */
	public List<ResPicBean> xingyuUploadSave(String userID, UploadParamBean paramBean) throws Throwable{
		List<ResPicBean> xingyuResPicList = new ArrayList<ResPicBean>();
		if(paramBean.getSrc() == null || paramBean.getSrc().size() == 0)
			return xingyuResPicList;
		
		ResPicBean resPicBean = null;
		String newPicID = "";
		String newPicPath = "";
		Multimap<String, String> destMulMap = ArrayListMultimap.<String, String> create();
		for (String srcPicPath : paramBean.getSrc()){
			newPicID = DBgetNextID.getInstance().getUserNewPicID();
			newPicPath = UploadPicUtil.getUserNewPicPath(userID, XingyunUploadFileConstant.UPLOAD_TYPE_XINGYU, newPicID);
			
			destMulMap.put("src", newPicPath);
			destMulMap.put("mid_640", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.XINGYU_WIDTH_640));
			destMulMap.put("mid_500", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.XINGYU_WIDTH_500));
			destMulMap.put("thumb", UploadPicUtil.replacePicPath(newPicPath, XingyunUploadFileConstant.XINGYU_WIDTH_150));
			
			resPicBean = new ResPicBean();
			resPicBean.setSrcPicPath(srcPicPath);
			resPicBean.setPicid(newPicID);
			xingyuResPicList.add(resPicBean);
		}
		String responseJson = UploadDeal.UPLOAD_TYPE_XINGYU.save(paramBean, destMulMap);
		return getUploadMsgResPicBeanList(responseJson, xingyuResPicList, userID, paramBean.getUploadServer(), XingyunUploadFileConstant.UPLOAD_TYPE_XINGYU);
	}

	/**
	 * 检查上传信息 获取图片资源bean 
	 */
	private ResPicBean getUploadResPicBean(String responseJson, String resPicID, String userID, String uploadServer, String uploadType) throws Throwable{
		//检查上传图片信息
		String code = getUploadPicCode(responseJson);
		if(!UploadResultCode.UPLOAD_PROCESS_SUCC.equals(code))
			throw new Throwable("upload error responseJson = " + responseJson);
		
		//整理图片资源信息
		ResPicBean resPicBean = new ResPicBean();
		resPicBean.setPicid(resPicID);
		resPicBean.setUserid(userID);
		resPicBean.setServertag(UploadDBUtil.PIC_WEBSERVERTAG_BY_UPLOADSERVER.get(uploadServer));
		resPicBean.setPictype(uploadType);
		resPicBean.setWidth(0);
		resPicBean.setHeight(0);
		resPicBean.setSystime(DateUtil.getSimpleDateFormat());
		return resPicBean;
	}
	
	/**
	 * 检查上传信息 获取图片资源bean 
	 */
	private List<ResPicBean> getUploadResPicBeanList(String responseJson, List<ResPicBean> resPicBeanList, String userID, String uploadServer, String uploadType) throws Throwable{
		//检查上传图片信息
		String code = getUploadPicCode(responseJson);
		if(!UploadResultCode.UPLOAD_PROCESS_SUCC.equals(code))
			throw new Throwable("upload error responseJson = " + responseJson);
		
		//整理图片资源信息
		String systime = DateUtil.getSimpleDateFormat();
		for(ResPicBean resPic : resPicBeanList){
			resPic.setUserid(userID);
			resPic.setServertag(UploadDBUtil.PIC_WEBSERVERTAG_BY_UPLOADSERVER.get(uploadServer));
			resPic.setPictype(uploadType);
			resPic.setWidth(0);
			resPic.setHeight(0);
			resPic.setSystime(systime);
		}
		return resPicBeanList;
	}
	
	/**
	 * 获取上传图片响应信息集合	(处理上传服务返回图片信息数据)
	 */
	private List<ResPicBean> getUploadMsgResPicBeanList(String responseJson, List<ResPicBean> resPicBeanList, String userID, String uploadServer, String uploadType) throws Throwable{
		//检查上传图片信息
		String code = getUploadPicCode(responseJson);
		if(!UploadResultCode.UPLOAD_PROCESS_SUCC.equals(code))
			throw new Throwable("upload error responseJson = " + responseJson);
		
		//整理上传图片信息
		JSONObject json = new JSONObject(responseJson);
		if(!json.has("data"))
			throw new Throwable("upload error responseJson = " + responseJson);
		JSONArray picSrc = json.getJSONArray("data");
		if(picSrc.length() == 0)
			throw new Throwable("upload error responseJson = " + responseJson);
		
		String serverTag = UploadDBUtil.PIC_WEBSERVERTAG_BY_UPLOADSERVER.get(uploadServer);
		JSONObject item = null;
		String srcPath = "";
		String sysTime = DateUtil.getSimpleDateFormat();
		
		List<ResPicBean> resPicList = new ArrayList<ResPicBean>();
		for(int i = 0; i < picSrc.length(); i++){
			item = picSrc.getJSONObject(i);
			srcPath = item.getString("srcpath");
			for(ResPicBean resPic : resPicBeanList){
				if(srcPath.equals(resPic.getSrcPicPath())){
					resPic.setUserid(userID);
					resPic.setServertag(serverTag);
					resPic.setPictype(uploadType);
					resPic.setWidth(item.getInt("width"));
					resPic.setHeight(item.getInt("height"));
					resPic.setSystime(sysTime);
					resPicList.add(resPic);
				}
			}
		}
		return resPicList;
	}
	
	/**
	 * 检查上传图片信息
	 */
	public String getUploadPicCode(String responseJson) throws Throwable{
		if(StringUtils.isBlank(responseJson))
			return StringUtils.EMPTY;
		
		JSONObject json = new JSONObject(responseJson);
		if(!json.has("result"))
			return StringUtils.EMPTY;
		return json.getString("result");
	}
}
