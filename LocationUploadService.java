package com.hpubts50.hpubustrackerserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class LocationUploadService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener, com.google.android.gms.location.LocationListener {

	String busnumber;
	private boolean UploadingDone;

	LocationClient mLocationClient;
	LocationRequest request;
	Intent busdetailintent, responseintent;

	private static final String TAG = "HPUBTS_SERVER_APP";
	public static final String BUSDETAIL_INTENT_FILTER = "com.hpubts50.hpubustrackerserver.BUSDETAIL";
	public static final String RESPONSE_INTENT_FILTER = "com.hpubts50.hpubustrackerserver.RESPONSE";

	@Override
	public void onCreate() {
		super.onCreate();
		UploadingDone = true;
		busdetailintent = new Intent(BUSDETAIL_INTENT_FILTER);
		responseintent = new Intent(RESPONSE_INTENT_FILTER);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Extracting bus ID from intent
		Bundle busbundle = intent.getExtras();
		busnumber = busbundle.getString("BUSNUMBER");

		// Location Client
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification startnotify = new Notification(R.drawable.busicon, getText(R.string.ticker_text), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, TerminateService.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		startnotify.setLatestEventInfo(this, getText(R.string.ticker_text), getText(R.string.ticker_sub_text), pendingIntent);

		startForeground(555, startnotify);

		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mLocationClient.removeLocationUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			double pLong = location.getLongitude();
			double pLat = location.getLatitude();
			String locality = null;
			// Code to determine area name

			Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
			try {
				List<Address> addresses = geocoder.getFromLocation(pLat, pLong, 1);
				if (addresses != null) {
					Address obj = addresses.get(0);

					if (obj.getAddressLine(0) != null) {
						locality = obj.getAddressLine(0);
					}

					if (obj.getLocality() != null) {
						locality = locality + ", " + obj.getLocality(); // Shimla
					}

					if (obj.getAdminArea() != null) {
						locality = locality + ", " + obj.getAdminArea(); // Himachal
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			float speed = location.getSpeed();
			float accuracy = location.getAccuracy();

			// 1m/s = 3.6km/h
			speed = speed * (3.6f);

			// Preparing Data Bundle
			Bundle busdetailbundle = new Bundle();
			busdetailbundle.putDouble("LATITUDE", pLat);
			busdetailbundle.putDouble("LONGITUDE", pLong);
			busdetailbundle.putFloat("ACCURACY", accuracy);
			busdetailbundle.putFloat("SPEED", speed);
			busdetailbundle.putString("ADDRESS", locality);
			busdetailintent.putExtras(busdetailbundle);

			// Broadcasting Updates
			sendBroadcast(busdetailintent);

			// Start Sending Data to Server in Background
			if (UploadingDone) {
				UploadingDone = false;

				new SendPostReqAsyncTask().execute(String.valueOf(pLat), String.valueOf(pLong), busnumber, Float.toString(speed), Float.toString(accuracy), locality);
			}

		}

	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.e(TAG, "Provider Disabled : " + provider);

	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.e(TAG, "Provider Enabled : " + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.e(TAG, "Provider : " + provider + " Status : " + status);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.e(TAG, "ConnectionFailed ");

	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.e(TAG, "Connected to Location Services. ");
		Toast.makeText(this, "Connected to Location Services", Toast.LENGTH_SHORT).show();
		request = LocationRequest.create();
		request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		request.setInterval(5000);
		request.setFastestInterval(5000);
		mLocationClient.requestLocationUpdates(request, this);

	}

	@Override
	public void onDisconnected() {
		Log.e(TAG, "Disconnected ");

	}

	// ASYNC TASK CLASS
	// ASYNC TASK CLASS
	// ASYNC TASK CLASS
	// ASYNC TASK CLASS
	// ASYNC TASK CLASS
	// ASYNC TASK CLASS

	class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

		// DO IN BACKGROUND METHOD
		@Override
		protected String doInBackground(String... params) {

			String lat = params[0];
			String lon = params[1];
			String busid = params[2];
			String accel = params[3];
			String accuracy = params[4];
			String address = params[5];
			final String SERVER_URL = "http://k2apps.in/";

			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(SERVER_URL);

			BasicNameValuePair latBasicNameValuePair = new BasicNameValuePair("latt", lat);
			BasicNameValuePair longBasicNameValuePAir = new BasicNameValuePair("long", lon);
			BasicNameValuePair busIdBasicNameValuePAir = new BasicNameValuePair("bus_id", busid);
			BasicNameValuePair accelBasicNameValuePAir = new BasicNameValuePair("accel", accel);
			BasicNameValuePair accuracyBasicNameValuePAir = new BasicNameValuePair("accuracy", accuracy);
			BasicNameValuePair addressBasicNameValuePAir = new BasicNameValuePair("address", address);

			List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
			nameValuePairList.add(latBasicNameValuePair);
			nameValuePairList.add(longBasicNameValuePAir);
			nameValuePairList.add(busIdBasicNameValuePAir);
			nameValuePairList.add(accelBasicNameValuePAir);
			nameValuePairList.add(accuracyBasicNameValuePAir);
			nameValuePairList.add(addressBasicNameValuePAir);

			try {

				UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);

				httpPost.setEntity(urlEncodedFormEntity);

				try {
					HttpResponse httpResponse = httpClient.execute(httpPost);
					InputStream inputStream = httpResponse.getEntity().getContent();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
					StringBuilder stringBuilder = new StringBuilder();
					String bufferedStrChunk = null;
					while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
						stringBuilder.append(bufferedStrChunk);
					}
					return stringBuilder.toString();

				} catch (ClientProtocolException cpe) {
					cpe.printStackTrace();
				} catch (IOException ioe) {
					System.out.println("Second Exception caz of HttpResponse :" + ioe);
					ioe.printStackTrace();
				}

			} catch (UnsupportedEncodingException uee) {
				Log.e(TAG, "An Exception given because of UrlEncodedFormEntity argument :" + uee);
				uee.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Bundle responsebundle = new Bundle();

			if (result != null) {
				responsebundle.putString("RESPONSE", result);
			} else {
				responsebundle.putString("RESPONSE", "NULL");
			}

			responseintent.putExtras(responsebundle);
			sendBroadcast(responseintent);

			// Uploading to web server done
			UploadingDone = true;

		}
	}
}
