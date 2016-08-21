package com.hpubts50.hpubustrackerserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PhotoHandler extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mHolder;
	private Camera mCamera;
	private String frontPicturePath, backPicturePath;
	private static final String TAG = "HPUBTS_SERVER_APP";
	private static String upLoadServerUri = "http://k2apps.in/test.php";
	private int serverResponseCode = 0;

	public PhotoHandler(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// empty. Take care of releasing the Camera preview in your activity.
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// start preview with new settings
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();

		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}

		mCamera.takePicture(null, null, mPicture);
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile(1);
			if (pictureFile == null) {
				Log.d(TAG, "Error creating media file, check storage permissions: ");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				Bitmap myBitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
				if (MainActivity.camera_info.equals("back")) {
					backPicturePath = pictureFile.getAbsolutePath();
					MainActivity.img_back.setImageBitmap(myBitmap);
					mCamera.release();
					MainActivity.takeBackPicture();
					MainActivity.txt_back.setText("Uploading...");
					new UploadImages().execute(backPicturePath, "back");

				} else {
					frontPicturePath = pictureFile.getAbsolutePath();
					MainActivity.img_front.setImageBitmap(myBitmap);

					// Call Async Task here
					MainActivity.txt_front.setText("Uploading...");
					new UploadImages().execute(frontPicturePath, "front");

				}

			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
		}
	};

	// Return proper file for saving
	private static File getOutputMediaFile(int type) {

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "HPUBTS");
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(TAG, "failed to create directory");
				return null;
			}
		}
		// Create a media file name
		File mediaFile;
		mediaFile = new File(mediaStorageDir.getPath() + File.separator + MainActivity.BusNumber + "_" + MainActivity.camera_info + ".jpg");
		return mediaFile;
	}

	// ASYNC TASK CLASS
	// ASYNC TASK CLASS
	// ASYNC TASK CLASS
	// ASYNC TASK CLASS
	// ASYNC TASK CLASS
	// ASYNC TASK CLASS

	class UploadImages extends AsyncTask<String, Void, String> {

		// DO IN BACKGROUND METHOD
		@Override
		protected String doInBackground(String... params) {
			String frontPath = params[0];
			String photoFlag = params[1];
			uploadFile(frontPath, photoFlag);
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

		}

		private int uploadFile(String sourceFileUri, String photoFlag) {

			String fileName = sourceFileUri;

			HttpURLConnection conn = null;
			DataOutputStream dos = null;
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";
			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 1 * 1024 * 1024;
			File sourceFile = new File(sourceFileUri);

			if (!sourceFile.isFile()) {
				Log.e(TAG, "Source File not exist :" + sourceFileUri);
				return 0;

			} else {
				try {

					// open a URL connection to the Servlet
					FileInputStream fileInputStream = new FileInputStream(sourceFile);
					URL url = new URL(upLoadServerUri);

					// Open a HTTP connection to the URL
					conn = (HttpURLConnection) url.openConnection();
					conn.setDoInput(true); // Allow Inputs
					conn.setDoOutput(true); // Allow Outputs
					conn.setUseCaches(false); // Don't use a Cached Copy
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Connection", "Keep-Alive");
					conn.setRequestProperty("ENCTYPE", "multipart/form-data");
					conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
					conn.setRequestProperty("uploaded_file", fileName);

					dos = new DataOutputStream(conn.getOutputStream());

					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=uploaded_file; filename=" + fileName + ";" + lineEnd);
					dos.writeBytes(lineEnd);

					// create a buffer of maximum size
					bytesAvailable = fileInputStream.available();

					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					buffer = new byte[bufferSize];

					// read file and write it into form...
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

					while (bytesRead > 0) {

						dos.write(buffer, 0, bufferSize);
						bytesAvailable = fileInputStream.available();
						bufferSize = Math.min(bytesAvailable, maxBufferSize);
						bytesRead = fileInputStream.read(buffer, 0, bufferSize);

					}

					// send multipart form data necesssary after file data...
					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

					// Responses from the server (code and message)
					serverResponseCode = conn.getResponseCode();
					String serverResponseMessage = conn.getResponseMessage();

					Log.d(TAG, "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

					if (serverResponseCode == 200) {
						if (photoFlag.equals("front")) {
							MainActivity.txt_front.setText("Uploaded");
						} else {
							MainActivity.txt_back.setText("Uploaded");
						}
					} else {
						if (photoFlag.equals("front")) {
							MainActivity.txt_front.setText("Error");
						} else {
							MainActivity.txt_back.setText("Error");
						}
					}

					// close the streams //
					fileInputStream.close();
					dos.flush();
					dos.close();

				} catch (Exception ex) {
					Log.d(TAG, "Exception Occured in Uploading Photos");
				}

				return serverResponseCode;

			} // End else block
		}
	}

}