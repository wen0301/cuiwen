package com.xingyun.actions.post;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.xingyun.actions.header.UserHeaderUtil;
import com.xingyun.base.AuthUserAction;
import com.xingyun.bean.ItemBean;
import com.xingyun.bean.ResPicBean;
import com.xingyun.bean.UserHeaderBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunLine;
import com.xingyun.constant.XingyunPostConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.services.post.PostService;
import com.xingyun.services.sinaoauth.SinaOauthService;
import com.xingyun.services.vocation.VocationService;
import com.xingyun.upload.bean.UploadParamBean;
import com.xingyun.upload.services.UploadService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.SpecialCharFilterUtil;
import com.xingyun.util.UploadPicUtil;

public class PostUpdateAction extends AuthUserAction {

	private static final long serialVersionUID = 7602619083802339311L;
	private static final Logger log = Logger.getLogger(PostUpdateAction.class);	

	private int postID; 					//作品ID
	private String zpTitle; 				//作品标题
	private int classid;    				//作品分组ID
	private int tradeid;					//作品所属行业ID
	private int displaytype;				//作品显示类型
	private int zpStatus;					//作品状态
	private List<String> zpTags;			//作品标签
	private List<ItemBean> itemBeanList; 	//作品明细集合
	private int shareType;					//作品是否分享到新浪	
	private List<UploadParamBean> p;
	private List<Map<Object, Object>> postClassList;	//用户作品分组集合
	private List<Map<Object, Object>> vocationList;		//一级行业集合
	private List<Integer> zpClassIDs;		//作品分组ID集合
	private List<String> zpClassNames;		//作品分组名集合
		
	private Map<Object, Object> postInfo;				//作品基础信息
	private List<Map<Object, Object>> postItemList;		//作品明细信息	
	private String postCoverResID;						//作品封面图片资源ID
	private int isIndex;								//作品是否首页显示 1：首页显示 0：不显示
	
	private List<Map<Object, Object>> postList;			//作品集合
	private UserHeaderBean userHeaderBean;
	private String postUserID;							//作品作者ID
	private int totalRecord;                            //作品收藏总数
	private boolean isFromIos;
	private int postType;								//作品类型 0：图片作品 1:视频作品
	/**
	 * 显示发布作品页面
	 */
	public String showPostAddPage(){
		try {
			isFromIos = CommonUtil.checkIsIOSDevice(servletRequest);
			postClassList = PostService.getInstance().findPostClassByPostType(user.getUserId(), postType);
			vocationList = VocationService.getInstance().getAllTradeList();
			return "showPostAddPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 保存发布新作品数据
	 */
	public void saveNewPost() {
		try {
			if(StringUtils.isBlank(zpTitle) || classid == -1 || tradeid == -1 || p == null || p.get(0) == null || itemBeanList == null || itemBeanList.size() == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			
			zpTitle = SpecialCharFilterUtil.filterEncodeAndForbidValue(zpTitle, XingyunPostConstant.ZP_TITLE_MAX_LENGTH);
			if(StringUtils.isBlank(zpTitle)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			//处理明细图片
			String firstPicPath = "";
			List<ResPicBean> postItemResPicBeanList = null;
			if(p.get(0).getSrc() != null && p.get(0).getSrc().size() > 0){
				postItemResPicBeanList = UploadService.getInstance().postItemSave(user.getUserId(), user.getNickName(), CommonUtil.getUserIndexHref(user.getUserId(), user.getWkey()), p.get(0));
				firstPicPath = CommonUtil.operateItemBeanListPic(itemBeanList, postItemResPicBeanList);
			}
			
			//处理封面图片
			ResPicBean coverResPicBean = null;
			if(p.get(1) != null && p.get(1).getSrc() != null && p.get(1).getSrc().size() > 0 && StringUtils.isNotBlank(p.get(1).getSrc().get(0)) && (p.get(1).getX1() != 0) ){
				coverResPicBean = UploadService.getInstance().postCoverSave(user.getUserId(), p.get(1));
			}else if(coverResPicBean == null && StringUtils.isNotBlank(firstPicPath)){
				coverResPicBean = UploadService.getInstance().postCoverSave(user.getUserId(), p.get(0), firstPicPath);	//如果没有上传作品封面图片 裁剪作品第一张图片
			}else{
				coverResPicBean = new ResPicBean();
				coverResPicBean.setPicid(XingyunUploadFileConstant.POST_SYS_COVER_RESID);
			}
			
			postID = PostService.getInstance().addPostData(user.getUserId(), postType, zpTitle, displaytype, tradeid, classid, itemBeanList, zpTags, coverResPicBean, postItemResPicBeanList);
			//星作品免检操作
			PostService.getInstance().recommendPostToWorks(postID, user.getUserId(), tradeid);
			//作品分享到新浪
			if(shareType == XingyunCommonConstant.XINGYUN_SHAREWEIBO_YES && postID != 0){
				String content = "我在最新上线的#星云网# "+XingyunLine.XINGYUN_CN+" 我是星云人才 "+PublicQueryUtil.getInstance().getScreenNameByUserId(user.getUserId())+"，我发布了一组作品( "+zpTitle+" ) 点击链接>> "+XingyunLine.XINGYUN_CN+"/show/"+postID+".html 展示的效果非常给力，各位请欣赏！";
				SinaOauthService.getInstance().shareToWeibo(user.getUserId(), content, UploadPicUtil.getPicWebUrl(coverResPicBean.getPicid(), XingyunUploadFileConstant.POST_COVER_WIDTH_250));
			}
			CommonUtil.stopTime();
			sendResponseMsg(String.valueOf(postID));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 显示作品编辑页面
	 */
	public String showPostEditPage(){
		try {
			postInfo = PostService.getInstance().findPostContentByID(postID, user.getUserId());
			if(postInfo == null)
				return "del";
			postInfo.put("coverPic", UploadPicUtil.getPicWebUrl(postInfo.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_250));
			postItemList = PostService.getInstance().getPostItemData(postID);
			zpTags = PostService.getInstance().findZpTags(postID);
			postClassList = PostService.getInstance().findPostClassByPostType(user.getUserId(), Integer.parseInt(postInfo.get("posttype").toString()));
			vocationList = VocationService.getInstance().getAllTradeList();
			isFromIos = CommonUtil.checkIsIOSDevice(servletRequest);
			return "showPostEditPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 保存作品修改
	 */
	public void editPostSave(){
		try {
			if(postID > 0 && StringUtils.isBlank(zpTitle) || classid == -1 || tradeid == -1 || p == null || p.get(0) == null || itemBeanList == null || itemBeanList.size() == 0){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			if(!PostService.getInstance().checkUpdatePostUser(postID, user.getUserId())){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			
			zpTitle = SpecialCharFilterUtil.filterEncodeAndForbidValue(zpTitle, XingyunPostConstant.ZP_TITLE_MAX_LENGTH);
			if(StringUtils.isBlank(zpTitle)){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			
			//处理明细图片
			String firstPicPath = "";
			List<ResPicBean> postItemResPicBeanList = null;
			if(p.get(0).getSrc() != null && p.get(0).getSrc().size() > 0){
				postItemResPicBeanList = UploadService.getInstance().postItemSave(user.getUserId(), user.getNickName(), CommonUtil.getUserIndexHref(user.getUserId(), user.getWkey()), p.get(0));
				firstPicPath = CommonUtil.operateItemBeanListPic(itemBeanList, postItemResPicBeanList);
			}
			//处理封面图片
			ResPicBean coverResPicBean = null;
			if(p.get(1) != null && p.get(1).getSrc() != null && p.get(1).getSrc().size() > 0 && StringUtils.isNotBlank(p.get(1).getSrc().get(0)))
				coverResPicBean = UploadService.getInstance().postCoverSave(user.getUserId(), p.get(1));
			else if(coverResPicBean == null && StringUtils.isNotBlank(firstPicPath) && XingyunUploadFileConstant.POST_SYS_COVER_RESID.equals(postCoverResID))
				coverResPicBean = UploadService.getInstance().postCoverSave(user.getUserId(), p.get(0), firstPicPath);	//如果没有上传作品封面图片 裁剪作品第一张图片
			if(coverResPicBean == null){
				coverResPicBean = new ResPicBean();
				coverResPicBean.setPicid(StringUtils.isNotBlank(postCoverResID) ? postCoverResID : XingyunUploadFileConstant.POST_SYS_COVER_RESID);
			}
			PostService.getInstance().updatePostData(user.getUserId(), postID, zpTitle, displaytype, tradeid, classid, itemBeanList, zpTags, coverResPicBean, postItemResPicBeanList);
			//作品分享到新浪
			if(shareType == XingyunCommonConstant.XINGYUN_SHAREWEIBO_YES && postID != 0){
				String content = "我在最新上线的#星云网# "+XingyunLine.XINGYUN_CN+" 我是星云人才 "+PublicQueryUtil.getInstance().getScreenNameByUserId(user.getUserId())+"，我发布了一组作品( "+zpTitle+" ) 点击链接>> "+XingyunLine.XINGYUN_CN+"/show/"+postID+".html 展示的效果非常给力，各位请欣赏！";
				SinaOauthService.getInstance().shareToWeibo(user.getUserId(), content, UploadPicUtil.getPicWebUrl(coverResPicBean.getPicid(), XingyunUploadFileConstant.POST_COVER_WIDTH_250));
			}
			CommonUtil.stopTime();
			sendResponseMsg(String.valueOf(postID));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 查看用户作品分组
	 */
	public String showPostClass(){
		try {
			postClassList = PostService.getInstance().findPostClassByPostType(user.getUserId(), postType);
			return "showPostClassPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 保存用户作品分组
	 */
	public void savePostClassAjax() {
		try {
			PostService.getInstance().savePostClass(user.getUserId(), postType, zpClassIDs, zpClassNames);	//保存作品分组
			String valuesStr = PostService.getInstance().getPostClassJson(user.getUserId(), postType);		//返回作品分组信息
			sendResponseMsg(valuesStr);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}

	/**
	 * 设置作品 是否首页显示 隐藏、显示状态 
	 */
	public void updatePostStatusAjax(){
		try {
			//验证用户是否可以操作该作品
			if(!PostService.getInstance().checkUpdatePostUser(postID, user.getUserId())){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			PostService.getInstance().updatePostStatus(postID, user.getUserId(), isIndex, zpStatus);	//设置作品隐藏、显示状态
			if(zpStatus == XingyunPostConstant.ZP_STATUS_TYPE_YC)
				PostService.getInstance().updatePostCmsStatusInfo(postID);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 将作品移到回收站
	 */
	public void setPostHuiShouTrueAjax(){
		setPostIsDelStatus(postID, XingyunPostConstant.ZP_DEL_TYPE_YES);
	}
	
	/**
	 * 将回收站作品还原
	 */
	public void setPostHuiShouFalseAjax() {
		setPostIsDelStatus(postID, XingyunPostConstant.ZP_DEL_TYPE_NO);
	}
	
	/**
	 * 设置作品逻辑删除状态
	 */
	private void setPostIsDelStatus(int postID, int isdelStatus){
		try {
			//验证用户是否可以操作该作品
			if(!PostService.getInstance().checkUpdatePostUser(postID, user.getUserId())){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			PostService.getInstance().updatePostDelStatus(postID, user.getUserId(), isdelStatus);		//修改作品状态
			if(isdelStatus == XingyunPostConstant.ZP_DEL_TYPE_YES)
				PostService.getInstance().updatePostCmsStatusInfo(postID);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}

	/**
	 * 查看回收站作品数据
	 */
	public String showHuiShouList(){
		try {
			userHeaderBean = UserHeaderUtil.getUserHeaderByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			
			postList = PostService.getInstance().findUsreHuiShouPostList(user.getUserId(), postType);	//查询用户回收站作品列表
			return "showPostHuiShouListPage";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 删除作品
	 */
	public void delPostAjax(){
		try {
			//验证用户是否可以操作该作品
			if(!PostService.getInstance().checkUpdatePostUser(postID, user.getUserId())){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			PostService.getInstance().delPostInfo(postID, user.getUserId());
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 收藏作品
	 */
	public void addCollectionPostAjax(){
		try {
			//验证用户是否可以收藏该作品
			String checkMsg = PostService.getInstance().checkCollectionPost(user.getUserId(), postID, userid);
			if(StringUtils.isNotBlank(checkMsg)){
				sendResponseMsg(checkMsg);
				return;
			}
			PostService.getInstance().addCollectionPost(postID, user.getUserId());
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 删除收藏作品
	 */
	public void delCollectionPostAjax(){
		try {
			//验证用户是否可以删除收藏作品
			boolean checkTag = PostService.getInstance().checkCollectionPost(postID, user.getUserId());
			if(checkTag){
				sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
				return;
			}
			PostService.getInstance().delCollectionPost(postID, user.getUserId());
			sendResponseMsg(XingyunCommonConstant.RESPONSE_SUCCESS_STRING);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
		}
	}
	
	/**
	 * 查看收藏作品列表数据
	 */
	public String showCollectionPostList(){
		try {
			userHeaderBean = UserHeaderUtil.getUserLeftByUserID(user, user.getUserId());
			if(userHeaderBean == null)
				return "del";
			
			totalRecord = userHeaderBean.getCollectionPostCount();
			curPage = curPage < 1 ? 1 : curPage;
			if(userHeaderBean.getCollectionPostCount() > 0)
				postList = PostService.getInstance().findCollectionPostList(user.getUserId(), curPage, XingyunPostConstant.POST_COOLECTIN_LIST_PAGESIZE);		//查询用户收藏作品数据
			return "showCollectionPostList";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 收藏作品翻页
	 */
	public String showCollectionPostListAjax(){
		try {
			totalRecord = PostService.getInstance().getCollectionPostCount(user.getUserId());
			curPage = curPage < 1 ? 1 : curPage;
			postList = PostService.getInstance().findCollectionPostList(user.getUserId(), curPage, XingyunPostConstant.POST_COOLECTIN_LIST_PAGESIZE);		//查询用户收藏作品数据
			return "showCollectionPostListAjax";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(XingyunCommonConstant.RESPONSE_ERR_STRING);
			return null;
		}
	}
	
	public String getZpTitle() {
		return zpTitle;
	}

	public void setZpTitle(String zpTitle) {
		this.zpTitle = zpTitle;
	}

	public int getClassid() {
		return classid;
	}

	public void setClassid(int classid) {
		this.classid = classid;
	}

	public List<UploadParamBean> getP() {
		return p;
	}

	public void setP(List<UploadParamBean> p) {
		this.p = p;
	}

	public List<ItemBean> getItemBeanList() {
		return itemBeanList;
	}

	public void setItemBeanList(List<ItemBean> itemBeanList) {
		this.itemBeanList = itemBeanList;
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

	public int getDisplaytype() {
		return displaytype;
	}

	public void setDisplaytype(int displaytype) {
		this.displaytype = displaytype;
	}

	public int getZpStatus() {
		return zpStatus;
	}

	public void setZpStatus(int zpStatus) {
		this.zpStatus = zpStatus;
	}

	public List<String> getZpTags() {
		return zpTags;
	}

	public void setZpTags(List<String> zpTags) {
		this.zpTags = zpTags;
	}

	public List<Map<Object, Object>> getPostClassList() {
		return postClassList;
	}

	public void setPostClassList(List<Map<Object, Object>> postClassList) {
		this.postClassList = postClassList;
	}

	public List<Integer> getZpClassIDs() {
		return zpClassIDs;
	}

	public void setZpClassIDs(List<Integer> zpClassIDs) {
		this.zpClassIDs = zpClassIDs;
	}

	public List<String> getZpClassNames() {
		return zpClassNames;
	}

	public void setZpClassNames(List<String> zpClassNames) {
		this.zpClassNames = zpClassNames;
	}

	public List<Map<Object, Object>> getVocationList() {
		return vocationList;
	}

	public void setVocationList(List<Map<Object, Object>> vocationList) {
		this.vocationList = vocationList;
	}

	public int getIsIndex() {
		return isIndex;
	}

	public void setIsIndex(int isIndex) {
		this.isIndex = isIndex;
	}

	public List<Map<Object, Object>> getPostList() {
		return postList;
	}

	public void setPostList(List<Map<Object, Object>> postList) {
		this.postList = postList;
	}

	public UserHeaderBean getUserHeaderBean() {
		return userHeaderBean;
	}

	public void setUserHeaderBean(UserHeaderBean userHeaderBean) {
		this.userHeaderBean = userHeaderBean;
	}

	public String getPostUserID() {
		return postUserID;
	}

	public void setPostUserID(String postUserID) {
		this.postUserID = postUserID;
	}

	public int getTradeid() {
		return tradeid;
	}

	public void setTradeid(int tradeid) {
		this.tradeid = tradeid;
	}

	public int getShareType() {
		return shareType;
	}

	public void setShareType(int shareType) {
		this.shareType = shareType;
	}

	public String getPostCoverResID() {
		return postCoverResID;
	}

	public void setPostCoverResID(String postCoverResID) {
		this.postCoverResID = postCoverResID;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public boolean getIsFromIos() {
		return isFromIos;
	}

	public void setIsFromIos(boolean isFromIos) {
		this.isFromIos = isFromIos;
	}
	public int getPostType() {
		return postType;
	}

	public void setPostType(int postType) {
		this.postType = postType;
	}
}
