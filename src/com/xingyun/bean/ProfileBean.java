package com.xingyun.bean;

import java.util.List;
import java.util.Map;

public class ProfileBean {
	private String realname;
	private String englishname;
	private String blogurl;
	private String nation;
	private String birthday;
	private String birthday_profile;
	private int birthday_status;
	private String constellation;
	private String constellation_up;
	private String height;
	private String weight;
	private int shape1;
	private int shape2;
	private int shape3;
	private String blood;
	private Map<Object,Object> locationMap;
	private int provinceid;
	private int cityid;
	private Map<Object,Object> locationMap_born;
	private int provinceid_born;
	private int cityid_born;
	private String school;
	private int school_status;
	private String language;
	private String delegate;
	private String company;
	private String broker;
	private String interest;
	private String agency;
	private int gender;
	private String wholebodypic;
	
	/** 以下为帐号设置里联系方式属性 */
	private String mobile;
	private String email;
	private String email_old;
	private String qq;
	private String weixin;
	private String msn;
	private String brokertel;
	private String assistanttel;
	private String others;
	private String express;
	private List<String> contactStatusList; // 联系方式显示与否状态列
	
	/** 以下为帐号设置属性 */
	private String nickName;
	private String nickName_Original;
	private String wkey;
	public String getRealname() {
		return realname;
	}
	public void setRealname(String realname) {
		this.realname = realname;
	}
	public String getEnglishname() {
		return englishname;
	}
	public void setEnglishname(String englishname) {
		this.englishname = englishname;
	}
	public String getBlogurl() {
		return blogurl;
	}
	public void setBlogurl(String blogurl) {
		this.blogurl = blogurl;
	}
	public String getNation() {
		return nation;
	}
	public void setNation(String nation) {
		this.nation = nation;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getConstellation() {
		return constellation;
	}
	public void setConstellation(String constellation) {
		this.constellation = constellation;
	}
	public String getConstellation_up() {
		return constellation_up;
	}
	public void setConstellation_up(String constellation_up) {
		this.constellation_up = constellation_up;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getBlood() {
		return blood;
	}
	public void setBlood(String blood) {
		this.blood = blood;
	}
	public Map<Object, Object> getLocationMap() {
		return locationMap;
	}
	public void setLocationMap(Map<Object, Object> locationMap) {
		this.locationMap = locationMap;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public int getSchool_status() {
		return school_status;
	}
	public void setSchool_status(int school_status) {
		this.school_status = school_status;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getDelegate() {
		return delegate;
	}
	public void setDelegate(String delegate) {
		this.delegate = delegate;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getBroker() {
		return broker;
	}
	public void setBroker(String broker) {
		this.broker = broker;
	}
	public String getInterest() {
		return interest;
	}
	public void setInterest(String interest) {
		this.interest = interest;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getQq() {
		return qq;
	}
	public void setQq(String qq) {
		this.qq = qq;
	}
	public String getMsn() {
		return msn;
	}
	public void setMsn(String msn) {
		this.msn = msn;
	}
	public String getBrokertel() {
		return brokertel;
	}
	public void setBrokertel(String brokertel) {
		this.brokertel = brokertel;
	}
	public String getAssistanttel() {
		return assistanttel;
	}
	public void setAssistanttel(String assistanttel) {
		this.assistanttel = assistanttel;
	}
	public String getOthers() {
		return others;
	}
	public void setOthers(String others) {
		this.others = others;
	}
	public int getBirthday_status() {
		return birthday_status;
	}
	public void setBirthday_status(int birthday_status) {
		this.birthday_status = birthday_status;
	}
	public int getShape1() {
		return shape1;
	}
	public void setShape1(int shape1) {
		this.shape1 = shape1;
	}
	public int getShape2() {
		return shape2;
	}
	public void setShape2(int shape2) {
		this.shape2 = shape2;
	}
	public int getShape3() {
		return shape3;
	}
	public void setShape3(int shape3) {
		this.shape3 = shape3;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public int getProvinceid() {
		return provinceid;
	}
	public void setProvinceid(int provinceid) {
		this.provinceid = provinceid;
	}
	public int getCityid() {
		return cityid;
	}
	public void setCityid(int cityid) {
		this.cityid = cityid;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getAgency() {
		return agency;
	}
	public void setAgency(String agency) {
		this.agency = agency;
	}
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public String getWkey() {
		return wkey;
	}
	public void setWkey(String wkey) {
		this.wkey = wkey;
	}
	public String getExpress() {
		return express;
	}
	public void setExpress(String express) {
		this.express = express;
	}
	public String getNickName_Original() {
		return nickName_Original;
	}
	public void setNickName_Original(String nickName_Original) {
		this.nickName_Original = nickName_Original;
	}
	public String getEmail_old() {
		return email_old;
	}
	public void setEmail_old(String email_old) {
		this.email_old = email_old;
	}
	public String getBirthday_profile() {
		return birthday_profile;
	}
	public void setBirthday_profile(String birthday_profile) {
		this.birthday_profile = birthday_profile;
	}
	public Map<Object, Object> getLocationMap_born() {
		return locationMap_born;
	}
	public void setLocationMap_born(Map<Object, Object> locationMap_born) {
		this.locationMap_born = locationMap_born;
	}
	public int getProvinceid_born() {
		return provinceid_born;
	}
	public void setProvinceid_born(int provinceid_born) {
		this.provinceid_born = provinceid_born;
	}
	public int getCityid_born() {
		return cityid_born;
	}
	public void setCityid_born(int cityid_born) {
		this.cityid_born = cityid_born;
	}
	public String getWholebodypic() {
		return wholebodypic;
	}
	public void setWholebodypic(String wholebodypic) {
		this.wholebodypic = wholebodypic;
	}
	public String getWeixin() {
		return weixin;
	}
	public void setWeixin(String weixin) {
		this.weixin = weixin;
	}
	public List<String> getContactStatusList() {
		return contactStatusList;
	}
	public void setContactStatusList(List<String> contactStatusList) {
		this.contactStatusList = contactStatusList;
	}
}
