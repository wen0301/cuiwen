package com.xingyun.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ComponentTagSupport;
import com.opensymphony.xwork2.util.ValueStack;


public class XingyunSpecialCharTag extends ComponentTagSupport {
	private static final long serialVersionUID = -7516594622733282032L;
	private String value;

	@Override
	public Component getBean(ValueStack valueStack, HttpServletRequest request,
			HttpServletResponse response) {
		return new XingyunSpecialChar(valueStack); 
	}
	protected void populateParams() {
		super.populateParams();
		XingyunSpecialChar mlr = (XingyunSpecialChar)component;
		mlr.setValue(value);
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}