package com.xingyun.tag;

import java.io.Writer;

import org.apache.log4j.Logger;
import org.apache.struts2.components.Component;

import com.opensymphony.xwork2.util.ValueStack;
import com.xingyun.constant.XingyunCommonConstant;

public class XingyunLevel extends Component {
	private static final Logger log = Logger.getLogger(XingyunLevel.class);
	private int lid;
	private int verified;
	private String classname;
	public static final String ELEMENT_SPAN = "<span class=\"%s\" title=\"%s\"></span>";

	public XingyunLevel(ValueStack stack) {
		super(stack);
	}
	
	public boolean start(Writer writer) {
		boolean result = super.start(writer);
		try {			
			writer.write(getLevelResult(lid,verified,classname));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
	public static String getLevelResult(int lid, int verified, String classname) throws Throwable{
		String strLevel = "";
		String strTitle = (lid == XingyunCommonConstant.USER_LEVEL_JINGYING ? "星云精英 | XINGYUN ELITE" : "星云明星 | XINGYUN STAR");
		if(lid == XingyunCommonConstant.USER_LEVEL_JINGYING && verified == XingyunCommonConstant.USER_VERIFIED_NO)
			strLevel = String.format(ELEMENT_SPAN, classname + "0", strTitle);
		else if(lid == XingyunCommonConstant.USER_LEVEL_JINGYING && verified == XingyunCommonConstant.USER_VERIFIED_YES)
			strLevel = String.format(ELEMENT_SPAN, classname + lid, strTitle);
		else if(lid == XingyunCommonConstant.USER_LEVEL_MINGXING)
			strLevel = String.format(ELEMENT_SPAN, classname + lid, strTitle);
		return strLevel;
	}

	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
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
}