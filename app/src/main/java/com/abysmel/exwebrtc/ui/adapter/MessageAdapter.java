package com.abysmel.exwebrtc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abysmel.exwebrtc.R;
import com.abysmel.exwebrtc.Utils.Logger;
import com.abysmel.exwebrtc.Utils.Util;
import com.abysmel.exwebrtc.dao.DataMine;
import com.abysmel.exwebrtc.dao.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Melvin Lobo on 6/5/2016.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

	////////////////////////////////////// CLASS MEMBERS ///////////////////////////////////////////
	/**
	 * View Types
	 */
	public static final int VIEW_SENT = 0;
	public static final int VIEW_RECEIVED = 1;

	/**
	 * The list of messages
	 */
	private ArrayList<Message> mMessageList;

	/**
	 * Weakreference of the context
	 */
	private WeakReference<Context> mContext = null;

	/**
	 * The contact for chat
	 */
	private String mContact;


	////////////////////////////////////// CLASS METHODS ///////////////////////////////////////////
	public MessageAdapter(Context context, ArrayList<Message> messageList, String contact) {
		mMessageList = (messageList != null) ? messageList : (new ArrayList<Message>(  ));
		mContext = new WeakReference<>( context );
		mContact = contact;
	}

	/**
	 * Refresh the message list
	 *
	 * @author Melvin Lobo
	 */
	public void refreshList(Message message) {
		Logger.d( "List - " + mMessageList.toString() );
		if(mMessageList.isEmpty())
			mMessageList = DataMine.getInstance().getMessageList( mContact );
		notifyItemChanged( getItemCount() );
	}

	@Override
	public MessageHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
		View view;
		if(viewType == VIEW_SENT)
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row_sent, parent, false);
		else if(viewType == VIEW_RECEIVED)
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row_received, parent, false);
		else
			view = null;

		MessageHolder holder = new MessageHolder( view );
		return holder;
	}

	@Override
	public void onBindViewHolder( MessageHolder holder, int position ) {
		final Message message = mMessageList.get( position );
		holder.mBadgeTextView.setText( message.getContactName().substring( 0, 1 ).toUpperCase() );
		holder.mMessageTextView.setText( message.getMessage() );
		holder.mTimeStampTextView.setText( Util.formatTimeStamp(message.getTimestamp()) );
	}

	@Override
	public int getItemViewType( int position ) {
		Message msg = mMessageList.get( position );
		if(msg.getMessageType().equals( Message.MSG_TYPE.SENT ) )
			return VIEW_SENT;
		else if(msg.getMessageType().equals( Message.MSG_TYPE.RECEIVED ) )
			return  VIEW_RECEIVED;
		else
			return -1;
	}

	/**
	 * Returns the total number of items in the data set hold by the adapter.
	 *
	 * @return The total number of items in this adapter.
	 */
	@Override
	public int getItemCount() {
		return ((mMessageList != null) && (!mMessageList.isEmpty())) ? mMessageList.size() : 0;
	}

	////////////////////////////////////// INNER CLASSES ///////////////////////////////////////////
	/**
	 * The View Holder
	 */
	static class MessageHolder extends RecyclerView.ViewHolder {

		/*********************************** CLASS MEMBERS ****************************************/
		@Bind( R.id.message )
		TextView mMessageTextView;

		@Bind( R.id.timestamp )
		TextView mTimeStampTextView;

		@Bind( R.id.badge )
		TextView mBadgeTextView;

		/*********************************** CLASS METHODS ****************************************/
		public MessageHolder( View view ) {
			super( view );
			ButterKnife.bind( this, view );
		}

	}
}
