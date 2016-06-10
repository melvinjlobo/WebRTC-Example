package com.abysmel.exwebrtc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.abysmel.exwebrtc.Utils.Logger;
import com.abysmel.exwebrtc.dao.Contact;
import com.abysmel.exwebrtc.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Melvin Lobo on 6/4/2016.
 *
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {

	////////////////////////////////////// CLASS MEMBERS ///////////////////////////////////////////
	/**
	 * The list of contacts
	 */
	private ArrayList<Contact> mContactList;

	/**
	 * Weakreference of the context
	 */
	private WeakReference<Context> mContext = null;

	/**
	 * The Click Listener
	 */
	private ContactClickListener mClickListener = null;


	////////////////////////////////////// CLASS METHODS ///////////////////////////////////////////
	public ContactAdapter(Context context, ArrayList<Contact> contactList, ContactClickListener listener) {
		mContactList = (contactList != null) ? contactList : (new ArrayList<Contact>(  ));
		mContext = new WeakReference<>( context );
		mClickListener = listener;
	}

	/**
	 * Refresh the contact list
	 *
	 * @author Melvin Lobo
	 */
	public void refreshList(Contact contact) {
		notifyItemChanged( getItemCount() );
	}

	public void itemModify(int position) {
		notifyItemChanged( position );
	}

	/**
	 * Called when RecyclerView needs a new {@link ContactHolder} of the given type to represent
	 * an item.
	 * <p>
	 * This new ViewHolder should be constructed with a new View that can represent the items
	 * of the given type. You can either create a new View manually or inflate it from an XML
	 * layout file.
	 * <p>
	 * The new ViewHolder will be used to display items of the adapter using
	 * onBindViewHolder. Since it will be re-used to display
	 * different items in the data set, it is a good idea to cache references to sub views of
	 * the View to avoid unnecessary {@link View#findViewById(int)} calls.
	 *
	 * @param parent   The ViewGroup into which the new View will be added after it is bound to
	 *                 an adapter position.
	 * @param viewType The view type of the new View.
	 * @return A new ViewHolder that holds a View of the given view type.
	 * @see #getItemViewType(int)
	 * @see #onBindViewHolder(ContactHolder, int)
	 */
	@Override
	public ContactHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_row, parent, false);
		ContactHolder holder = new ContactHolder(view);
		return holder;
	}

	/**
	 * Called by RecyclerView to display the data at the specified position. This method should
	 * update the contents of the ViewHolderto reflect the item at the given
	 * position.
	 * <p>
	 * Note that unlike {@link ListView}, RecyclerView will not call this method
	 * again if the position of the item changes in the data set unless the item itself is
	 * invalidated or the new position cannot be determined. For this reason, you should only
	 * use the <code>position</code> parameter while acquiring the related data item inside
	 * this method and should not keep a copy of it. If you need the position of an item later
	 * on (e.g. in a click listener), use ViewHolder which will
	 * have the updated adapter position.
	 * <p>
	 * Override onBindViewHolder instead if Adapter can
	 * handle effcient partial bind.
	 *
	 * @param holder   The ViewHolder which should be updated to represent the contents of the
	 *                 item at the given position in the data set.
	 * @param position The position of the item within the adapter's data set.
	 */
	@Override
	public void onBindViewHolder( ContactHolder holder, final int position ) {
		final Contact contact = mContactList.get( position );
		holder.mBadgeTextView.setText( contact.getContactName().substring( 0, 1 ).toUpperCase() );
		holder.mContactNameTextView.setText( contact.getContactName() );
		holder.mCurrentStatusTextView.setText( (!TextUtils.isEmpty( contact.getContactStatus() )) ?
				contact.getContactStatus() : mContext.get().getResources().getString( R.string.offline ));
		holder.mDeleteImageView.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				if(mClickListener != null) {
					mClickListener.onClickView( v.getId(), contact.getContactName() );
					notifyItemRemoved( position );
				}
			}
		} );
		holder.mRootView.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				if(mClickListener != null) {
					Logger.d( "Opening chat window for - " + contact.getContactName() );
					mClickListener.onClickView( v.getId(), contact.getContactName() );
				}
			}
		} );
	}

	/**
	 * Returns the total number of items in the data set hold by the adapter.
	 *
	 * @return The total number of items in this adapter.
	 */
	@Override
	public int getItemCount() {
		return mContactList.size();
	}

	////////////////////////////////////// INNER CLASSES ///////////////////////////////////////////
	/**
	 * The View Holder
	 */
	static class ContactHolder extends RecyclerView.ViewHolder {

		/*********************************** CLASS MEMBERS ****************************************/
		@Bind( R.id.contactname )
		TextView mContactNameTextView;

		@Bind( R.id.status )
		TextView mCurrentStatusTextView;

		@Bind( R.id.badge )
		TextView mBadgeTextView;

		@Bind( R.id.delete_contact )
		ImageView mDeleteImageView;

		View mRootView;

		/*********************************** CLASS METHODS ****************************************/
		public ContactHolder( View view ) {
			super( view );
			ButterKnife.bind( this, view );
			mRootView = view;
		}

	}

	////////////////////////////////////// INTERFACES //////////////////////////////////////////////
	public interface ContactClickListener {
		void onClickView(int id, String contactName);
	}
}
