package com.xingyun.bean;

import java.util.List;
import java.util.Map;

public class ModuleBean {
	private int id;
	private String name;
	private int seq;
	private int visible;
	private List<Map<Object, Object>> moduleDetailList;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public List<Map<Object, Object>> getModuleDetailList() {
		return moduleDetailList;
	}
	public void setModuleDetailList(List<Map<Object, Object>> moduleDetailList) {
		this.moduleDetailList = moduleDetailList;
	}
	public int getVisible() {
		return visible;
	}
	public void setVisible(int visible) {
		this.visible = visible;
	}
}
