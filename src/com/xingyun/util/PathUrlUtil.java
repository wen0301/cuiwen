package com.xingyun.util;

/**
 * 定义了获取MOKO项目中的各种路径和Url的静态方法
 */
public class PathUrlUtil {
	/** 存储的根路径 */
	public static final String STOREROOT;

	static {
		STOREROOT = getRootUrl();
	}

	/**
	 * 获得存储的全路径
	 */
	public static String getFullStorePath(String path) {
		if(path.startsWith("/"))
			path = path.substring(1);
		return getRootUrl() + path;
	}

	/** 获取根的存储路径 */
	public static String getRootUrl() {
		return new PathUrlUtil().getRoot();
	}

	/**
	 * 获得项目根的存储路径
	 */
	private String getRoot() {
		String path = getClass().getResource("").getPath();
		int pos = path.indexOf("/WEB-INF/");
		int begin = 0;
		if (path.indexOf(":") != -1)// 解决windows和linux的兼容问题
			begin = 1;
		path = path.substring(begin, pos) + "/";
		return path;
	}
}