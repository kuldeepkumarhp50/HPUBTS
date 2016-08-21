package com.hpubts50.hpubustrackerserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {


	// CLASS DATA MEMBERS
	private Intent updateintent, camServiceIntent;

	private TextView txt_busname, txt_busno, txt_longitude, txt_lattitude, txt_accuracy, txt_speed, txt_date, txt_time, txt_status, txt_location, txt_locationdatetime, txt_labellat, txt_labellong, txt_labelaccuracy, txt_kmh;
	public static TextView txt_front, txt_back;
	private ImageView img_busicon, img_led;
	public static ImageView img_front, img_back;
	private int BusPosition;
	private static int frontCameraID = 0;
	private static int backCameraID = 0;
	private int m_interval = 90000;
	private float BusSpeed, accuracy;
	public static String BusNumber;
	public static String camera_info;
	private String BusName;
	private String update_date;
	private String update_time;
	private Button btn_takephoto;
	private Handler m_handler;
	private static Camera camera;
	public static FrameLayout frm_preview;
	private static PhotoHandler mPreview;
	private static Context context;
	private boolean timerFlag = false;

	private Date date;
	private static final int IconID[] = { R.drawable.airavat, R.drawable.alaknanda, R.drawable.chaitanya, R.drawable.garud, R.drawable.nandi, R.drawable.neela, R.drawable.pushpak };
	private static final String TAG = "HPUBTS_SERVER_APP";

	// CLASS METHODS
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Prevent phone from sleeping
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Initialize Views
		initializeViews();

		// Get Application Context
		context = getApplicationContext();

		// Getting data from Bundle
		Bundle GotMainBundle = getIntent().getExtras();
		BusName = GotMainBundle.getString("BusName");
		BusPosition = GotMainBundle.getInt("BusPosition");

		// Setting Titles
		setBusTitles();

		// Initialize Timer Handler
		m_handler = new Handler();

		// Check for camera on Device
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			finish();
			Log.d("HPU SERVER", "NO CAMERA FOUND");
		} else {
			frm_preview = (FrameLayout) findViewById(R.id.camera_preview);
			frontCameraID = findCameraID(CameraInfo.CAMERA_FACING_FRONT);
			backCameraID = findCameraID(CameraInfo.CAMERA_FACING_BACK);
			Log.d("HPU SERVER", "CAMERA FOUND");

		}

	}

	private void setBusTitles() {
		txt_busname.setText(BusName.toUpperCase());
		// Getting Bus Numbers
		String busnumbers[] = getResources().getStringArray(R.array.HPU_BusNo);
		BusNumber = busnumbers[BusPosition];
		txt_busno.setText(BusNumber);
		// Setting Bus Icon
		img_busicon.setImageResource(IconID[BusPosition]);

	}

	private void initializeViews() {
		btn_takephoto = (Button) findViewById(R.id.btn_takephoto);
		txt_busname = (TextView) findViewById(R.id.txt_busname);
		txt_busno = (TextView) findViewById(R.id.txt_busno);
		txt_longitude = (TextView) findViewById(R.id.txt_longitude);
		txt_lattitude = (TextView) findViewById(R.id.txt_lattitude);
		txt_speed = (TextView) findViewById(R.id.txt_speed);
		txt_accuracy = (TextView) findViewById(R.id.txt_accuracy);
		img_busicon = (ImageView) findViewById(R.id.img_busicon);
		img_led = (ImageView) findViewById(R.id.img_led);
		img_front = (ImageView) findViewById(R.id.img_front);
		img_back = (ImageView) findViewById(R.id.img_back);
		txt_date = (TextView) findViewById(R.id.txt_date);
		txt_time = (TextView) findViewById(R.id.txt_time);
		txt_status = (TextView) findViewById(R.id.txt_status);
		txt_location = (TextView) findViewById(R.id.txt_location);
		txt_locationdatetime = (TextView) findViewById(R.id.txt_locationdatetime);
		txt_labellat = (TextView) findViewById(R.id.txt_labellat);
		txt_labellong = (TextView) findViewById(R.id.txt_labellong);
		txt_labelaccuracy = (TextView) findViewById(R.id.txt_labelaccuracy);
		txt_back = (TextView) findViewById(R.id.txt_back);
		txt_front = (TextView) findViewById(R.id.txt_front);
		txt_kmh = (TextView) findViewById(R.id.txt_kmh);
		frm_preview = (FrameLayout) findViewById(R.id.camera_preview);

		setTypeface(txt_busname);
		setTypeface(txt_busno);
		setTypeface(txt_longitude);
		setTypeface(txt_lattitude);
		setTypeface(txt_speed);
		setTypeface(txt_accuracy);
		setTypeface(txt_status);
		setTypeface(txt_location);
		setTypeface(txt_locationdatetime);
		setTypeface(txt_labellat);
		setTypeface(txt_labellong);
		setTypeface(txt_labelaccuracy);
		setTypeface(txt_back);
		setTypeface(txt_front);
		setTypeface(txt_kmh);

		btn_takephoto.setOnClickListener(this);

	}

	// Photo Timers

	Runnable m_statusChecker = new Runnable() {
		@Override
		public void run() {
			takeFrontPicture();
			m_handler.postDelayed(m_statusChecker, m_interval);
		}

	};

	public static void takeBackPicture() {
		if (frontCameraID >= 0) {
			if (camera != null) {
				camera.release();
			}
			camera_info = "front";
			camera = getCameraInstance(frontCameraID);
			camera.setDisplayOrientation(90);
			mPreview = new PhotoHandler(context, camera);
			frm_preview.addView(mPreview);

		}
	}

	private void takeFrontPicture() {
		if (backCameraID >= 0) {
			if (camera != null) {
				camera.release();
			}
			camera_info = "back";
			camera = getCameraInstance(backCameraID);
			camera.setDisplayOrientation(90);
			mPreview = new PhotoHandler(context, camera);
			frm_preview.addView(mPreview);

		}
	}

	void startRepeatingTask() {
		m_statusChecker.run();
	}

	void stopRepeatingTask() {
		m_handler.removeCallbacks(m_statusChecker);
	}

	// Method to set Font Type to Compact
	private void setTypeface(TextView textView) {
		Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/compact.ttf");
		textView.setTypeface(custom_font);
	}

	// Check for front and back camera
	private int findCameraID(int ID) {
		int cameraId = -1;
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == ID) {
				cameraId = i;
				break;
			}
		}
		return cameraId;
	}

	// METHOD TO GET INSTANCE OF CAMERA OBJECT
	public static Camera getCameraInstance(int cameraId) {
		Camera c = null;
		try {
			c = Camera.open(cameraId); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private BroadcastReceiver busdetailsreceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			Bundle busdetailbundle = intent.getExtras();
			txt_lattitude.setText(": " + busdetailbundle.getDouble("LATITUDE"));
			txt_longitude.setText(": " + busdetailbundle.getDouble("LONGITUDE"));
			txt_accuracy.setText(": " + busdetailbundle.getFloat("ACCURACY") + " meters");
			txt_speed.setText(String.format("%02d", Math.round(busdetailbundle.getFloat("SPEED"))));
			if (busdetailbundle.getString("ADDRESS") != null) {
				txt_location.setText(busdetailbundle.getString("ADDRESS"));
			} else {
				txt_location.setText("Locating ... ");
			}
		}
	};

	private BroadcastReceiver responsereceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle responsebundle = intent.getExtras();
			if (responsebundle.getString("RESPONSE").equals("NULL")) {
				img_led.setImageResource(R.drawable.led_yellow);
				txt_status.setText("RETRYING");
			} else {
				if (responsebundle.getString("RESPONSE").toLowerCase().equals("xml generated")) {
					img_led.setImageResource(R.drawable.led_green);
					date = new Date();
					SimpleDateFormat format_date = new SimpleDateFormat("dd MMMM yyyy");
					SimpleDateFormat format_time = new SimpleDateFormat("hh:mm:ss a");
					update_date = format_date.format(date);
					update_time = format_time.format(date);

					txt_locationdatetime.setText(update_time + ", " + update_date);
					txt_status.setText("ACTIVE");

					// Starting Take Photo Thread
					if (!timerFlag) {
						startRepeatingTask();
						timerFlag = true;
					}
					
				} else {
					img_led.setImageResource(R.drawable.led_red);
					// txt_status.setText(responsebundle.getString("RESPONSE"));
					txt_status.setText("SERVER ERROR");
				}
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		updateintent = new Intent(this, LocationUploadService.class);
		Bundle busidbundle = new Bundle();
		busidbundle.putString("BUSNUMBER", BusNumber);
		updateintent.putExtras(busidbundle);
		startService(updateintent);
		registerReceiver(busdetailsreceiver, new IntentFilter(LocationUploadService.BUSDETAIL_INTENT_FILTER));
		registerReceiver(responsereceiver, new IntentFilter(LocationUploadService.RESPONSE_INTENT_FILTER));
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(busdetailsreceiver);
		unregisterReceiver(responsereceiver);
		stopRepeatingTask();
		camera.release();
		// stopService(updateintent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_takephoto:

		}
	}

}
