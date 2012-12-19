package com.xingyun.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ComponentTagSupport;

import com.opensymphony.xwork2.util.ValueStack;

public class XingyunSubstringTag extends ComponentTagSupport {

	private static final long serialVersionUID = -1384117990359293209L;
	
	private String value;			//用户昵称
	private int maxLength;			//截取的最大长度

	@Override
	public Component getBean(ValueStack stack, HttpServletRequest request,
			HttpServletResponse response) {
		return new XingyunSubstring(stack);
	}
	
	@Override
	protected void populateParams() {
		super.populateParams();
		XingyunSubstring substring = (XingyunSubstring) component;
		substring.setValue(value);
		substring.setMaxLength(maxLength);
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
