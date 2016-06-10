package com.abysmel.exwebrtc.dao;

import com.abysmel.exwebrtc.Utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Melvin Lobo on 6/4/2016.
 */
public class DataMine {
	////////////////////////////////////////  CLASS MEMBERS ////////////////////////////////////////
	/**
	 * Static object
	 */
	private static DataMine sDataMine = null;

	/**
	 * Array List of Contacts
	 */
	private ArrayList<Contact> mContactList = new ArrayList<>(  );

	/**
	 * The messages map
	 */
	private HashMap<String, ArrayList<Message>> mMessageMap = new HashMap<>(  );

	/**
	 * The current user
	 */
	private String mUserName;

	////////////////////////////////////////  CLASS METHODS ////////////////////////////////////////
	private DataMine() {}

	/**
	 * Static method to get singleton Object
	 *
	 * @author Melvin Lobo
	 */
	public static synchronized DataMine getInstance() {
		if(sDataMine == null)
			sDataMine = new DataMine();

		return sDataMine;
	}

	public String getUserName() {
		return mUserName;
	}

	public void setUserName( String userName ) {
		this.mUserName = userName;
	}

	public ArrayList<Contact> getContactList() {
		return mContactList;
	}

	/**
	 * Add Contact
	 * @param contact
	 *      The contact to add
	 *
	 * @author Melvin Lobo
	 */
	public synchronized void addContact(Contact contact) {
		if(isExisting( contact.getContactName() ))
			return;

		mContactList.add( contact );

		/*
		Add to shared preferences.
		Note: we have to create a new set since it is forbidden to modify the existing object in shared preferences
		 */
		HashSet<String> newSet = new HashSet<>( SharedPreferencesHelper.getStringSet( SharedPreferencesHelper.KEY_CONTACTS, new HashSet<String>() ) );
		newSet.add( contact.getContactName() );
		SharedPreferencesHelper.putStringSet( SharedPreferencesHelper.KEY_CONTACTS, newSet );
	}

	/**
	 * Remove contact
	 *
	 * @param contactName
	 *      The contact name to remove
	 *
	 * @author Melvin Lobo
	 */
	public synchronized void removeContact(String contactName) {
		if(!isExisting( contactName ))
			return;

		mContactList.remove( getContact( contactName ) );

		/*
		Remove from shared preferences
		Note: we have to create a new set since it is forbidden to modify the existing object in shared preferences
		 */
		HashSet<String> newSet = new HashSet<>(SharedPreferencesHelper.getStringSet( SharedPreferencesHelper.KEY_CONTACTS, new HashSet<String>() ));
		newSet.remove( contactName );
		SharedPreferencesHelper.putStringSet( SharedPreferencesHelper.KEY_CONTACTS, newSet );
	}

	/**
	 * Get a contact by name
	 * @return
	 *      Contact
	 * @author Melvin Lobo
	 */
	public Contact getContact( String contactName ) {
		for(Contact contact : mContactList) {
			if(contact.getContactName().equalsIgnoreCase( contactName ))
				return contact;
		}
		return null;
	}

	/**
	 * Check if the contact exists
	 *
	 * @author Melvin Lobo
	 */
	public boolean isExisting(String contactName) {
		for(Contact contact : mContactList) {
			if(contact.getContactName().equalsIgnoreCase( contactName ))
				return true;
		}
		return false;
	}

	/**
	 * Refresh the status
	 *
	 * @return int
	 *      THe position for which the presence was updated
	 *
	 * @author Melvin Lobo
	 */
	public synchronized int refreshStatus(String contactName, String status) {
		int pos = -1, nCtr = 0;
		for(Contact contact : mContactList) {
			if(contact.getContactName().equalsIgnoreCase( contactName )) {
				contact.setContactStatus( status );
				pos = nCtr;
			}
			nCtr++;
		}

		return pos;
	}

	/**
	 * add a message
	 *
	 * @return Message
	 *      The added message
	 *
	 * @author Melvin Lobo
	 */
	public synchronized Message addMessage(String contact, String message) {
		ArrayList<Message> messageList =  mMessageMap.get( contact );
		Message msg = new Message( contact, message );
		if(messageList == null) {
			messageList = new ArrayList<>();
			messageList.add( msg );
			mMessageMap.put( contact,  messageList);
		}
		else
			messageList.add( msg );

		return msg;
	}

	/**
	 * add a message
	 *
	 * @return Message
	 *      The added message
	 *
	 * @author Melvin Lobo
	 */
	public synchronized Message addMessage(String contact, Message message) {
		ArrayList<Message> messageList =  mMessageMap.get( contact );
		if(messageList == null) {
			messageList = new ArrayList<>();
			messageList.add( message );
			mMessageMap.put( contact,  messageList);
		}
		else
			messageList.add( message );

		return message;
	}

	public synchronized ArrayList<Message> getMessageList(String contact) {
		return mMessageMap.get( contact );
	}

	/**
	 * Clear Data
	 *
	 * @author Melvin Lobo
	 */
	public void clear() {
		if(mContactList != null) {
			mContactList.clear();
			mContactList = null;
		}

		if(mMessageMap != null) {
			mMessageMap.clear();
			mMessageMap = null;
		}

		sDataMine = null;
	}

}
