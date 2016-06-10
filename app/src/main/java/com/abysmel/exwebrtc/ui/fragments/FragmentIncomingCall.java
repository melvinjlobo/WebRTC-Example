package com.abysmel.exwebrtc.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.abysmel.exwebrtc.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Melvin Lobo  on 6/5/2016.
 */
public class FragmentIncomingCall extends Fragment {

	//////////////////////////////////////// CLASS MEMBERS /////////////////////////////////////////
	/**
	 * Static defns
	 */
	public static final String USER_NAME = "UserName";
	public static final String CALL_ID = "CallID";

	/**
	 * THe call response listener
	 */
	private CallResponseAction mResponseListener;

	/**
	 * THe caller badge
	 */
	@Bind( R.id.caller_badge )
	TextView mCalledBadgeTextView;

	/**
	 * The incoming caller
	 */
	@Bind( R.id.incoming_name )
	TextView mIncomingCallerNameTextView;

	/**
	 * The accept button
	 */
	@Bind( R.id.accept_call )
	Button mAcceptButton;

	/**
	 * The decline button
	 */
	@Bind( R.id.decline_call )
	Button mDeclineButton;

	/**
	 * The contact who is calling
	 */
	private String mContact;

	/**
	 * The Call Id
	 */
	private String mCallId;

	//////////////////////////////////////// CLASS METHODS /////////////////////////////////////////
	public static FragmentIncomingCall newInstance(String user, String callID) {
		Bundle args = new Bundle();
		FragmentIncomingCall frag = new FragmentIncomingCall();
		args.putString(USER_NAME, user);
		args.putString(CALL_ID, callID);
		frag.setArguments( args );
		return frag;
	}


	@Nullable
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View incomingCallView = inflater.inflate( R.layout.fragment_incoming_call, container, false);
		ButterKnife.bind( this, incomingCallView );

		mContact = getArguments().getString( USER_NAME );
		mCallId = getArguments().getString( CALL_ID );
		mIncomingCallerNameTextView.setText( mContact );
		mCalledBadgeTextView.setText( mContact.substring( 0, 1 ).toUpperCase() );

		mAcceptButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				acceptCall( );
			}
		} );

		mDeclineButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				declineCall();
			}
		} );

		return incomingCallView;
	}

	/**
	 * Accept Button click
	 *
	 * @author Melvin Lobo
	 */
	public void acceptCall() {
		if(mResponseListener != null)
			mResponseListener.onCallAction( mContact, mCallId, true );
	}

	/**
	 * Decline Button click
	 *
	 * @author Melvin Lobo
	 */
	public void declineCall() {
		if(mResponseListener != null)
			mResponseListener.onCallAction( mContact, mCallId, false );
	}

	/**
	 * Extract the parent activity and initialize the listener
	 *
	 * @param context
	 * 		The context that this fragment is hosted in (which would be the parent Activity)
	 *
	 * @author Melvin Lobo
	 */
	@Override
	public void onAttach(Context context) {
		super.onAttach( context );

		FragmentActivity activity = (context instanceof FragmentActivity) ? (FragmentActivity) context : null;
		if (activity != null) {
			mResponseListener = (CallResponseAction) activity;
		}
	}

	//////////////////////////////////////// INTERFACES /////////////////////////////////////////
	public interface CallResponseAction {
		void onCallAction(String contact, String callID, boolean bCallAccepted);
	}
}
