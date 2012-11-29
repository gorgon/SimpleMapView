package com.gorgon.mapview;

import android.app.Activity;
import android.os.Bundle;

public class SimpleMapViewActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mView = (SimpleMapView)findViewById(R.id.mapview);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mView.destroy();
	}

	// ///////////////////////// PRIVATE SECTION //////////////////////////////
	private SimpleMapView mView;
}