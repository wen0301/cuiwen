package com.xingyun.constant;


public class XingyunOauthConstant {
	/**新浪账户绑定 appkey */
	public static final String SINA_APPID = XingyunSystemConstant.getSystemConstantValue(XingyunSystemConstant.XINGYUN_APPID);
	/**新浪账户绑定 app_secret */
	public static final String SINA_APPKEY = XingyunSystemConstant.getSystemConstantValue(XingyunSystemConstant.XINGYUN_APPKEY);
	/**新浪账户绑定回调地址 */
	public static final String SINA_REDIRECT_URI = XingyunLine.XINGYUN_CN + "/sina/callback";
	/**新浪微博api */
	public static final String SINA_BASEURL = "https://api.weibo.com/";
	/**获取新浪微博授权用户的access_token */
	public static final String SINA_ACCESSTOKENURL = "https://api.weibo.com/oauth2/access_token";
	/**请求用户授权Token */
	public static final String SINA_AUTHORIZEURL = "https://api.weibo.com/oauth2/authorize";
	/**请求用户授权Token详细请求url */
	public static final String SINA_BINDURL = SINA_AUTHORIZEURL + "?client_id=" + SINA_APPID + "&response_type=code&redirect_uri=" + SINA_REDIRECT_URI;
	/** token 有效*/
	public static final String TOKEN_T = "T";
	/** token 失效*/
	public static final String TOKEN_F = "F";
	/** 请求授权来源 游客 */
	public static final String SOURCE_YOUKE = "youke";
	/** 请求授权来源 推荐用户/作品时分享 */
	public static final String SOURCE_SHARE_OTHER = "share_other";
	/** 请求授权来源 发布作品时分享 */
	public static final String SOURCE_SHARE_POST = "share_post";
	/**写入cookie的新浪token的key值 */
	public static final String XINGYUN_SINA_TOKENKEY = "XINGYUN_SINA_TOKENKEY";
	/**写入cookie的新浪expires的key值 */
	public static final String XINGYUN_SINA_EXPIRES = "XINGYUN_SINA_EXPIRES";
	/**写入cookie的新浪用户id的key值 */
	public static final String XINGYUN_SINA_USERIDKEY = "XINGYUN_SINA_USERIDKEY";
	/**写入cookie的新浪token的key值 */
	public static final String XINGYUN_SINA_REFERER = "XINGYUN_SINA_REFERER";
	/**记录token的cookie超时时间 半小时*/
	public static final int XINGYUN_TOKEN_SAVE_TIME = 60 * 30;
	/**新浪微博域名 */
	public static final String SINA_DOMAIN = "http://weibo.com";
	/**星云网新浪微博用户id */
	public static final String WEIBO_XINGYUN_UID = "2438243845";
	/**api调用次数：如果api调取失败再次调用 */
	public static final int SINA_APPID_COUNT = 5; 
	/**用户或授权服务器拒绝授予数据访问权限 */
	public static final String ERROR_CODE_DENY = "21330";
}
