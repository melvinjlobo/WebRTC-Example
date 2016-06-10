package com.abysmel.exwebrtc.constants;

/**
 * Created by Melvin Lobo on 6/4/2016.
 */
public class Constants {

	public static boolean isDevMode = true;
	public static final String STDBY_SUFFIX = "-standdby";
	public static final String FILE_ADDER = "_vid_";
	public static final String FILE_EXT = ".mp4";

	/**
	 * Keys
	 */
	public static final String PUB_KEY = ""; // Your Pub Key
	public static final String SUB_KEY = ""; // Your Sub Key
	public static final String SINCH_APP_KEY = ""; //Your Sinch App Key
	public static final String SINCH_APP_SECRET = ""; //Your Sinch App Secret
	public static final String SINCH_ENVIRONMENT = "sandbox.sinch.com"; //Your Sinch Environment
	public static final String DB_API = ""; //Your DropBox App API Key
	public static final String DB_SECRET = ""; //Your DropBox App Secret Key
	public static final String DOWNLOAD_PATH = "ExWebRTC/";

	/**
	 * Received JSON Field names
	 */
	public static final String JSON_CALL_USER = "call_user";
	public static final String JSON_CALL_TIME = "call_time";
	public static final String JSON_VIDEO_TYPE = "video_type";
	public static final String JSON_OCCUPANCY = "occupancy";
	public static final String JSON_STATUS    = "status";
	public static final String JSON_DATA      = "data";
	public static final String JSON_ACTION    = "action";
	public static final String JSON_JOIN      = "join";
	public static final String JSON_LEAVE     = "leave";
	public static final String JSON_TIMEOUT   = "timeout";
	public static final String JSON_STATE_CHANGE      = "state-change";
	public static final String VIDEO_TYPE_MSG = "video_type_msg";
	public static final String VIDEO_MSG_FILE = "video_msg_file";

	/**
	 * Status fields
	 */
	public static final String STATUS_AVAILABLE = "Available";
	public static final String STATUS_OFFLINE   = "Offline";

	/**
	 * Fragment Tags
	 */
	public static final String FRAGMENT_INCOMING = "FRAGMENT_INCOMING";
	public static final String FRAGMENT_CONTACTS = "FRAGMENT_CONTACTS";
	public static final String FRAGMENT_CHAT = "FRAGMENT_CHAT";

	public static final String GLOBAL_ROOM = "globalroom";

	/**
	 * Messaging
	 */
	public static final String MSG_CONTACT = "msgContact";
	public static final String MSG_SENDER = "msgSender";
	public static final String MSG_DATA = "msgData";
}
