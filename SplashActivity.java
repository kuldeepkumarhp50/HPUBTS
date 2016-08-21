package com.hpubts50.hpubustrackerserver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class SplashActivity extends Activity {
	private static boolean BackButtonPressed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		Thread timer = new Thread() {
			public void run() {
				try {
					sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					if (BackButtonPressed != true) {
						Intent BusList = new Intent(SplashActivity.this, BusListActivity.class);
						startActivity(BusList);
					}
				}
			}
		};
		timer.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		BackButtonPressed = false;
	}
	@Override
	protected void onPause() {
		BackButtonPressed = true;
		super.onPause();
		finish();
	}
}
