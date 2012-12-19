package com.xingyun.actions.recommend;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.XingyunBaseAction;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunMyIndexConstant;
import com.xingyun.services.recommend.RecommendService;
import com.xingyun.util.CounterUtil;
import com.xingyun.util.MessageUtil;

public class RecommendShowAction extends XingyunBaseAction{

	private static final long serialVersionUID = 4948316245679302359L;
	private static final Logger log = Logger.getLogger(RecommendShowAction.class);
	
	private UserHeaderBean userHeaderBean;			//头部数据bean
	private int totalRecord;						//总数
	private int recommendPostCount;					//推荐作品总数
	private int recommendUserCount;					//推荐用户总数
	private int recommendToUserCount;				//推荐我的总数
	private List<Map<Object, Object>> postList;		//作品数据
	
	private int levelType;						    //用户等级 0:默认全部  1:精英 2： 明星
	private List<Map<Object, Object>> userList;		//推荐用户数据
	private String recommentFilter;                 //TA推荐的作品 TA推荐的人才 推荐过TA的人
	
	private void setUserHeader() throws Throwable{
		userHeaderBean = UserHeaderUtil.getUserHeaderByUserID(user, userid);
		if(userHeaderBean == null)
			throw new Throwable("userHeaderBean null userid = " + userid);
		recommendPostCount = RecommendService.getInstance().findRecommendPostCount(userHeaderBean.getUserId());
		recommendUserCount = RecommendService.getInstance().findRecommendFromUserCount(userHeaderBean.getUserId(), 0);
		recommendToUserCount = RecommendService.getInstance().findRecommendToUserCount(userHeaderBean.getUserId(), 0);
	}
	
	/**
	 * 显示推荐作品列表页面(主)
	 */
	public String showRecommend(){
		try {
			setUserHeader();			//设置头部数据
			CounterUtil.setRecommendCount(userHeaderBean.getUserId());
			if(XingyunCommonConstant.USER_EDIT.equals(userHeaderBean.getLookState()))
				MessageUtil.clearNewMessage(userHeaderBean.getUserId(), "recommendcount");
			userid = userHeaderBean.getUserId();
			setRecommendToUserData();
			recommentFilter = XingyunCommonConstant.RECOMMEND_FILTER_FORME;
			if (recommendToUserCount != 0){
				return "showRecommend";
			}else if (recommendUserCount != 0){
				setRecommendUserData();
				recommentFilter = XingyunCommonConstant.RECOMMEND_FILTER_USER;
			}else if (recommendPostCount != 0){
				setRecommendPostData();		//获取推荐作品数据
				recommentFilter = XingyunCommonConstant.RECOMMEND_FILTER_POST;
			}
			return "showRecommend";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 整理推荐作品显示数据
	 */
	private void setRecommendPostData() throws Throwable{
		totalRecord = RecommendService.getInstance().findRecommendPostCount(userid);;
		if(totalRecord > 0)
			postList = RecommendService.getInstance().getShowRecommendPostList(userid, curPage, XingyunMyIndexConstant.MYINDEX_RECOMMEND_POST_PAGE_SIZE);
	}
	
	/**
	 * 推荐作品ajax翻页
	 */
	public String getRecommendPostAjax(){
		try {
			setRecommendPostData();
			return "showRecommendPostAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}

	/**
	 * 整理推荐用户显示数据
	 */
	private void setRecommendUserData() throws Throwable{
		totalRecord = RecommendService.getInstance().findRecommendFromUserCount(userid, levelType);
		if(totalRecord > 0)
			userList = RecommendService.getInstance().getShowRecommendFromUserList(userid, levelType, curPage, XingyunMyIndexConstant.MYINDEX_RECOMMEND_USER_PAGE_SIZE);
	}
	
	/**
	 * 推荐用户ajax翻页
	 */
	public String getRecommendUserAjax(){
		try {
			setRecommendUserData();
			return "showRecommendUserAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 整理推荐过他的用户数据
	 */
	private void setRecommendToUserData() throws Throwable{
		totalRecord = RecommendService.getInstance().findRecommendToUserCount(userid, levelType);
		if(totalRecord > 0)
			userList = RecommendService.getInstance().getShowRecommendToUserList(userid, levelType, curPage, XingyunMyIndexConstant.MYINDEX_RECOMMEND_USER_PAGE_SIZE);
	}
	
	/**
	 * 推荐过他的用户 ajax翻页
	 */
	public String getRecommendToUserAjax(){
		try {
			setRecommendToUserData();
			return "showRecommendToUserAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 显示推荐过他的用户
	 */
	public String showRecommendToUser(){
		try {
			setUserHeader();			//设置头部数据
			setRecommendToUserData();
			recommentFilter = XingyunCommonConstant.RECOMMEND_FILTER_FORME;
			return "showRecommend";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	

	public UserHeaderBean getUserHeaderBean() {
		return userHeaderBean;
	}

	public void setUserHeaderBean(UserHeaderBean userHeaderBean) {
		this.userHeaderBean = userHeaderBean;
	}

	public List<Map<Object, Object>> getPostList() {
		return postList;
	}

	public void setPostList(List<Map<Object, Object>> postList) {
		this.postList = postList;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public List<Map<Object, Object>> getUserList() {
		return userList;
	}

	public void setUserList(List<Map<Object, Object>> userList) {
		this.userList = userList;
	}

	public int getRecommendPostCount() {
		return recommendPostCount;
	}

	public void setRecommendPostCount(int recommendPostCount) {
		this.recommendPostCount = recommendPostCount;
	}

	public int getRecommendUserCount() {
		return recommendUserCount;
	}

	public void setRecommendUserCount(int recommendUserCount) {
		this.recommendUserCount = recommendUserCount;
	}

	public int getRecommendToUserCount() {
		return recommendToUserCount;
	}

	public void setRecommendToUserCount(int recommendToUserCount) {
		this.recommendToUserCount = recommendToUserCount;
	}

	public int getLevelType() {
		return levelType;
	}

	public void setLevelType(int levelType) {
		this.levelType = levelType;
	}

	public String getRecommentFilter() {
		return recommentFilter;
	}

	public void setRecommentFilter(String recommentFilter) {
		this.recommentFilter = recommentFilter;
	}
}
