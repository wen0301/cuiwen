package com.xingyun.upload.actions;

import java.util.List;

import org.apache.log4j.Logger;

import com.xingyun.base.AuthUserAction;
import com.xingyun.constant.UploadResultCode;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.upload.bean.UploadParamBean;
import com.xingyun.upload.services.UploadService;
import com.xingyun.util.ResponseUtil;

public class UploadPreAction extends AuthUserAction {

	private static final long serialVersionUID = 4442868856366306743L;
	private static final Logger log = Logger.getLogger(UploadPreAction.class);
	private List<UploadParamBean> p;
	private static final String errMsg = "{\"code\":" + UploadResultCode.FILE_UPLOAD_SECURITY_ERROR + "}";
	/**
	 * 动态封面 预览
	 */
	public void faceDynamicPre(){
		try {
			if(p == null || p.get(0) == null || p.get(0).getSrc() == null || p.get(0).getSrc().size() == 0){
				ResponseUtil.sendResponseMsg(errMsg);
				return ;
			}
			String uploadType = p.get(0).getUploadType();
			if(XingyunUploadFileConstant.UPLOAD_TYPE_FACE_DYNAMIC.equals(uploadType)){
				ResponseUtil.sendResponseMsg(UploadService.getInstance().faceDynamicPre(p.get(0)));
				return;
			}
			ResponseUtil.sendResponseMsg(errMsg);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			ResponseUtil.sendResponseMsg("{\"code\":" + UploadResultCode.FILE_UPLOAD_SECURITY_ERROR + "}");
		}
	}

	/**
	 * 展示明细预览
	 */
	public void postItem_prepare() {
		try{
			if(p == null || p.get(0) == null || p.get(0).getSrc() == null || p.get(0).getSrc().size() == 0){
				ResponseUtil.sendResponseMsg(errMsg);
				return;
			}
			ResponseUtil.sendResponseMsg(UploadService.getInstance().postItemPre(p.get(0)));
		}catch (Throwable e) {
			log.error(e.getMessage(), e);
			ResponseUtil.sendResponseMsg(errMsg);
		}
	}
	
	/**
	 * 展示封面预览
	 */
	public void postCover_pre() {
		try{
			if(p == null || p.get(0) == null || p.get(0).getSrc() == null || p.get(0).getSrc().size() == 0){
				ResponseUtil.sendResponseMsg(errMsg);
				return;
			}
			ResponseUtil.sendResponseMsg(UploadService.getInstance().postCoverPre(p.get(0)));
		}catch (Throwable e) {
			log.error(e.getMessage(), e);
			ResponseUtil.sendResponseMsg(errMsg);
		}
	}
	/**
	 * 视频简介封面图片
	 */
	public void videoCoverPre(){
		try {
			if(p == null || p.get(0) == null || p.get(0).getSrc() == null || p.get(0).getSrc().size() == 0){
				ResponseUtil.sendResponseMsg(errMsg);
				return ;
			}
			ResponseUtil.sendResponseMsg(UploadService.getInstance().videoCoverPre(p.get(0)));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			ResponseUtil.sendResponseMsg(errMsg);
		}
	}
	/**
	 * 档案自定义模块图片
	 */
	public void profileOtherItem_prepare() {
		try{
			if(p == null || p.get(0) == null || p.get(0).getSrc() == null || p.get(0).getSrc().size() == 0){
				ResponseUtil.sendResponseMsg(errMsg);
				return;
			}
			ResponseUtil.sendResponseMsg(UploadService.getInstance().profileOtherItemPrepare(p.get(0)));
		}catch (Throwable e) {
			log.error(e.getMessage(), e);
			ResponseUtil.sendResponseMsg(errMsg);
		}
	}
	
	/**
	 * 头像预览
	 */
	public void userLogoPre(){
		try {
			if(p == null || p.get(0) == null || p.get(0).getSrc() == null || p.get(0).getSrc().size() == 0){
				ResponseUtil.sendResponseMsg(errMsg);
				return ;
			}
			ResponseUtil.sendResponseMsg(UploadService.getInstance().userLogoPre(p.get(0)));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			ResponseUtil.sendResponseMsg(errMsg);
		}
	}
	
	/**
	 * 全身照预览
	 */
	public void wholeBodyPre(){
		try {
			if(p == null || p.get(0) == null || p.get(0).getSrc() == null || p.get(0).getSrc().size() == 0){
				ResponseUtil.sendResponseMsg(errMsg);
				return ;
			}
			ResponseUtil.sendResponseMsg(UploadService.getInstance().wholeBodyPre(p.get(0)));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			ResponseUtil.sendResponseMsg(errMsg);
		}
	}
	
	/**
	 * 星语图片预览
	 */
	public void xingyu_prepare() {
		try{
			if(p == null || p.get(0) == null || p.get(0).getSrc() == null || p.get(0).getSrc().size() == 0){
				ResponseUtil.sendResponseMsg(errMsg);
				return;
			}
			ResponseUtil.sendResponseMsg(UploadService.getInstance().xingyuUploadPre(p.get(0)));
		}catch (Throwable e) {
			log.error(e.getMessage(), e);
			ResponseUtil.sendResponseMsg(errMsg);
		}
	}
	
	public List<UploadParamBean> getP() {
		return p;
	}

	public void setP(List<UploadParamBean> p) {
		this.p = p;
	}
}
