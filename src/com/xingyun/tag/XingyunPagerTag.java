package com.xingyun.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ComponentTagSupport;

import com.opensymphony.xwork2.util.ValueStack;

/**
 * 自定义分页标签
 */
public class XingyunPagerTag extends ComponentTagSupport {

	private static final long serialVersionUID = -3664776877859872903L;
	
	/** 总记录数 */
	private int total;
	
	/** 当前页 */
	private int current;
	
	/** 中间显示的页数 */
	private int pageSize = XingyunPages.PAGE_SIZE;
	
	/** 最前边显示的页数 */
	private int maxPages = XingyunPages.MAX_PAGES;
	
	private String curPageParam = XingyunPages.CURPAGE_PARAMETER;
	
	/** 控制数据显示的Action的Url */
	private String url;
	
	private String onclick;
	
	private String divClass;
	
	public String getOnclick() {
		return onclick;
	}

	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public String getDivClass() {
		return divClass;
	}

	public void setDivClass(String divClass) {
		this.divClass = divClass;
	}

	@Override
	public Component getBean(ValueStack valueStack, HttpServletRequest request,HttpServletResponse response) {
		return new XingyunPages(valueStack,response); 
	}

	protected void populateParams() {
		super.populateParams();
		XingyunPages pages = (XingyunPages)component;
		pages.setCurrent(current);   
        pages.setTotal(total);   
        pages.setUrl(url);
        pages.setOnclick(onclick);
        pages.setPageSize(pageSize);
        pages.setMaxPages(maxPages);
        pages.setCurPageParam(curPageParam);
        pages.setDivClass(divClass);
	}
	
	public int getMaxPages() {
		return maxPages;
	}

	public void setMaxPages(int maxPages) {
		this.maxPages = maxPages;
	}

	public String getCurPageParam() {
		return curPageParam;
	}

	public void setCurPageParam(String curPageParam) {
		this.curPageParam = curPageParam;
	}
}
