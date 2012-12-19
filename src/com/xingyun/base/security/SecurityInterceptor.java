package com.xingyun.base.security;

import org.apache.log4j.Logger;
import org.apache.struts2.StrutsStatics;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.xingyun.base.AuthUserAction;
import com.xingyun.base.XingyunBaseAction;
import com.xingyun.bean.User;
import com.xingyun.constant.XingyunCommonConstant;
import com.xingyun.util.CommonUtil;

public class SecurityInterceptor extends AbstractInterceptor implements StrutsStatics {
	
	private static final long serialVersionUID = -6956411468906131781L;
	private static final Logger log=Logger.getLogger(SecurityInterceptor.class);
	public String intercept(ActionInvocation invocation){	
		try {
			final Object action = invocation.getAction();
			XingyunBaseAction baseAction = (XingyunBaseAction)action;
			if(baseAction.getWKey() != null)
				baseAction.setUserid(CommonUtil.getUserIdBywKey(baseAction.getWKey()));
			if (action instanceof AuthUserAction){			
				User user = baseAction.getUser();
				if(user == null)					
					return "relogin";
				if(user.getLid() == XingyunCommonConstant.USER_LEVEL_YOUKE)
					return "del";
			}
			return invocation.invoke();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return "del";
		}
	}
}