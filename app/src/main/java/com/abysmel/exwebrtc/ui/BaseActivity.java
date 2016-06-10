package com.abysmel.exwebrtc.ui;

/**
 * Created by Melvin Lobo on 6/8/2016.
 */

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.abysmel.exwebrtc.service.SinchService;

public abstract class BaseActivity extends AppCompatActivity implements ServiceConnection {

	private SinchService.SinchServiceInterface mSinchServiceInterface;
	protected boolean mBound = false;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		getApplicationContext().bindService( new Intent( this, SinchService.class ), this,
				BIND_AUTO_CREATE );
	}

	@Override
	public void onServiceConnected( ComponentName componentName, IBinder iBinder ) {
		if ( SinchService.class.getName().equals( componentName.getClassName() ) ) {
			mSinchServiceInterface = (SinchService.SinchServiceInterface) iBinder;
			onServiceConnected();
			mBound = true;
		}
	}

	@Override
	public void onServiceDisconnected( ComponentName componentName ) {
		if ( SinchService.class.getName().equals( componentName.getClassName() ) ) {
			mSinchServiceInterface = null;
			onServiceDisconnected();
			mBound = false;
		}
	}

	protected void onServiceConnected() {
		// for subclasses
	}

	protected void onServiceDisconnected() {
		// for subclasses
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	protected SinchService.SinchServiceInterface getSinchServiceInterface() {
		return mSinchServiceInterface;
	}

}
