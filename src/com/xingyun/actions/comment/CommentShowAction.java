package com.xingyun.actions.comment;

import java.util.List;

import org.apache.log4j.Logger;

import com.xingyun.actions.index.MyIndexAction;
import com.xingyun.base.XingyunBaseAction;
import com.xingyun.bean.CommentBean;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.services.comment.CommentService;

public class CommentShowAction extends XingyunBaseAction{
	private static final long serialVersionUID = -5695311058378721122L;
	private static final Logger log = Logger.getLogger(MyIndexAction.class);
	private int postID;										//作品ID
	private int xingyuID;									//星语ID
	private int totalRecord;                            	//评论总数
	private List<CommentBean> commentBeanList;         		//评论列表
	/**
	 * ajax分页获取评论数据
	 */
	public String getPostCommentItemAjax(){
		try {
			totalRecord = CommentService.getInstance().getCommentCount(XingyunCommonConstant.COMMENT_SOURCE_POST, postID);
			commentBeanList = CommentService.getInstance().getCommentList(XingyunCommonConstant.COMMENT_SOURCE_POST, postID, curPage, XingyunCommonConstant.COMMENT_PREPAGE_MAXSIZE);
			return "showPostComment";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
	
	/**
	 * 获取星语评论
	 */
	public String getXingyuCommentItemAjax() throws Throwable{
		try {
			totalRecord = CommentService.getInstance().getCommentCount(XingyunCommonConstant.COMMENT_SOURCE_XINGYU, xingyuID);
			commentBeanList = CommentService.getInstance().getCommentList(XingyunCommonConstant.COMMENT_SOURCE_XINGYU, xingyuID, curPage, XingyunCommonConstant.COMMENT_PREPAGE_MAXSIZE);
			return "showXingyuComment";
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}

	public int getPostID() {
		return postID;
	}

	public void setPostID(int postID) {
		this.postID = postID;
	}

	public int getXingyuID() {
		return xingyuID;
	}

	public void setXingyuID(int xingyuID) {
		this.xingyuID = xingyuID;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public List<CommentBean> getCommentBeanList() {
		return commentBeanList;
	}

	public void setCommentBeanList(List<CommentBean> commentBeanList) {
		this.commentBeanList = commentBeanList;
	}
}
