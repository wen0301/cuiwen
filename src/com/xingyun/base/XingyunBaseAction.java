package com.xingyun.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import com.xingyun.bean.User;
import com.xingyun.session.UserSessionEnvironment;
import com.xingyun.util.ResponseUtil;

public class XingyunBaseAction extends ActionSupport implements Preparable,ServletRequestAware {

	private static final long serialVersionUID = -8867453348056823714L;
	
	protected User user;
	
	protected String wKey;
	
	protected String userid;
	
	protected HttpServletRequest servletRequest = null;
	
	protected int curPage = 1;
	
	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	public void setServletRequest(HttpServletRequest servletRequest) {
		this.servletRequest = servletRequest;
		user = UserSessionEnvironment.getXingyunUser(servletRequest);
	}

	public void setWKey(String wKey) {
		this.wKey = wKey;
	}

	public String getWKey() {
		return wKey;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void prepare() throws Exception {

	}

	protected HttpSession getSession() {
		return servletRequest.getSession();
	}

	protected HttpServletResponse getResponse() {
		return getHttpServletResponse();
	}
	public static HttpServletResponse getHttpServletResponse() {
		return ResponseUtil.getHttpServletResponse();
	}
	/**
	 * 返回响应信息
	 */
	public void sendResponseMsg(String responseMsg){
		ResponseUtil.sendResponseMsg(responseMsg);
	}	
	
	public int getCurPage() {
		return curPage;
	}
	public void setCurPage(int curPage) {
		this.curPage = curPage;
	}
}