package com.xingyun.actions.post;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.XingyunBaseAction;
import com.xingyun.bean.CommentBean;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunPostConstant;
import com.xingyun.services.comment.CommentService;
import com.xingyun.services.post.PostService;
import com.xingyun.services.profile.ProfileService;
import com.xingyun.services.recommend.RecommendService;
import com.xingyun.services.zan.ZanService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.CounterUtil;

public class PostShowAction extends XingyunBaseAction{

	private static final long serialVersionUID = 7602619083802339311L;
	private static final Logger log = Logger.getLogger(PostShowAction.class);	

	private int postID; 								//作品ID
	private Map<Object, Object> postInfo;				//作品基础信息
	private List<Map<Object, Object>> postItemList;		//作品明细信息	
	private List<Map<Object, Object>> allPostList;		//底部左右滚动浏览作品列表
	private int zuoYouPostTotalPage; 					//左右滚动展示的总页数
	private List<Map<Object, Object>> postList;			//作品集合
	private int classID; 								//作品组ID
	private String className;                           //作品分组名
	private UserHeaderBean userHeaderBean;
	private String lookState;
	private int pageShowPostType;						//页面切换作品显示类型
	private int postDelCount;							//回收站作品数量
	private boolean isFromIpad;
	private int prePostId;                         		//上一个展示ID
	private int nextPostId;                             //下一个展示ID
	private List<CommentBean> commentBeanList;          //作品评论列表
	private int totalRecord;                            //作品评论总数
	private List<Map<Object, Object>> zanList;			//星语赞数据
	private int showContact;							//显示联系方式
	private int postType;								//作品类型 0：图片作品 1:视频作品
	private int picPostCount;							//图片作品数量
	private int videoPostCount;							//视频作品数量
	
	/**
	 * 整理头部数据
	 */
	private void setUserHeader() throws Throwable{
		userHeaderBean = UserHeaderUtil.getUserHeaderByUserID(user, userid);
		if(userHeaderBean == null)
			throw new Throwable("userHeaderBean null userid = " + userid);
	}
	
	/**
	 * 查看单个作品页面
	 */
	public String showPostByID(){
		try {
			if(CommonUtil.checkIsMobileDevice(servletRequest, XingyunCommonConstant.MOBILEDEVICE_TYPE_IPHONE))
				return showPostByIphoneUser();
			postInfo = PostService.getInstance().findPostDetailMap(postID);
			if(postInfo == null)
				return "del";
			
			userid = postInfo.get("userid").toString();
			setUserHeader();	//整理头部数据
			isFromIpad = CommonUtil.checkIsMobileDevice(servletRequest, XingyunCommonConstant.MOBILEDEVICE_TYPE_IPAD);
			boolean checkTag = PostService.getInstance().checkLookPost(postInfo, userHeaderBean.getLookState());
			if(!checkTag)
				return "del";
			
			boolean isCollectionPost = StringUtils.isBlank(PostService.getInstance().checkCollectionPost(user == null ? StringUtils.EMPTY : user.getUserId(), postID, userid)) ? true : false;
			postInfo.put("isCollectionPost", isCollectionPost);											//作品是否可以收藏
			postInfo.put("isRecommendPost", RecommendService.getInstance().checkRecommendPost(user == null ? StringUtils.EMPTY : user.getUserId(), postID, userid));	//作品是否可以推荐
			postInfo.put("isZan", false);
			if(user != null)
				postInfo.put("isZan", ZanService.getInstance().checkZan(user.getUserId(), postID, userid, XingyunCommonConstant.ZAN_TYPE_POST));
			CounterUtil.setPostCount(userid);
			CounterUtil.setPostViewCount(postID);														//修改作品点击量
			postItemList = PostService.getInstance().getPostItemData(postID);							//查询作品明细信息
			zanList = ZanService.getInstance().getZanListData(postID, XingyunCommonConstant.ZAN_TYPE_POST);
			getPostCommentList();
			setPreOrNextPostId(postID, userid, Integer.parseInt(postInfo.get("posttype").toString()), userHeaderBean.getLookState());
			setZuoYouPostList(userid, Integer.parseInt(postInfo.get("posttype").toString()), userHeaderBean.getLookState(), 1);	//设置左右滚动作品数据
			setShowContact(ProfileService.getInstance().checkShowContact(userid)); 						//作品详细页面显示联系方式
			return "showPostList";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 设置左右展示、给力、小图标数据
	 */
	private void setZuoYouPostList(String userID, int postType, String lookState, int curPage) throws Throwable {
		int count = PostService.getInstance().getUserTotalPost(userID, postType, lookState);
		if(count == 0)
			return;
		allPostList = PostService.getInstance().findZuoYouPostList(userid, postType, lookState, curPage); 	//整理底部左右切换作品集合
		int zuoYouPostCount = XingyunPostConstant.POST_ZUOYOU_SHOW_COUNT;
		zuoYouPostTotalPage = count % zuoYouPostCount == 0 ? count / zuoYouPostCount : count / zuoYouPostCount + 1;
	}
	
	/**
	 * 获取左右滚动展示列表
	 */
	public String zuoYouPostListAjax() {
		try {
			lookState = CommonUtil.getLookState(user, userid);
			setZuoYouPostList(userid,postType, lookState, curPage);
			return "zuoYouPostAjaxPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 查看作品列表
	 */
	public String showPostList(){
		try {
			setUserHeader();	//整理头部数据
			CounterUtil.setPostCount(userHeaderBean.getUserId());
			picPostCount = PostService.getInstance().findUserPostCountByType(userHeaderBean.getUserId(), userHeaderBean.getLookState(), XingyunPostConstant.POST_TYPE_PIC);		//查询用户图片作品总数
			videoPostCount = PostService.getInstance().findUserPostCountByType(userHeaderBean.getUserId(), userHeaderBean.getLookState(), XingyunPostConstant.POST_TYPE_VIDEO);	//查询用户视频作品总数
			postList = PostService.getInstance().findUserPostList(userHeaderBean.getUserId(), userHeaderBean.getLookState(), postType);	//查询用户作品列表
			if(XingyunCommonConstant.USER_EDIT.equals(userHeaderBean.getLookState()))
				postDelCount = PostService.getInstance().findPostDelCount(userHeaderBean.getUserId(), postType);	//查询回收站作品数量
			return "showPostListPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 查看分组作品列表
	 */
	public String showPostListByClass(){
		try {
			setUserHeader();	//整理头部数据
			picPostCount = PostService.getInstance().findUserPostCountByType(userHeaderBean.getUserId(), userHeaderBean.getLookState(), XingyunPostConstant.POST_TYPE_PIC);		//查询用户图片作品总数
			videoPostCount = PostService.getInstance().findUserPostCountByType(userHeaderBean.getUserId(), userHeaderBean.getLookState(), XingyunPostConstant.POST_TYPE_VIDEO);	//查询用户视频作品总数
			postList = PostService.getInstance().findUserPostClassList(userHeaderBean.getUserId(), postType, classID, userHeaderBean.getLookState());	//查询用户分组作品列表
			if(postList == null || postList.size() == 0)
				className = PostService.getInstance().getPostClassName(classID);
			if(XingyunCommonConstant.USER_EDIT.equals(userHeaderBean.getLookState()))
				postDelCount = PostService.getInstance().findPostDelCount(userHeaderBean.getUserId(), postType);										//查询回收站作品数量
			return "showPostListPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 查看单个作品页面
	 */
	public String showPostByIphoneUser(){
		try {
			postInfo = PostService.getInstance().findPostDetailMap(postID);
			if(postInfo == null)
				return "del";
			userid = postInfo.get("userid").toString();
			userHeaderBean = UserHeaderUtil.getUserHeaderByIphoneUserID(userid, "post");
			boolean checkTag = PostService.getInstance().checkLookPost(postInfo, XingyunCommonConstant.USER_FREE);
			if(!checkTag)
				return "postError";
			CounterUtil.setPostCount(userid);
			CounterUtil.setPostViewCount(postID);									//修改作品点击量
			postItemList = PostService.getInstance().getPostItemData(postID);		//查询作品明细信息
			return "showPostByIphone";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 设置上一个，下一个展示的操作
	 */
	private void setPreOrNextPostId(int postId, String postAuthorId, int postType, String lookState) throws Throwable{
		List<Map<Object, Object>> allTempPostList = PostService.getInstance().getPreOrNextPostIdList(postAuthorId, postType, lookState);
		int[] ids = PostService.getInstance().findNextPreviousID(postId, allTempPostList, "postID");
		prePostId = ids[0];
		nextPostId = ids[1];
	}
	/**
	 * 按页获取作品评论数据
	 */
	private void getPostCommentList() throws Throwable{
		totalRecord = CommentService.getInstance().getCommentCount(XingyunCommonConstant.COMMENT_SOURCE_POST, postID);
		commentBeanList = CommentService.getInstance().getCommentList(XingyunCommonConstant.COMMENT_SOURCE_POST, postID, curPage, XingyunCommonConstant.COMMENT_PREPAGE_MAXSIZE);
	}

	public int getPostID() {
		return postID;
	}

	public void setPostID(int postID) {
		this.postID = postID;
	}

	public Map<Object, Object> getPostInfo() {
		return postInfo;
	}

	public void setPostInfo(Map<Object, Object> postInfo) {
		this.postInfo = postInfo;
	}

	public List<Map<Object, Object>> getPostItemList() {
		return postItemList;
	}

	public void setPostItemList(List<Map<Object, Object>> postItemList) {
		this.postItemList = postItemList;
	}

	public List<Map<Object, Object>> getAllPostList() {
		return allPostList;
	}

	public void setAllPostList(List<Map<Object, Object>> allPostList) {
		this.allPostList = allPostList;
	}

	public int getZuoYouPostTotalPage() {
		return zuoYouPostTotalPage;
	}

	public void setZuoYouPostTotalPage(int zuoYouPostTotalPage) {
		this.zuoYouPostTotalPage = zuoYouPostTotalPage;
	}

	public List<Map<Object, Object>> getPostList() {
		return postList;
	}

	public void setPostList(List<Map<Object, Object>> postList) {
		this.postList = postList;
	}

	public int getClassID() {
		return classID;
	}

	public void setClassID(int classID) {
		this.classID = classID;
	}

	public UserHeaderBean getUserHeaderBean() {
		return userHeaderBean;
	}

	public void setUserHeaderBean(UserHeaderBean userHeaderBean) {
		this.userHeaderBean = userHeaderBean;
	}

	public String getLookState() {
		return lookState;
	}

	public void setLookState(String lookState) {
		this.lookState = lookState;
	}

	public int getPageShowPostType() {
		return pageShowPostType;
	}

	public void setPageShowPostType(int pageShowPostType) {
		this.pageShowPostType = pageShowPostType;
	}

	public int getPostDelCount() {
		return postDelCount;
	}

	public void setPostDelCount(int postDelCount) {
		this.postDelCount = postDelCount;
	}

	public boolean getIsFromIpad() {
		return isFromIpad;
	}

	public void setIsFromIpad(boolean isFromIpad) {
		this.isFromIpad = isFromIpad;
	}

	public int getNextPostId() {
		return nextPostId;
	}

	public void setNextPostId(int nextPostId) {
		this.nextPostId = nextPostId;
	}

	public void setFromIpad(boolean isFromIpad) {
		this.isFromIpad = isFromIpad;
	}

	public int getPrePostId() {
		return prePostId;
	}

	public void setPrePostId(int prePostId) {
		this.prePostId = prePostId;
	}

	public List<CommentBean> getCommentBeanList() {
		return commentBeanList;
	}

	public void setCommentBeanList(List<CommentBean> commentBeanList) {
		this.commentBeanList = commentBeanList;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public List<Map<Object, Object>> getZanList() {
		return zanList;
	}

	public void setZanList(List<Map<Object, Object>> zanList) {
		this.zanList = zanList;
	}

	public int getShowContact() {
		return showContact;
	}

	public void setShowContact(int showContact) {
		this.showContact = showContact;
	}

	public int getPostType() {
		return postType;
	}

	public void setPostType(int postType) {
		this.postType = postType;
	}

	public int getPicPostCount() {
		return picPostCount;
	}

	public void setPicPostCount(int picPostCount) {
		this.picPostCount = picPostCount;
	}

	public int getVideoPostCount() {
		return videoPostCount;
	}

	public void setVideoPostCount(int videoPostCount) {
		this.videoPostCount = videoPostCount;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
