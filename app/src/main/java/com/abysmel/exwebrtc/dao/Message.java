package com.abysmel.exwebrtc.dao;

import java.util.Date;

/**
 * Created by Melvin Lobo on 6/5/2016.
 */
public class Message {
	public static enum MSG_TYPE {NONE, SENT, RECEIVED};

	private MSG_TYPE mMessageType = MSG_TYPE.NONE;
	private String mMessage = null;
	private long mTimestamp;
	private String mContactName;

	public Message(String contact, String message) {
		if(DataMine.getInstance().getUserName().equalsIgnoreCase( contact ))
			mMessageType = MSG_TYPE.SENT;
		else
			mMessageType = MSG_TYPE.RECEIVED;

		mContactName = contact;
		mMessage = message;
		mTimestamp = System.currentTimeMillis();
	}

	public Message(String contact, String message, long time) {
		mMessageType = MSG_TYPE.RECEIVED;
		mContactName = contact;
		mMessage = message;
		mTimestamp = System.currentTimeMillis();
	}


	public MSG_TYPE getMessageType() {
		return mMessageType;
	}

	public void setMessageType( MSG_TYPE messageType ) {
		this.mMessageType = messageType;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage( String message ) {
		this.mMessage = message;
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp( long timestamp ) {
		this.mTimestamp = timestamp;
	}

	public String getContactName() {
		return mContactName;
	}

	public void setContactName( String contactName ) {
		this.mContactName = contactName;
	}
}
