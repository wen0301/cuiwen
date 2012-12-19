package com.xingyun.actions.xingyu;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.XingyunBaseAction;
import com.xingyun.bean.CommentBean;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.bean.XingyuBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.services.comment.CommentService;
import com.xingyun.services.post.PostService;
import com.xingyun.services.xingyu.XingyuService;
import com.xingyun.services.zan.ZanService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.CounterUtil;

public class XingyuShowAction extends XingyunBaseAction{

	private static final long serialVersionUID = 2892879321275315495L;
	private static final Logger log = Logger.getLogger(XingyuShowAction.class);	

	private UserHeaderBean userHeaderBean;	//头部数据bean
	private List<XingyuBean> xingyuList;	//星语主题数据列表
	private int totalRecord;				//总数
	private int xingyuID;					//星语主题ID
	private String xingyuUserID;            //星语主题用户ID
	private XingyuBean xyBean;				//星语主题bean
	private List<Map<Object, Object>> zanList;			//星语赞数据
	private List<Map<Object, Object>> postList;			//最新作品数据
	private List<Map<Object, Object>> recommendList;	//Ta推荐的人数据
	private List<CommentBean> commentBeanList;          //评论列表
	private String userHref;
	private boolean isFromIos;
	
	/**
	 * 查看星语列表
	 */
	public String showXingYuList(){
		try {
			//整理头部数据
			userHeaderBean = UserHeaderUtil.getUserHeaderByUserID(user, userid);
			if(userHeaderBean == null)
				return "del";
			CounterUtil.setXingyuCount(userHeaderBean.getUserId());
			setXingyuListData();		//整理星语列表数据
			setRightData(userid);		//设置右边数据	
			isFromIos = CommonUtil.checkIsIOSDevice(servletRequest);
			return "showXingYuList";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 整理星语列表数据
	 */
	private void setXingyuListData() throws Throwable{
		List<Map<Object, Object>> indexList = XingyuService.getInstance().findXingYuAllIndex(userid, user); 
		if(indexList != null && indexList.size() > 0){
			totalRecord = indexList.size();
			xingyuList = XingyuService.getInstance().findXingYuList(user == null ? "" : user.getUserId(), indexList, curPage, XingyunCommonConstant.XINGYU_LIST_PAGE_SIZE);
		}
	}
	
	/**
	 * 星语列表ajax 翻页
	 */
	public String getXingyuListAjax(){
		try {
			setXingyuListData();
			return "showXingYuListAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 设置右边数据
	 */
	private void setRightData(String userID) throws Throwable{
		postList = PostService.getInstance().findUserNewPostList(userID, XingyunCommonConstant.NEW_POST_MAXSIZE);
		recommendList = XingyuService.getInstance().findRecommendFromUserList(userID);
	}
	
	/**
	 * 打开星云 赞 评论数据 
	 */
	public String showXingYuZanCommentAjax(){
		try {
			userHref = XingyuService.getInstance().getUserHrefByXingyuId(xingyuID);
			if(StringUtils.EMPTY.equals(userHref))
				return "del";
			zanList = ZanService.getInstance().getZanListData(xingyuID, XingyunCommonConstant.ZAN_TYPE_XINGYU);
			getXingyuCommentList(XingyunCommonConstant.COMMENT_TOP10_SIZE);
			return "showXingYuZanCommentAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	/**
	 * 查看单个星语
	 */
	public String showXingYuDetail(){
		try {
			//整理星语数据
			xyBean = XingyuService.getInstance().findXingYuData(xingyuID, user == null ? "" : user.getUserId());
			if(xyBean == null)
				return "del";
			
			//整理头部数据
			userHeaderBean = UserHeaderUtil.getUserHeaderByUserID(user, xyBean.getUserid());
			if(userHeaderBean == null)
				return "del";
			CounterUtil.setXingyuCount(userHeaderBean.getUserId());
			zanList = ZanService.getInstance().getZanListData(xingyuID, XingyunCommonConstant.ZAN_TYPE_XINGYU);
			getXingyuCommentList(XingyunCommonConstant.COMMENT_PREPAGE_MAXSIZE);
			//设置右边数据	
			setRightData(xyBean.getUserid());		
			return "showXingYuDetail";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	/**
	 * 获取星语评论列表
	 */
	private void getXingyuCommentList(int maxSize) throws Throwable{
		totalRecord = CommentService.getInstance().getCommentCount(XingyunCommonConstant.COMMENT_SOURCE_XINGYU, xingyuID);
		commentBeanList = CommentService.getInstance().getCommentList(XingyunCommonConstant.COMMENT_SOURCE_XINGYU, xingyuID, curPage, maxSize);
	}

	public UserHeaderBean getUserHeaderBean() {
		return userHeaderBean;
	}

	public void setUserHeaderBean(UserHeaderBean userHeaderBean) {
		this.userHeaderBean = userHeaderBean;
	}

	public List<XingyuBean> getXingyuList() {
		return xingyuList;
	}

	public void setXingyuList(List<XingyuBean> xingyuList) {
		this.xingyuList = xingyuList;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public List<Map<Object, Object>> getPostList() {
		return postList;
	}

	public void setPostList(List<Map<Object, Object>> postList) {
		this.postList = postList;
	}

	public List<Map<Object, Object>> getRecommendList() {
		return recommendList;
	}

	public void setRecommendList(List<Map<Object, Object>> recommendList) {
		this.recommendList = recommendList;
	}

	public int getXingyuID() {
		return xingyuID;
	}

	public void setXingyuID(int xingyuID) {
		this.xingyuID = xingyuID;
	}

	public List<Map<Object, Object>> getZanList() {
		return zanList;
	}

	public void setZanList(List<Map<Object, Object>> zanList) {
		this.zanList = zanList;
	}

	public XingyuBean getXyBean() {
		return xyBean;
	}

	public void setXyBean(XingyuBean xyBean) {
		this.xyBean = xyBean;
	}

	public List<CommentBean> getCommentBeanList() {
		return commentBeanList;
	}

	public void setCommentBeanList(List<CommentBean> commentBeanList) {
		this.commentBeanList = commentBeanList;
	}

	public String getXingyuUserID() {
		return xingyuUserID;
	}

	public void setXingyuUserID(String xingyuUserID) {
		this.xingyuUserID = xingyuUserID;
	}

	public String getUserHref() {
		return userHref;
	}

	public void setUserHref(String userHref) {
		this.userHref = userHref;
	}

	public boolean getIsFromIos() {
		return isFromIos;
	}

	public void setIsFromIos(boolean isFromIos) {
		this.isFromIos = isFromIos;
	}
}
