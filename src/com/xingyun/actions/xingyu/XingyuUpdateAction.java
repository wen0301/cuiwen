package com.xingyun.actions.xingyu;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.base.AuthUserAction;
import com.xingyun.bean.ResPicBean;
import com.xingyun.bean.XingyuBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.services.xingyu.XingyuService;
import com.xingyun.upload.bean.UploadParamBean;
import com.xingyun.upload.services.UploadService;
import com.xingyun.util.CommonUtil;

public class XingyuUpdateAction extends AuthUserAction {

	private static final long serialVersionUID = -2289303117725012523L;
	private static final Logger log = Logger.getLogger(XingyuUpdateAction.class);

	private List<UploadParamBean> p;	//星语上传图片信息
	private String xingyuContent;		//星语文本内容
	private int xingyuShowtype;			//星语显示类型
	private XingyuBean xyBean;			//星语主题bean
	private int xingyuID;				//星语主题ID
	private int addfrom;				//发布星语位置 0：动态页面发布星语 1：星语列表发布星语
	
	//添加星语内容
	public String addXingYu(){
		try {
			if(StringUtils.isBlank(xingyuContent)){
				sendResponseMsg("content is null");
				return null;
			}
			if(!XingyuService.getInstance().checkXingyuInterval(user.getUserId(), xingyuContent)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_REQUEST_HOURLY_STRING);
				return null;
			}
			//处理星语图片
			List<ResPicBean> xingyuResPicBeanList = null;
			if(p.get(0).getSrc() != null && p.get(0).getThumb() != null && p.get(0).getSrc().size() > 0 && p.get(0).getThumb().size() > 0 && p.get(0).getSrc().size() == p.get(0).getThumb().size()){
				xingyuResPicBeanList = UploadService.getInstance().xingyuUploadSave(user.getUserId(), p.get(0));
			}
			xingyuID = XingyuService.getInstance().saveXingYuData(user.getUserId(), xingyuContent, xingyuShowtype, xingyuResPicBeanList); 
			if(xingyuID <= 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return null;
			}
			xyBean = XingyuService.getInstance().findXingYuData(xingyuID, user.getUserId());
			CommonUtil.stopTime();
			if(com.xingyun.constant.XingyunCommonConstant.XINGYU_ADD_FROM_DYNAMIC == addfrom)
				return "xingyuTopicDynamicPop";
			return "xingyuTopicPop";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 星语列表中删除星语
	 */
	public void delXingYu(){
		try {
			if(xingyuID == 0 || !XingyuService.getInstance().checkXingyu(xingyuID, user.getUserId())){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			//删除星语数据
			XingyuService.getInstance().delXingYuData(xingyuID, user.getUserId());
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	public List<UploadParamBean> getP() {
		return p;
	}
	public void setP(List<UploadParamBean> p) {
		this.p = p;
	}
	public String getXingyuContent() {
		return xingyuContent;
	}
	public void setXingyuContent(String xingyuContent) {
		this.xingyuContent = xingyuContent;
	}
	public int getXingyuShowtype() {
		return xingyuShowtype;
	}
	public void setXingyuShowtype(int xingyuShowtype) {
		this.xingyuShowtype = xingyuShowtype;
	}

	public int getXingyuID() {
		return xingyuID;
	}

	public void setXingyuID(int xingyuID) {
		this.xingyuID = xingyuID;
	}

	public int getAddfrom() {
		return addfrom;
	}

	public void setAddfrom(int addfrom) {
		this.addfrom = addfrom;
	}

	public XingyuBean getXyBean() {
		return xyBean;
	}

	public void setXyBean(XingyuBean xyBean) {
		this.xyBean = xyBean;
	}
}
