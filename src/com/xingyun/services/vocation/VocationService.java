package com.xingyun.services.vocation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.xingyun.bean.UserSkillBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.SpecialCharFilterUtil;

public class VocationService {

	private static final DBOperate db = new DBOperate();
	private static final VocationService vocationService = new VocationService();
	private VocationService(){}
	public static VocationService getInstance() {
		return vocationService;
	}
	
	/**
	 * 查询所有行业数据
	 */
	public List<Map<Object, Object>> getAllTradeList() throws Throwable{
		String sql = "SELECT id, name, englishname, seq, iconname FROM dic_trade";
		List<Map<Object, Object>> list = db.retrieveSQL(sql);
		if(list.size() > 0){
			String[] seqValue = {"seq"};
			CommonUtil.compositor(list, seqValue, 0);
		}
		return list;
	}
	
	/**
	 * 查询行业下的职业数据
	 * 使用索引：index_dic_vocation_tradeid_seq
	 */
	public List<Map<Object, Object>> getVocationList(int tradeId) throws Throwable{
		String sql = "SELECT id, name FROM dic_vocation WHERE tradeid = ? ORDER BY seq";
		List<Object> vList = new ArrayList<Object>();
		vList.add(tradeId);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 查询行业下的职业数据
	 * 使用索引：index_dic_vocation_tradeid_seq
	 */
	public List<Map<Object, Object>> getVocationList(int tradeId, int isIndex) throws Throwable{
		String sql = "SELECT id, name FROM dic_vocation WHERE tradeid = ? AND isindex = ? ORDER BY seq";
		List<Object> vList = new ArrayList<Object>();
		vList.add(tradeId);
		vList.add(isIndex);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 查询职业下的热门技能数据
	 * 使用索引：index_dic_skill_tradeid_vocationid_seq
	 */
	public List<Map<Object, Object>> getSkillHotList(int tradeId, int vocationId) throws Throwable{
		String sql = "SELECT id, tradeid, vocationid, name FROM dic_skill WHERE tradeid = ? AND vocationid = ? AND status = ? ORDER BY seq";
		List<Object> vList = new ArrayList<Object>();
		vList.add(tradeId);
		vList.add(vocationId);
		vList.add(XingyunCommonConstant.VOCATION2_STATUS_SYS_HOT);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 保存用户技能数据
	 * @param userID			用户ID
	 * @param userSkillData		用户技能
	 */
	public void saveUserSkillData(String userID, List<UserSkillBean> userSkillData) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		if(userSkillData != null && userSkillData.size() > 0){
			String sysTime = DateUtil.getSimpleDateFormat();
			for(UserSkillBean bean : userSkillData){
				int vocation2ID = getVocation2ID(bean.getVocation0ID(), bean.getVocation1ID(), bean.getSkillName());
				if(vocation2ID == 0)
					continue;
				sqlList.add("INSERT INTO user_vocation(userid, tradeid, vocationid, skillid, systime) VALUES('" + userID + "', " + bean.getVocation0ID() + ", " + bean.getVocation1ID() + ", " + vocation2ID + ", '" + sysTime + "')");
			}
		}
		sqlList.add(0, "DELETE FROM user_vocation WHERE userid = '" + userID + "'");
		db.batchExecute(sqlList, true);
	}
	
	/**
	 * 整理获取技能ID
	 * @param tradeId	行业ID
	 * @param vocationId	职业ID
	 * @param name			技能名
	 * @param seq			排序
	 * 使用索引：index_dic_skill_tradeid_vocationid_seq
	 */
	private int getVocation2ID(int tradeId, int vocationId, String name) throws Throwable{
		name = SpecialCharFilterUtil.replaceSpecialChar(name, 50);
		String sql = "SELECT id FROM dic_skill WHERE tradeid = ? AND vocationid = ? AND name = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(tradeId);
		vList.add(vocationId);
		vList.add(name);
		int id = CommonUtil.getIntValue(sql, vList, "id");
		if(id > 0)
			return id;
		sql = "SELECT id FROM dic_vocation WHERE id= ? AND tradeid = ?";
		vList.clear();
		vList.add(vocationId);
		vList.add(tradeId);
		id = CommonUtil.getIntValue(sql, vList, "id");
		if(id == 0)
			return 0;
		sql = "INSERT INTO dic_skill(tradeid, vocationid, name, status, seq, systime) values(?, ?, ?, ?, ?, ?)";
		vList.clear();
		vList.add(tradeId);
		vList.add(vocationId);
		vList.add(name);
		vList.add(XingyunCommonConstant.VOCATION2_STATUS_USER_ADD);
		vList.add(XingyunCommonConstant.VOCATION2_SEQ_USER_ADD);
		vList.add(new Date());
		return db.insertData(sql, vList);
	}
	
	/**
	 * 添加单个技能 
	 */
	public int addSkillData(String userID, int tradeId, int vocationId, String skillName) throws Throwable {
		String sysTime = DateUtil.getSimpleDateFormat();
		int vocation2ID = getVocation2ID(tradeId, vocationId, skillName);
		if(vocation2ID == 0)
			return 0;
		String sql = "INSERT INTO user_vocation(userid, tradeid, vocationid, skillid, systime) VALUES(?, ?, ?, ?, ?)";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userID);
		valueList.add(tradeId);
		valueList.add(vocationId);
		valueList.add(vocation2ID);
		valueList.add(sysTime);
		return db.insertData(sql, valueList);
	}
	
	/**
	 * 获取技能Id
	 * @param vocationId 
	 * @param tradeId 
	 */
	public int getSkillIdByName(int tradeId, int vocationId, String skillName) throws Throwable {
		skillName = SpecialCharFilterUtil.replaceSpecialChar(skillName, 50);
		String sql = "SELECT id FROM dic_skill WHERE tradeid = ? AND vocationid = ? AND name = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(tradeId);
		valueList.add(vocationId);
		valueList.add(skillName);
		Map<Object, Object> tmpMap = db.retrieveSQL(sql, valueList).get(0);
		if (tmpMap.get("id") == null)
			return 0;
		return Integer.parseInt(tmpMap.get("id").toString() );
	}
	
	/**
	 * 删除单个技能
	 */
	public void delSkillInfo(String userID, int tradeId, int vocationId, int skillId) throws Throwable {
		String sql = "DELETE FROM user_vocation WHERE userid= ? AND skillid = ? AND tradeid = ? AND vocationid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userID);
		valueList.add(skillId);
		valueList.add(tradeId);
		valueList.add(vocationId);
		db.deleteData(sql, valueList);
	}
}
