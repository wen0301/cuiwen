package com.xingyun.services.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.constant.XingyunFaceConstant;
import com.xingyun.constant.XingyunSearchConstant;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.db.DBOperate;
import com.xingyun.services.follow.FollowService;
import com.xingyun.services.header.UserHeaderService;
import com.xingyun.util.CommonUtil;
import com.xingyun.util.JsonObjectUtil;
import com.xingyun.util.PublicQueryUtil;
import com.xingyun.util.UploadPicUtil;

public class SearchService {

	private static final DBOperate db = new DBOperate();
	private static final SearchService searchService = new SearchService();
	private SearchService(){}
	public static SearchService getInstance() {
		return searchService;
	}
	
	/**
	 * 查询搜索用户索引数据 
	 */
	public List<Map<Object, Object>> getSearchUserNavIndex(String searchContent, int searchFilterType) throws Throwable{
		searchContent = replaceSearchContent(searchContent);	//替换搜索内容特殊字符
		if(StringUtils.isBlank(searchContent))
			return null;
		
		boolean isChinese = CommonUtil.isChinese(searchContent);
		String sql = "SELECT u.userid, u.lid, u.verified, uc.recommendcount FROM user u, user_counter uc WHERE uc.userid = u.userid AND u.nickname LIKE '%" + searchContent + "%'";
		if(!isChinese)
			sql = "SELECT u.userid, u.lid, u.verified, uc.recommendcount FROM user u, user_counter uc WHERE uc.userid = u.userid AND u.pinyinname LIKE '%" + searchContent + "%'";
		
		List<Map<Object, Object>> indexList = db.retrieveSQL(sql);
		indexList = CommonUtil.setList(indexList);
		String[] seqValue = {"lid","verified","recommendcount"};
		CommonUtil.compositor(indexList, seqValue, 1);
		if(indexList.size() > XingyunSearchConstant.SEARCH_LIST_MAXSIZE)
			indexList = indexList.subList(0, XingyunSearchConstant.SEARCH_LIST_MAXSIZE);
		
		if(XingyunSearchConstant.SEARCH_FILTER_TYPE_MX == searchFilterType || XingyunSearchConstant.SEARCH_FILTER_TYPE_JY == searchFilterType){
			List<Map<Object, Object>> filterIndexList = new ArrayList<Map<Object,Object>>();
			for(Map<Object, Object> map : indexList){
				if(XingyunSearchConstant.SEARCH_FILTER_TYPE_MX == searchFilterType && XingyunCommonConstant.USER_LEVEL_MINGXING == Integer.parseInt(map.get("lid").toString())){
					filterIndexList.add(map);
				}else if(XingyunSearchConstant.SEARCH_FILTER_TYPE_JY == searchFilterType && XingyunCommonConstant.USER_LEVEL_JINGYING == Integer.parseInt(map.get("lid").toString())){
					filterIndexList.add(map);
				}
			}
			return filterIndexList;
		}
		return indexList;
	}
	
	/**
	 * 整理搜索用户显示数据
	 */
	public List<Map<Object, Object>> getSearchUserData(List<Map<Object, Object>> indexList, int pageIndex, int pageSize) throws Throwable{
		//整理分页索引数据
		indexList = CommonUtil.subList(indexList, pageIndex, pageSize);
		if(indexList == null || indexList.size() == 0)
			return null;
		
		String userID = "";
		Map<Object,Object> userMap = null;
		Map<Object,Object> profileMap = null;
		List<Map<Object,Object>> userList = new ArrayList<Map<Object,Object>>();
		for(Map<Object,Object> map : indexList){
			userID = map.get("userid").toString();
			userMap = PublicQueryUtil.getInstance().findUserCommonMap(userID);
			if(userMap == null)
				continue;
			profileMap = FollowService.getInstance().getUserProfileMap(userID);
			if(profileMap == null)
				continue;
			
			userMap.putAll(profileMap);
			userMap.put("logourl", UploadPicUtil.getPicWebUrl(userMap.get("logourl").toString(), XingyunUploadFileConstant.LOGO_WIDTH_50));
			
			int isShowfollow = UserHeaderService.getInstance().checkIsShowfollow(userID);
			userMap.put("isShowfollow", isShowfollow == XingyunFaceConstant.FACE_MODULE_SHOW_YES);
			if(isShowfollow == XingyunFaceConstant.FACE_MODULE_SHOW_YES)
				userMap.put("fanscount", FollowService.getInstance().getFansCount(userID));
			userMap.put("skillList", FollowService.getInstance().getSkillListByUserId(userID));
			userMap.put("recommendCount", UserHeaderService.getInstance().getRecommendUserCount(userID));
			userList.add(userMap);
		}
		return userList;
	}
	
	/**
	 * 整理搜索提示json
	 */
	public String getSearchUserTishiJson(String nickName, List<Map<Object, Object>> tishiList) throws Throwable{
		Map<String, Object> friendTishiMap = new HashMap<String, Object>();
		List<Map<Object, Object>> friendTiShiList = new ArrayList<Map<Object,Object>>();
		if(tishiList == null || tishiList.size() == 0){
			friendTishiMap.put("count", 0);
			friendTishiMap.put("list", friendTiShiList);
			return JsonObjectUtil.getJsonStr(friendTishiMap);
		}
		
		boolean isChinese = CommonUtil.isChinese(nickName);
		for(Map<Object,Object> map : tishiList)
			map.put("index", isChinese ? map.get("nickname").toString().indexOf(nickName) : map.get("pinyinname").toString().indexOf(nickName));
		
		String[] seqValue = {"index"};
		CommonUtil.compositor(tishiList, seqValue, 0);
		if(tishiList.size() > XingyunCommonConstant.FRIEND_SEARCH_TISHI_SHOWCOUNT)
			tishiList = tishiList.subList(0, XingyunCommonConstant.FRIEND_SEARCH_TISHI_SHOWCOUNT);
		
		friendTishiMap.put("count", tishiList.size());
		friendTishiMap.put("list", tishiList);
		return JsonObjectUtil.getJsonStr(friendTishiMap);
	}
	
	/**
	 * 替换搜索内容特殊字符
	 */
	public String replaceSearchContent(String searchContent){
		if(StringUtils.isBlank(searchContent))
			return StringUtils.EMPTY;
		
		searchContent = searchContent.replaceAll("'", "''");
		searchContent = searchContent.replaceAll("_", "");
		searchContent = searchContent.replaceAll("%", "");
		searchContent = searchContent.replaceAll(" ", "");

		searchContent = searchContent.replaceAll("\\(", "");
		searchContent = searchContent.replaceAll("\\)", "");
		searchContent = searchContent.replaceAll("\\[", "");
		searchContent = searchContent.replaceAll("\\]", "");
		searchContent = searchContent.replaceAll("\\{", "");
		searchContent = searchContent.replaceAll("\\}", "");
		searchContent = searchContent.replaceAll("\\^", "");
		searchContent = searchContent.replaceAll("\\-", "");
		searchContent = searchContent.replaceAll("\\$", "");
		searchContent = searchContent.replaceAll("\\?", "");
		searchContent = searchContent.replaceAll("\\*", "");
		searchContent = searchContent.replaceAll("\\+", "");
		searchContent = searchContent.replaceAll("\\.", "");
		return searchContent.trim();
	}
}
