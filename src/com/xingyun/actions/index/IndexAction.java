package com.xingyun.actions.index;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xingyun.base.XingyunBaseAction;
import com.xingyun.bean.IndexUserBean;
import com.xingyun.bean.RecommendPostBean;
import com.xingyun.bean.SwitchCityBean;
import com.xingyun.constant.XingyunAdConstant;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.services.index.IndexService;
import com.xingyun.services.vocation.VocationService;
import com.xingyun.util.AreaUtil;
import com.xingyun.util.CommonUtil;

public class IndexAction extends XingyunBaseAction{

	private static final long serialVersionUID = -8485111335784075872L;
	private static final Logger log = Logger.getLogger(IndexAction.class);
	private List<Map<Object,Object>> tradeList;     //行业集合
	private List<IndexUserBean> indexUserBeanList;
	private List<Map<Object,Object>> indexUserList;
	private List<RecommendPostBean> recommendPostBeanList;
	private List<Map<Object,Object>> indexPostList;
	private List<Map<Object,Object>> newRecommendPostList;
	private List<Map<Object,Object>> adPicList;
	private int totalRecord;
	private int type;
	private int tradeId;
	private int sort;
	private int vocationId;
	private String tradeName;
	private String tradeName_english;
	private SwitchCityBean switchCityBean;			//城市切换城市bean
	private List<Map<Object,Object>> provinceList;	//省数据集合
	private List<Map<Object,Object>> cityList;		//市数据集合
	
	private List<Map<Object,Object>> rencaiList;		//星云人才目录推荐数据
	private int rencaiTebieCurPage;						//星云人才目录特别推荐显示页码
	private int rencaiTebieCount;						//星云人才目录特别推荐显示总数
	private List<Map<Object,Object>> rencaiTebieList;	//星云人才目录特别推荐数据
	private List<Map<Object,Object>> newJoinList;		//星云人才目录页面最新加入大侠数据
	private List<Map<Object,Object>> postList;			//星云人才目录页面推荐作品
	private int indexNewJoinTotal;						//星云首页最新加入大侠 精英总数

	/**
	 * 星云首页
	 */
	public String execute(){
		try {
			return "index";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 首页作品翻页
	 */
	public String showIndexWorksItem(){
		try {
			setIndexPostData(curPage);
			return "showIndexWorksItem";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 设置首页作品数据
	 */
	private void setIndexPostData(int pageIndex) throws Throwable{
		totalRecord = IndexService.getInstance().getIndexPostCount();
		pageIndex = pageIndex > XingyunCommonConstant.INDEX_POST_MAXPAGE ? XingyunCommonConstant.INDEX_POST_MAXPAGE : pageIndex;
		if(totalRecord != 0)
			indexPostList = IndexService.getInstance().getIndexPostList(pageIndex);
	}
	
	/**
	 * 首页最新加入大侠精英
	 */
	public String showIndexNewDaxiaJingyin(){
		try {
			//最新加入大侠 精英
			setIndexNewDaxiaJingyinData(curPage);
			return "showIndexNewDaxiaJingyin";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * 设置首页最近加入大侠精英数据
	 */
	private void setIndexNewDaxiaJingyinData(int pageIndex) throws Throwable{
		newJoinList = IndexService.getInstance().findIndexNewDaxiaJingyingList();						
		if(newJoinList != null){
			indexNewJoinTotal = newJoinList.size(); 
			newJoinList = CommonUtil.subList(newJoinList, pageIndex, XingyunCommonConstant.INDEX_NEWJOIN_DX_JY_PAGESIZE);
		}
	}
	
	/**
	 * 首页切换城市页面
	 */
	public String showSwitchCity(){
		try {
			switchCityBean = IndexService.getInstance().getUserSwitchCityBean(servletRequest);
			provinceList = AreaUtil.getInstance().findAreaInfo(0);
			if(switchCityBean != null && switchCityBean.getProvinceid() > 0)
				cityList = AreaUtil.getInstance().findAreaInfo(switchCityBean.getProvinceid());
			return "showSwitchCity";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 保存切换城市数据
	 */
	public String saveSwitchCity(){
		try {
			IndexService.getInstance().saveUserSwitchCityBean(switchCityBean);
			return "changeSwitchCity";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 星感言
	 */
	public String showXingGanYanAdAjax(){
		try {
			adPicList = IndexService.getInstance().getAdPicList(XingyunAdConstant.XY_AD_TYPE_XINGGANYAN);
			return "showIndexXingGanYanAD";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 星作品
	 */
	public String showRecommendWorksList(){
		try {
			adPicList = IndexService.getInstance().getAdPicList(XingyunAdConstant.XY_AD_TYPE_XINGZUOPIN);
			newRecommendPostList = IndexService.getInstance().findNewRecommendPostList(curPage);
			totalRecord = XingyunCommonConstant.RECOMMEND_NEW_WORKS_PAGENUM * XingyunCommonConstant.RECOMMEND_NEW_WORKS_MAXNUM;
			tradeList = IndexService.getInstance().getPostTradeList();
			recommendPostBeanList = IndexService.getInstance().getRecommendPostBeanList(tradeList);
			return "showRecommendWorks";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 星作品:ajax分页
	 */
	public String showRecommendWorksItem(){
		try {
			if(totalRecord == 0)
				totalRecord = IndexService.getInstance().getRecommendWorksCount(tradeId);
			curPage = curPage > XingyunCommonConstant.RECOMMEND_WORKS_PAGENUM ? XingyunCommonConstant.RECOMMEND_WORKS_PAGENUM : curPage;
			if(totalRecord != 0)
				indexPostList = IndexService.getInstance().getRecommendWorksList(tradeId, curPage, XingyunCommonConstant.RECOMMEND_WORKS_MAXNUM);
			return "showRecommendWorksItem";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 最新作品:ajax分页
	 */
	public String showNewRecommendWorksItem(){
		try {
			if(totalRecord == 0)
				totalRecord = XingyunCommonConstant.RECOMMEND_NEW_WORKS_PAGENUM * XingyunCommonConstant.RECOMMEND_NEW_WORKS_MAXNUM;
			curPage = curPage > XingyunCommonConstant.RECOMMEND_NEW_WORKS_PAGENUM ? XingyunCommonConstant.RECOMMEND_NEW_WORKS_PAGENUM : curPage;
			newRecommendPostList = IndexService.getInstance().findNewRecommendPostList(curPage);
			return "showRecommendWorksNewItem";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 星作品频道
	 */
	public String showRecommendPindaoList(){
		try {
			totalRecord = IndexService.getInstance().getRecommendPindaoCount(tradeId);
			if(totalRecord != 0)
				indexPostList = IndexService.getInstance().getRecommendPindaoList(tradeId, sort, curPage);
			Map<Object,Object> tradeInfoMap = IndexService.getInstance().getTradeInfoMap(tradeId);
			if(tradeInfoMap != null){
				tradeName = CommonUtil.getStringValue(tradeInfoMap.get("name"));
				tradeName_english = CommonUtil.getStringValue(tradeInfoMap.get("englishname"));
			}
			return "showRecommendPindao";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 星作品频道:ajax分页
	 */
	public String showPostRecommendPindaoItem(){
		try {
			if(totalRecord == 0)
				totalRecord = IndexService.getInstance().getRecommendPindaoCount(tradeId);
			if(totalRecord != 0)
				indexPostList = IndexService.getInstance().getRecommendPindaoList(tradeId, sort, curPage);
			return "showRecommendPindaoItem";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 首页静态页面头部
	 */
	public String getHtmlHeader(){
		try {
			return "showHtmlHeader";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}

	/**
	 * 检查用户是否登录
	 */
	public void checkUserLogin(){
		try {
			if(user != null)
				sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 首页：切换行业用户分页
	 */
	public String showTradeUserListAjax(){
		try {
			List<Map<Object,Object>> indexVocationList = VocationService.getInstance().getVocationList(tradeId, XingyunCommonConstant.VOCATION_ISINDEX_YES);
			if(indexVocationList.size() == 0)
				return "indexTradeUserItem";
			
			switchCityBean = IndexService.getInstance().getUserSwitchCityBean(servletRequest);
			totalRecord = IndexService.getInstance().getUserIndexCountByTradeId(tradeId, indexVocationList, switchCityBean);
			if(totalRecord > 0)
				indexUserList = IndexService.getInstance().getUserListByTradeId(tradeId, indexVocationList, switchCityBean, curPage);
			return "indexTradeUserItem";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 首页：切换职业用户分页
	 */
	public String showVocationUserListAjax(){
		try {
			switchCityBean = IndexService.getInstance().getUserSwitchCityBean(servletRequest);
			totalRecord = IndexService.getInstance().getUserIndexCountByVocation(vocationId, switchCityBean);
			if(totalRecord > 0)
				indexUserList = IndexService.getInstance().getUserListByVocationId(vocationId, switchCityBean, curPage);
			return "indexVocationUserItem";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 首页：星云人才
	 */
	public String showRencaiList(){
		try {
			if(user == null)
				return "relogin";
			tradeList = IndexService.getInstance().findRencaiTradeList();
			if(tradeList != null && tradeList.size() > 0 && tradeId <= 0)
				tradeId = Integer.parseInt(tradeList.get(0).get("id").toString());
			
			setRencaiTradeList();	//人才目录推荐数据
			setRencaiTebieList();	//人才目录特别推荐数据
			newJoinList = IndexService.getInstance().findNewTradeDaxiaJingyingList(tradeId);	//行业最新加入 大侠 精英
			postList = IndexService.getInstance().findNewTradePostList(tradeId);				//推荐作品
			return "showRencaiList";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 首页：星云人才目录 翻页
	 */
	public String showRencaiListAjax(){
		try {
			setRencaiTradeList();	//人才目录推荐数据
			return "showRencaiListAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 首页：星云人才目录 特别推荐翻页
	 */
	public String showRencaiTebieListAjax(){
		try {
			setRencaiTebieList();	//人才目录特别推荐数据
			return "showRencaiTebieListAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 设置人才目录推荐用户数据
	 */
	private void setRencaiTradeList() throws Throwable{
		List<Map<Object, Object>> indexList = IndexService.getInstance().getRencaiTradeIndex(tradeId);
		if(indexList != null && indexList.size() > 0){
			totalRecord = indexList.size();
			rencaiList = IndexService.getInstance().getRencaiTradeData(indexList, curPage, XingyunCommonConstant.INDEX_RENCAI_LIST_PAGESIZE);
		}
	}
	
	/**
	 * 设置人才目录特别推荐用户数据
	 */
	private void setRencaiTebieList() throws Throwable{
		List<Map<Object, Object>> indexList = IndexService.getInstance().getRencaiTebieIndex(tradeId);
		if(indexList != null && indexList.size() > 0){
			rencaiTebieCount = XingyunCommonConstant.INDEX_RENCAI_TEBIE_MAXPAGE * XingyunCommonConstant.INDEX_RENCAI_TEBIE_PAGESIZE;
			rencaiTebieCount = rencaiTebieCount > indexList.size() ? indexList.size() : rencaiTebieCount;
			curPage = curPage > XingyunCommonConstant.INDEX_RENCAI_TEBIE_MAXPAGE ? XingyunCommonConstant.INDEX_RENCAI_TEBIE_MAXPAGE : curPage;
			rencaiTebieList = IndexService.getInstance().getRencaiTebieData(indexList, curPage, XingyunCommonConstant.INDEX_RENCAI_TEBIE_PAGESIZE);
		}
	}
	
	/**
	 * 首页推荐回顾
	 */
	public String showIndexAdReview(){
		try {
			totalRecord = IndexService.getInstance().getIndexAdCount();
			if(totalRecord > 0)
				adPicList = IndexService.getInstance().getIndexAdList(curPage);
			return "showIndexAdReview";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 首页推荐回顾:Ajax分页
	 */
	public String showIndexAdReviewItem(){
		try {
			if(totalRecord == 0)
				totalRecord = IndexService.getInstance().getIndexAdCount();
			adPicList = IndexService.getInstance().getIndexAdList(curPage);
			return "showIndexAdReviewItem";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}

	public List<Map<Object, Object>> getTradeList() {
		return tradeList;
	}

	public void setTradeList(List<Map<Object, Object>> tradeList) {
		this.tradeList = tradeList;
	}

	public List<IndexUserBean> getIndexUserBeanList() {
		return indexUserBeanList;
	}

	public void setIndexUserBeanList(List<IndexUserBean> indexUserBeanList) {
		this.indexUserBeanList = indexUserBeanList;
	}

	public List<Map<Object, Object>> getIndexUserList() {
		return indexUserList;
	}

	public void setIndexUserList(List<Map<Object, Object>> indexUserList) {
		this.indexUserList = indexUserList;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getTradeId() {
		return tradeId;
	}

	public void setTradeId(int tradeId) {
		this.tradeId = tradeId;
	}

	public int getVocationId() {
		return vocationId;
	}

	public void setVocationId(int vocationId) {
		this.vocationId = vocationId;
	}

	public String getTradeName() {
		return tradeName;
	}

	public void setTradeName(String tradeName) {
		this.tradeName = tradeName;
	}

	public List<Map<Object, Object>> getIndexPostList() {
		return indexPostList;
	}

	public void setIndexPostList(List<Map<Object, Object>> indexPostList) {
		this.indexPostList = indexPostList;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public List<Map<Object, Object>> getAdPicList() {
		return adPicList;
	}

	public void setAdPicList(List<Map<Object, Object>> adPicList) {
		this.adPicList = adPicList;
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

	public SwitchCityBean getSwitchCityBean() {
		return switchCityBean;
	}

	public void setSwitchCityBean(SwitchCityBean switchCityBean) {
		this.switchCityBean = switchCityBean;
	}

	public List<RecommendPostBean> getRecommendPostBeanList() {
		return recommendPostBeanList;
	}

	public void setRecommendPostBeanList(
			List<RecommendPostBean> recommendPostBeanList) {
		this.recommendPostBeanList = recommendPostBeanList;
	}

	public String getTradeName_english() {
		return tradeName_english;
	}

	public void setTradeName_english(String tradeName_english) {
		this.tradeName_english = tradeName_english;
	}

	public List<Map<Object, Object>> getRencaiList() {
		return rencaiList;
	}

	public void setRencaiList(List<Map<Object, Object>> rencaiList) {
		this.rencaiList = rencaiList;
	}

	public List<Map<Object, Object>> getRencaiTebieList() {
		return rencaiTebieList;
	}

	public void setRencaiTebieList(List<Map<Object, Object>> rencaiTebieList) {
		this.rencaiTebieList = rencaiTebieList;
	}

	public int getRencaiTebieCount() {
		return rencaiTebieCount;
	}

	public void setRencaiTebieCount(int rencaiTebieCount) {
		this.rencaiTebieCount = rencaiTebieCount;
	}

	public int getRencaiTebieCurPage() {
		return rencaiTebieCurPage;
	}

	public void setRencaiTebieCurPage(int rencaiTebieCurPage) {
		this.rencaiTebieCurPage = rencaiTebieCurPage;
	}

	public List<Map<Object, Object>> getNewJoinList() {
		return newJoinList;
	}

	public void setNewJoinList(List<Map<Object, Object>> newJoinList) {
		this.newJoinList = newJoinList;
	}

	public List<Map<Object, Object>> getPostList() {
		return postList;
	}

	public void setPostList(List<Map<Object, Object>> postList) {
		this.postList = postList;
	}

	public List<Map<Object, Object>> getNewRecommendPostList() {
		return newRecommendPostList;
	}

	public void setNewRecommendPostList(
			List<Map<Object, Object>> newRecommendPostList) {
		this.newRecommendPostList = newRecommendPostList;
	}

	public int getIndexNewJoinTotal() {
		return indexNewJoinTotal;
	}

	public void setIndexNewJoinTotal(int indexNewJoinTotal) {
		this.indexNewJoinTotal = indexNewJoinTotal;
	}
}
