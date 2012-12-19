package com.xingyun.constant;

public class UploadResultCode {

	// 全局
	public static final String UPLOAD_PROCESS_SUCC = "1";// 操作成功
	public static final String UPLOAD_PROCESS = "2";// 上传中状态

	// 图片上传相关
	public static final String FILE_UPLOAD_ERRMSG_FILETYPE = "101";			// 图片类型错误
	public static final String FILE_UPLOAD_ERRMSG_FILELENGTH = "102";		// 文件超过8M 限制
	public static final String FILE_UPLOAD_ERRMSG_ALLFILELENGTH = "103";	// 上传总大小超过 限制
	public static final String FILE_UPLOAD_TYPE_ERR = "104";				// 上传类型不正确
	public static final String FILE_UPLOAD_SECURITY_ERROR = "105";			// 上传安全验证失败
	
	// 图片加工相关
	public static final String FILE_CROP_COPYFILE_ERR = "201";		// 转存图片错误
	public static final String FILE_CROP_CORP_ERR = "202";			// 根据滤镜裁剪错误
	public static final String FILE_NOT_EXISTS = "210";				// 图片不存在
	public static final String FILE_UPLOAD_PROCESS_ERROR = "211";	// 加工安全验证失败
	public static final String FILE_CROP_ADDWATER_ERROR = "211";	// 图片加水印错误
}
