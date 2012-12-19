package com.xingyun.services.index;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.bean.IndexUserBean;
import com.xingyun.bean.RecommendPostBean;
import com.xingyun.bean.SwitchCityBean;
import com.xingyun.constant.XingyunAdConstant;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunFaceConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.follow.FollowService;
import com.xingyun.services.header.UserHeaderService;
import com.xingyun.services.post.PostService;
import com.xingyun.services.profile.ProfileService;
import com.xingyun.services.recommend.RecommendService;
import com.xingyun.services.vocation.VocationService;
import com.xingyun.session.SessionCookie;
import com.xingyun.util.AreaUtil;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.CounterUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.UploadPicUtil;

public class IndexService {
	private static final Logger log = Logger.getLogger(IndexService.class);
	private static final DBOperate db = new DBOperate();
	private static final IndexService indexService = new IndexService();
	private IndexService(){}
	public static IndexService getInstance() {
		return indexService;
	}
	/**
	 * 获取星首页用户列表
	 */
	public List<IndexUserBean> getIndexUserBeanList(SwitchCityBean switchCityBean) throws Throwable{
		List<Map<Object,Object>> tradeList = VocationService.getInstance().getAllTradeList();
		if(tradeList.size() == 0)
			return null;
		int tradeId = 0;
		IndexUserBean indexUserBean = null;
		List<Map<Object,Object>> vocationList = null;
		List<IndexUserBean> indexUserBeanList = new ArrayList<IndexUserBean>();
		for(Map<Object,Object> map : tradeList){
			tradeId = Integer.parseInt(map.get("id").toString());
			vocationList = VocationService.getInstance().getVocationList(tradeId, XingyunCommonConstant.VOCATION_ISINDEX_YES);
			if(vocationList.size() == 0)
				continue;
			indexUserBean = new IndexUserBean();
			indexUserBean.setTradeId(tradeId);
			indexUserBean.setTradeName(map.get("name").toString());
			indexUserBean.setTradeEnglishName(map.get("englishname").toString());
			indexUserBean.setTradeIconName(map.get("iconname").toString());
			indexUserBean.setVocationList(vocationList);
			indexUserBean.setUserList(getUserListByTradeId(tradeId, indexUserBean.getVocationList(), switchCityBean, 1));
			indexUserBean.setTotalRecord(getUserIndexCountByTradeId(tradeId, indexUserBean.getVocationList(), switchCityBean));
			indexUserBeanList.add(indexUserBean);
		}
		return indexUserBeanList;
	}
	/**
	 * 整理首页行业里的用户列表
	 */
	public List<Map<Object,Object>> setIndexTradeList(List<IndexUserBean> indexUserBeanList) throws Throwable{
		List<Map<Object,Object>> indexTradeList = new ArrayList<Map<Object,Object>>();
		Map<Object,Object> map = null;
		for(IndexUserBean indexUserBean : indexUserBeanList){
			map = new HashMap<Object, Object>();
			map.put("id", indexUserBean.getTradeId());
			map.put("name", indexUserBean.getTradeName());
			map.put("englishname", indexUserBean.getTradeEnglishName());
			map.put("iconname", indexUserBean.getTradeIconName());
			indexTradeList.add(map);
		}
		return indexTradeList;
	}
	
	/**
	 * 通过行业id获取用户列表
	 */
	public List<Map<Object,Object>> getUserListByTradeId(int tradeId, List<Map<Object, Object>> indexVocationList, SwitchCityBean switchCityBean, int curPage) throws Throwable{
		String vocationIds = CommonUtil.getStringID(indexVocationList, "id");
		List<Map<Object,Object>> userIndexList = getUserIndexListByTradeId(tradeId, vocationIds, switchCityBean, curPage);
		if(userIndexList.size() == 0)
			return null;
		return setUserIndexList(userIndexList);
	}
	
	/**
	 * 通过行业id获取用户列表
	 */
	public List<Map<Object,Object>> getUserListByTradeId(int tradeId, int curPage) throws Throwable{
		List<Map<Object,Object>> userIndexList = getUserIndexListByTradeId(tradeId, curPage);
		if(userIndexList.size() == 0)
			return null;
		return setUserIndexList(userIndexList);
	}
	
	/**
	 *  通过职业id获取用户列表
	 */
	public List<Map<Object,Object>> getUserListByVocationId(int vocationId, SwitchCityBean switchCityBean, int curPage) throws Throwable{
		List<Map<Object,Object>> userIndexList = null;
		if(switchCityBean != null && switchCityBean.getProvinceid() > 0)
			userIndexList = getUserIndexListByVocationId_SwitchCity(vocationId, switchCityBean, curPage);	
		else
			userIndexList = getUserIndexListByVocationId(vocationId, curPage);
		if(userIndexList == null || userIndexList.size() == 0)
			return null;
		return setUserIndexList(userIndexList);
	}
	
	/**
	 * 根据行业id查询用户列表
	 * 使用索引：index_cms_index_user_vocationid
	 */
	private List<Map<Object,Object>> getUserIndexListByTradeId(int tradeId, String vocationIds, SwitchCityBean switchCityBean, int curPage) throws Throwable{
		String sql = "";
		List<Object> vList = new ArrayList<Object>();
		if(switchCityBean != null && switchCityBean.getProvinceid() > 0){
			sql = "SELECT c.id, c.userid FROM cms_user_rencai_index c, user_profile u  WHERE c.userid = u.userid AND c.tradeid = ? AND c.vocationid IN (" + vocationIds + ") AND u.provinceid = ?";
			vList.add(tradeId);
			vList.add(switchCityBean.getProvinceid());
			if(switchCityBean.getCityid() > 0){
				sql += " AND u.cityid = ?";
				vList.add(switchCityBean.getCityid());
			}
		}else{
			sql = "SELECT id, userid FROM cms_user_rencai_index WHERE tradeid = ? AND iscountry = ? AND vocationid IN (" + vocationIds + ")";
			vList.add(tradeId);
			vList.add(XingyunCommonConstant.USER_INDEX_COUNTRY_YES);
		}
		List<Map<Object, Object>> list = db.retrieveSQL(sql, vList);
		if(list.size() == 0)
			return list;
		
		String[] seqValue = {"id"};
		CommonUtil.compositor(list, seqValue, 1);
		return CommonUtil.subList(list, curPage, XingyunCommonConstant.INDEX_USER_MAXNUM);
	}
	
	/**
	 * 根据行业id查询用户列表
	 * 使用索引：index_cms_index_user_tradeid
	 */
	private List<Map<Object,Object>> getUserIndexListByTradeId(int tradeId, int curPage) throws Throwable{
		String sql = "SELECT userid FROM cms_user_rencai_index WHERE tradeid = ? ORDER BY id DESC LIMIT ?,?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(tradeId);
		valueList.add((curPage -1) * XingyunCommonConstant.INDEX_USER_MAXNUM);
		valueList.add(XingyunCommonConstant.INDEX_USER_MAXNUM);
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 根据职业id查询用户列表
	 * 使用索引：index_cms_index_user_vocationid
	 */
	private List<Map<Object,Object>> getUserIndexListByVocationId(int vocationId, int curPage) throws Throwable{
		String sql = "SELECT userid FROM cms_user_rencai_index WHERE vocationid = ? AND iscountry = ? ORDER BY id DESC LIMIT ?,?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(vocationId);
		valueList.add(XingyunCommonConstant.USER_INDEX_COUNTRY_YES);
		valueList.add((curPage -1) * XingyunCommonConstant.INDEX_USER_MAXNUM);
		valueList.add(XingyunCommonConstant.INDEX_USER_MAXNUM);
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 根据职业id查询用户列表
	 */
	private List<Map<Object,Object>> getUserIndexListByVocationId_SwitchCity(int vocationId, SwitchCityBean switchCityBean, int curPage) throws Throwable{
		String sql = "SELECT c.userid FROM cms_user_rencai_index c, user_profile u  WHERE c.userid = u.userid AND c.vocationid = ? AND u.provinceid = ? ORDER BY c.id DESC LIMIT ?, ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(vocationId);
		valueList.add(switchCityBean.getProvinceid());
		valueList.add((curPage -1) * XingyunCommonConstant.INDEX_USER_MAXNUM);
		valueList.add(XingyunCommonConstant.INDEX_USER_MAXNUM);
		return db.retrieveSQL(sql, valueList);
	}
	/**
	 * 根据行业id查询用户总数
	 * 使用索引：index_cms_index_user_vocationid
	 */
	public int getUserIndexCountByTradeId(int tradeId, List<Map<Object, Object>> indexVocationList, SwitchCityBean switchCityBean) throws Throwable{
		String sql = "";
		List<Object> vList = new ArrayList<Object>();
		String vocationIds = CommonUtil.getStringID(indexVocationList, "id");
		if(switchCityBean != null && switchCityBean.getProvinceid() > 0){
			sql = "SELECT COUNT(*) FROM cms_user_rencai_index c, user_profile u  WHERE c.userid = u.userid AND c.tradeid = ? AND c.vocationid IN (" + vocationIds + ") AND u.provinceid = ?";
			vList.add(tradeId);
			vList.add(switchCityBean.getProvinceid());
			if(switchCityBean.getCityid() > 0){
				sql += " AND u.cityid = ?";
				vList.add(switchCityBean.getCityid());
			}
		}else{		//全国
			sql = "SELECT COUNT(*) FROM cms_user_rencai_index WHERE tradeid = ? AND iscountry = ? AND vocationid IN ("+vocationIds+") ";
			vList.add(tradeId);
			vList.add(XingyunCommonConstant.USER_INDEX_COUNTRY_YES);
		}
		int allCount = db.getRecordCountSQL(sql, vList);
		return allCount > XingyunCommonConstant.INDEX_USER_MAXSIZE ? XingyunCommonConstant.INDEX_USER_MAXSIZE : allCount;
	}
	/**
	 * 根据行业id查询用户总数
	 * 使用索引：index_cms_index_user_tradeid
	 */
	public int getUserIndexCountByTradeId(int tradeId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM cms_user_rencai_index WHERE tradeid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(tradeId);
		return db.getRecordCountSQL(sql, valueList);
	}

	/**
	 * 查询首页 二级行业 用户数量 
	 */
	public int getUserIndexCountByVocation(int vocationId, SwitchCityBean switchCityBean) throws Throwable{
		if(switchCityBean != null && switchCityBean.getProvinceid() > 0)
			return getUserIndexCountByVocationId_provinceId(vocationId, switchCityBean);
		return getUserIndexCountByVocationId(vocationId);
	}
	
	/**
	 * 根据职业id查询用户总数
	 * 使用索引：index_cms_index_user_vocationid
	 */
	public int getUserIndexCountByVocationId(int vocationId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM cms_user_rencai_index WHERE vocationid = ? AND iscountry = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(vocationId);
		valueList.add(XingyunCommonConstant.USER_INDEX_COUNTRY_YES);
		return db.getRecordCountSQL(sql, valueList);
	}
	
	/**
	 * 根据职业id查询用户总数
	 */
	public int getUserIndexCountByVocationId_provinceId(int vocationId, SwitchCityBean switchCityBean) throws Throwable{
		String sql = "SELECT COUNT(*) FROM cms_user_rencai_index c, user_profile u  WHERE c.userid = u.userid AND c.vocationid = ? AND u.provinceid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(vocationId);
		valueList.add(switchCityBean.getProvinceid());
		return db.getRecordCountSQL(sql, valueList);
	}
	
	/**
	 * 整理用户列表数据
	 */
	private List<Map<Object,Object>> setUserIndexList(List<Map<Object,Object>> userIndexList) throws Throwable{
		String userId = "";
		Map<Object,Object> userMap = null;
		Map<Object,Object> tmpMap = null;
		List<Map<Object,Object>> userInfoList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> map : userIndexList){
			userId = map.get("userid").toString();
			userMap = PublicQueryUtil.getInstance().findUserCommonMap(userId);
			if(userMap == null)
				continue;
			userMap.put("logourl", UploadPicUtil.getPicWebUrl(userMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_100));
			userMap.put("supportcount", UserHeaderService.getInstance().getRecommendUserCount(userId));
			userMap.put("fanscount", FollowService.getInstance().getFansCount(userId));
			userMap.put("isShowfollow", UserHeaderService.getInstance().checkIsShowfollow(userId));
			tmpMap = getUserProfileMapByUserId(userId);
			if(tmpMap != null)
				userMap.putAll(tmpMap);
			userInfoList.add(userMap);
		}
		return userInfoList;
	}
	/**
	 * 根据用户id获取城市及星座
	 * 使用索引：index_user_profile_userid
	 */
	private Map<Object,Object> getUserProfileMapByUserId(String userId) throws Throwable{
		String sql = "SELECT provinceid,constellation FROM user_profile WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList, true);
		if(list.size() == 0)
			return null;
		Map<Object,Object> map = list.get(0);
		map.put("provinceName", AreaUtil.getInstance().findAreaNameById(Integer.parseInt(map.get("provinceid").toString())));
		return map;
	}
	
	public List<RecommendPostBean> getRecommendPostBeanList(List<Map<Object,Object>> tradeList) throws Throwable{
		List<RecommendPostBean> recommendPostBeanList = new ArrayList<RecommendPostBean>();
		RecommendPostBean postBean = null;
		int tradeId = 0;
		for(Map<Object,Object> map : tradeList){
			tradeId = CommonUtil.getIntValue(map.get("id"));
			postBean = new RecommendPostBean();
			postBean.setTradeId(tradeId);
			postBean.setTradeName(CommonUtil.getStringValue(map.get("name")));
			postBean.setTradeEnglishName(CommonUtil.getStringValue(map.get("englishname")));
			postBean.setTotalRecord(getRecommendWorksCount(tradeId));
			if(postBean.getTotalRecord() != 0)
				postBean.setPostList(getRecommendWorksList(tradeId, 1, XingyunCommonConstant.RECOMMEND_WORKS_MAXNUM));
			recommendPostBeanList.add(postBean);
		}
		return recommendPostBeanList;
	}
	
	/**
	 * 获取星作品列表
	 */
	public List<Map<Object,Object>> getRecommendWorksList(int tradeId, int curPage, int maxSize) throws Throwable{
		List<Map<Object,Object>> postIndexList = getRecommendWorksIndexList(tradeId, curPage, maxSize);
		if(postIndexList == null || postIndexList.size() == 0)
			return null;
		return setPostIndexList(postIndexList);
	}
	/**
	 * 获取星作品列表
	 */
	public List<Map<Object,Object>> getRecommendPindaoList(int tradeId, int sort, int curPage) throws Throwable{
		List<Map<Object,Object>> postIndexList = null;
		if(sort == XingyunCommonConstant.INDEX_POST_SORT_DEFAULT)
			postIndexList = getRecommendPindaoIndexList(tradeId, curPage);
		else if(sort == XingyunCommonConstant.INDEX_POST_SORT_VIEW)
			postIndexList = getRecommendPindaoIndexListSortView(tradeId, curPage);
		else if(sort == XingyunCommonConstant.INDEX_POST_SORT_SUPPORT)
			postIndexList = getRecommendPindaoIndexListSortSupport(tradeId, curPage);
		if(postIndexList == null || postIndexList.size() == 0)
			return null;
		return setPostIndexList(postIndexList);
	}
	/**
	 * 根据行业id查询作品总数
	 * 使用索引：index_cms_post_recommend_tradeid
	 */
	public int getRecommendWorksCount(int tradeId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM cms_post_recommend WHERE tradeid = " + tradeId;
		int count =  db.getRecordCountSQL(sql);
		int maxNum = XingyunCommonConstant.RECOMMEND_WORKS_PAGENUM * XingyunCommonConstant.RECOMMEND_WORKS_MAXNUM;
		if(count > maxNum)
			count = maxNum;
		return count;
	}
	
	/**
	 * 根据行业id查询作品总数
	 * 使用索引：index_cms_post_pindao_tradeid
	 */
	public int getRecommendPindaoCount(int tradeId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM cms_post_pindao WHERE tradeid = " + tradeId;
		return db.getRecordCountSQL(sql);
	}
	
	/**
	 * 根据行业id及分页查找推荐的作品集合
	 * 使用索引：index_cms_post_recommend_tradeid
	 */
	private List<Map<Object,Object>> getRecommendWorksIndexList(int tradeId, int curPage, int maxSize) throws Throwable{
		String sql = "SELECT postid,userid FROM cms_post_recommend WHERE tradeid = ? ORDER BY id DESC LIMIT ?,?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(tradeId);
		valueList.add((curPage -1) * maxSize);
		valueList.add(maxSize);
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 根据行业id及分页查找推荐的作品集合
	 * 使用索引：index_cms_post_pindao_tradeid
	 */
	private List<Map<Object,Object>> getRecommendPindaoIndexList(int tradeId, int curPage) throws Throwable{
		String sql = "SELECT postid,userid FROM cms_post_pindao WHERE tradeid = ? ORDER BY id DESC LIMIT ?,?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(tradeId);
		valueList.add((curPage -1) * XingyunCommonConstant.RECOMMEND_PINDAO_MAXNUM);
		valueList.add(XingyunCommonConstant.RECOMMEND_PINDAO_MAXNUM);
		return db.retrieveSQL(sql, valueList);
	}
	/**
	 * 根据行业id,排序及分页查找推荐的作品集合
	 * 使用索引：index_cms_post_pindao_tradeid index_post_counter_postid
	 */
	private List<Map<Object, Object>> getRecommendPindaoIndexListSortView(int tradeId, int curPage) throws Throwable{
		String sql = "SELECT p.postid, pv.viewcount FROM cms_post_pindao p, post_counter pv WHERE p.postid = pv.postid";
		List<Object> valueList = new ArrayList<Object>();
		if(tradeId > 0){
			sql = "SELECT p.postid, pv.viewcount FROM cms_post_pindao p, post_counter pv WHERE p.postid = pv.postid AND p.tradeid = ?";
			valueList.add(tradeId);
		}
		sql += " ORDER BY p.id DESC";
		List<Map<Object, Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		for(Map<Object,Object> map : list){
			map.put("viewcount", CounterUtil.getPostViewCount(CommonUtil.getIntValue(map.get("postid"))) + CommonUtil.getIntValue(map.get("viewcount")));
		}
		String[] seqValue = {"viewcount"};
		CommonUtil.compositor(list, seqValue, 1);
		list = CommonUtil.subList(list, curPage, XingyunCommonConstant.RECOMMEND_PINDAO_MAXNUM);
		return list;
	}
	
	/**
	 * 根据行业id,排序及分页查找推荐的作品集合
	 * 使用索引：index_cms_post_pindao_tradeid  index_post_counter_postid
	 */
	private List<Map<Object, Object>> getRecommendPindaoIndexListSortSupport(int tradeId, int curPage) throws Throwable{
		String sql = "SELECT p.postid, pv.recommendcount FROM cms_post_pindao p, post_counter pv WHERE p.postid = pv.postid";
		List<Object> valueList = new ArrayList<Object>();
		if(tradeId > 0){
			sql = "SELECT p.postid, pv.recommendcount FROM cms_post_pindao p, post_counter pv WHERE p.postid = pv.postid AND p.tradeid = ?";
			valueList.add(tradeId);
		}
		sql += " ORDER BY p.id DESC";
		List<Map<Object, Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() > 0){
			String[] seqValue = {"recommendcount"};
			CommonUtil.compositor(list, seqValue, 1);
			list = CommonUtil.subList(list, curPage, XingyunCommonConstant.RECOMMEND_PINDAO_MAXNUM);
		}
		return list;
	}
	
	/**
	 * 整理作品列表集合
	 */
	private List<Map<Object,Object>> setPostIndexList(List<Map<Object,Object>> list) throws Throwable{
		int postId = 0;
		Map<Object,Object> postMap = null;
		Map<Object,Object> userMap = null;
		List<Map<Object,Object>> postList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> map : list){
			postId = Integer.parseInt(map.get("postid").toString());
			postMap = getPostInfoMap(postId);
			if(postMap == null)
				continue;
			userMap = PublicQueryUtil.getInstance().findUserCommonMap(postMap.get("userid").toString());
			if(userMap == null)
				continue;
			postMap.putAll(userMap);
			postMap.put("coverpath_190", UploadPicUtil.getPicWebUrl(postMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_190));
			postMap.put("coverpath_250", UploadPicUtil.getPicWebUrl(postMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_250));
			if(map.get("viewcount") != null)
				postMap.put("zpViewcount", map.get("viewcount"));
			else
				postMap.put("zpViewcount", PostService.getInstance().getPostViewCount(postId));
			if(map.get("recommendcount") != null)
				postMap.put("zpRecommendCount", map.get("recommendcount"));
			else
				postMap.put("zpRecommendCount", RecommendService.getInstance().getPostRecommendCount(postId));
			if(map.get("collectcount") != null)
				postMap.put("collectcount", map.get("collectcount"));
			else
				postMap.put("collectcount", PostService.getInstance().getPostCollectionCount(postId));
				
			postMap.put("isNew", CommonUtil.checkPostIsNew((Date)postMap.get("systime") ) );
			postList.add(postMap);
		}
		return postList;
	}
	
	/**
	 * 获取作品信息
	 */
	private Map<Object,Object> getPostInfoMap(int postId) throws Throwable{
		String sql = "SELECT id,userid,posttype,title,coverpath,systime FROM post WHERE id = " + postId;
		List<Map<Object,Object>> list = db.retrieveSQL(sql, null, true);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	
	/**
	 * 整理推荐位图片集合
	 */
	public List<Map<Object,Object>> getAdPicList(int adType) throws Throwable{
		List<Map<Object, Object>> adPicList = findAdPicList(adType);
		for(Map<Object,Object> map : adPicList){
			map.put("picUrl", UploadPicUtil.getSysPicWebUrl(map.get("picpath").toString()));
		}
		return adPicList;
	}
	
	/**
	 * 查询推荐位图片
	 * 使用索引：index_ad_pic_type_status_seq
	 */
	private List<Map<Object, Object>> findAdPicList(int adType) throws Throwable{
		String sql = "SELECT p.title, p.href, p.picpath,a.seq FROM ad_pic a, ad_pic_item p WHERE a.picid = p.id AND a.type = ? AND a.status = ? ORDER BY a.seq";
		List<Object> vList = new ArrayList<Object>();
		vList.add(adType);
		vList.add(XingyunAdConstant.XY_AD_STATUS_UP);
		return db.retrieveSQL(sql, vList);
	}
	/**
	 * 筛选出勾选了展示的行业列表
	 */
	public List<Map<Object,Object>> getPostTradeList() throws Throwable{
		String sql = "SELECT DISTINCT tradeid FROM cms_post_pindao";
		List<Map<Object,Object>> indexPostTradeIdList = db.retrieveSQL(sql);
		if(indexPostTradeIdList == null || indexPostTradeIdList.size() == 0)
			return null;
		String tradeIds = CommonUtil.getStringID(indexPostTradeIdList, "tradeid");
		sql = "SELECT id, name, englishname, iconname FROM dic_trade WHERE id IN ("+tradeIds+") ORDER BY seq";
		return db.retrieveSQL(sql);
	}
	/**
	 * 通过行业ID查询行业名
	 */
	public Map<Object,Object> getTradeInfoMap(int tradeId) throws Throwable{
		String sql = "SELECT name, englishname FROM dic_trade WHERE id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(tradeId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	/**
	 * 首页作品
	 */
	public List<Map<Object,Object>> getIndexPostList(int curPage) throws Throwable{
		List<Map<Object,Object>> postIndexList = getIndexPostIndexList(curPage);
		if(postIndexList.size() == 0)
			return null;
		return setPostIndexList(postIndexList);
	}
	/**
	 * 首页作品总数
	 */
	public int getIndexPostCount() throws Throwable{
		String sql = "SELECT COUNT(*) FROM cms_post_index";
		int count = db.getRecordCountSQL(sql);
		int maxNum = XingyunCommonConstant.INDEX_POST_MAXPAGE * XingyunCommonConstant.INDEX_POST_PAGESIZE;
		if(count > maxNum)
			count = maxNum;
		return count;
	}
	/**
	 * 获取首页作品id集合
	 * 索引：PRIMARY
	 */
	private List<Map<Object,Object>> getIndexPostIndexList(int curPage) throws Throwable{
		String sql = "SELECT postid FROM cms_post_index ORDER BY id DESC LIMIT ?, ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add((curPage - 1) * XingyunCommonConstant.INDEX_POST_PAGESIZE);
		valueList.add(XingyunCommonConstant.INDEX_POST_PAGESIZE);
		return db.retrieveSQL(sql, valueList);
	}

	/**
	 * 获取切换程序bean
	 */
	public SwitchCityBean getUserSwitchCityBean(HttpServletRequest request) throws Throwable{
		try {
			SwitchCityBean cityBean = SessionCookie.getIndexSwitchCityBean(request);
			String cityName = "";
			if(cityBean.getCityid() > 0){
				cityName = AreaUtil.getInstance().findAreaNameByIdParentid(cityBean.getCityid(), cityBean.getProvinceid());
			}else if(cityBean.getProvinceid() > 0){
				cityName = AreaUtil.getInstance().findAreaNameById(cityBean.getProvinceid());
			}
			if(StringUtils.isBlank(cityName)){
				cityBean.setCityid(0);
				cityBean.setProvinceid(0);
				cityBean.setCityName("中国");
			}else{
				cityBean.setCityName(cityName);
			}
			return cityBean;
		} catch (Throwable e) {
			log.error(e);
			SwitchCityBean cityBean = new SwitchCityBean();
			cityBean.setCityName("中国");
			return cityBean;
		}
	}
	
	/**
	 * 保存城市切换数据
	 */
	public void saveUserSwitchCityBean(SwitchCityBean cityBean) throws Throwable{
		if(cityBean == null){
			cityBean = new SwitchCityBean();
			cityBean.setCityid(0);
			cityBean.setProvinceid(0);
			cityBean.setCityName("中国");
		}
		SessionCookie.setIndexSwitchCityCookieMC(cityBean);
	}
	
	/**
	 * 根据行业ID查询 星语人才目录索引数据
	 */
	public List<Map<Object, Object>> getRencaiTradeIndex(int tradeId) throws Throwable{
		String sql = "SELECT c.id, u.userid, u.lid, u.verified, uc.recommendcount FROM cms_user_rencai_trade c, user u, user_counter uc WHERE c.userid = u.userid AND uc.userid = u.userid AND c.tradeid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(tradeId);
		List<Map<Object, Object>> indexList = db.retrieveSQL(sql, vList);
		indexList = CommonUtil.setList(indexList);
		String[] seqValue = {"lid","verified","recommendcount","id"};
		CommonUtil.compositor(indexList, seqValue, 1);
		return indexList;
	}
	
	/**
	 * 查询首页人才目录 显示行业数据
	 */
	public List<Map<Object, Object>> findRencaiTradeList() throws Throwable{
		String sql = "SELECT t.id, t.name, t.englishname, t.seq, t.iconname FROM dic_trade t, dic_vocation v WHERE v.tradeid = t.id AND v.isindex = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunCommonConstant.VOCATION_ISINDEX_YES);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, vList);
		list = CommonUtil.setList(list);
		String[] seqValue = {"seq"};
		CommonUtil.compositor(list, seqValue, 0);
		return list;
	}
	
	/**
	 * 根据行业ID查询 星语人才目录显示数据
	 */
	public List<Map<Object, Object>> getRencaiTradeData(List<Map<Object, Object>> indexList, int pageIndex, int pageSize) throws Throwable{
		//整理星云人才分页索引数据
		indexList = CommonUtil.subList(indexList, pageIndex, pageSize);
		if(indexList == null || indexList.size() == 0)
			return null;
		
		String userID = "";
		String userIDs = CommonUtil.getStringUserID(indexList, "userid");
		Map<Object, Map<Object, Object>> userCounterMap = getUserCounterMap(userIDs);
		Map<Object, List<Map<Object, Object>>> userPriceMap = getUserCooperationPriceMap(userIDs);
		Map<Object, Object> userCityMap = getUserCityMap(userIDs);
		Map<Object,Object> userMap = null;
		Map<Object,Object> tmpMap = null;
		List<Map<Object,Object>> userInfoList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> map : indexList){
			userID = map.get("userid").toString();
			userMap = PublicQueryUtil.getInstance().findUserCommonMap(userID);
			if(userMap == null)
				continue;
			
			tmpMap = userCounterMap.get(userID);
			userMap.put("recommendcount", tmpMap == null ? 0 : tmpMap.get("recommendcount"));
			userMap.put("fanscount", tmpMap == null ? 0 : tmpMap.get("fanscount"));
			userMap.put("isShowfollow", UserHeaderService.getInstance().checkIsShowfollow(userID));
			userMap.put("postRecommendCount", getUserPostRecommendCount(userID));
			userMap.put("logourl", UploadPicUtil.getPicWebUrl(userMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_100));
			userMap.put("city", userCityMap.get(userID));
			if(checkShowCooperationPrice(userID))
				userMap.put("price", userPriceMap.get(userID));
			userInfoList.add(userMap);
		}
		return userInfoList;
	}
	
	/**
	 * 检查是否显示合作报价
	 */
	private boolean checkShowCooperationPrice(String userID) throws Throwable{
		Map<Object, Object> faceInfoMap = ProfileService.getInstance().getFaceInfo(userID);
		if(faceInfoMap == null)
			return false;
		
		if(Integer.parseInt(faceInfoMap.get("isshowcooperation").toString()) == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
			return true;
		return false;
	}

	/**
	 * 查询用户被推荐数量 和 被关注数量
	 * 使用索引：index_user_counter_userid
	 */
	public Map<Object, Map<Object, Object>> getUserCounterMap(String userIDs) throws Throwable{
		String sql = "SELECT userid, fanscount, recommendcount FROM user_counter WHERE userid IN(" + userIDs + ")";
		List<Map<Object, Object>> list = db.retrieveSQL(sql);
		Map<Object, Map<Object, Object>> userCounterMap = new HashMap<Object, Map<Object,Object>>();
		for(Map<Object, Object> map : list){
			userCounterMap.put(map.get("userid"), map);
		}
		return userCounterMap;
	}
	
	/**
	 * 查询用户所在地数据
	 */
	public Map<Object, Object> getUserCityMap(String userIDs) throws Throwable{
		String sql = "SELECT p.userid, a.name FROM user_profile p, dic_area a WHERE p.provinceid = a.id AND p.userid IN(" + userIDs + ")";
		List<Map<Object, Object>> list = db.retrieveSQL(sql);
		Map<Object, Object> userCityMap = new HashMap<Object, Object>();
		for(Map<Object, Object> map : list){
			userCityMap.put(map.get("userid"), map.get("name"));
		}
		return userCityMap;
	}
	
	/**
	 * 查询用户合作报价数据
	 */
	public Map<Object, List<Map<Object, Object>>> getUserCooperationPriceMap(String userIDs) throws Throwable{
		String sql = "SELECT userid, name, price_min, price_max, price_type FROM user_cooperation_price WHERE userid IN(" + userIDs + ") ORDER BY id";
		List<Map<Object, Object>> list = db.retrieveSQL(sql);
		Map<Object, List<Map<Object, Object>>> userCooperationPriceMap = new HashMap<Object, List<Map<Object,Object>>>();
		List<Map<Object, Object>> cooperationList = null;
		for(Map<Object, Object> map : list){
			map.put("price_min", CommonUtil.formatPrice(Integer.parseInt(map.get("price_min").toString())));
			map.put("price_max", CommonUtil.formatPrice(Integer.parseInt(map.get("price_max").toString())));
			cooperationList = userCooperationPriceMap.get(map.get("userid"));
			if(cooperationList == null){
				cooperationList = new ArrayList<Map<Object,Object>>();
				cooperationList.add(map);
				userCooperationPriceMap.put(map.get("userid"), cooperationList);
			}else{
				cooperationList.add(map);
				userCooperationPriceMap.put(map.get("userid"), cooperationList);
			}
		}
		return userCooperationPriceMap;
	}
	
	/**
	 * 查询用户作品被推荐数量
	 */
	public int getUserPostRecommendCount(String userID) throws Throwable{
		String sql = "SELECT SUM(recommendcount) AS count FROM post_counter WHERE userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		return CommonUtil.getIntValue(sql, vList, "count");
	}
	
	/**
	 * 根据行业ID查询 星语人才目录特别推荐索引数据
	 */
	public List<Map<Object, Object>> getRencaiTebieIndex(int tradeId) throws Throwable{
		String sql = "SELECT id, userid FROM cms_user_rencai_tebie WHERE tradeid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(tradeId);
		List<Map<Object, Object>> indexList = db.retrieveSQL(sql, vList);
		indexList = CommonUtil.setList(indexList);
		String[] seqValue = {"id"};
		CommonUtil.compositor(indexList, seqValue, 1);
		return indexList;
	}
	
	/**
	 * 根据行业ID查询 星语人才目录特别推荐数据
	 */
	public List<Map<Object, Object>> getRencaiTebieData(List<Map<Object, Object>> indexList, int pageIndex, int pageSize) throws Throwable{
		//整理星云人才分页索引数据
		indexList = CommonUtil.subList(indexList, pageIndex, pageSize);
		if(indexList == null || indexList.size() == 0)
			return null;
		
		String userID = "";
		Map<Object,Object> userMap = null;
		List<Map<Object,Object>> userInfoList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> map : indexList){
			userID = map.get("userid").toString();
			userMap = PublicQueryUtil.getInstance().findUserCommonMap(userID);
			if(userMap == null)
				continue;
			userMap.put("logourl", UploadPicUtil.getPicWebUrl(userMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_200));
			userInfoList.add(userMap);
		}
		return userInfoList;
	}
	
	/**
	 * 行业下最新加入大侠精英
	 */
	public List<Map<Object, Object>> findNewTradeDaxiaJingyingList(int tradeId) throws Throwable{
		List<Map<Object, Object>> indexList = new ArrayList<Map<Object,Object>>();
		indexList.addAll(findNewTradeDaxiaList(tradeId));
		indexList.addAll(findNewTradeJingyingList(tradeId));
		if(indexList.size() == 0)
			return null;
		
		//去除重复
		indexList = CommonUtil.setList(indexList);
		
		//排序
		String[] seqValue = {"systime"};
		CommonUtil.compositor(indexList, seqValue, 1);
		
		if(indexList.size() > XingyunCommonConstant.INDEX_RENCAI_NEW_DAXIA_SIZE)
			indexList = CommonUtil.subList(indexList, 0, XingyunCommonConstant.INDEX_RENCAI_NEW_DAXIA_SIZE);
		return MyIndexService.getInstance().setNewUserList(indexList);
	}
	
	/**
	 * 查询行业下最新加入的大侠数据
	 */
	private List<Map<Object, Object>> findNewTradeDaxiaList(int tradeId) throws Throwable{
		String sql = "SELECT d.userid, d.systime FROM cms_user_new_daxia d, user_vocation uv WHERE d.userid = uv.userid AND uv.tradeid = ? ORDER BY d.id DESC LIMIT 100";
		List<Object> vList = new ArrayList<Object>();
		vList.add(tradeId);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 查询行业下最新加入的精英数据
	 */
	private List<Map<Object, Object>> findNewTradeJingyingList(int tradeId) throws Throwable{
		String sql = "SELECT d.userid, d.systime FROM cms_user_new_jingying d, user_vocation uv WHERE d.userid = uv.userid AND uv.tradeid = ? ORDER BY d.id DESC LIMIT 100";
		List<Object> vList = new ArrayList<Object>();
		vList.add(tradeId);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 人才行业页面 最新推荐作品
	 */
	public List<Map<Object, Object>> findNewTradePostList(int tradeId) throws Throwable{
		List<Map<Object, Object>> indexList = new ArrayList<Map<Object,Object>>();
		indexList.addAll(findNewTradePostRecommendList(tradeId));
		indexList.addAll(findNewTradePostPindaoList(tradeId));
		if(indexList.size() == 0)
			return null;
		
		//去除重复
		indexList = CommonUtil.setList(indexList);
		
		//排序
		String[] seqValue = {"systime"};
		CommonUtil.compositor(indexList, seqValue, 1);
		
		if(indexList.size() > XingyunCommonConstant.INDEX_RENCAI_RECOMMEND_POST_SIZE)
			indexList = CommonUtil.subList(indexList, 0, XingyunCommonConstant.INDEX_RENCAI_RECOMMEND_POST_SIZE);
		return MyIndexService.getInstance().setRecommWorks(indexList);
	}
	
	/**
	 * 查询作品频道 最新推荐数据
	 */
	private List<Map<Object, Object>> findNewTradePostRecommendList(int tradeId) throws Throwable{
		String sql = "SELECT postid, userid, systime FROM cms_post_recommend WHERE tradeid = ? ORDER BY id DESC LIMIT ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(tradeId);
		vList.add(XingyunCommonConstant.INDEX_RENCAI_RECOMMEND_POST_SIZE);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 查询星作品作品频道 最新推荐数据
	 */
	private List<Map<Object, Object>> findNewTradePostPindaoList(int tradeId) throws Throwable{
		String sql = "SELECT postid, userid, systime FROM cms_post_pindao WHERE tradeid = ? ORDER BY id DESC LIMIT ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(tradeId);
		vList.add(XingyunCommonConstant.INDEX_RENCAI_RECOMMEND_POST_SIZE);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 星作品页 最新作品
	 */
	public List<Map<Object, Object>> findNewRecommendPostList(int curPage) throws Throwable{
		List<Map<Object, Object>> postIndexList = new ArrayList<Map<Object,Object>>();
		postIndexList.addAll(findNewIndexPostIndexList());
		postIndexList.addAll(findNewRecommendPostIndexList());
		if(postIndexList.size() == 0)
			return null;
		
		//去除重复
		postIndexList = CommonUtil.setList(postIndexList);
		
		//排序
		String[] seqValue = {"systime"};
		CommonUtil.compositor(postIndexList, seqValue, 1);
		
		if(postIndexList.size() > XingyunCommonConstant.RECOMMEND_NEW_WORKS_MAXNUM)
			postIndexList = CommonUtil.subList(postIndexList, curPage, XingyunCommonConstant.RECOMMEND_NEW_WORKS_MAXNUM);
		return setPostIndexList(postIndexList);
	}

	/**
	 * 查询首页作品 最新推荐数据
	 */
	private List<Map<Object, Object>> findNewIndexPostIndexList() throws Throwable{
		String sql = "SELECT postid, systime FROM cms_post_index ORDER BY id DESC LIMIT ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunCommonConstant.RECOMMEND_NEW_WORKS_MAXNUM * XingyunCommonConstant.RECOMMEND_NEW_WORKS_PAGENUM);
		return db.retrieveSQL(sql, vList);
	}

	/**
	 * 查询作品频道 最新推荐数据
	 */
	private List<Map<Object, Object>> findNewRecommendPostIndexList() throws Throwable{
		String sql = "SELECT postid, systime FROM cms_post_recommend ORDER BY id DESC LIMIT ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunCommonConstant.RECOMMEND_NEW_WORKS_MAXNUM * XingyunCommonConstant.RECOMMEND_NEW_WORKS_PAGENUM);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 首页最新加入大侠精英
	 */
	public List<Map<Object, Object>> findIndexNewDaxiaJingyingList() throws Throwable{
		List<Map<Object, Object>> indexList = new ArrayList<Map<Object,Object>>();
		indexList.addAll(findIndexNewDaxiaList());
		indexList.addAll(findIndexNewJingyingList());
		if(indexList.size() == 0)
			return null;
		
		//去除重复
		indexList = CommonUtil.setList(indexList);
		
		//排序
		String[] seqValue = {"systime"};
		CommonUtil.compositor(indexList, seqValue, 1);
		
		if(indexList.size() > XingyunCommonConstant.INDEX_NEWJOIN_DX_JY_MAXSIZW)
			indexList = CommonUtil.subList(indexList, 0, XingyunCommonConstant.INDEX_NEWJOIN_DX_JY_MAXSIZW);
		return setIndexNewUserList(indexList);
	}
	
	/**
	 * 查询首页最新加入的大侠数据
	 */
	private List<Map<Object, Object>> findIndexNewDaxiaList() throws Throwable{
		String sql = "SELECT userid, systime FROM cms_user_new_daxia ORDER BY id DESC LIMIT ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunCommonConstant.INDEX_NEWJOIN_DX_JY_MAXSIZW);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 查询首页最新加入的精英数据
	 */
	private List<Map<Object, Object>> findIndexNewJingyingList() throws Throwable{
		String sql = "SELECT userid, systime FROM cms_user_new_jingying ORDER BY id DESC LIMIT ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunCommonConstant.INDEX_NEWJOIN_DX_JY_MAXSIZW);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 设置首页最新加入的用户详细信息
	 */
	public List<Map<Object, Object>> setIndexNewUserList(List<Map<Object, Object>> newUserlist) throws Throwable {
		String userId = "";
		Map<Object, Object> userInfoMap = null;
		String userIDs = CommonUtil.getStringUserID(newUserlist, "userid");
		Map<Object, Map<Object, Object>> userCounterMap = getUserCounterMap(userIDs);
		Map<Object, Object> userCityMap = getUserCityMap(userIDs);
		List<Map<Object,Object>> startList = new ArrayList<Map<Object,Object>>();
		Map<Object,Object> tmpMap = null;
		for(Map<Object,Object> map : newUserlist){
			userId = map.get("userid").toString();
			userInfoMap = PublicQueryUtil.getInstance().findUserCommonMap(userId);
			if (userInfoMap == null )
				continue;
			map.putAll(userInfoMap);
			map.put("city", userCityMap.get(userId));
			map.put("verifyContent", userInfoMap.get("verified_reason")); 	// 认证信息
			map.put("logourl", UploadPicUtil.getPicWebUrl(userInfoMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_100));	// 图像URL
		
			tmpMap = userCounterMap.get(userId);
			map.put("recommendcount", tmpMap == null ? 0 : tmpMap.get("recommendcount"));
			map.put("fanscount", tmpMap == null ? 0 : tmpMap.get("fanscount"));
			startList.add(map);
		}
		return startList;
	}
	/**
	 * 查询首页推荐位回顾总数
	 */
	public int getIndexAdCount() throws Throwable{
		String sql = "SELECT COUNT(*) FROM ad_pic a, ad_pic_item p WHERE a.picid = p.id AND a.type = ? AND a.status = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunAdConstant.XY_AD_TYPE_INDEX);
		vList.add(XingyunAdConstant.XY_AD_STATUS_DOWN);
		return db.getRecordCountSQL(sql, vList);
	}
	
	/**
	 * 查询首页推荐位回顾
	 * 使用索引：index_ad_pic_type_status_seq
	 */
	public List<Map<Object, Object>> getIndexAdList(int curPage) throws Throwable{
		String sql = "SELECT p.title, p.href, p.picpath,a.seq FROM ad_pic a, ad_pic_item p WHERE a.picid = p.id AND a.type = ? AND a.status = ? ORDER BY a.seq LIMIT ?,?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(XingyunAdConstant.XY_AD_TYPE_INDEX);
		vList.add(XingyunAdConstant.XY_AD_STATUS_DOWN);
		vList.add((curPage - 1) * XingyunCommonConstant.INDEX_AD_MAXNUM);
		vList.add(XingyunCommonConstant.INDEX_AD_MAXNUM);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, vList);
		if(list.size() == 0)
			return null;
		for(Map<Object,Object> map : list){
			map.put("picUrl", UploadPicUtil.getSysPicWebUrl(map.get("picpath").toString()));
		}
		return list;
	}
}