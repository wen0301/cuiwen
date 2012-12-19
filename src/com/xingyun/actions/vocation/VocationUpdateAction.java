package com.xingyun.actions.vocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.base.AuthUserAction;
import com.xingyun.bean.UserSkillBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.services.header.UserHeaderService;
import com.xingyun.services.vocation.VocationService;
import com.xingyun.util.JsonObjectUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.ResponseUtil;

public class VocationUpdateAction extends AuthUserAction {

	private static final long serialVersionUID = -5583025864402249852L;
	private static final Logger log = Logger.getLogger(VocationUpdateAction.class);
	
	private List<Map<Object,Object>> userVocationList;		//用户职业技能数据
	private List<Map<Object,Object>> vocation0List;			//系统行业数据
	private List<Map<Object,Object>> vocation1List;			//职业数据
	private List<Map<Object,Object>> vocation2List;			//技能数据
	private int vocation0ID;								//行业ID
	private int vocation1ID;								//职业ID
	private List<UserSkillBean> userSkillData;				//用户保存技能数据
	
	/**
	 * 显示职业技能编辑页面
	 */
	public String showVocationEditPage(){
		try {
			userVocationList = UserHeaderService.getInstance().getUserVocationByUserId(user.getUserId());
			vocation0List = VocationService.getInstance().getAllTradeList();
			if(vocation0List != null && vocation0List.size() > 0){
				vocation0ID = Integer.parseInt(vocation0List.get(0).get("id").toString()); 
				vocation1List = VocationService.getInstance().getVocationList(vocation0ID);
				if(vocation1List != null && vocation1List.size() > 0){
					vocation1ID = Integer.parseInt(vocation1List.get(0).get("id").toString()); 
					vocation2List = VocationService.getInstance().getSkillHotList(vocation0ID, vocation1ID);
				}
			}
			return "showVocationEditPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 显示职业技能编辑页面
	 */
	public void getVocation1Data(){
		try {
			vocation1List = VocationService.getInstance().getVocationList(vocation0ID);
			if(vocation1List == null || vocation1List.size() == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			
			if(vocation1List != null && vocation1List.size() > 0){
				vocation1ID = Integer.parseInt(vocation1List.get(0).get("id").toString()); 
				vocation2List = VocationService.getInstance().getSkillHotList(vocation0ID, vocation1ID);
			}
			Map<String, Object> vocationMap = new HashMap<String, Object>();
			vocationMap.put("vocationList", vocation1List);
			vocationMap.put("hotList", vocation2List);
			sendResponseMsg(JsonObjectUtil.getJsonStr(vocationMap));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 显示职业技能编辑页面
	 */
	public void getVocation2HotData(){
		try {
			vocation2List = VocationService.getInstance().getSkillHotList(vocation0ID, vocation1ID);
			Map<String, Object> vocationMap = new HashMap<String, Object>();
			vocationMap.put("hotList", vocation2List);
			sendResponseMsg(JsonObjectUtil.getJsonStr(vocationMap));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 显示职业技能编辑页面
	 */
	public String saveUserSkillData(){
		try {
			VocationService.getInstance().saveUserSkillData(user.getUserId(), userSkillData);
			return "savaSkillOK";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 技能管理_添加单个技能
	 */
	public void addSkillData() {
		try {
			if (userSkillData.get(0) == null)
				return;
			String skillName = userSkillData.get(0).getSkillName();
			int tradeId = userSkillData.get(0).getVocation0ID();
			int vocationId = userSkillData.get(0).getVocation1ID();
			if(StringUtils.isBlank(skillName)){
				ResponseUtil.sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			int retId = VocationService.getInstance().addSkillData(user.getUserId(), tradeId, vocationId, skillName);
			if(retId != 0)
				PublicQueryUtil.getInstance().addDynamicDataByType(user.getUserId(), XingyunCommonConstant.DYNAMIC_TYPE_SKILL);
			ResponseUtil.sendResponseMsg(retId == 0 ? XingyunCommonConstant.RESPONSE_ERR_STRING : XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			ResponseUtil.sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 技能管理_删除单个技能
	 */
	public void delSkillData() {
		try {
			if (userSkillData.get(0) == null)
				return;
			String skillName = userSkillData.get(0).getSkillName();
			int tradeId = userSkillData.get(0).getVocation0ID();
			int vocationId = userSkillData.get(0).getVocation1ID();
			if(StringUtils.isBlank(skillName)){
				ResponseUtil.sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			int skillId = VocationService.getInstance().getSkillIdByName(tradeId, vocationId, skillName);
			VocationService.getInstance().delSkillInfo(user.getUserId(), tradeId, vocationId, skillId);
			ResponseUtil.sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			ResponseUtil.sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}

	public List<Map<Object, Object>> getUserVocationList() {
		return userVocationList;
	}

	public void setUserVocationList(List<Map<Object, Object>> userVocationList) {
		this.userVocationList = userVocationList;
	}

	public List<Map<Object, Object>> getVocation0List() {
		return vocation0List;
	}

	public void setVocation0List(List<Map<Object, Object>> vocation0List) {
		this.vocation0List = vocation0List;
	}

	public List<Map<Object, Object>> getVocation1List() {
		return vocation1List;
	}

	public void setVocation1List(List<Map<Object, Object>> vocation1List) {
		this.vocation1List = vocation1List;
	}

	public List<Map<Object, Object>> getVocation2List() {
		return vocation2List;
	}

	public void setVocation2List(List<Map<Object, Object>> vocation2List) {
		this.vocation2List = vocation2List;
	}

	public int getVocation0ID() {
		return vocation0ID;
	}

	public void setVocation0ID(int vocation0ID) {
		this.vocation0ID = vocation0ID;
	}

	public int getVocation1ID() {
		return vocation1ID;
	}

	public void setVocation1ID(int vocation1ID) {
		this.vocation1ID = vocation1ID;
	}

	public List<UserSkillBean> getUserSkillData() {
		return userSkillData;
	}

	public void setUserSkillData(List<UserSkillBean> userSkillData) {
		this.userSkillData = userSkillData;
	}
}
