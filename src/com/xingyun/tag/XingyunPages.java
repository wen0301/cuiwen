package com.xingyun.tag;

import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.components.Component;

import com.opensymphony.xwork2.util.ValueStack;

public class XingyunPages extends Component {
	//页数占位符 (ajax分页时将onclick方法的 此([page])参数 转换为选择的页数)
	public static final String PAGE_PLACEHOLDER = "[page]"; 
	private static final Logger log = Logger.getLogger(XingyunPages.class);
	public static final int MAX_PAGES = 7;
	public static final int PAGE_SIZE = 20;
	public static final int START_END_OMIT_PAGES = 2;
	public static final String CURPAGE_PARAMETER = "curPage";
	public static final String CURRENT_PAGE_ITEM_FORMAT = "<a hidefocus=\"true\" title=\"\" href=\"javascript:void(0);\" class=\"alive\">%d</a>";
	public static final String PAGE_ITEM_FORMAT = "<a hidefocus=\"true\" title=\"\" href=\"%s\">%d</a>";
	
	private int total;
	private int current;
	private int pageSize = PAGE_SIZE;
	private int maxPages = MAX_PAGES;
	private String curPageParam = CURPAGE_PARAMETER;
	private String url;
	private String onclick;
	private String divClass;
	private HttpServletResponse response;
	
	public String getCurPageParam() {
		return curPageParam;
	}

	public void setCurPageParam(String curPageParam) {
		this.curPageParam = curPageParam;
	}
	public XingyunPages(ValueStack valueStatck,HttpServletResponse response) {
		super(valueStatck);
		this.response = response;
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

	public int getMaxPages() {
		return maxPages;
	}

	public void setMaxPages(int maxPages) {
		this.maxPages = maxPages;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getOnclick() {
		return onclick;
	}

	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}

	public String getDivClass() {
		return divClass;
	}

	public void setDivClass(String divClass) {
		this.divClass = divClass;
	}

	@Override  
    public boolean start(Writer writer) {
		boolean result = super.start(writer);
		try {
			if (pageSize <= 0)
				pageSize = PAGE_SIZE;
			if (maxPages <= 0)
				maxPages = MAX_PAGES;
			if (current < 1)
				current = 1;
			int totalPages = total / pageSize;
			if (total % pageSize > 0){
				totalPages ++;
			}
			if (current > totalPages)
				current = totalPages;
			int omitStart = 0;
			int omitEnd = 0;
			int startIndex=1;
			int pageCount = 0;
			if (totalPages > 2 * START_END_OMIT_PAGES + maxPages){
				if(current <= START_END_OMIT_PAGES + (maxPages + 1) / 2) {
					omitStart = 0;
					omitEnd = START_END_OMIT_PAGES;
					if(current <= START_END_OMIT_PAGES) {
						pageCount = maxPages;
					} else if(current < maxPages - maxPages / 2) {
						pageCount = maxPages;
					} else {
						pageCount = current + maxPages / 2;
					}
				} else {
					omitStart = START_END_OMIT_PAGES;
					pageCount = maxPages;
					startIndex = current - maxPages / 2;
					if(current + maxPages / 2 < totalPages - START_END_OMIT_PAGES) {
						omitEnd = START_END_OMIT_PAGES;
					} else if(current + maxPages / 2 < totalPages) {
						pageCount = totalPages - startIndex + 1;
					} else {
						startIndex = totalPages - maxPages + 1;
					}
				}
			} else{
				pageCount = totalPages;
			}
			
			StringBuilder outStr = new StringBuilder();
			outStr.append("<div class=\"").append(StringUtils.isBlank(divClass) ? "page" : divClass).append("\">");
			if("page2".equals(divClass))
				outStr.append("<span style=\"display: none\" class=\"iconLoading1\"></span>");
			
			if (current < totalPages && StringUtils.isBlank(divClass))
				outStr.append(String.format("<a hidefocus=\"true\" title=\"\" href=\"%s\" class=\"next\">→</a>", createLink(current + 1)));
			if (current > 1 && StringUtils.isBlank(divClass))
				outStr.append(String.format("<a hidefocus=\"true\" title=\"\" href=\"%s\" class=\"prev\">←</a>", createLink(current - 1)));
			
			
			int i = 0;
			if (omitStart > 0){
				for (i = 1; i <= omitStart; i ++){
					outStr.append(String.format(PAGE_ITEM_FORMAT, createLink(i), i));
				}
				outStr.append("<a href=\"javascript:void(0);\" title=\"\" hidefocus=\"true\">...</a>");
			}
			
			if(pageCount > 1) {
				for (i = 0; i < pageCount; i ++){
					String str;
					int page = startIndex + i;
					if(page > omitStart) {
						if (page == current){
							str = String.format(CURRENT_PAGE_ITEM_FORMAT, page);
						} else{
							str = String.format(PAGE_ITEM_FORMAT, createLink(page), page);
						}
						outStr.append(str);
					}
				}
			}
			if (omitEnd > 0){
				outStr.append("<a href=\"javascript:void(0);\" title=\"\" hidefocus=\"true\">...</a>");
				for (i = totalPages - omitEnd + 1; i <= totalPages; i ++){
					outStr.append(String.format(PAGE_ITEM_FORMAT, createLink(i), i));
				}
			}

			outStr.append("</div>");
			if("<div class=\"page\"></div>".equals(outStr.toString()))
				writer.write(StringUtils.EMPTY);
			else
				writer.write(response.encodeURL(outStr.toString()));
		} catch (Throwable e) { 
			log.error(e.getMessage(), e);
		}
		return result;
	}
	
	private String createLink(int page){
		if (curPageParam == null || "".equals(curPageParam)) {
			curPageParam = CURPAGE_PARAMETER;
		}

		if (StringUtils.isNotBlank(onclick)) {
			String link = "javascript:void(0);\" onclick=\"%s";
			return String.format(link, onclick.replace(PAGE_PLACEHOLDER, page + ""));
		} else if (StringUtils.isNotBlank(url)) {
			if (url.indexOf("?") >= 0) {
				return url + "&" + curPageParam + "=" + page;
			} else {
				return url + "?" + curPageParam + "=" + page;
			}
		} else {
			throw new IllegalArgumentException("onclick or url param is required in pager tag");
		}
	}
}
