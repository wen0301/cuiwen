package com.xingyun.init;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class InitContent implements ServletContextListener{
	private static final Logger log = Logger.getLogger(InitContent.class);
	
	public void contextInitialized(ServletContextEvent arg0) {
		try {
//			XingyunNginx.initJsCssVersion();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void contextDestroyed(ServletContextEvent arg0) {
	}
}
