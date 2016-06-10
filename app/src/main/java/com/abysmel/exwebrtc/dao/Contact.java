package com.abysmel.exwebrtc.dao;

/**
 * Created by Melvin Lobo on 6/4/2016.
 */
public class Contact {
	/////////////////////////////////////// CLASS MEMBERS //////////////////////////////////////////
	private String msContactName;
	private String msContactStatus;

	/////////////////////////////////////// CLASS METHODS //////////////////////////////////////////
	public Contact(String name, String status) {
		msContactName = name;
		msContactStatus = status;
	}


	public String getContactStatus() {
		return msContactStatus;
	}

	public void setContactStatus( String contactStatus ) {
		msContactStatus = contactStatus;
	}

	public String getContactName() {
		return msContactName;
	}

	public void setContactName( String contactName ) {
		msContactName = contactName;
	}


}
