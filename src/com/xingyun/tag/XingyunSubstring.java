package com.xingyun.tag;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.components.Component;

import com.opensymphony.xwork2.util.ValueStack;
import com.xingyun.util.SpecialCharFilterUtil;

public class XingyunSubstring extends Component {

	private static final Logger log = Logger.getLogger(XingyunSubstring.class);

	private String value; // 字符串
	private int maxLength; // 截取的最大长度

	public XingyunSubstring(ValueStack stack) {
		super(stack);
	}

	@Override
	public boolean start(Writer writer) {
		boolean con = super.start(writer);
		try {
			writer.write(subStringValue(value, maxLength));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return con;
	}

	/**
	 * 截取字符长度（两个英文算一个字符）
	 * @param value 字符
	 * @param maxLength 截取的长度
	 */
	private static String subStringValue(String value, int maxLength) {
		if (StringUtils.isBlank(value))
			return value;
		
		value = SpecialCharFilterUtil.decoderSpecialChar(value);
		// 将 * 替换成 空格
		String v1 = value.replaceAll("\\*", " ");
		// 将 非ASCII 替换成 **
		String v2 = v1.replaceAll("[^\\x00-\\xff]", "**");
		int max = maxLength * 2;
		if (v2.length() <= max)
			return SpecialCharFilterUtil.encodeSpecialChar(value); // 加encodeSpecialChar防止前台执行脚本对应
		
		// 截取长度、还原字符数、清楚因截取多出的*
		int length = v2.substring(0, max).replaceAll("\\*\\*", " ").replaceAll("\\*", "").length();
		// 源内容截取
		return SpecialCharFilterUtil.encodeSpecialChar(value.substring(0, length) + "..."); // 加encodeSpecialChar防止前台执行脚本对应
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
