package com.example.contacts2csv;

public class ContactInfo {
	private String displayName;
	private String mobileNum;
	private String homeNum;
	
	public ContactInfo(String displayName,String mobileNum,String homeNum) {
		this.displayName = displayName;
		this.mobileNum = mobileNum;
		this.homeNum = homeNum;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getMobileNum() {
		return mobileNum;
	}

	public String getHomeNum() {
		return homeNum;
	}
	
	
}
