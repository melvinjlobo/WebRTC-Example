package com.abysmel.exwebrtc.ui;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.abysmel.exwebrtc.R;
import com.abysmel.exwebrtc.Utils.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Melvin Lobo on 06/09/2016.
 */
public class VideoPlayerActivity extends AppCompatActivity {

    ///////////////////////////////////////// CLASS MEMBERS ////////////////////////////////////////
	/**
	 * Static members
	 */
	public static final String FILE_PATH = "FIlePath";

	/**
	 * THe video View
	 */
	@Bind( R.id.video_view )
    VideoView mVideoView;

	/**
	 * THe close button
	 */
	@Bind( R.id.img_close )
    ImageView mImgCloseButton;

	/**
	 * Seek mnPosition
	 */
    private int mnPosition = 0;

	/**
	 * THe media control
	 */
    private MediaController mMediaControl;

	/**
	 * Play Again View
	 */
	@Bind( R.id.play_again)
    TextView mPlayAgainTextView;

	/**
	 * THe file path
	 */
    private String mFilePath;

    ///////////////////////////////////////// CLASS METHODS ////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    Bundle bundle = getIntent().getExtras();
	    if ((bundle != null) && (bundle.containsKey( FILE_PATH )))
            mFilePath = bundle.getString(FILE_PATH);

        setContentView( R.layout.activity_video_player);
	    ButterKnife.bind( this );

        //set the media controller buttons
        if ( mMediaControl == null) {
            mMediaControl = new MediaController(VideoPlayerActivity.this);
        }

        mImgCloseButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoPlayerActivity.this.finish();
            }
        });

        mPlayAgainTextView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoView.seekTo(0);
                mVideoView.start();
                mPlayAgainTextView.setVisibility( View.GONE);
            }
        });

        try {
            /*
            Set the media controller in the VideoView
             */
            mVideoView.setMediaController( mMediaControl );

            /*
            set the uri of the video to be played
             */
            mVideoView.setVideoURI( Uri.parse( mFilePath));
        } catch (Exception e) {
            Logger.d(e.getMessage());
        }

        mVideoView.requestFocus();
        /*
        we also set an setOnPreparedListener in order to know when the video file is ready for playback
         */
        mVideoView.setOnPreparedListener( new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer mediaPlayer) {
                //if we have a mnPosition on savedInstanceState, the video playback should start from here
                mVideoView.seekTo( mnPosition );
                if ( mnPosition == 0) {
                    mVideoView.start();
                } else {
                    //if we come from a resumed activity, video playback will be paused
                    mVideoView.pause();
                }
            }
        });

        mVideoView.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayAgainTextView.setVisibility( View.VISIBLE);
                mPlayAgainTextView.bringToFront();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("Position", mVideoView.getCurrentPosition());
        mVideoView.pause();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mnPosition = savedInstanceState.getInt("Position");
        mVideoView.seekTo( mnPosition );
    }
}
