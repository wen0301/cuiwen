package com.xingyun.base;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

public class DisableUrlSessionFilter implements Filter{
	public void doFilter(ServletRequest request, ServletResponse response,FilterChain chain) throws IOException, ServletException {
		if(chain == null)
			return;
		// 防止SQLURL注入
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String param = httpRequest.getQueryString();
		if(param!=null && isFind(param))
			return;		
		// skip non-http requests
		if (!(request instanceof HttpServletRequest)) {
			chain.doFilter(request, response);
			return;
		}		
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // clear session if session id in URL
        if (httpRequest.isRequestedSessionIdFromURL()) {
            HttpSession session = httpRequest.getSession();
            if (session != null) session.invalidate();
        }
        // wrap response to remove URL encoding
        HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(httpResponse) {
            @Override
            public String encodeRedirectUrl(String url) {
                return url;
            }

            @Override
            public String encodeRedirectURL(String url) {
                return url;
            }

            @Override
            public String encodeUrl(String url) {
                return url;
            }

            @Override
            public String encodeURL(String url) {
                return url;
            }
        };
        // process next request in chain
        chain.doFilter(request, wrappedResponse);
	}
	private boolean isFind(String url){
		String regEx="select";
		Pattern p=Pattern.compile(regEx,Pattern.CASE_INSENSITIVE);
		Matcher m=p.matcher(url);
		boolean rs=m.find(); 
		return rs;
	}
	public void destroy() {
		
	}
	public void init(FilterConfig arg0) throws ServletException {
		
	}	
}