package com.xingyun.upload.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;
import com.xingyun.constant.XingyunUploadFileConstant;
import com.xingyun.upload.bean.AddWaterBean;
import com.xingyun.upload.bean.CompositeBean;
import com.xingyun.upload.bean.CompressBean;
import com.xingyun.upload.bean.CopyFileBean;
import com.xingyun.upload.bean.CropBean;
import com.xingyun.upload.bean.PicMsgBean;
import com.xingyun.upload.bean.UploadParamBean;
import com.xingyun.util.SerializeUtils;
import com.xingyun.util.UploadPicUtil;

public enum UploadDeal {
	UPLOAD_TYPE_LOGO(XingyunUploadFileConstant.UPLOAD_TYPE_LOGO) {// 用户上传头像处理
		@Override
		public String save(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			//裁剪图片  640
			List<String> dest640 = new ArrayList<String>(destMulMap.get("dest640"));
			CropBean cropBean = new CropBean();
			cropBean.setSrc(paramBean.src());
			cropBean.setDest(dest640);
			cropBean.setX1(paramBean.getX1());
			cropBean.setY1(paramBean.getY1());
			cropBean.setX2(paramBean.getX2());
			cropBean.setY2(paramBean.getY2());
			cropBean.setTimes(paramBean.getTimes());
			beanList.add(cropBean);
			
			//改变图片形状 250
			List<String> dest250 = new ArrayList<String>(destMulMap.get("dest250"));
			CompressBean compressBean = new CompressBean();
			compressBean.setSrc(dest640);
			compressBean.setDest(dest250);
			compressBean.setMaxWidth(XingyunUploadFileConstant.LOGO_WIDTH_250);
			compressBean.setMaxHeight(XingyunUploadFileConstant.LOGO_WIDTH_250);
			beanList.add(compressBean);
			
			//改变图片形状 200
			List<String> dest200 = new ArrayList<String>(destMulMap.get("dest200"));
			compressBean = new CompressBean();
			compressBean.setSrc(dest640);
			compressBean.setDest(dest200);
			compressBean.setMaxWidth(XingyunUploadFileConstant.LOGO_WIDTH_200);
			compressBean.setMaxHeight(XingyunUploadFileConstant.LOGO_WIDTH_200);
			beanList.add(compressBean);
			
			//改变图片形状 150
			List<String> dest150 = new ArrayList<String>(destMulMap.get("dest150"));
			compressBean = new CompressBean();
			compressBean.setSrc(dest640);
			compressBean.setDest(dest150);
			compressBean.setMaxWidth(XingyunUploadFileConstant.LOGO_WIDTH_150);
			compressBean.setMaxHeight(XingyunUploadFileConstant.LOGO_WIDTH_150);
			beanList.add(compressBean);
			
			//改变图片形状 100
			List<String> dest100 = new ArrayList<String>(destMulMap.get("dest100"));
			compressBean = new CompressBean();
			compressBean.setSrc(dest640);
			compressBean.setDest(dest100);
			compressBean.setMaxWidth(XingyunUploadFileConstant.LOGO_WIDTH_100);
			compressBean.setMaxHeight(XingyunUploadFileConstant.LOGO_WIDTH_100);
			beanList.add(compressBean);
			
			//改变图片形状 75
			List<String> dest75 = new ArrayList<String>(destMulMap.get("dest75"));
			compressBean = new CompressBean();
			compressBean.setSrc(dest640);
			compressBean.setDest(dest75);
			compressBean.setMaxWidth(XingyunUploadFileConstant.LOGO_WIDTH_75);
			compressBean.setMaxHeight(XingyunUploadFileConstant.LOGO_WIDTH_75);
			beanList.add(compressBean);
			
			//改变图片形状 50
			List<String> dest50 = new ArrayList<String>(destMulMap.get("dest50"));
			compressBean = new CompressBean();
			compressBean.setSrc(dest640);
			compressBean.setDest(dest50);
			compressBean.setMaxWidth(XingyunUploadFileConstant.LOGO_WIDTH_50);
			compressBean.setMaxHeight(XingyunUploadFileConstant.LOGO_WIDTH_50);
			beanList.add(compressBean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}

		@Override
		public String pre(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			//裁剪图片
			CropBean bean = new CropBean();
			bean.setSrc(paramBean.src());
			bean.setDest(paramBean.getDest());
			bean.setX1(paramBean.getX1());
			bean.setY1(paramBean.getY1());
			bean.setX2(paramBean.getX2());
			bean.setY2(paramBean.getY2());
			bean.setTimes(paramBean.getTimes());
			beanList.add(bean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}
	},

	UPLOAD_TYPE_POST_ITEM(XingyunUploadFileConstant.UPLOAD_TYPE_POST_ITEM) {// 展示明细
		@Override
		public String pre(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			CropBean bean = new CropBean();
			bean.setSrc(paramBean.src());
			bean.setDest(new ArrayList<String>(destMulMap.get("thumb")));
			bean.setX1(XingyunUploadFileConstant.POST_ITEM_WIDTH_150);
			bean.setY1(XingyunUploadFileConstant.POST_ITEM_WIDTH_150);
			bean.setPostThumb(true);
			beanList.add(bean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}

		@Override
		public String save(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			//获取原图信息
			PicMsgBean msgBean = new PicMsgBean();
			msgBean.setSrc(paramBean.src());
			beanList.add(msgBean);
			
			//保存原图
			List<String> srcPic = new ArrayList<String>(destMulMap.get("src"));
			CompressBean midBean = new CompressBean();
			midBean.setSrc(paramBean.src());
			midBean.setDest(srcPic);
			midBean.setMaxWidth(XingyunUploadFileConstant.IMAGE_MAX_WIDTH_SIZE);
			beanList.add(midBean);
			
			//保存大图
			List<String> mid = new ArrayList<String>(destMulMap.get("mid"));
			midBean = new CompressBean();
			midBean.setSrc(srcPic);
			midBean.setDest(mid);
			midBean.setMaxWidth(XingyunUploadFileConstant.POST_ITEM_WIDTH_1010);
			midBean.setMaxHeight(XingyunUploadFileConstant.POST_ITEM_WIDTH_1010);
			beanList.add(midBean);
			
			List<String> mid_640 = new ArrayList<String>(destMulMap.get("mid_640"));
			midBean = new CompressBean();
			midBean.setSrc(srcPic);
			midBean.setDest(mid_640);
			midBean.setMaxWidth(XingyunUploadFileConstant.POST_ITEM_WIDTH_640);
			beanList.add(midBean);
			
			List<String> thumb = new ArrayList<String>(destMulMap.get("thumb"));
			CopyFileBean thumbBean = new CopyFileBean();
			thumbBean.setSrc(paramBean.thumb());
			thumbBean.setDest(thumb);
			beanList.add(thumbBean);
			
			//加水印
			if(paramBean.getIsWater()){
				AddWaterBean srcAddWaterBean = new AddWaterBean();
				srcAddWaterBean.setSrc(srcPic);
				srcAddWaterBean.setDest(srcPic);
				srcAddWaterBean.setNickName(paramBean.getWatermarkNickName());
				srcAddWaterBean.setWkey(paramBean.getWatermarkWkey());
				beanList.add(srcAddWaterBean);
				
				srcAddWaterBean = new AddWaterBean();
				srcAddWaterBean.setSrc(mid);
				srcAddWaterBean.setDest(mid);
				srcAddWaterBean.setNickName(paramBean.getWatermarkNickName());
				srcAddWaterBean.setWkey(paramBean.getWatermarkWkey());
				beanList.add(srcAddWaterBean);
			}
			
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}
	},
	UPLOAD_TYPE_POST_COVER(XingyunUploadFileConstant.UPLOAD_TYPE_POST_COVER) {// 展示封面
		@Override
		public String pre(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			CropBean bean = new CropBean();
			bean.setSrc(paramBean.src());
			bean.setDest(paramBean.getDest());
			bean.setX1(paramBean.getX1());
			bean.setY1(paramBean.getY1());
			bean.setX2(paramBean.getX2());
			bean.setY2(paramBean.getY2());
			bean.setTimes(paramBean.getTimes());
			List<Object> beanList = new ArrayList<Object>();
			beanList.add(bean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}

		@Override
		public String save(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			List<String> dest250 = new ArrayList<String>(destMulMap.get("dest250"));
			
			boolean coverTag = true;
			if(destMulMap.get("postCoverType") != null && destMulMap.get("postCoverType").size() > 0){
				List<String> zpCoverTypeList = new ArrayList<String>(destMulMap.get("postCoverType"));
				for(String zpCoverType : zpCoverTypeList){
					if("item_cover".equals(zpCoverType)){
						coverTag = false;
					}
				}
			}
			if(coverTag){	//正常封面图片裁剪
				CropBean bean = new CropBean();
				bean.setSrc(paramBean.src());
				bean.setDest(dest250);
				bean.setX1(paramBean.getX1());
				bean.setY1(paramBean.getY1());
				bean.setX2(paramBean.getX2());
				bean.setY2(paramBean.getY2());
				bean.setTimes(paramBean.getTimes());
				beanList.add(bean);
			}else{	//作品第一张图片 裁剪成封面
				CropBean bean = new CropBean();
				bean.setSrc(paramBean.src());
				bean.setDest(dest250);
				bean.setX1(XingyunUploadFileConstant.POST_COVER_WIDTH_250);
				bean.setY1(XingyunUploadFileConstant.POST_COVER_WIDTH_250);
				bean.setPostThumb(true);
				beanList.add(bean);
			}
			
			List<String> dest220 = new ArrayList<String>(destMulMap.get("dest220"));
			CompressBean coverBean = new CompressBean();
			coverBean.setSrc(dest250);
			coverBean.setDest(dest220);
			coverBean.setMaxWidth(XingyunUploadFileConstant.POST_COVER_WIDTH_220);
			coverBean.setMaxHeight(XingyunUploadFileConstant.POST_COVER_WIDTH_220);
			beanList.add(coverBean);
			
			List<String> dest200 = new ArrayList<String>(destMulMap.get("dest200"));
			coverBean = new CompressBean();
			coverBean.setSrc(dest250);
			coverBean.setDest(dest200);
			coverBean.setMaxWidth(XingyunUploadFileConstant.POST_COVER_WIDTH_200);
			coverBean.setMaxHeight(XingyunUploadFileConstant.POST_COVER_WIDTH_200);
			beanList.add(coverBean);
			
			List<String> dest190 = new ArrayList<String>(destMulMap.get("dest190"));
			coverBean = new CompressBean();
			coverBean.setSrc(dest250);
			coverBean.setDest(dest190);
			coverBean.setMaxWidth(XingyunUploadFileConstant.POST_COVER_WIDTH_190);
			coverBean.setMaxHeight(XingyunUploadFileConstant.POST_COVER_WIDTH_190);
			beanList.add(coverBean);
			
			List<String> dest165 = new ArrayList<String>(destMulMap.get("dest165"));
			coverBean = new CompressBean();
			coverBean.setSrc(dest250);
			coverBean.setDest(dest165);
			coverBean.setMaxWidth(XingyunUploadFileConstant.POST_COVER_WIDTH_165);
			coverBean.setMaxHeight(XingyunUploadFileConstant.POST_COVER_WIDTH_165);
			beanList.add(coverBean);
			
			List<String> dest150 = new ArrayList<String>(destMulMap.get("dest150"));
			coverBean = new CompressBean();
			coverBean.setSrc(dest250);
			coverBean.setDest(dest150);
			coverBean.setMaxWidth(XingyunUploadFileConstant.POST_COVER_WIDTH_150);
			coverBean.setMaxHeight(XingyunUploadFileConstant.POST_COVER_WIDTH_150);
			beanList.add(coverBean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}
	},
	
	UPLOAD_TYPE_FACE_DYNAMIC(XingyunUploadFileConstant.UPLOAD_TYPE_FACE_DYNAMIC) {// 动态封面
		@Override
		public String prepare(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			// 图片合成
			CompositeBean bean = new CompositeBean();
			bean.setSrc(paramBean.src());
			bean.setDest(paramBean.getDest());
			beanList.add(bean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));// 处理并返回结果
		}

		@Override
		public String pre(UploadParamBean paramBean, Multimap<String, String> destMulMap) {// 预览
			List<Object> beanList = new ArrayList<Object>();
			// 裁剪图片
			CropBean bean = new CropBean();
			bean.setSrc(paramBean.src());
			bean.setDest(paramBean.getDest());
			bean.setX1(paramBean.getX1());
			bean.setY1(paramBean.getY1());
			bean.setX2(paramBean.getX2());
			bean.setY2(paramBean.getY2());
			bean.setTimes(paramBean.getTimes());
			beanList.add(bean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}
		
		@Override
		public String save(UploadParamBean paramBean, Multimap<String, String> destMulMap) {// 保存
			List<Object> beanList = new ArrayList<Object>();
			//拷贝大图
			List<String> dest = new ArrayList<String>(destMulMap.get("dest"));
			CopyFileBean bean = new CopyFileBean();
			bean.setSrc(paramBean.src());
			bean.setDest(dest);
			beanList.add(bean);	
			
			//生成 phone图
			List<String> phone = new ArrayList<String>(destMulMap.get("phone"));
			CompressBean compressBean = new CompressBean();
			compressBean.setSrc(dest);
			compressBean.setDest(phone);
			compressBean.setMaxWidth(XingyunUploadFileConstant.FACE_PHONE_WIDTH_619);
			beanList.add(compressBean);
			
			//生成缩略图
			List<String> thumb = new ArrayList<String>(destMulMap.get("thumb"));
			compressBean = new CompressBean();
			compressBean.setSrc(dest);
			compressBean.setDest(thumb);
			compressBean.setMaxWidth(XingyunUploadFileConstant.FACE_DYNAMIC_WIDTH_150);
			beanList.add(compressBean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}
	},
	
	UPLOAD_TYPE_VIDEO(XingyunUploadFileConstant.UPLOAD_TYPE_VIDEO) {// 视频简历封面处理
		@Override
		public String pre(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			//裁剪图片
			CropBean bean = new CropBean();
			bean.setSrc(paramBean.src());
			bean.setDest(paramBean.getDest());
			bean.setX1(paramBean.getX1());
			bean.setY1(paramBean.getY1());
			bean.setX2(paramBean.getX2());
			bean.setY2(paramBean.getY2());
			bean.setTimes(paramBean.getTimes());
			beanList.add(bean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}
		
		@Override
		public String save(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			//第一步 裁剪图片
			List<String> dest250 = new ArrayList<String>(destMulMap.get("dest250"));
			CropBean cropBean = new CropBean();
			cropBean.setSrc(paramBean.src());
			cropBean.setDest(dest250);
			cropBean.setX1(paramBean.getX1());
			cropBean.setY1(paramBean.getY1());
			cropBean.setX2(paramBean.getX2());
			cropBean.setY2(paramBean.getY2());
			cropBean.setTimes(paramBean.getTimes());
			beanList.add(cropBean);
			
			//第二布 改变图片形状
			List<String> dest150 = new ArrayList<String>(destMulMap.get("dest150"));
			CompressBean compressBean = new CompressBean();
			compressBean.setSrc(dest250);
			compressBean.setDest(dest150);
			compressBean.setMaxWidth(XingyunUploadFileConstant.VIDEO_WIDTH_150);
			compressBean.setMaxHeight(XingyunUploadFileConstant.VIDEO_HEIGHT_90);
			beanList.add(compressBean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}
	},
	
	UPLOAD_TYPE_PROFILE_OTHER_ITEM(XingyunUploadFileConstant.UPLOAD_TYPE_PROFILE_OTHER_ITEM) {// 档案自定义模块图片明细
		@Override
		public String pre(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			CropBean bean = new CropBean();
			bean.setSrc(paramBean.src());
			bean.setDest(new ArrayList<String>(destMulMap.get("thumb")));
			bean.setX1(XingyunUploadFileConstant.PROFILE_OTHER_WIDTH_150);
			bean.setY1(XingyunUploadFileConstant.PROFILE_OTHER_WIDTH_150);
			bean.setPostThumb(true);
			beanList.add(bean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}

		@Override
		public String save(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			//获取src图信息
			PicMsgBean msgBean = new PicMsgBean();
			msgBean.setSrc(paramBean.src());
			beanList.add(msgBean);

			//src图
			List<String> srcList = new ArrayList<String>(destMulMap.get("src"));
			CompressBean compressBean = new CompressBean();
			compressBean.setSrc(paramBean.src());
			compressBean.setDest(srcList);
			compressBean.setMaxWidth(XingyunUploadFileConstant.PROFILE_OTHER_WIDTH_740);
			beanList.add(compressBean);
			
			//mid图
			List<String> midList = new ArrayList<String>(destMulMap.get("mid"));
			compressBean = new CompressBean();
			compressBean.setSrc(srcList);
			compressBean.setDest(midList);
			compressBean.setMaxWidth(XingyunUploadFileConstant.PROFILE_OTHER_WIDTH_500);
			beanList.add(compressBean);
			
			//缩略图
			List<String> thumbList = new ArrayList<String>(destMulMap.get("thumb"));
			CopyFileBean thumbBean = new CopyFileBean();
			thumbBean.setSrc(paramBean.thumb());
			thumbBean.setDest(thumbList);
			beanList.add(thumbBean);
			
			//加水印
			if(paramBean.getIsWater()){
				AddWaterBean srcAddWaterBean = new AddWaterBean();
				srcAddWaterBean.setSrc(srcList);
				srcAddWaterBean.setDest(srcList);
				srcAddWaterBean.setNickName(paramBean.getWatermarkNickName());
				srcAddWaterBean.setWkey(paramBean.getWatermarkWkey());
				beanList.add(srcAddWaterBean);
			}
			
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}
	},
	UPLOAD_TYPE_PROFILE_WHOLEBODY(XingyunUploadFileConstant.UPLOAD_TYPE_PROFILE_WHOLEBODY) {// 用户上传全身照处理
		@Override
		public String save(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			//裁剪图片  640
			List<String> dest300 = new ArrayList<String>(destMulMap.get("dest300"));
			CropBean cropBean = new CropBean();
			cropBean.setSrc(paramBean.src());
			cropBean.setDest(dest300);
			cropBean.setX1(paramBean.getX1());
			cropBean.setY1(paramBean.getY1());
			cropBean.setX2(paramBean.getX2());
			cropBean.setY2(paramBean.getY2());
			cropBean.setTimes(paramBean.getTimes());
			beanList.add(cropBean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}

		@Override
		public String pre(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			//裁剪图片
			CropBean bean = new CropBean();
			bean.setSrc(paramBean.src());
			bean.setDest(paramBean.getDest());
			bean.setX1(paramBean.getX1());
			bean.setY1(paramBean.getY1());
			bean.setX2(paramBean.getX2());
			bean.setY2(paramBean.getY2());
			bean.setTimes(paramBean.getTimes());
			beanList.add(bean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}
	},
	
	UPLOAD_TYPE_XINGYU(XingyunUploadFileConstant.UPLOAD_TYPE_XINGYU) {// 星语图片
		@Override
		public String pre(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			CropBean bean = new CropBean();
			bean.setSrc(paramBean.src());
			bean.setDest(new ArrayList<String>(destMulMap.get("thumb")));
			bean.setX1(XingyunUploadFileConstant.XINGYU_WIDTH_150);
			bean.setY1(XingyunUploadFileConstant.XINGYU_WIDTH_150);
			bean.setPostThumb(true);
			beanList.add(bean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}

		@Override
		public String save(UploadParamBean paramBean, Multimap<String, String> destMulMap) {
			List<Object> beanList = new ArrayList<Object>();
			//获取原图信息
			PicMsgBean msgBean = new PicMsgBean();
			msgBean.setSrc(paramBean.src());
			beanList.add(msgBean);
			
			//保存原图
			List<String> srcPic = new ArrayList<String>(destMulMap.get("src"));
			CompressBean midBean = new CompressBean();
			midBean.setSrc(paramBean.src());
			midBean.setDest(srcPic);
			midBean.setMaxWidth(XingyunUploadFileConstant.IMAGE_MAX_WIDTH_SIZE);
			beanList.add(midBean);
			
			//保存大图
			List<String> mid_640 = new ArrayList<String>(destMulMap.get("mid_640"));
			midBean = new CompressBean();
			midBean.setSrc(srcPic);
			midBean.setDest(mid_640);
			midBean.setMaxWidth(XingyunUploadFileConstant.XINGYU_WIDTH_640);
			beanList.add(midBean);
			
			List<String> mid_500 = new ArrayList<String>(destMulMap.get("mid_500"));
			midBean = new CompressBean();
			midBean.setSrc(srcPic);
			midBean.setDest(mid_500);
			midBean.setMaxWidth(XingyunUploadFileConstant.XINGYU_WIDTH_500);
			beanList.add(midBean);
			
			//保存缩略图
			List<String> thumb = new ArrayList<String>(destMulMap.get("thumb"));
			CopyFileBean thumbBean = new CopyFileBean();
			thumbBean.setSrc(paramBean.thumb());
			thumbBean.setDest(thumb);
			beanList.add(thumbBean);
			return SerializeUtils.processRequest(beanList, UploadPicUtil.getUploadProcessURL(paramBean.getUploadServer()));
		}
	},
	
	;
	
	private String uploadType;
	private static final Map<String, UploadDeal> map;

	static {
		map = new HashMap<String, UploadDeal>(UploadDeal.values().length);
		for (UploadDeal fusd : UploadDeal.values()) {
			map.put(fusd.getUploadType(), fusd);
		}
	}
	
	private UploadDeal(String uploadType) {
		this.uploadType = uploadType;
	}

	public static UploadDeal getDealByType(String uploadType) {
		return map.get(uploadType);
	}

	public String getUploadType() {
		return uploadType;
	}

	public void setUploadType(String uploadType) {
		this.uploadType = uploadType;
	}

	// 准备数据
	public String prepare(UploadParamBean paramBean, Multimap<String, String> map) {
		return null;
	}

	// 预览图片
	public String pre(UploadParamBean paramBean, Multimap<String, String> map) {
		return null;
	}

	// 保存图片
	public String save(UploadParamBean paramBean, Multimap<String, String> map) {
		return null;
	}
}
