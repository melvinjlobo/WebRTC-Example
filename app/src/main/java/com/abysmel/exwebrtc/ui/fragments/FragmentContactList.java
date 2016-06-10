package com.abysmel.exwebrtc.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.abysmel.exwebrtc.R;
import com.abysmel.exwebrtc.constants.Constants;
import com.abysmel.exwebrtc.dao.Contact;
import com.abysmel.exwebrtc.dao.DataMine;
import com.abysmel.exwebrtc.ui.adapter.ContactAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Melvin Lobo on 6/4/2016.
 */
public class FragmentContactList extends Fragment implements ContactAdapter.ContactClickListener {
	//////////////////////////////////////// CLASS MEMBERS /////////////////////////////////////////
	/**
	 * The recycler View
	 */
	@Bind( R.id.recyclerview )
	RecyclerView mRecyclerView = null;

	/**
	 * The empty contacts message
	 */
	@Bind( R.id.empty_text )
	TextView mEmptyContactTextView;

	/**
	 * Add a contact edit
	 */
	@Bind( R.id.contactnameedit )
	EditText mContactNameEditText;

	/**
	 * Add contact button
	 */
	@Bind( R.id.add_contact )
	Button mAddContactButton;

	/**
	 * Contact Adapter
	 */
	private ContactAdapter mAdapter;

	/**
	 * Event listener
	 */
	private ContactListEventListener mEventListener;

	//////////////////////////////////////// CLASS METHODS /////////////////////////////////////////
	/**
	 * Create a new instance of the Fragment
	 *
	 * @author Melvin Lobo
	 */
	public static FragmentContactList newInstance() {
		FragmentContactList frag = new FragmentContactList();
		return frag;
	}

	@Nullable
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contactView = inflater.inflate( R.layout.fragment_contacts, container, false);
		ButterKnife.bind( this, contactView );

		mAddContactButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				addNewContact( v );
			}
		} );

		setupRecyclerView( mRecyclerView );
		return contactView;
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
		ArrayList<Contact> contactList = DataMine.getInstance().getContactList();
		if(contactList.size() == 0)
			showContent( false );
		else
			showContent( true );

		mAdapter = new ContactAdapter( getContext(), contactList, this );
		recyclerView.setAdapter( mAdapter );
	}

	/**
	 * Show empty contacts message
	 *
	 * @author Melvin Lobo
	 */
	private void showContent(boolean bShowList) {
		mEmptyContactTextView.setVisibility( (!bShowList) ? View.VISIBLE : View.GONE );
		mRecyclerView.setVisibility( (bShowList) ? View.VISIBLE : View.GONE );
	}

	/**
	 * Add Contact
	 *
	 * @author Melvin Lobo
	 */
	public void addNewContact(View view) {
		String contact = mContactNameEditText.getText().toString();
		if( !TextUtils.isEmpty( contact )) {
			Contact contactObj = new Contact( contact, Constants.STATUS_OFFLINE );
			DataMine.getInstance().addContact( contactObj );
			showContent( true );
			mAdapter.refreshList( contactObj );
			if(mEventListener != null)
				mEventListener.fetchUserStatus( contact );
		}
	}

	@Override
	public void onClickView( int id, String contactName ) {
		if(id == R.id.delete_contact) {
			DataMine.getInstance().removeContact( contactName );
			if(DataMine.getInstance().getContactList().isEmpty())
				showContent( false );
		}
		else if(id == R.id.row_root) {
			if(mEventListener != null)
				mEventListener.startChat( contactName );
		}
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
			mEventListener = (ContactListEventListener) activity;
		}
	}

	public void presenceUpdate(int position) {
		mAdapter.itemModify( position );
	}


	//////////////////////////////////////// INTERFACE /////////////////////////////////////////////
	public interface ContactListEventListener {
		void fetchUserStatus(String contactName);
		void startChat(String contactName);
	}
}
