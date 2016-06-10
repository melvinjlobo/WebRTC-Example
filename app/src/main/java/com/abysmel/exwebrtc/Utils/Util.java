package com.abysmel.exwebrtc.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.abysmel.exwebrtc.Application.ExWRTCApplication;
import com.abysmel.exwebrtc.constants.Constants;
import com.abysmel.exwebrtc.dao.DataMine;
import com.abysmel.exwebrtc.dao.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by a1019261 on 6/5/2016.
 */
public class Util {

	/**
	 * Format the date time into something meaningful
	 *
	 * @param timeStamp
	 *      The timestamp tp format
	 *
	 * @return
	 *      The formatted timestamp
	 *
	 * @author Melvin Lobo
	 */
	public static String formatTimeStamp(long timeStamp){
		// Create a DateFormatter object for displaying date in specified format.
		SimpleDateFormat formatter = new SimpleDateFormat("h:mm.ss a");

		// Create a calendar object that will convert the date and time value in milliseconds to date.
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeStamp);
		return formatter.format(calendar.getTime());
	}

	/**
	 * Check internet connectivity
	 *
	 * @author Melvin Lobo
	 */
	public static boolean isNetworkConnectionAvailable() {
		ConnectivityManager cm = (ConnectivityManager) ExWRTCApplication.getGlobalContext().getSystemService( Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
	}

	/**
	 * Get a list of TURN and STUN servers from XirSysIceServers
	 * NOte: You will need an account on XirSys for this
	 * @return
	 *      The list of STUN and TURN servers
	 *
	 * @author Melvin Lobo
	 */
	/*public static List<PeerConnection.IceServer> getXirSysIceServers(){
		List<PeerConnection.IceServer> servers = new ArrayList<PeerConnection.IceServer>();
		try {
			servers = new XirSysRequestTask().execute().get();
		} catch (InterruptedException e){
			Logger.d("InterruptedException when trying to connect to XirSYs - " + e.getMessage());
		}catch (ExecutionException e){
			Logger.d("ExecutionException when trying to connect to XirSYs - " + e.getMessage());
		}
		return servers;
	}*/

	/**
	 * Get the standby channel name
	 *
	 * @author Melvin Lobo
	 */
	public static String getChannelName(String contact) {
		return contact + Constants.STDBY_SUFFIX;
	}

	/**
	 * Create outgoing message
	 *
	 * @author Melvin Lobo
	 */
	public static JSONObject createOutGoingMessage(String contact, String message) {
		JSONObject messageJSON = new JSONObject();

		try {
			messageJSON.put( Constants.MSG_CONTACT, contact );
			messageJSON.put( Constants.MSG_DATA, message );
			messageJSON.put( Constants.MSG_SENDER, DataMine.getInstance().getUserName() );

		} catch ( JSONException e ) {
			Logger.d( "JSONException when converting outgoing message to JSON.. Message" + message + ", Contact - " + contact );
		}
		return messageJSON;
	}

	/**
	 * Parse Incoming Message
	 *
	 * @author Melvin Lobo
	 */
	public static Message createIncomingMessage(JSONObject jsonObj) {
		Message message = null;
		try {
			if ( ! (jsonObj instanceof JSONObject) ) return null;
			if ( (! jsonObj.has( Constants.MSG_CONTACT )) || (! (jsonObj.has( Constants.MSG_DATA ))) )
				return null;

			String contact = jsonObj.getString( Constants.MSG_CONTACT );
			String msgData = jsonObj.getString( Constants.MSG_DATA );
			String msgSender = jsonObj.getString( Constants.MSG_SENDER );

			/*
			 Ignore the message if it is from the logged in user himself or ignore if it is not for
			 the logged in user
			 */
			if((msgSender.equalsIgnoreCase( DataMine.getInstance().getUserName() ))
				||
				(!contact.equalsIgnoreCase( DataMine.getInstance().getUserName() )))
				message = null;
			else {
				message = new Message( msgSender, msgData );
				message.setMessageType( Message.MSG_TYPE.RECEIVED );
			}
		} catch ( JSONException e ) {
			Logger.d( "JSONException when converting Incoming message to Object - " + jsonObj.toString() );
		}
		return message;
	}


	/**
	 * Extract the presence from the JSON
	 *
	 * @author Melvin Lobo
	 */
	public static String getPresenceFromJSON(JSONObject jsonObject) {
		try {
			if (!(jsonObject instanceof JSONObject)) return null;

			if (jsonObject instanceof JSONObject) {
				final JSONObject messageJSON = (JSONObject) jsonObject;
				if(!messageJSON.has("action")){
					Logger.d("The presence has no value because its a status message");
					return null;
				}
				Logger.d(messageJSON.toString());

				String action = messageJSON.getString(Constants.JSON_ACTION);

				if (action.equals(Constants.JSON_JOIN)) {
					return Constants.STATUS_AVAILABLE;
				} else if ((action.equals(Constants.JSON_STATE_CHANGE) == true) || (messageJSON.has(Constants.JSON_DATA))) {
					JSONObject dataJSON = messageJSON.getJSONObject( Constants.JSON_DATA );
					if(dataJSON instanceof JSONObject) {
						String presence = dataJSON.getString( Constants.JSON_STATUS );
						return (( TextUtils.isEmpty( presence )) ? null : presence);
					}
					else {
						return null;
					}
				} else if ((action.equalsIgnoreCase( Constants.JSON_LEAVE )) || (
						action.equalsIgnoreCase( Constants.JSON_TIMEOUT )
				)) {
					return Constants.STATUS_OFFLINE;
				}
			}
		}
		catch ( JSONException e ) {
			Logger.d( "JSONException when converting Presence from JSON - " + jsonObject.toString() );
		}

		return null;
	}

	/**
	 * Get the file name froma path
	 * @param path
	 * @return
	 *
	 * @author Melvin Lobo
	 */
	public static String getFileName(String path) {
		return path.substring(path.lastIndexOf("/"));
	}

	/**
	 * Create a valid path for the downloaded file to be stored
	 *
	 * @param sFullPath          The path to store the file to
	 * @param bHasFileNameInPath True if the path has a filename attached to its end
	 * @author Melvin Lobo
	 */
	public static File createValidPath( String sFullPath, boolean bHasFileNameInPath) {
		//Get the directory path by excluding the file name
		String sPath = (bHasFileNameInPath) ? sFullPath.substring(0, sFullPath.lastIndexOf("/")) : sFullPath;

		//Split the path to get the directory
		String[] pathArray = sPath.split("/");

		File root = null;
		for (String part : pathArray) {
			if (root == null) {
				root = new File( Environment.getExternalStorageDirectory(), part);
			} else {
				root = new File(root, part);
			}

			if (!root.exists()) {
				root.mkdirs();
			}
		}

		return root;
	}

	/**
	 * Get the file path on Sd Card
	 * @return
	 *      THe file path on sd card made up of contact,etc.
	 * @author Melvin Lobo
	 */
	public static String getSDCardFilePath(String contact) {
		return Environment.getExternalStorageDirectory() + File.separator + Constants.FILE_ADDER + contact + Constants.FILE_EXT;
	}

	/**
	 * Get DropBox file name
	 *
	 * @author Melvin Lobo
	 */
	public static String getDBFilePath(String contact) {
		return File.separator + Constants.FILE_ADDER + contact + Constants.FILE_EXT;
	}

}
