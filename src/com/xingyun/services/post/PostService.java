package com.xingyun.services.post;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xingyun.bean.ItemBean;
import com.xingyun.bean.ResPicBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunPostConstant;
import com.xingyun.constant.XingyunSystemConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.recommend.RecommendService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.CounterUtil;
import com.xingyun.util.DateUtil;
import com.xingyun.util.JsonObjectUtil;
import com.xingyun.util.MessageUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.SpecialCharFilterUtil;
import com.xingyun.util.UploadPicUtil;

public class PostService {

	private static final DBOperate db = new DBOperate();
	private static final PostService postService = new PostService();
	private PostService(){}
	public static PostService getInstance() {
		return postService;
	}
	
	/**
	 * 添加新作品数据	
	 * @param userID					用户ID
	 * @param postType					作品类型
	 * @param zpTitle					作品标题
	 * @param displaytype				作品显示类型
	 * @param tradeid					作品所属行业
	 * @param classid					作品分组ID
	 * @param status					作品状态
	 * @param itemBeanList				作品明细记录
	 * @param zpTags					作品标签信息
	 * @param coverResPicBean			作品封面图片资源
	 * @param postItemResPicBeanList	作品明细图片资源
	 */
	public int addPostData(String userID, int postType, String zpTitle, int displaytype, int tradeid, int classid, List<ItemBean> itemBeanList, List<String> zpTags, ResPicBean coverResPicBean, List<ResPicBean> postItemResPicBeanList) throws Throwable{
		int postID = 0;
		try {
			List<Object> valueList = new ArrayList<Object>();
			valueList.add(userID);
			valueList.add(postType = XingyunPostConstant.POST_TYPE_VIDEO == postType ? XingyunPostConstant.POST_TYPE_VIDEO : XingyunPostConstant.POST_TYPE_PIC);
			valueList.add(zpTitle);
			valueList.add(coverResPicBean.getPicid());
			valueList.add(displaytype == XingyunPostConstant.ZP_DISPLAYTYPE_HD ? displaytype : XingyunPostConstant.ZP_DISPLAYTYPE_DEF);
			valueList.add(tradeid);
			valueList.add(classid);	
			valueList.add(0);	
			valueList.add(DateUtil.getSimpleDateFormat());		
			valueList.add(DateUtil.getSimpleDateFormat());
			postID = insertPostData(valueList);									//作品表插入数据 获取主键ID
			addPostCounter(postID, userID);										//作品点击量表插入数据
			updatePostItem(0, postID, userID, itemBeanList, true);				//更新作品明细信息		
			addZpTags(postID, zpTags, true);									//添加作品标签
			PublicQueryUtil.getInstance().addDynamicData(userID, postID, XingyunCommonConstant.DYNAMIC_TYPE_ZP_FB); //添加发布作品动态数据
			addPostResPicData(coverResPicBean, postItemResPicBeanList, true);	//添加作品图片资源
			return postID;
		} catch (Throwable e) {
			if(postID > 0){
				List<String> sqlList = new ArrayList<String>();
				sqlList.add("DELETE FROM post_counter WHERE postid = " + postID);
				sqlList.add("DELETE FROM post_item WHERE postid = " + postID);
				sqlList.add("DELETE FROM post_tag WHERE postid = " + postID);
				sqlList.add("DELETE FROM dynamic WHERE userid = '" + userID + "' AND type = " + XingyunCommonConstant.DYNAMIC_TYPE_ZP_FB + " AND topicid = '" + postID + "'");
				sqlList.add("DELETE FROM post WHERE id = " + postID + " AND userid = '" + userID + "'");
				db.batchExecute(sqlList, true);
			}
			throw new Throwable(e);
		}
	}
	
	/**
	 * 编辑作品数据
	 * @param userID					用户ID	
	 * @param postID					作品ID
	 * @param zpTitle					作品标题
	 * @param displaytype				作品显示类型
	 * @param tradeid					作品所属行业
	 * @param classid					作品分组ID
	 * @param status					作品状态
	 * @param itemBeanList				作品明细记录
	 * @param zpTags					作品标签信息
	 * @param coverResPicBean			作品封面图片资源
	 * @param postItemResPicBeanList	作品明细图片资源
	 */
	public void updatePostData(String userID, int postID, String zpTitle, int displaytype, int tradeid, int classid, List<ItemBean> itemBeanList, List<String> zpTags, ResPicBean coverResPicBean, List<ResPicBean> postItemResPicBeanList) throws Throwable{
		String sysTime = DateUtil.getSimpleDateFormat();
		displaytype = displaytype == XingyunPostConstant.ZP_DISPLAYTYPE_HD ? displaytype : XingyunPostConstant.ZP_DISPLAYTYPE_DEF;
		String updatePostSql = "UPDATE post SET title = '" + zpTitle + "', coverpath = '" + coverResPicBean.getPicid() + "', displaytype = " + displaytype + ", tradeid = " + tradeid + ", classid = " + classid + ", updatetime = '" + sysTime + "' WHERE id = " + postID + " AND userid = '" + userID + "'";
		
		String noDelItemIds = CommonUtil.getNotDelItemIds(itemBeanList);
		String delItemSql = "DELETE FROM post_item WHERE postid = " + postID;
		if(StringUtils.isNotBlank(noDelItemIds))
			delItemSql += " AND id NOT IN(" + noDelItemIds + ")";
		
		List<String> itemSqlList = updatePostItem(1, postID, userID, itemBeanList, false);	//更新作品明细信息		
		
		String delZpTagsSql = "DELETE FROM post_tag WHERE postid = " + postID;
		List<String> addTagsList = addZpTags(postID, zpTags, false);						//添加作品标签
		
		List<String> updatePostSqlList = new ArrayList<String>();
		updatePostSqlList.add(updatePostSql);				//修改作品主题信息
		if(StringUtils.isNotBlank(delItemSql))
			updatePostSqlList.add(delItemSql);				//清理删除的作品明细
		if(itemSqlList != null && itemSqlList.size() > 0)
			updatePostSqlList.addAll(itemSqlList);			//修改添加作品明细
		updatePostSqlList.add(delZpTagsSql);
		if(addTagsList != null && addTagsList.size() > 0)
			updatePostSqlList.addAll(addTagsList);			//修改添加作品标签
		List<String> resSqlList = addPostResPicData(coverResPicBean, postItemResPicBeanList, false);	//添加作品图片资源
		if(resSqlList.size() > 0)
			updatePostSqlList.addAll(resSqlList);			//添加作品图片资源
		db.batchExecute(updatePostSqlList, true);
	}
	
	/**
	 * 修改作品其它相关表数据
	 */
	public void updatePostCmsStatusInfo(int postId) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("UPDATE post SET status_cms = " + XingyunPostConstant.POST_OPTYPE_DEFAULT + ", isindex = "+XingyunPostConstant.ZP_INDEX_TYPE_NO+" WHERE id = " + postId);//个人主页显示
		sqlList.add("DELETE FROM cms_post_index WHERE postid = " + postId);
		sqlList.add("DELETE FROM cms_post_recommend WHERE postid = " + postId);//星作品
		sqlList.add("DELETE FROM cms_post_pindao WHERE postid = " + postId);//星作品频道
		sqlList.add("DELETE FROM cms_post_strong WHERE postid = " + postId);//强力推荐
		sqlList.add("DELETE FROM cms_post_status WHERE postid = " + postId);
		sqlList.add("DELETE FROM dynamic WHERE type IN("+XingyunCommonConstant.DYNAMIC_TYPE_ZP_FB+","+XingyunCommonConstant.DYNAMIC_TYPE_ZP_TJ+") AND topicid = " + postId);
		sqlList.add("DELETE FROM message_system WHERE type IN( "+XingyunCommonConstant.MESSAGE_SYSTEM_TYPE_POST_INDEX+","+XingyunCommonConstant.MESSAGE_SYSTEM_TYPE_POST+","+XingyunCommonConstant.MESSAGE_SYSTEM_TYPE_POST_PINDAO+") AND postid = " + postId);
		db.batchExecute(sqlList, true);
		RecommendService.getInstance().delRecommendPost(XingyunSystemConstant.getSystemConstantValue(XingyunSystemConstant.XINGYUN_UID), postId);//取消动态推荐
	}
	
	/**
	 * 处理作品资源图片
	 * @param coverResPicBean			封面图片资源
	 * @param postItemResPicBeanList	明细图片资源
	 * @param opType					操作类型 true 执行sql  false：不执行
	 */
	private List<String> addPostResPicData(ResPicBean coverResPicBean, List<ResPicBean> postItemResPicBeanList, boolean opType) throws Throwable{
		List<ResPicBean> resPicBeanList = new ArrayList<ResPicBean>();		
		resPicBeanList.add(coverResPicBean);
		if(postItemResPicBeanList != null)
			resPicBeanList.addAll(postItemResPicBeanList);
		
		List<String> sqlList = new ArrayList<String>();
		String resPicSql = "";
		for(ResPicBean resPic : resPicBeanList){
			resPicSql = CommonUtil.getInsertResPicSql(resPic);	
			if(StringUtils.isNotBlank(resPicSql))
				sqlList.add(resPicSql);
		}
		if(opType)
			db.batchExecute(sqlList);
		return sqlList;
	}
	/**
	 * 记录作品标签内容
	 */
	private List<String> addZpTags(int postID, List<String> zpTags, boolean opType) throws Throwable{
		if(zpTags == null || zpTags.size() == 0)
			return null;
		
		String sysTime = DateUtil.getSimpleDateFormat();
		List<String> sqlList = new ArrayList<String>();
		int count = 1;
		for(String tag : zpTags){
			tag = SpecialCharFilterUtil.replaceSpecialChar(tag);
			if(StringUtils.isBlank(tag))
				continue;

			tag = SpecialCharFilterUtil.filterEncodeAndForbidValue(tag, XingyunPostConstant.ZP_TAG_MAX_LENGTH);
			sqlList.add("INSERT INTO post_tag(postid, tagname, systime) VALUES(" + postID + ", '" + tag + "', '" + sysTime + "')");
			
			count++;
			if(count > XingyunPostConstant.ZP_TAG_MAX_COUNT)
				break;
		}
		if(opType)
			db.batchExecute(sqlList, true);
		return sqlList;
	}
	
	/**
	 * 添加作品表数据
	 */
	private int insertPostData(List<Object> valueList) throws Throwable{
		String sql = "INSERT INTO post(userid, posttype, title, coverpath, displaytype, tradeid, classid, isindex, updatetime, systime) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		return db.insertData(sql, valueList);
	}
	
	/**
	 * 插入作品点击量记录
	 */
	private void addPostCounter(int postId, String userId) throws Throwable{
		String sql = "INSERT INTO post_counter(postid,userid,systime) VALUES(?,?,?)";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postId);
		valueList.add(userId);
		valueList.add(new Date());
		db.insertData(sql, valueList);
	}
	
	/**
	 * 插入或更新作品明细数据
	 * @param tag	        标识符 0为插入 1为更新
	 * @param postID	    展示Id
	 * @param itemBeanList	展示明细集合
	 */
	private List<String> updatePostItem(int tag, int postID, String userId, List<ItemBean> itemBeanList, boolean opType) throws Throwable{
		if(itemBeanList == null || itemBeanList.size() <= 0)
			return null;
		String sysTime = DateUtil.getSimpleDateFormat();
		String insertSql = "INSERT INTO post_item(postid, type, c1, c2, c3, c4, c5, seq, systime) VALUES(%s, %s, '%s', '%s', '%s', '%s', '%s', %s, '" + sysTime + "')";
		String updateSql = "UPDATE post_item SET c1 = '%s', c2 = '%s', seq = %s, systime = '" + sysTime + "' WHERE id = %s AND postid = %s";	
		return CommonUtil.updateItemBeanList(tag, postID, itemBeanList, insertSql, updateSql, opType);
	}
	
	/**
	 * 查询用户作品分组信息
	 */
	public List<Map<Object, Object>> findPostClassByPostType(String userID, int postType) throws Throwable{
		List<Map<Object, Object>> list = findUserPostClassByPostType(userID, postType);
		if(list == null || list.size() == 0){
			addPostDefaultClass(userID, postType);
			list = findUserPostClassByPostType(userID, postType);
		}
		return list;
	}
	
	/**
	 * 查询用户作品分组数据
	 * 使用索引：index_post_class_userid_seq
	 */
	private List<Map<Object, Object>> findUserPostClassByPostType(String userID, int postType) throws Throwable{
		String sql = "SELECT id, userid, classname, seq, type FROM post_class WHERE userid = ? AND posttype = ? ORDER BY seq";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userID);
		valueList.add(postType);
		return db.retrieveSQL(sql,valueList);
	}
	
	/**
	 * 查询用户作品分组信息
	 */
	public List<Map<Object, Object>> findPostClass(String userID, int classID) throws Throwable{
		String sql = "SELECT id, userid, classname, seq, type FROM post_class WHERE id = ? AND userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(classID);
		valueList.add(userID);
		return db.retrieveSQL(sql,valueList);
	}
	
	/**
	 * 保存用户作品分组信息
	 */
	public void savePostClass(String userID, int postType, List<Integer> classIDs, List<String> classNames) throws Throwable{
		if(StringUtils.isBlank(userID) || classIDs == null || classIDs.size() <= 0 || classNames == null || classNames.size() <= 0)
			return ;
		
		int defClassID = findZpDefaultClassID(userID, postType);
		if(defClassID <= 0)
			throw new Throwable("user post_class default 0");
		
		String sysTime = DateUtil.getSimpleDateFormat();
		StringBuilder inID = new StringBuilder();
		List<String> batchList = new ArrayList<String>();
		int seq = 0;
		int index = 0;
		String defClassName = "";
		for (String className : classNames) {
			if(seq >= (XingyunPostConstant.ZP_CLASS_MAX_COUNT))
            	break;
		    className = SpecialCharFilterUtil.replaceSpecialChar(className, XingyunPostConstant.ZP_CLASS_NAME_LENGTH);
		    if(defClassID == classIDs.get(index))
		    	defClassName = className;
            if(StringUtils.isNotBlank(className)){
	            if(classIDs.get(index) > 0){
	                batchList.add("UPDATE post_class SET classname = '" + className + "', seq = " + seq + " WHERE userid = '" + userID + "' AND id = " + classIDs.get(index));
	                inID.append(classIDs.get(index)).append(",");
	            }else{
	                batchList.add("INSERT INTO post_class(userid, classname, seq, type, posttype, systime) VALUES('" + userID + "', '" + className + "', " + seq + ", " + XingyunPostConstant.ZP_CLASS_TYPE_USER + ", " + postType + ",'" + sysTime + "')");
	            }
	            seq++;
            }
            index++;
        }
		if(inID.length() > 0)
			inID.deleteCharAt(inID.length() - 1);
		String delClassSql = "DELETE FROM post_class WHERE userid = '" + userID + "' AND posttype = " + postType + " AND id NOT IN(" + inID.toString() + ") AND type != " + XingyunPostConstant.ZP_CLASS_TYPE_DEF;
		batchList.add(0, delClassSql);
		defClassName = StringUtils.isBlank(defClassName) ? XingyunPostConstant.ZP_CLASS_DEF_NAME : defClassName;
		batchList.add("UPDATE post_class SET classname = '" + defClassName + "' WHERE userid = '" + userID + "' AND id = " + defClassID);
		batchList.add("UPDATE post SET classid = " + defClassID + ", updateTime = '" + sysTime + "' WHERE userid = '" + userID + "' AND posttype = " + postType + " AND classid NOT IN(" + inID.toString() + ")");
		db.batchExecute(batchList, true);
	}
	
	/**
	 * 查询用户系统默认分组ID
	 * 使用索引：index_post_class_userid_seq
	 */
	private int findZpDefaultClassID(String userID, int postType) throws Throwable{
		String sql = "SELECT id FROM post_class WHERE userid = ? AND type = ? AND posttype = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunPostConstant.ZP_CLASS_TYPE_DEF);
		vList.add(postType);
		int id = CommonUtil.getIntValue(sql, vList, "id");
		if(id == 0)
			id = addPostDefaultClass(userID, postType);
		return id; 
	}
	
	/**
	 * 添加作品默认分组数据
	 */
	private int addPostDefaultClass(String userID, int postType) throws Throwable{
		String sql = "INSERT INTO post_class(userid, classname, seq, type, posttype, systime) VALUES(?, ? , ? , ?, ?, ?);";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunPostConstant.ZP_CLASS_DEF_NAME);
		vList.add(0);
		vList.add(XingyunPostConstant.ZP_CLASS_TYPE_DEF);
		vList.add(postType);
		vList.add(new Date());
		return db.insertData(sql, vList);
	}
	
	/**
	 * 整理用户作品分组信息 格式：json
	 */
	public String getPostClassJson(String userID, int postType) throws Throwable{		
		List<Map<Object, Object>> classList = findPostClassByPostType(userID, postType);
		return JsonObjectUtil.getJsonStr(classList);
	}
	
	/**
	 * 检查作品是否可以查看
	 */
	public boolean checkLookPost(Map<Object, Object> postInfo, String lookState) throws Throwable{
		if(XingyunCommonConstant.USER_EDIT.equals(lookState))
			return true;
		
		if(Integer.parseInt(postInfo.get("isdel").toString()) == XingyunPostConstant.ZP_DEL_TYPE_YES)
			return false;
		
		if(Integer.parseInt(postInfo.get("status").toString()) == XingyunPostConstant.ZP_STATUS_TYPE_FB)
			return true;
		
		return false;
	}
	
	/**
	 * 跟据作品ID 查找作品信息
	 */
	public Map<Object, Object> findPostDetailMap(int postID) throws Throwable{
		Map<Object,Object> postInfoMap = findPostContentByID(postID);
		if(postInfoMap != null){
			//作品封面图片
			postInfoMap.put("coverPic", UploadPicUtil.getPicWebUrl(postInfoMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_190));
			postInfoMap.put("coverPic_150", UploadPicUtil.getPicWebUrl(postInfoMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_150));
			postInfoMap.put("coverPic_250", UploadPicUtil.getPicWebUrl(postInfoMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_250));
			postInfoMap.put("zpViewcount", getPostViewCount(postID));				//作品访问量
			postInfoMap.put("zpRecommendCount", RecommendService.getInstance().getPostRecommendCount(postID));				//作品被推荐数量
			postInfoMap.put("zpCollectionCount", getPostCollectionCount(postID));											//作品被收藏数量
			postInfoMap.put("zpClassName", getPostClassName(Integer.parseInt(postInfoMap.get("classid").toString())));		//作品分组信息
			postInfoMap.put("zpVocationName", getVocationName(Integer.parseInt(postInfoMap.get("tradeid").toString())));	//作品所属行业
			postInfoMap.put("updatetime", DateUtil.getSimpleDateFormatHM((Date)postInfoMap.get("updatetime") ) );
			postInfoMap.put("isNew", CommonUtil.checkPostIsNew((Date)postInfoMap.get("systime") ) );
		}
		if(CommonUtil.getIntValue(postInfoMap.get("status_cms")) == XingyunPostConstant.POST_OPTYPE_RECOMMEND){
			Map<Object,Object> cmsStatusMap = getPostCmsStatus(postID);
			if(cmsStatusMap != null)
				postInfoMap.putAll(cmsStatusMap);
		}
		return postInfoMap;
	}
	
	/**
	 * 根据postID查找 作品信息
	 */
	public Map<Object, Object> findPostContentByID(int postID, String userID) throws Throwable{
		String sql = "SELECT id, userid, posttype, title, coverpath, displaytype, tradeid, classid, status, isindex, updatetime, systime FROM post WHERE id = ? AND userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postID);
		valueList.add(userID);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, valueList);
		return list.size() == 0 ? null : list.get(0);
	}
	
	/**
	 * 根据postID查找 作品信息
	 */
	public Map<Object, Object> findPostContentByID(int postID) throws Throwable{
		String sql = "SELECT id, userid, posttype, title, coverpath, displaytype, tradeid, classid, status, isindex, isdel, updatetime, systime, status_cms FROM post WHERE id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postID);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, valueList);
		return list.size() == 0 ? null : list.get(0);
	}
	
	/**
	 * 根据作品ID整理编辑作品详情数据
	 */
	public List<Map<Object, Object>> getPostItemData(int postID) throws Throwable{
		List<Map<Object, Object>> list = findPostItemData(postID);
		if(list == null || list.size() == 0)
			return null;
			
		for(Map<Object, Object> map : list){
			String valueStr = map.get("c1").toString();
			if(XingyunPostConstant.XINGYUN_MODUAL_PIC == Integer.parseInt(map.get("type").toString())){
				map.put("picSrc", UploadPicUtil.getPicWebUrl(valueStr, 0));
				map.put("picMid", UploadPicUtil.getPicWebUrl(valueStr, XingyunUploadFileConstant.POST_ITEM_WIDTH_1010));
				map.put("picMid_640", UploadPicUtil.getPicWebUrl(valueStr, XingyunUploadFileConstant.POST_ITEM_WIDTH_640));
				map.put("picMidHeight", UploadPicUtil.getPicHeight(valueStr, XingyunUploadFileConstant.POST_ITEM_WIDTH_1010));
				map.put("picThumb", UploadPicUtil.getPicWebUrl(valueStr, XingyunUploadFileConstant.POST_ITEM_WIDTH_150));	
				map.put("showBigPic", UploadPicUtil.checkShowBigPic(valueStr, XingyunUploadFileConstant.POST_ITEM_WIDTH_1010));
				map.put("picName", valueStr);
			} 
		}
		return list;
	}
	
	/**
	* 查询水平滚动展示数据（按时间排序，图片封面） 所有数据
	*/
	public List<Map<Object, Object>> findZuoYouPostList(String userID, int postType, String lookState, int curPage) throws Throwable{
		List<Map<Object,Object>> list = getZuoYouPostIdList(userID, postType, lookState, curPage);
		if(list == null || list.size() == 0)
			return null;
		
		int postID = 0;
		Map<Object,Object> postMap = null;
		List<Map<Object,Object>> zuoYouPostList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> map : list){
			postID = Integer.parseInt(map.get("id").toString());
			postMap = getZuoYouPostInfoById(postID);
			if(postMap == null)
				continue;
			postMap.put("coverPic", UploadPicUtil.getPicWebUrl(postMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_190));
			postMap.put("zpViewcount", getPostViewCount(postID));				//作品访问量
			postMap.put("zpRecommendCount", RecommendService.getInstance().getPostRecommendCount(postID));		//作品被推荐数量	
			postMap.put("isNew", CommonUtil.checkPostIsNew((Date)postMap.get("systime")));						//作品是否为最新
			postMap.put("userHref", CommonUtil.getUserIndexHref(CommonUtil.getStringValue(postMap.get("userid")), postMap.get("wkey")));
			zuoYouPostList.add(postMap);
		}
		return zuoYouPostList;
	}
	
	
	/**
	 * 根据用户ID 查询最新作品
	 */
	public List<Map<Object, Object>> findUserNewPostList(String userID, int pageSize) throws Throwable{
		List<Map<Object, Object>> list = findPostList(userID, pageSize);
		for(Map<Object,Object> map : list){
			map.put("coverPic", UploadPicUtil.getPicWebUrl(map.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_220));
			map.put("userHref", CommonUtil.getUserIndexHref(CommonUtil.getStringValue(map.get("userid")), map.get("wkey")));
		}
		return list;
	}
	
	/**
	 * 根据用户ID 查询最新作品数据
	 */
	private List<Map<Object, Object>> findPostList(String userID, int pageSize) throws Throwable{
		String sql = "SELECT p.id, p.posttype, p.title, p.coverpath, p.userid, u.wkey FROM post p, user u WHERE p.userid = u.userid AND p.userid = ? AND p.isdel = ? AND p.status = ? ORDER BY p.id DESC LIMIT ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userID);
		valueList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		valueList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		valueList.add(pageSize);
		return db.retrieveSQL(sql, valueList);
	}
	
	/**
	 * 获取用户所有展示ID集合
	 * 使用索引：index_post_userid
	 */
	private List<Map<Object,Object>> getZuoYouPostIdList(String userID, int postType, String lookState, int curPage) throws Throwable{
		String sql = "SELECT id FROM post WHERE userid = ? AND posttype = ? AND isdel = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(postType);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		if(!XingyunCommonConstant.USER_EDIT.equals(lookState)){
			sql += " AND status = ?";				//如果不是自己查看，则只显示发布的作品
			vList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		}
		List<Map<Object,Object>> list = db.retrieveSQL(sql, vList);
		if(list.size() == 0)
			return null;
		
		CommonUtil.compositor(list, new String[]{"id"}, 1);		//按发布时间 倒序排序
		return CommonUtil.subList(list, curPage, XingyunPostConstant.POST_ZUOYOU_SHOW_COUNT);
	}
	
	/**
	 * 根据当前用户的访问状态，查询用户作品总数
	 * 使用索引：index_post_userid
	 */
	public int getUserTotalPost(String userID, int postType, String lookState) throws Throwable{
		String sql = "SELECT COUNT(*) FROM post WHERE userid = ? AND isdel = ? AND posttype = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);		
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);	
		vList.add(postType);	
		if(!XingyunCommonConstant.USER_EDIT.equals(lookState)){
			sql += " AND status = ?";
			vList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);	//如果不是自己查看，则只显示发布的作品
		}
		return db.getRecordCountSQL(sql, vList);
	}
	
	/**
	 * 根据作品ID获取左右展示信息
	 */
	private Map<Object,Object> getZuoYouPostInfoById(int postID) throws Throwable{
		String sql = "SELECT p.id, p.posttype, p.title, p.coverpath, p.systime, p.userid, u.wkey FROM post p, user u WHERE p.userid = u.userid AND p.id = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postID);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	
	/**
	 * 根据作品ID 查询展示明细信息
	 * 使用索引：index_post_item_postid
	 */
	private List<Map<Object, Object>> findPostItemData(int postID) throws Throwable{
		String sql = "SELECT id, postid, type, c1, c2, c3, c4, c5, seq FROM post_item WHERE postid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postID);
		List<Map<Object, Object>> list =  db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		CommonUtil.compositor(list, new String[]{"seq"}, 0);
		return list;
	}
	
	/**
	 * 根据作品ID 查询展示明细信息
	 * 使用索引：index_post_tag_postid
	 */
	public List<String> findZpTags(int postID) throws Throwable{
		String sql = "SELECT id, tagname FROM post_tag WHERE postid = ? ORDER BY id";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postID);
		List<Map<Object, Object>> list =  db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		
		List<String> tagList = new ArrayList<String>();
		for(Map<Object, Object> map : list)
			tagList.add(map.get("tagname").toString());
		return tagList;
	}
	
	/**
	 * 检测但前用户是否可以对该作品进行操作
	 */
	public boolean checkUpdatePostUser(int postID, String userID) throws Throwable{
		String sql = "SELECT COUNT(*) FROM post WHERE id = ? AND userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postID);
		valueList.add(userID);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
	
	/**
	 * 根据作品ID 获取作品访问量
	 * 使用索引：index_post_counter_postid
	 */
	public int getPostViewCount(int postID) throws Throwable{
		String sql = "SELECT viewcount FROM post_counter WHERE postid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(postID);
		int viewCount = CommonUtil.getIntValue(sql, vList, "viewcount");
		int memCount = CounterUtil.getPostViewCount(postID);
		return viewCount + memCount;
	}
	
	/**
	 * 根据作品ID 获取作品被收藏数量
	 * 使用索引：index_post_counter_postid
	 */
	public int getPostCollectionCount(int postID) throws Throwable{
		String sql = "SELECT collectioncount FROM post_counter WHERE postid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(postID);
		return CommonUtil.getIntValue(sql, vList, "collectioncount");
	}
	
	/**
	 * 查询作品分组名
	 */
	public String getPostClassName(int classID) throws Throwable{
		String sql = "SELECT classname FROM post_class WHERE id = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(classID);
		String className = CommonUtil.getStringValue(sql, vList, "classname");
		if(StringUtils.isBlank(className))
			return XingyunPostConstant.ZP_CLASS_DEF_NAME;
		return className;
	}
	
	/**
	 * 查询作品分组名
	 */
	private String getVocationName(int tradeId) throws Throwable{
		String sql = "SELECT name FROM dic_trade WHERE id = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(tradeId);
		String name = CommonUtil.getStringValue(sql, vList, "name");
		if(StringUtils.isBlank(name))
			return "其他";
		return name;
	}
	
	/**
	 * 修改作品删除状态
	 * @param postID 	作品ID
	 * @param userID	用户ID
	 * @param delStatus	作品状态
	 */
	public void updatePostDelStatus(int postID, String userID, int delStatus) throws Throwable{
		String sql = "UPDATE post SET isdel = "+delStatus+", isindex = "+XingyunPostConstant.ZP_INDEX_TYPE_NO+" WHERE id = "+postID+" AND userid = " + userID;
		db.updateData(sql);
	}
	
	/**
	 * 修改作品是否首页显示状态
	 * @param postID		作品ID
	 * @param userID		用户ID
	 * @param indexStatus	作品首页是否显示状态
	 * @param zpStatus		作品状态
	 */
	public void updatePostStatus(int postID, String userID, int indexStatus, int zpStatus) throws Throwable{
		int isIndex = indexStatus == XingyunPostConstant.ZP_INDEX_TYPE_YES ? XingyunPostConstant.ZP_INDEX_TYPE_YES : XingyunPostConstant.ZP_INDEX_TYPE_NO;
		String sql = "UPDATE post SET status = " + zpStatus + ", isindex = " + isIndex + " WHERE id = " + postID + " AND userid = " + userID;
		db.updateData(sql);
	}
	
	/**
	 * 查询用户作品列表
	 */
	public List<Map<Object, Object>> findUserPostList(String postUserID, String lookState, int postType) throws Throwable{
		List<Map<Object, Object>> postList = findPostListData(postUserID, postType, 0, lookState);
		if(postList == null || postList.size() == 0)
			return null;
		List<Map<Object, Object>> zpClassList = findPostClassByPostType(postUserID, postType);
		return setPostListData(zpClassList, postList);
	}
	
	/**
	 * 查询用户分组作品列表
	 */
	public List<Map<Object, Object>> findUserPostClassList(String postUserID, int postType, int zpClassID, String lookState) throws Throwable{
		List<Map<Object, Object>> postList = findPostListData(postUserID, postType, zpClassID, lookState);
		if(postList == null || postList.size() == 0)
			return null;
		List<Map<Object, Object>> zpClassList = findPostClass(postUserID, zpClassID);
		return setPostListData(zpClassList, postList);
	}
	
	private List<Map<Object, Object>> setPostListData(List<Map<Object, Object>> zpClassList, List<Map<Object, Object>> postList) throws Throwable{
		List<Map<Object, Object>> zpList = new ArrayList<Map<Object,Object>>();
		for(Map<Object, Object> map : zpClassList){
			int classID = Integer.parseInt(map.get("id").toString());
			Map<Object, Object> zpMap = new HashMap<Object, Object>();
			zpMap.put("classid", classID);
			zpMap.put("className", map.get("classname").toString());
			List<Map<Object, Object>> classZpList = new ArrayList<Map<Object,Object>>();
			for(Map<Object, Object> postMap : postList){
				if(classID == Integer.parseInt(postMap.get("classid").toString())){
					int postID = Integer.parseInt(postMap.get("id").toString());
					Map<Object, Object> itemMap = new HashMap<Object, Object>();
					itemMap.put("postID", postID);
					itemMap.put("title", postMap.get("title"));
					itemMap.put("status", postMap.get("status"));
					itemMap.put("status_cms", postMap.get("status_cms"));
					itemMap.put("isindex", postMap.get("isindex"));
					itemMap.put("coverPic", UploadPicUtil.getPicWebUrl(postMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_250));
					itemMap.put("zpViewcount", getPostViewCount(postID));
					itemMap.put("zpRecommendCount", RecommendService.getInstance().getPostRecommendCount(postID));
					itemMap.put("isNew", CommonUtil.checkPostIsNew((Date)postMap.get("systime")));
					classZpList.add(itemMap);
				}
			}
			zpMap.put("zpList", classZpList);
			zpList.add(zpMap);
		}
		return zpList;
	}
	
	/**
	 * 查询用户回收站作品列表
	 */
	public List<Map<Object, Object>> findUsreHuiShouPostList(String userID, int postType) throws Throwable{
		List<Map<Object, Object>> postList = findHuiShouPostListData(userID, postType);
		if(postList == null || postList.size() == 0)
			return null;

		for(Map<Object, Object> postMap : postList){
			int postID = Integer.parseInt(postMap.get("id").toString());
			postMap.put("coverPic", UploadPicUtil.getPicWebUrl(postMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_200));
			postMap.put("zpViewcount", getPostViewCount(postID));
			postMap.put("zpRecommendCount", RecommendService.getInstance().getPostRecommendCount(postID));
		}
		return postList;
	}
	
	/**
	 * 查询用户作品列表
	 * 使用索引：index_post_userid
	 */
	private List<Map<Object, Object>> findPostListData(String userID, int postType, int classID, String lookState) throws Throwable{
		String sql = "SELECT id, title, coverpath, classid, status, status_cms, isindex, systime FROM post WHERE userid = ? AND isdel = ? AND posttype = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		vList.add(postType);
		if(classID > 0){
			sql += " AND classid = ? ";
			vList.add(classID);
		}
		StringBuilder sb = new StringBuilder();
		//整理可以查看的作品类型
		if(XingyunCommonConstant.USER_EDIT.equals(lookState)){
			sb.append(XingyunPostConstant.ZP_STATUS_TYPE_FB).append(",").append(XingyunPostConstant.ZP_STATUS_TYPE_YC);
		}else{
			sb.append(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		}
		sql += " AND status IN (" + sb.toString() + ")";				
		
		List<Map<Object,Object>> list = db.retrieveSQL(sql, vList);
		if(list.size() == 0)
			return null;
		
		CommonUtil.compositor(list, new String[]{"id"}, 1);		//按发布时间 倒序排序
		return list;
	}
	
	/**
	 * 查询用户回收站作品列表
	 * 使用索引：index_post_userid
	 */
	private List<Map<Object, Object>> findHuiShouPostListData(String userID, int postType) throws Throwable{
		String sql = "SELECT id, posttype, title, coverpath FROM post WHERE userid = ? AND isdel = ? AND posttype = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_YES);		
		vList.add(postType);		
		List<Map<Object,Object>> list = db.retrieveSQL(sql, vList);
		if(list.size() == 0)
			return null;
		CommonUtil.compositor(list, new String[]{"id"}, 1);		//按发布时间 倒序排序
		return list;
	}
	
	/**
	 * 根据作品ID 查询作品信息
	 */
	public Map<Object, Object> findPostMap(int postID, String userID) throws Throwable{
		String sql = "SELECT userid, status, isdel FROM post WHERE id = ? AND userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(postID);
		vList.add(userID);
		List<Map<Object, Object>> list = db.retrieveSQL(sql, vList);
		return list.size() == 0 ? null : list.get(0);
	}
	
	/**
	 * 查询用户回收站作品数量
	 */
	public int findPostDelCount(String userID, int postType) throws Throwable{
		String sql = "SELECT COUNT(*) AS count FROM post WHERE userid = ? AND isdel = ? AND posttype = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_YES);
		vList.add(postType);
		return CommonUtil.getIntValue(sql, vList, "count");
	}
	
	/**
	 * 作品删除
	 * @param postID 	作品ID
	 * @param userID	用户ID
	 */
	public void delPostInfo(int postID, String userID) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("DELETE FROM post WHERE id = " + postID + " AND userid = '" + userID+"'");
		sqlList.add("DELETE FROM post_item WHERE postid = " + postID);
		sqlList.add("DELETE FROM post_tag WHERE postid = " + postID);
		sqlList.add("DELETE FROM post_counter WHERE postid = " + postID);
		sqlList.add("DELETE FROM comment WHERE type = " + XingyunCommonConstant.COMMENT_SOURCE_POST + " AND topicid = " + postID);
		sqlList.add("DELETE FROM cms_post_index WHERE postid = " + postID);
		sqlList.add("DELETE FROM cms_post_recommend WHERE postid = " + postID);
		sqlList.add("DELETE FROM cms_post_pindao WHERE postid = " + postID);
		sqlList.add("DELETE FROM cms_post_strong WHERE postid = " + postID);
		sqlList.add("DELETE FROM cms_post_status WHERE postid = " + postID);
		sqlList.add("DELETE FROM recommend WHERE type = " + XingyunCommonConstant.RECOMMEND_TYPE_POST + " AND topicid = " + postID);
		sqlList.add("DELETE FROM dynamic WHERE type IN("+XingyunCommonConstant.DYNAMIC_TYPE_ZP_FB+","+XingyunCommonConstant.DYNAMIC_TYPE_ZP_TJ+") AND topicid = " + postID);
		sqlList.add("DELETE FROM message_system WHERE type IN( "+XingyunCommonConstant.MESSAGE_SYSTEM_TYPE_POST_INDEX+","+XingyunCommonConstant.MESSAGE_SYSTEM_TYPE_POST+","+XingyunCommonConstant.MESSAGE_SYSTEM_TYPE_POST_PINDAO+") AND postid = " + postID);
		db.batchExecute(sqlList, true);
	}
	/**
	 * 获取上一个，下一个展示列表
	 */
	public List<Map<Object,Object>> getPreOrNextPostIdList(String userID, int postType, String lookState) throws Throwable{
		String sql = "SELECT id, classid FROM post WHERE userid = ? AND isdel = ? AND posttype = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		vList.add(postType);
		StringBuilder sb = new StringBuilder();
		//整理可以查看的作品类型
		if(XingyunCommonConstant.USER_EDIT.equals(lookState))
			sb.append(XingyunPostConstant.ZP_STATUS_TYPE_FB).append(",").append(XingyunPostConstant.ZP_STATUS_TYPE_YC);
		else
			sb.append(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		sql += " AND status IN (" + sb.toString() + ")";				
		List<Map<Object,Object>> list = db.retrieveSQL(sql, vList);
		if(list.size() == 0)
			return null;
		CommonUtil.compositor(list, new String[]{"id"}, 1);		//按发布时间 倒序排序
		List<Map<Object, Object>> zpClassList = findPostClassByPostType(userID, postType);
		List<Map<Object, Object>> zpList = new ArrayList<Map<Object,Object>>();
		int classID = 0;
		Map<Object, Object> itemMap = null;
		for(Map<Object, Object> map : zpClassList){
			classID = Integer.parseInt(map.get("id").toString());
			for(Map<Object, Object> postMap : list){
				if(classID != Integer.parseInt(postMap.get("classid").toString()))
					continue;
				itemMap = new HashMap<Object, Object>();
				itemMap.put("postID", postMap.get("id"));
				zpList.add(itemMap);
			}
		}
		return zpList;
	}
	/**
	 * 根据id查找集合中的上一个及下一个
	 */
	public int[] findNextPreviousID(int id, List<Map<Object, Object>> list, String columnName){
		int[] ids = {0, 0};
		if(list != null && list.size() > 0){
			int tempID;
			int index = 0;			
			for(Map<Object, Object> map : list){
				tempID = Integer.parseInt(map.get(columnName).toString());
				if(id == tempID)
					break;
				index++;
			}
			Map<Object, Object> tempMap = null;
			int nextID = 0;
			int previousID = 0;
			int size = list.size();
			if(index == 0){
				if(size > 1){
					tempMap = list.get(index + 1);
					nextID = Integer.parseInt(tempMap.get(columnName).toString());
				}
			}else if((index + 1 == size) || (index == size)){
				tempMap = list.get(index - 1);
				previousID = Integer.parseInt(tempMap.get(columnName).toString());
			}else{
				tempMap = list.get(index - 1);
				previousID = Integer.parseInt(tempMap.get(columnName).toString());
				tempMap = list.get(index + 1);
				nextID = Integer.parseInt(tempMap.get(columnName).toString());
			}
			ids[0] = previousID;
			ids[1] = nextID;
		}
		return ids;
	}
	
	private Map<Object,Object> getPostCmsStatus(int postId) throws Throwable{
		String sql = "SELECT status_index, status_works, status_pindao FROM cms_post_status WHERE postid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(postId);
		List<Map<Object,Object>> list = db.retrieveSQL(sql, valueList);
		if(list.size() == 0)
			return null;
		return list.get(0);
	}
	
	/**
	 * 星作品免检操作
	 */
	public void recommendPostToWorks(int postId, String userId, int tradeId) throws Throwable{
		if(!checkUserIsMianjian(userId))
			return;
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("INSERT INTO cms_post_recommend(postid,userid,tradeid,systime) VALUES("+postId+",'"+userId+"',"+tradeId+",NOW())");
		sqlList.add("INSERT INTO cms_post_pindao(postid,userid,tradeid,systime) VALUES("+postId+",'"+userId+"',"+tradeId+",NOW())");
		sqlList.add("INSERT INTO cms_post_status(postid, userid, status_works, status_pindao, tradeid, publishtime, systime) VALUES("+postId+", '"+userId+"', 1, 1, "+tradeId+", NOW(), NOW())");
		sqlList.add("UPDATE post SET status_cms = "+XingyunPostConstant.POST_OPTYPE_RECOMMEND+" WHERE id = " + postId);
		sqlList.add("INSERT INTO message_system(type,userid,postid,systime) VALUES("+XingyunCommonConstant.MESSAGE_SYSTEM_TYPE_POST+",'"+userId+"',"+postId+",NOW())");
		db.batchExecute(sqlList, true);
		MessageUtil.addNewMessage(userId,"noticecount");
	}
	/**
	 * 检测是否为星作品免检用户
	 */
	private boolean checkUserIsMianjian(String userId) throws Throwable{
		String sql = "SELECT COUNT(*) FROM cms_user_mianjian WHERE userid = ?";
		List<Object> valueList = new ArrayList<Object>();
		valueList.add(userId);
		return db.getRecordCountSQL(sql, valueList) > 0;
	}
	
	/**
	 * 检查用户是否可以收藏该作品
	 * @param fromUser		当前用户
	 * @param postID		作品ID
	 * @param postUserID	作品作者ID
	 */
	public String checkCollectionPost(String fromUserId, int postID, String postUserID) throws Throwable{
		Map<Object, Object> postMap = PostService.getInstance().findPostMap(postID, postUserID);
		if(postMap == null)
			return "notPost";
		
		if(fromUserId.equals(postMap.get("userid").toString()))
			return "postUser";
		
		if(Integer.parseInt(postMap.get("isdel").toString()) == XingyunPostConstant.ZP_DEL_TYPE_YES)
			return "notPost";
		
		if(Integer.parseInt(postMap.get("status").toString()) != XingyunPostConstant.ZP_STATUS_TYPE_FB)
			return "notPost";
		
		return checkCollectionPost(postID, fromUserId) ? "" : "collectionPostOK";
	}
	
	/**
	 * 检查用户是否可以收藏作品 
	 */
	public boolean checkCollectionPost(int postID, String userID) throws Throwable{
		String sql = "SELECT COUNT(*) FROM post_collection WHERE postid = ? AND userid = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(postID);
		vList.add(userID);
		return db.getRecordCountSQL(sql, vList) == 0;
	}
	
	/**
	 * 添加收藏该作品
	 * @param fromUser		当前用户
	 * @param postID		作品ID
	 */
	public void addCollectionPost(int postID, String fromUserId) throws Throwable{
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("INSERT INTO post_collection(postid, userid, systime) VALUES(" + postID + ", '" + fromUserId + "', NOW())");
		sqlList.add("UPDATE post_counter SET collectioncount = collectioncount + 1 WHERE postid = " + postID);
		db.batchExecute(sqlList, true);
	}
	
	/**
	 * 删除收藏作品
	 */
	public void delCollectionPost(int postID, String fromUserId) throws Throwable{
		int postCollectionCount = getPostCollectionCount(postID) - 1;
		postCollectionCount = postCollectionCount < 0 ? 0 : postCollectionCount;
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("DELETE FROM post_collection WHERE postid = " + postID + " AND userid = '" + fromUserId + "'");
		sqlList.add("UPDATE post_counter SET collectioncount = " + postCollectionCount + " WHERE postid = " + postID);
		db.batchExecute(sqlList, true);
	}
	
	/**
	 * 查询收藏作品总数
	 */
	public int getCollectionPostCount(String userID) throws Throwable{
		String sql = "SELECT COUNT(*) FROM post_collection c, post p WHERE c.postid = p.id AND c.userid = ? AND p.status = ? AND p.isdel = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		return db.getRecordCountSQL(sql, vList);
	}
	
	/**
	 * 收藏作品显示数据
	 */
	public List<Map<Object, Object>> findCollectionPostList(String userID, int pageIndex, int pageSize) throws Throwable{
		List<Map<Object, Object>> postList = findCollectionPostIndexList(userID, pageIndex, pageSize);
		if(postList.size() == 0)
			return null;
		
		String postUserID = "";
		int postId = 0;
		Map<Object, Object> userMap = null;
		for(Map<Object, Object> postMap : postList){
			postUserID = postMap.get("userid").toString();
			postId = Integer.parseInt(postMap.get("id").toString() );
			userMap = PublicQueryUtil.getInstance().findUserCommonMap(postUserID);
			if(userMap == null)
				continue;
			
			postMap.put("coverPic", UploadPicUtil.getPicWebUrl(postMap.get("coverpath").toString(), XingyunUploadFileConstant.POST_COVER_WIDTH_165));
			postMap.put("nickName", userMap.get("nickname").toString());
			postMap.put("userHref", userMap.get("userHref").toString());
			postMap.put("level", Integer.parseInt(userMap.get("lid").toString()));
			postMap.put("verified", userMap.get("verified"));
			postMap.put("zpViewcount", PostService.getInstance().getPostViewCount(postId) );
			postMap.put("zpRecommendCount", RecommendService.getInstance().getPostRecommendCount(postId) );
		}
		return postList;
	}
	
	/**
	 * 收藏作品索引数据
	 */
	private List<Map<Object, Object>> findCollectionPostIndexList(String userID, int pageIndex, int pageSize) throws Throwable{
		String sql = "SELECT p.id, p.userid, p.posttype, p.title, p.coverpath FROM post_collection c, post p WHERE c.postid = p.id AND c.userid = ? AND p.status = ? AND p.isdel = ? ORDER BY c.id DESC LIMIT ?, ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		vList.add((pageIndex - 1) * pageSize );
		vList.add(pageSize);
		return db.retrieveSQL(sql, vList);
	}
	
	/**
	 * 查询用户作品数量
	 * 使用索引：index_post_userid
	 */
	public int findUserPostCountByType(String userID, String lookState, int postType) throws Throwable{
		String sql = "SELECT COUNT(*) FROM post WHERE userid = ? AND isdel = ? AND posttype = ?";
		List<Object> vList = new ArrayList<Object>();
		vList.add(userID);
		vList.add(XingyunPostConstant.ZP_DEL_TYPE_NO);
		vList.add(postType);
		StringBuilder sb = new StringBuilder();
		//整理可以查看的作品类型
		if(XingyunCommonConstant.USER_EDIT.equals(lookState)){
			sb.append(XingyunPostConstant.ZP_STATUS_TYPE_FB).append(",").append(XingyunPostConstant.ZP_STATUS_TYPE_YC);
		}else{
			sb.append(XingyunPostConstant.ZP_STATUS_TYPE_FB);
		}
		sql += " AND status IN (" + sb.toString() + ")";				
		return db.getRecordCountSQL(sql, vList);
	}
}
