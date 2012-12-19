package com.xingyun.tag;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ComponentTagSupport;

import com.opensymphony.xwork2.util.ValueStack;

public class XingyunLevelTag extends ComponentTagSupport {

	private static final long serialVersionUID = 3239684485084209634L;
	private int lid;
	private int verified;
	private String classname;

	@Override
	public Component getBean(ValueStack valueStack, HttpServletRequest request,
			HttpServletResponse response) {
		return new XingyunLevel(valueStack); 
	}
	protected void populateParams() {
		super.populateParams();
		XingyunLevel xylevel = (XingyunLevel)component;
		xylevel.setLid(lid);
		xylevel.setVerified(verified);
		xylevel.setClassname(classname);
	}
	public int getLid() {
		return lid;
	}
	public void setLid(int lid) {
		this.lid = lid;
	}
	public int getVerified() {
		return verified;
	}
	public void setVerified(int verified) {
		this.verified = verified;
	}
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
}
