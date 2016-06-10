package com.abysmel.exwebrtc.ui;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.abysmel.exwebrtc.R;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Melvin Lobo on 6/8/2016.
 */
public class VideoChatActivitySinch extends BaseActivity {

	///////////////////////////////////////// CLASS MEMBERS ////////////////////////////////////////
	/**
	 * Static definitions
	 */
	public static final String CONTACT = "Contact";
	public static final String INCOMING = "IncomingCall";

	/**
	 * The surface view to show Video on
	 */
	@Bind( R.id.remote_surface_view )
	LinearLayout mRemoteVideoView;

	/**
	 * THe local video
	 */
	@Bind( R.id.local_surface_view )
	RelativeLayout mLocalVideoView;

	/**
	 * Call hangup button
	 */
	@Bind( R.id.hangup )
	ImageButton mHangUpButton;

	/**
	 * The contact with whom we are having this Video chat
	 */
	private String mContact;

	/**
	 * Check if listener has been added
	 */
	private boolean mbAddedListener;

	/**
	 * Check if views have been added
	 */
	private boolean mbVideoViewsAdded;

	///////////////////////////////////////// CLASS METHODS ////////////////////////////////////////

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_video_chat_sinch );
		ButterKnife.bind( this );

		/*
		Get the contact with whom we are chatting
		 */
		Bundle bundle = getIntent().getExtras();
		if ((bundle != null) && (bundle.containsKey( CONTACT )))
			mContact = bundle.getString( CONTACT );
		else {
			Toast.makeText( this, "Contact for Video Chat not specified", Toast.LENGTH_SHORT ).show();
			finish();
			return;
		}

		mHangUpButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				endCall();
			}
		} );

	}

	@Override
	public void onServiceConnected() {
		Call call = getSinchServiceInterface().getCall(mContact);
		if (call != null) {
			if ( !mbAddedListener ) {
				call.addCallListener(new SinchCallListener());
				mbAddedListener = true;
			}
		} else {
			finish();
		}

		updateUI();
	}

	/**
	 * The Call has started. Update the UI
	 *
	 * @author Melvin Lobo
	 */
	private void updateUI() {
		if (getSinchServiceInterface() == null) {
			return; // early
		}

		Call call = getSinchServiceInterface().getCall(mContact);
		if (call != null) {
			if (call.getState() == CallState.ESTABLISHED) {
				final VideoController vc = getSinchServiceInterface().getVideoController();
				if (vc != null) {
					if(!mbVideoViewsAdded) {
						mLocalVideoView.addView( vc.getLocalView() );
						mRemoteVideoView.addView( vc.getRemoteView() );
						mbVideoViewsAdded = true;
					}
				}
			}
		}
	}

	/**
	 * Remove the Video Views
	 *
	 * @author Melvin Lobo
	 */
	private void removeVideoViews() {
		if (getSinchServiceInterface() == null) {
			return; // early
		}

		final VideoController vc = getSinchServiceInterface().getVideoController();
		if (vc != null) {
			if(mbVideoViewsAdded) {
				mLocalVideoView.removeView( vc.getLocalView() );
				mRemoteVideoView.removeView( vc.getRemoteView() );
				mbVideoViewsAdded = false;
			}
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		updateUI();
	}

	private void endCall() {
		Call call = getSinchServiceInterface().getCall(mContact);
		if (call != null) {
			call.hangup();
		}
		finish();
	}


	@Override
	protected void onStop() {
		super.onStop();
		removeVideoViews();
	}

	/////////////////////////////////////////// INNER CLASSES //////////////////////////////////////
	private class SinchCallListener implements VideoCallListener {
		@Override
		public void onCallEnded(Call call) {
			setVolumeControlStream( AudioManager.USE_DEFAULT_STREAM_TYPE);
			endCall();
		}

		@Override
		public void onCallEstablished(Call call) {
			setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
			AudioController audioController = getSinchServiceInterface().getAudioController();
			audioController.enableSpeaker();
			audioController.mute();
		}

		@Override
		public void onCallProgressing(Call call) {
		}

		@Override
		public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
		}

		@Override
		public void onVideoTrackAdded( Call call ) {
			updateUI();
		}
	}
}
