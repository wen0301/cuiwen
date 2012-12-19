package com.xingyun.tag;

import java.io.Writer;
import org.apache.log4j.Logger;
import org.apache.struts2.components.Component;
import com.opensymphony.xwork2.util.ValueStack;

public class XingyunSpecialChar extends Component{
	private static final Logger log = Logger.getLogger(XingyunSpecialChar.class);
	private String value;
	public XingyunSpecialChar(ValueStack stack) {
		super(stack);
	}	
	public boolean start(Writer writer) {
		boolean result = super.start(writer);
		try {			
			writer.write(com.xingyun.util.SpecialCharFilterUtil.encodeToUnicode(value));
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
