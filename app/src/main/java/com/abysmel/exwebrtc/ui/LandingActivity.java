package com.abysmel.exwebrtc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abysmel.exwebrtc.R;
import com.abysmel.exwebrtc.Utils.Logger;
import com.abysmel.exwebrtc.Utils.Util;
import com.abysmel.exwebrtc.constants.Constants;
import com.abysmel.exwebrtc.dao.Contact;
import com.abysmel.exwebrtc.dao.DataMine;
import com.abysmel.exwebrtc.dao.Message;
import com.abysmel.exwebrtc.ui.fragments.FragmentChat;
import com.abysmel.exwebrtc.ui.fragments.FragmentContactList;
import com.abysmel.exwebrtc.ui.fragments.FragmentIncomingCall;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Melvin Lobo on 6/4/2016.
 */
public class LandingActivity extends BaseActivity implements FragmentIncomingCall.CallResponseAction,
								FragmentContactList.ContactListEventListener, FragmentChat.ChatEventListener {

	//////////////////////////////////////// CLASS MEMBERS /////////////////////////////////////////
	/**
	 * Static Definitons
	 */
	public static final String USER = "User";

	/**
	 * THe frame layout where the fragment will be housed
	 */
	@Bind( R.id.base_container )
	FrameLayout mFragmentContainerFrameLayout;

	/**
	 * The Toolbar
	 */
	@Bind( R.id.toolbar )
	Toolbar mToolbar;

	/**
	 * The Toolbar Logo
	 */
	@Bind( R.id.toolbar_logo )
	ImageView mToolbarLogoImageView;

	/**
	 * The Toolbar status
	 */
	@Bind( R.id.toolbar_status )
	TextView mToolbarStatusTextView;

	/**
	 * The content container id. it can either be the default container or any id specified in case
	 * the layout has to differ
	 */
	protected int mnContentContainerId = R.id.base_container;

	/**
	 * The entered user name
	 */
	String msUserName = null;

	/**
	 * The PubNub Object
	 */
	private Pubnub mPubNub;

	/**
	 * The Standby String
	 */
	private String mStandByChannelName;

	/**
	 * Set if able to make calls
	 */
	private boolean mbAbleToMakeCalls = false;

	//////////////////////////////////////// CLASS METHODS /////////////////////////////////////////

	@Override
	protected void onCreate( @Nullable Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		Bundle bundle = getIntent().getExtras();
		if(bundle != null) {
			setContentView( R.layout.landing_frame );
			ButterKnife.bind( this );
			setTitle( null );
			msUserName = bundle.getString( USER );
			initPubNub();
			setupToolBar();
			subscribe();
			loadContactListFragment();
		}
		else {
			Toast.makeText( this, getString( R.string.username_not_received ), Toast.LENGTH_SHORT ).show();
			Intent intent = new Intent( this, LoginActivity.class );
			startActivity( intent );
			finish();
			return;
		}
	}

	@Override
	protected void onServiceConnected() {
		mbAbleToMakeCalls = true;
		if (!getSinchServiceInterface().isStarted()) {
			getSinchServiceInterface().startClient( msUserName );
		}
		getSinchServiceInterface().setCallClientListener( new SinchCallClientListener() );
	}

	@Override
	protected void onServiceDisconnected() {
		mbAbleToMakeCalls = false;
	}

	/**
	 * Subscribe to presence and messages
	 *
	 * @author Melvin Lobo
	 */
	private void subscribe() {
		subscribeToContactListPresence();
		subscribeToMessages();
	}

	/**
	 * Set up the action bar
	 */
	private void setupToolBar() {
		setSupportActionBar( mToolbar );
		mToolbarStatusTextView.setText( msUserName );
	}

	/**
	 * Add menu options
	 * @author Melvin Lobo
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_logout:
				logOut();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Subscribe to standby channel so that it doesn't interfere with the WebRTC Signaling
	 *
	 * @author Melvin Lobo
	 */
	public void initPubNub(){
		mStandByChannelName = msUserName + Constants.STDBY_SUFFIX;
		mPubNub  = new Pubnub(Constants.PUB_KEY, Constants.SUB_KEY);
		mPubNub.setUUID(msUserName);
		mPubNub.setHeartbeat( 120 );
		subscribeToStandByChannel();        //declare your presence
	}

	/**
	 * Notify your presence on your channel
	 *
	 * @author Melvin Lobo
	 */
	private void subscribeToStandByChannel(){
		try {
			mPubNub.subscribe( mStandByChannelName, new Callback() {
				@Override
				public void successCallback(String channel, Object message) {
					if (!(message instanceof JSONObject))
						return;

					/*
					 * If there is an incoming call, take it
					 */
					JSONObject jsonMsg = (JSONObject) message;
					try {
						if (!jsonMsg.has(Constants.JSON_CALL_USER)) return;     //Ignore Signaling messages.
						final String user = jsonMsg.getString(Constants.JSON_CALL_USER);
						String type = jsonMsg.getString( Constants.JSON_VIDEO_TYPE );
						if(type.equalsIgnoreCase( Constants.VIDEO_TYPE_MSG )) {
							final String file = jsonMsg.getString( Constants.VIDEO_MSG_FILE );
							if(file == null) return;

							runOnUiThread( new Runnable() {
								@Override
								public void run() {
									String dialogMsg = String.format( getString( R.string.video_msg_dialog ), user );
									final SweetAlertDialog dialog = new SweetAlertDialog( LandingActivity.this, SweetAlertDialog.NORMAL_TYPE );
									dialog.setContentText( dialogMsg )
											.setTitleText( "Video Message" )
											.setCancelText( "No" )
											.setConfirmText( "Yes" )
											.setConfirmClickListener( new SweetAlertDialog.OnSweetClickListener() {
												@Override
												public void onClick( SweetAlertDialog sweetAlertDialog ) {
													dialog.dismiss();
													showVideoMessage(user, file);

												}
											} ).setCancelClickListener( new SweetAlertDialog.OnSweetClickListener() {
										@Override
										public void onClick( SweetAlertDialog sweetAlertDialog ) {
											dialog.dismiss();
										}
									} ).show();
								}
							} );

						}
					} catch (JSONException e){
						e.printStackTrace();
					}
				}

				@Override
				public void connectCallback(String channel, Object message) {
					setMyStatus(Constants.STATUS_AVAILABLE);
				}

				@Override
				public void errorCallback(String channel, PubnubError error) {
				}

			});
		} catch (PubnubException e){
			Logger.d( "PubNubException when subscribing to a channel self - " + e.getMessage() );
		}
	}

	/**
	 * Download and show the video message
	 * @param contact
	 * @param fileName
	 *
	 * @author Melvin Lobo
	 */
	private void showVideoMessage(String contact, String fileName) {
		Intent intent = new Intent( this, VideoMessageActivity.class );
		intent.putExtra( VideoMessageActivity.IS_UPLOAD, false );
		intent.putExtra( VideoMessageActivity.CONTACT, contact );
		intent.putExtra( VideoMessageActivity.FILE_NAME, fileName );
		startActivityForResult( intent, VideoMessageActivity.REQUEST_VIDEO_MESSAGE );
	}

	/**
	 * Upload a video message
	 * @param contact
	 * @param fileName
	 *
	 * @author Melvin Lobo
	 */
	private void uploadVideoMessage(String contact, String fileName) {
		Intent intent = new Intent( this, VideoMessageActivity.class );
		intent.putExtra( VideoMessageActivity.IS_UPLOAD, true );
		intent.putExtra( VideoMessageActivity.CONTACT, contact );
		intent.putExtra( VideoMessageActivity.FILE_NAME, fileName );
		startActivityForResult( intent, VideoMessageActivity.REQUEST_VIDEO_MESSAGE );
	}

	/**
	 * Check if the user is online and make a call.
	 *
	 * @author Melvin Lobo
	 */
	private void makeCall(final String calleeNumber) {
		//Use SInch to make calls
		if(mbAbleToMakeCalls) {
			Call call = getSinchServiceInterface().callUserVideo( calleeNumber );
			startVideoActivity( call.getCallId(), false);
		}
	}

	/**
	 * send Video message
	 *
	 * @author Melvin Lobo
	 */
	private void createVideoMessage(final String contact, final String fileName) {
		JSONObject jsonVideoMsg = new JSONObject();
		try {
			jsonVideoMsg.put( Constants.JSON_CALL_USER, msUserName );
			jsonVideoMsg.put( Constants.JSON_CALL_TIME, System.currentTimeMillis() );
			jsonVideoMsg.put( Constants.JSON_VIDEO_TYPE, Constants.VIDEO_TYPE_MSG );
			jsonVideoMsg.put( Constants.VIDEO_MSG_FILE, fileName );

			sendVideoMessage( contact, jsonVideoMsg );

		} catch ( JSONException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Send Video message
	 *
	 * @author Melvin Lobo
	 */
	private void sendVideoMessage( final String contact, final JSONObject msg ) {
		final String contactChannel = contact + Constants.STDBY_SUFFIX;
		mPubNub.hereNow( contactChannel, new Callback() {
			@Override
			public void successCallback( String channel, Object message ) {
				try {
					/*
					The user is not Online if occupancy is zero
					 */
					int occupancy = 0;
					occupancy = ((JSONObject) message).getInt( Constants.JSON_OCCUPANCY );

					if ( occupancy == 0 ) {
						String presenceMsg = String.format( getString( R.string.not_online ), contact );
						showMessage( presenceMsg );
						return;
					}

					/*
					The user is online, publish the message
					 */
					mPubNub.publish(contactChannel, msg, new Callback() {
						@Override
						public void successCallback(String channel, Object message) {
						}
					});

				}
				catch ( JSONException e ) {
					e.printStackTrace();
				}

			}
		} );
	}

	/**
	 * Set the user status (Available, offline, etc.)
	 *
	 * @param status
	 *      The status to set in PubNub
	 * @author Melvin Lobo
	 */
	private void setMyStatus( String status ) {
		try {
			JSONObject state = new JSONObject();
			state.put(Constants.JSON_STATUS, status);
			mPubNub.setState( mStandByChannelName, msUserName, state, new Callback() {
				@Override
				public void successCallback(String channel, Object message) {
				}
			});
		} catch (JSONException e){
			e.printStackTrace();
		}
	}

	/**
	 * Subscribe to messages in the global room.
	 */
	private void subscribeToMessages() {
		try {
			mPubNub.subscribe( Constants.GLOBAL_ROOM, new Callback() {
				/**
				 * This callback will be invoked when a message is received on the channel
				 *
				 * @param channel Channel Name
				 * @param message
				 */
				@Override
				public void successCallback( String channel, final Object message ) {
					super.successCallback( channel, message );
					runOnUiThread( new Runnable() {
						@Override
						public void run() {
							parseMessage( message );
						}
					} );
				}

				@Override
				public void connectCallback( String channel, Object message ) {
					super.connectCallback( channel, message );
				}

				/**
				 * This callback will be invoked when an error occurs
				 *
				 * @param channel Channel Name
				 * @param error
				 */
				@Override
				public void errorCallback( String channel, PubnubError error ) {
					super.errorCallback( channel, error );
				}
			} );
		} catch ( PubnubException e ) {
			Logger.d( "PubNubException when subscribing to a channels for messages - " + e.getMessage() );
		}
	}

	/**
	 * Parse the incoming message. if a chat window is already open, the redirect the message to it
	 * else open a new window
	 *
	 * @author Melvin Lobo
	 */
	private void parseMessage(Object jsonObj) {
		if ( ! (jsonObj instanceof JSONObject) ) return;
		final Message msg = Util.createIncomingMessage( (JSONObject) jsonObj );
		if(msg == null) return;
		DataMine.getInstance().addMessage( msg.getContactName(), msg );
		FragmentChat frag = (FragmentChat) getSupportFragmentManager().findFragmentByTag( Constants.FRAGMENT_CHAT );
		if ( frag != null ) {
			if ( frag.getContact().equalsIgnoreCase( msg.getContactName() ) )
				frag.addMessage( msg );
			else {
				showIncomingMessageDialog( msg );
			}
		}
		else {
			showIncomingMessageDialog( msg );
		}
	}

	private void showIncomingMessageDialog(final Message msg) {
		final SweetAlertDialog dialog = new SweetAlertDialog( this, SweetAlertDialog.NORMAL_TYPE );
		String chatMsg = String.format(getString( R.string.chat_req_dialog ), msg.getContactName());
		dialog.setContentText( chatMsg )
				.setTitleText( "Chat Request" )
				.setCancelText( "No" )
				.setConfirmText( "Yes" )
				.setConfirmClickListener( new SweetAlertDialog.OnSweetClickListener() {
					@Override
					public void onClick( SweetAlertDialog sweetAlertDialog ) {
						dialog.dismiss();
						getSupportFragmentManager().popBackStack();
						loadChatFragment( msg.getContactName() );
					}
				} ).setCancelClickListener( new SweetAlertDialog.OnSweetClickListener() {
			@Override
			public void onClick( SweetAlertDialog sweetAlertDialog ) {
				dialog.dismiss();
			}
		} ).show();
	}

	/**
	 * Subscribe to contact presence for all contacts in list.
	 * Also get the current status
	 *
	 * @author Melvin Lobo
	 */
	private void subscribeToContactListPresence() {
		ArrayList<Contact> contactList = DataMine.getInstance().getContactList();
		for( final Contact contact : contactList) {
			try {
				mPubNub.presence( Util.getChannelName( contact.getContactName() ), new Callback() {
					/**
					 * This callback will be invoked when an error occurs
					 *
					 * @param channel Channel Name
					 * @param error
					 */
					@Override
					public void errorCallback( String channel, PubnubError error ) {
						super.errorCallback( channel, error );
					}

					/**
					 * This callback will be invoked when a message is received on the channel
					 *
					 * @param channel Channel Name
					 * @param message
					 */
					@Override
					public void successCallback( String channel, Object message ) {
						super.successCallback( channel, message );
						if (!(message instanceof JSONObject)) return;
						final String presence = Util.getPresenceFromJSON( (JSONObject) message );
						if(presence == null)
							return;
						else
							runOnUiThread( new Runnable() {
								@Override
								public void run() {
									updateContactPresence( contact.getContactName(), presence );
								}
							} );
					}

					@Override
					public void reconnectCallback( String channel, Object message ) {
						super.reconnectCallback( channel, message );
					}

					@Override
					public void connectCallback( String channel, Object message ) {
						super.connectCallback( channel, message );
					}

					@Override
					public void disconnectCallback( String channel, Object message ) {
						super.disconnectCallback( channel, message );
						runOnUiThread( new Runnable() {
							@Override
							public void run() {
								updateContactPresence( contact.getContactName(), Constants.STATUS_OFFLINE );
							}
						} );
					}
				} );
			} catch ( PubnubException e ) {
				Logger.d("PubnubException when subscribing for presence - " + e.getMessage());
			}

			/*
			Get the current status too
			 */
			getUserStatus( contact.getContactName() );
		}
	}

	/**
	 * Get the User status
	 * @param contactID
	 *      The contact ID whose status is required
	 *
	 * @author Melvin Lobo
	 */
	private void getUserStatus(final String contactID){
		final String stdByUserChannel = contactID + Constants.STDBY_SUFFIX;
		mPubNub.getState(stdByUserChannel, contactID, new Callback() {
			@Override
			public void successCallback(String channel, Object message) {

				/*
				Extract the presence from the JSON and update it
				 */
				if (!(message instanceof JSONObject)) return;
				JSONObject jsonMsg = (JSONObject) message;
				try {
					if(!jsonMsg.has( Constants.JSON_STATUS )) return;
					final String presence = jsonMsg.getString( Constants.JSON_STATUS );
					runOnUiThread( new Runnable() {
						@Override
						public void run() {
							updateContactPresence( contactID, presence );
						}
					} );

				} catch (JSONException e){
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Update the contact presence
	 *
	 * 1. Update status in Global data
	 * 2. Update presence in the contact list if shown
	 *
	 * @param presence
	 *      The presence to update
	 * @param contact
	 *      The contact whose presence has to be updated
	 *
	 * @author Melvin Lobo
	 */
	private void updateContactPresence(String contact, String presence) {
		final int position = DataMine.getInstance().refreshStatus( contact, presence );
		Fragment frag = getSupportFragmentManager().findFragmentByTag( Constants.FRAGMENT_CONTACTS );
		if(frag != null)
			((FragmentContactList)frag).presenceUpdate( position );
	}

	/**
	 * Ensures that a toast is run on the UI thread with the message
	 * @param message
	 *      The message to show
	 *
	 * @author Melvin Lobo
	 */
	private void showMessage(final String message){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(LandingActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Load the contact list fragment
	 *
	 * @author Melvin Lobo
	 */
	private void loadContactListFragment() {
		performFragmentTransaction( FragmentContactList.newInstance(), Constants.FRAGMENT_CONTACTS, false );
	}

	/**
	 * Load incoming call fragment
	 *
	 * @author Melvin Lobo
	 */
	private void loadIncomingCallFragment(String username, String callId) {
		performFragmentTransaction( FragmentIncomingCall.newInstance( username, callId ), Constants.FRAGMENT_INCOMING, true);
	}

	/**
	 * Load chat fragment
	 *
	 * @author Melvin Lobo
	 */
	private void loadChatFragment(String contactName) {
		performFragmentTransaction( FragmentChat.newInstance( contactName ), Constants.FRAGMENT_CHAT, true);
	}

	/**
	 * Start Video Chat
	 *
	 * @param contact
	 *      THe contact to start the video call with
	 * @param bIncoming
	 *      Notifies if the call is incoming or not
	 * @author Melvin Lobo
	 */
	private void startVideoActivity( String contact, boolean bIncoming) {
		Intent intent = new Intent(this, VideoChatActivitySinch.class);
		intent.putExtra( VideoChatActivitySinch.CONTACT, contact );
		intent.putExtra( VideoChatActivitySinch.INCOMING, bIncoming );
		startActivity( intent );
	}

	/**
	 * Perform a fragment transaction
	 * @param fragmentObject
	 *      The fragment to transact
	 * @param fragmentTag
	 *      THe fragment tag
	 *
	 *  @author Melvin Lobo
	 */
	private void performFragmentTransaction( Fragment fragmentObject, String fragmentTag, boolean bAddToBackStack) {
		FragmentManager fragMgr = getSupportFragmentManager();
		FragmentTransaction transaction = fragMgr.beginTransaction();
		transaction.setCustomAnimations(R.anim.scale_in_from_center, R.anim.frag_slide_out);
		Fragment currentFragment = fragMgr.findFragmentById( mnContentContainerId );
		if (currentFragment == null) {
			transaction.add(mnContentContainerId, fragmentObject, fragmentTag);
		} else {
			transaction.replace(mnContentContainerId, fragmentObject, fragmentTag);
		}

		if(bAddToBackStack)
			transaction.addToBackStack( fragmentTag );

		transaction.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.commit();
	}

	/**
	 * Unsubscribe all users
	 *
	 * @author MElvin Lobo
	 */
	private void unsubscribe() {
		if(mPubNub!=null) {
			mPubNub.unsubscribeAll();
		}
	}

	/**
	 * Sign out from the app
	 *
	 * @author Melvin Lobo
	 */
	private void logOut() {
		unsubscribe();
		Intent intent = new Intent( this, LoginActivity.class );
		startActivity( intent );
		finish();
	}

	@Override
	public void onCallAction( String contact, String callID, boolean bCallAccepted ) {
		getSupportFragmentManager().popBackStack();
		Call call = getSinchServiceInterface().getCall( callID );
		if(bCallAccepted) {
			call.answer();
			startVideoActivity( callID, true );
		}
		else
			call.hangup();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mPubNub!=null){
			mPubNub.unsubscribeAll();
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if(mPubNub==null){
			initPubNub();
		} else {
			subscribeToStandByChannel();
			subscribe();
		}
	}

	@Override
	public void fetchUserStatus( String contactName ) {
		getUserStatus( contactName );
	}

	@Override
	public void startChat( String contactName ) {
		loadChatFragment(contactName);
	}

	@Override
	public void sendMessage( final String contact, final Message msg ) {

		JSONObject json = Util.createOutGoingMessage( contact, msg.getMessage() );
		mPubNub.publish( Constants.GLOBAL_ROOM, json, new Callback() {
			/**
			 * This callback will be invoked when a message is received on the channel
			 *
			 * @param channel Channel Name
			 * @param message
			 */
			@Override
			public void successCallback( String channel, Object message ) {
				super.successCallback( channel, message );
			}

			/**
			 * This callback will be invoked when an error occurs
			 *
			 * @param channel Channel Name
			 * @param error
			 */
			@Override
			public void errorCallback( String channel, PubnubError error ) {
				super.errorCallback( channel, error );
			}
		} );
	}

	@Override
	public Pubnub getPubNub() {
		return mPubNub;
	}

	/**
	 * The user is in the chat window, but the contact is not available for chat.
	 * Pop back stack as we know this is the current chat window
	 * @param contact
	 *      The contact that is not available
	 *
	 * @author Melvin Lobo
	 */
	@Override
	public void notAvailableForChat( String contact ) {
		runOnUiThread( new Runnable() {
			@Override
			public void run() {
				Toast.makeText( LandingActivity.this, getString( R.string.not_avail_chat ), Toast.LENGTH_SHORT ).show();
				getSupportFragmentManager().popBackStack();
			}
		} );
	}

	@Override
	public void startVideoChat( String contact ) {
		makeCall( contact );
	}

	@Override
	public void startVideoMessage( String contact ) {
		uploadVideoMessage(contact, Util.getSDCardFilePath(contact));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == VideoMessageActivity.REQUEST_VIDEO_MESSAGE && resultCode == RESULT_OK) {
			Bundle bundle = intent.getExtras();
			if(bundle == null) return;
			String filename = bundle.getString( VideoMessageActivity.RESULT );
			String contact = bundle.getString( VideoMessageActivity.CONTACT );
			boolean bIsUpload = bundle.getBoolean( VideoMessageActivity.RESULT_TYPE_UPLOAD );
			if(bIsUpload) {
				Toast.makeText( this, getString( R.string.video_sent ), Toast.LENGTH_SHORT ).show();
				createVideoMessage( contact, filename );
			}
			else {
				/*
				Create intent to see video
				 */
				Intent videoIntent = new Intent( this, VideoPlayerActivity.class );
				videoIntent.putExtra( VideoPlayerActivity.FILE_PATH, filename );
				startActivity( videoIntent );
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (getSinchServiceInterface() != null) {
			getSinchServiceInterface().stopClient();
		}

		super.onDestroy();
		unsubscribe();
		DataMine.getInstance().clear();
	}

	//////////////////////////////////////////// INNER CLASSES//////////////////////////////////////
	private class SinchCallClientListener implements CallClientListener {

		@Override
		public void onIncomingCall( CallClient callClient, Call call ) {
			loadIncomingCallFragment( call.getRemoteUserId(), call.getCallId() );
		}
	}

}
