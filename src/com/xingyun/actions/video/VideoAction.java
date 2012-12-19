package com.xingyun.actions.video;

import org.apache.log4j.Logger;

import com.xingyun.base.XingyunBaseAction;
import com.xingyun.util.VideoParse;

public class VideoAction extends XingyunBaseAction {

	private static final long serialVersionUID = -8982639697131741839L;
	private Logger log = Logger.getLogger(VideoParse.class);
	
	private String url;
	
	public void parse() {
		try {
			sendResponseMsg(VideoParse.process(url));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(VideoParse.getErrorJson(""));
		}
	}
	
	public void getSinaVideoJson() {
		try {
			sendResponseMsg(VideoParse.getSinaVideoJson(url));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			sendResponseMsg(VideoParse.getErrorJson(""));
		}
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
