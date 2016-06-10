package com.abysmel.exwebrtc.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.abysmel.exwebrtc.R;
import com.abysmel.exwebrtc.Utils.Logger;
import com.abysmel.exwebrtc.Utils.SharedPreferencesHelper;
import com.abysmel.exwebrtc.Utils.Util;
import com.abysmel.exwebrtc.constants.Constants;
import com.abysmel.exwebrtc.dao.DataMine;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Melvin Lobo on 6/9/2016.
 */
public class VideoMessageActivity extends AppCompatActivity {

	///////////////////////////////////////////// CLASS MEMBERS ////////////////////////////////////
	/**
	 * Static members
	 */
	public static final String CONTACT = "Contact";
	public static final String IS_UPLOAD = "IsUpLoad";
	public static final String FILE_NAME = "FileName";
	public static final int REQUEST_VIDEO_CAPTURE = 1001;
	public static final int REQUEST_VIDEO_MESSAGE = 1002;
	public static final String RESULT = "VideoMessageActivityResult";
	public static final String RESULT_TYPE_UPLOAD = "VideoMessageActivityResultType";

	/**
	 * THe DropBox instance
	 */
	private static DropboxAPI<AndroidAuthSession> mDBApi = null;

	/**
	 * Notifies if it is an upload or a download
	 */
	private boolean mbIsUpload = false;

	/**
	 * THe File name
	 */
	private String msFileName;

	/**
	 * The contact to upload the file to or download from
	 */
	private String mContact;

	/**
	 * The Video URI for the recorded file to upload
	 */
	private  Uri mUploadVideoUri;

	/**
	 * The Video file to upload
	 */
	private  File mUploadVideoFile;

	/**
	 * Status Text View
	 */
	@Bind( R.id.video_msg_txt )
	TextView mStatusTextView;

	///////////////////////////////////////////// CLASS METHODS ////////////////////////////////////

	@Override
	protected void onCreate( @Nullable Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		Bundle bundle = getIntent().getExtras();
		if ((bundle != null) && (bundle.containsKey( IS_UPLOAD )))
			mbIsUpload = bundle.getBoolean( IS_UPLOAD );
		if ((bundle != null) && (bundle.containsKey( FILE_NAME )))
			msFileName = bundle.getString( FILE_NAME );
		if ((bundle != null) && (bundle.containsKey( CONTACT )))
			mContact = bundle.getString( CONTACT );

		setContentView( R.layout.activity_video_capture );
		ButterKnife.bind( this );

		if(mbIsUpload)
			mStatusTextView.setText( "Uploading..." );
		else
			mStatusTextView.setText( "Downloading..." );

		authenticate();

		/*
		 Show upload or download process depending on the situation
		 */
		if(mbIsUpload)
			showUploadProcess();
		else
			showDownloadProcess();
	}

	/**
	 * Show the upload process. Start recording a video and upload
	 *
	 * @author Melvin Lobo
	 */
	private void showUploadProcess() {
		Intent takeVideoIntent = new Intent( MediaStore.ACTION_VIDEO_CAPTURE);
		mUploadVideoFile = new File( Util.getSDCardFilePath(mContact) );
		Uri uri = Uri.fromFile(mUploadVideoFile);

		takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
		takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
		}
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
			if(!mUploadVideoFile.exists())
				return;

			if(intent != null)
				mUploadVideoUri = intent.getData();
			else
				mUploadVideoUri = Uri.fromFile( mUploadVideoFile );

			/*VideoView videoView = (VideoView) findViewById( R.id.video_record_view);
			videoView.setVideoURI(Uri.parse( mUploadVideoUri.toString()));
			videoView.start();*/
			new UploadToDropbox(mUploadVideoFile.getAbsolutePath()).execute(  );
		}
	}

	/**
	 * Called when upload is completed by AsyncTask
	 *
	 * @param filePath
	 *  The resultant file path
	 *
	 * @author MElvin Lobo
	 */
	private void onUploadComplete(String filePath) {
		Intent resultIntent = new Intent(  );
		resultIntent.putExtra( RESULT, filePath );
		resultIntent.putExtra( RESULT_TYPE_UPLOAD, true );
		resultIntent.putExtra( CONTACT, mContact );
		setResult( Activity.RESULT_OK, resultIntent );
		finish();
	}

	/**
	 * Called when download is completed by AsyncTask
	 *
	 * @param filePath
	 *  The resultant file path
	 *
	 * @author MElvin Lobo
	 */
	private void onDownloadComplete(String filePath) {
		Intent resultIntent = new Intent(  );
		resultIntent.putExtra( RESULT, filePath );
		resultIntent.putExtra( RESULT_TYPE_UPLOAD, false );
		resultIntent.putExtra( CONTACT, mContact );
		setResult( Activity.RESULT_OK, resultIntent );
		finish();
	}

	/**
	 * Show the download process. Download the file and play
	 *
	 * @author Melvin Lobo
	 */
	private void showDownloadProcess() {
		new DownloadFromDropbox(Util.getSDCardFilePath(mContact), Util.getDBFilePath( DataMine.getInstance().getUserName() ) ).execute(  );
	}

	/**
	 * Authenticate with DropBox
	 *
	 * @author Melvin Lobo
	 */
	private void authenticate() {
		/*
		 Create the Auth session. Check if we already have the Key Pair Stored in SharedPref
		 */
		if(mDBApi == null) {
			AndroidAuthSession mOAuthSession;
			AppKeyPair pair = new AppKeyPair( Constants.DB_API, Constants.DB_SECRET );
			mOAuthSession = new AndroidAuthSession( pair );
			mDBApi = new DropboxAPI<>( mOAuthSession );
			mDBApi.getSession().startOAuth2Authentication( this );
		}
	}

	/**
	 * Check if the Auth with DB is done
	 */
	protected void onResume() {
		super.onResume();

		/**
		 * Save the access token on successfull login
		 */
		if (mDBApi.getSession().authenticationSuccessful()) {
			try {
				// Required to complete auth, sets the access token on the session
				mDBApi.getSession().finishAuthentication();
				String accessToken = mDBApi.getSession().getOAuth2AccessToken();
				SharedPreferencesHelper.putString( SharedPreferencesHelper.KEY_DB_TOKEN, accessToken);

			} catch (IllegalStateException e) {
				Logger.d("DropBox Error authenticating - " + e.getMessage());
			}
		}
		else {
			String dbToken = SharedPreferencesHelper.getString( SharedPreferencesHelper.KEY_DB_TOKEN, null );
			if(dbToken != null) {
				mDBApi.getSession().setOAuth2AccessToken( dbToken );
				if (mDBApi.getSession().authenticationSuccessful()) {
					try {
						// Required to complete auth, sets the access token on the session
						mDBApi.getSession().finishAuthentication();
						String accessToken = mDBApi.getSession().getOAuth2AccessToken();
						SharedPreferencesHelper.putString( SharedPreferencesHelper.KEY_DB_TOKEN, accessToken);

					} catch (IllegalStateException e) {
						Logger.d("DropBox Error authenticating - " + e.getMessage());
					}
				}
			}
			else {
				Toast.makeText( this, "Could not authenticate with DB. Please try again", Toast.LENGTH_SHORT ).show();
				finish();
			}
		}
	}

	//////////////////////////////////////// INNER CLASSES /////////////////////////////////////////
	/**
	 * Upload to Dropbox Task
	 *
	 * @author Melvin Lobo
	 */
	public class UploadToDropbox extends AsyncTask<Void, Void, String> {

		/************************************** CLASS MEMBERS *************************************/
		/**
		 * THe file path of the file to upload
		 */
		private String msFilePath;

		/**
		 * Progress Dialog
		 */
		private SweetAlertDialog mProgressDialog;

		/************************************** CLASS METHODS *************************************/
		public UploadToDropbox( String path) {
			msFilePath = path;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mProgressDialog = new SweetAlertDialog( VideoMessageActivity.this, SweetAlertDialog.PROGRESS_TYPE )
			.setTitleText( "Sending" );
			mProgressDialog.setCancelable( false );
			mProgressDialog.getProgressHelper().setBarColor( ContextCompat.getColor( VideoMessageActivity.this, R.color.colorPrimary ) );
			mProgressDialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				File tempFile = new File( msFilePath );
				if(!tempFile.exists()) {
					Logger.d( "File to upload Does not exist - " + msFilePath );
					return null;
				}

				FileInputStream fileInputStream = new FileInputStream(tempFile);
				String fileName =  Util.getFileName(msFilePath);
				mDBApi.putFileOverwrite( fileName , fileInputStream,
						tempFile.length(), null);
				tempFile.delete();

				DropboxAPI.Entry existingEntry = mDBApi.metadata( fileName, 1, null, false, null );
				return fileName;
			} catch (IOException e) {
				Logger.d( "UploadToDropbox AsyncTask IOException Error - " + e.getMessage() );
			} catch (DropboxException e) {
				Logger.d( "UploadToDropbox AsyncTask DropboxException Error - " + e.getMessage() );
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if ( mProgressDialog.isShowing() )
				mProgressDialog.dismiss();

			if ( result != null ) {

				onUploadComplete( result );
			} else {
				setResult( Activity.RESULT_CANCELED );
				finish();
			}
		}
	}

	/**
	 * Download from Dropbox Task
	 *
	 * @author Melvin Lobo
	 */
	public class DownloadFromDropbox extends AsyncTask<Void, Void, String> {

		/************************************** CLASS MEMBERS *************************************/
		/**
		 * THe file path of the file to upload
		 */
		private String msFilePath;

		/**
		 * THe File to get
		 */
		private String msDownloadFileName;

		/**
		 * Progress Dialog
		 */
		private SweetAlertDialog mProgressDialog;


		/************************************** CLASS METHODS *************************************/
		public DownloadFromDropbox( String path, String downloadFileName) {
			msFilePath = path;
			msDownloadFileName = downloadFileName;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mProgressDialog = new SweetAlertDialog( VideoMessageActivity.this, SweetAlertDialog.PROGRESS_TYPE )
					.setTitleText( "Receiving" );
			mProgressDialog.setCancelable( false );
			mProgressDialog.getProgressHelper().setBarColor( ContextCompat.getColor( VideoMessageActivity.this, R.color.colorPrimary ) );
			mProgressDialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				File tempFile = new File( msFilePath );
				if(!tempFile.exists()) {
					Util.createValidPath( msFilePath, true );
					tempFile = new File( msFilePath );
				}

				FileOutputStream outputStream = new FileOutputStream(tempFile);
				String fileName =  msDownloadFileName;
				DropboxAPI.DropboxFileInfo info = mDBApi.getFile(fileName, null, outputStream, null);

				return msFilePath;
			} catch (IOException e) {
				Logger.d( "DownloadFromDropbox AsyncTask IOException Error - " + e.getMessage() );
			} catch (DropboxException e) {
				Logger.d( "DownloadFromDropbox AsyncTask DropboxException Error - " + e.getMessage() );
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if(mProgressDialog.isShowing())
				mProgressDialog.dismiss();

			if (result != null) {
				onDownloadComplete( result );
			} else {
				setResult( Activity.RESULT_CANCELED );
				finish();
			}
		}
	}

}
