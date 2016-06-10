package com.abysmel.exwebrtc.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.abysmel.exwebrtc.R;
import com.abysmel.exwebrtc.Utils.Logger;
import com.abysmel.exwebrtc.constants.Constants;
import com.abysmel.exwebrtc.dao.DataMine;
import com.abysmel.exwebrtc.dao.Message;
import com.abysmel.exwebrtc.ui.adapter.MessageAdapter;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import java.util.ArrayList;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Melvin Lobo on 6/5/2016.
 */
public class FragmentChat extends Fragment {

	//////////////////////////////////////////// CLASS MEMBERS /////////////////////////////////////
	public static final String CONTACT = "contact";
	/**
	 * The recycler VIew
	 */
	@Bind( R.id.chatrecyclerview )
	RecyclerView mRecyclerView;

	/**
	 * The message edit text
	 */
	@Bind( R.id.sendedit )
	EditText mSendEditText;

	/**
	 * The message button
	 */
	@Bind( R.id.sendbutton )
	Button mSendButton;

	/**
	 * The remote contact text
	 */
	@Bind( R.id.remote_contact )
	TextView mContactTextView;

	/**
	 * The Message Adapter
	 */
	private MessageAdapter mAdapter;

	/**
	 * The contact
	 */
	private String mContact;

	/**
	 * Event listener
	 */
	private ChatEventListener mEventListener;

	/**
	 * The PubNub Object
	 */
	private Pubnub mPubnub;

	//////////////////////////////////////////// CLASS METHODS /////////////////////////////////////
	public static FragmentChat newInstance(String user) {
		Bundle args = new Bundle();
		FragmentChat frag = new FragmentChat();
		args.putString(CONTACT, user);
		frag.setArguments( args );
		return frag;
	}

	@Override
	public void onCreate( @Nullable Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		/*
		We need to add action items in the toolbar
		 */
		setHasOptionsMenu( true );
	}

	@Nullable
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contactView = inflater.inflate( R.layout.fragment_chat, container, false);
		ButterKnife.bind( this, contactView );
		mContact = getArguments().getString( CONTACT );

		Logger.d("Opened chat window for - " + mContact);
		setupRecyclerView( mRecyclerView );

		mSendButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				String msg = mSendEditText.getText().toString();
				Message message = DataMine.getInstance().addMessage( mContact, new Message( DataMine.getInstance().getUserName(), msg, System.currentTimeMillis() ) );
				message.setMessageType( Message.MSG_TYPE.SENT );
				if(msg == null) return;
				if(mAdapter != null)
					mAdapter.refreshList( message );

				mRecyclerView.smoothScrollToPosition( mAdapter.getItemCount() - 1 );

				if(mEventListener != null)
					mEventListener.sendMessage( mContact, message );

				mSendEditText.setText( "" );
			}
		} );

		/*
		Display the contact we're chatting with
		 */
		mContactTextView.setText( String.format( getContext().getResources().getString( R.string.chatting_with ), mContact ) );

		return contactView;
	}

	@Override
	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
		inflater.inflate( R.menu.menu_chat, menu );
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch ( item.getItemId() ) {
			case R.id.action_videomsg :
				startVideoMessage();
				return true;
			case R.id.action_videochat:
				startVideoChat();
				return true;
			default:
				return super.onOptionsItemSelected( item );
		}
	}

	public String getContact() {
		return mContact;
	}

	@Override
	public void onResume() {
		super.onResume();
		hideSoftKeyboard();
	}

	/**
	 * Hide the keyboard
	 *
	 * @author Melvin Lobo
	 */
	public void hideSoftKeyboard() {
		getActivity().getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	/**
	 * Indicate that the user is not available for a chat. It may happen that the user goes
	 * offline
	 *
	 * @author Melvin Lobo
	 */
	private void indicateChatFailure() {
		if(mEventListener != null)
			mEventListener.notAvailableForChat( mContact );
	}

	/**
	 * Set up recycler View
	 *
	 * @param recyclerView
	 *      THe recycler View to setup
	 *
	 * @author Melvin Lobo
	 */
	private void setupRecyclerView(RecyclerView recyclerView) {
		recyclerView.setLayoutManager( new LinearLayoutManager( recyclerView.getContext() ) );
		ArrayList<Message> messageList = DataMine.getInstance().getMessageList( mContact );
		mAdapter = new MessageAdapter( getContext(), messageList, mContact);
		recyclerView.setAdapter( mAdapter );
	}

	public void addMessage(Message msg) {
		mAdapter.refreshList( msg );
		mRecyclerView.smoothScrollToPosition( mAdapter.getItemCount() - 1 );
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
			mEventListener = (ChatEventListener) activity;
			mPubnub  = mEventListener.getPubNub();
		}
	}

	/**
	 * Start the Video chat activity
	 *
	 * @author Melvin Lobo
	 */
	private void startVideoChat() {
		if(mEventListener != null)
			mEventListener.startVideoChat( mContact );
	}

	/**
	 * Start the Video Message activity
	 *
	 * @author Melvin Lobo
	 */
	private void startVideoMessage() {
		if(mEventListener != null)
			mEventListener.startVideoMessage( mContact );
	}

	////////////////////////////////////////// INNER CLASSES ///////////////////////////////////////

	//////////////////////////////////////////// INTERFACES ////////////////////////////////////////
	public interface ChatEventListener {
		void sendMessage(String contact, Message message);
		Pubnub getPubNub();
		void notAvailableForChat(String contact);
		void startVideoChat(String contact);
		void startVideoMessage(String contact);
	}

}
