package com.jayway.amazon;

import java.net.URL;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class StartActivity extends Activity {

	private static final int PHOTO_SELECTED = 1;

	private Button photoButton = null;

	private Button showInBrowserButton = null;

	private AmazonS3Client s3Client = new AmazonS3Client(
			new BasicAWSCredentials(Constants.ACCESS_KEY_ID,
					Constants.SECRET_KEY));

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_uploader);

		photoButton = (Button) findViewById(R.id.photo_button);
		photoButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Start the image picker.
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(intent, PHOTO_SELECTED);
			}
		});

		showInBrowserButton = (Button) findViewById(R.id.show_in_browser_button);
		showInBrowserButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				new ShowInBrowserTask().execute();
			}
		});
	}

	// This method is automatically called by the image picker when an image is
	// selected.
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case PHOTO_SELECTED:
			if (resultCode == RESULT_OK) {

				Uri selectedImage = imageReturnedIntent.getData();
				new ShowPhotoTask().execute(selectedImage);
			}
		}
	}

	private class ShowPhotoTask extends AsyncTask<Uri, Void, String> {

		ProgressDialog dialog;

		String result = "Success";

		protected void onPreExecute() {
			dialog = new ProgressDialog(StartActivity.this);
			dialog.setMessage(StartActivity.this.getString(R.string.uploading));
			dialog.setCancelable(false);
			dialog.show();
		}

		protected String doInBackground(Uri... uris) {

			if (uris == null || uris.length != 1) {
				return null;
			}

			// The file location of the image selected.
			Uri selectedImage = uris[0];

			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String filePath = cursor.getString(columnIndex);
			cursor.close();

			// Put the image data into S3.
			try {
				s3Client.createBucket(Constants.getPictureBucket());

				// Content type is determined by file extension.
				PutObjectRequest por = new PutObjectRequest(
						Constants.getPictureBucket(), Constants.PICTURE_NAME,
						new java.io.File(filePath));
				s3Client.putObject(por);
			} catch (Exception exception) {
				result = "Failure";
			}

			return result;
		}

		protected void onPostExecute(String result) {
			// TODO create toast with result
		}
	}

	private class ShowInBrowserTask extends AsyncTask<Void, Void, Uri> {

		Uri uri = null;

		protected Uri doInBackground(Void... voids) {

			try {
				// Ensure that the image will be treated as such.
				// ResponseHeaderOverrides override = new
				// ResponseHeaderOverrides();
				// override.setContentType("image/jpeg");

				// Generate the presigned URL.

				// Added an hour's worth of milliseconds to the current time.
				Date expirationDate = new Date(
						System.currentTimeMillis() + 3600000);
				// GeneratePresignedUrlRequest urlRequest = new
				// GeneratePresignedUrlRequest(
				// Constants.getPictureBucket(), Constants.PICTURE_NAME);
				// urlRequest.setExpiration(expirationDate);
				// urlRequest.setResponseHeaders(override);

				URL url = s3Client.generatePresignedUrl(
						Constants.getPictureBucket(), Constants.PICTURE_NAME,
						expirationDate);

				uri = Uri.parse(url.toURI().toString());

			} catch (Exception exception) {
				// TODO
			}

			return uri;
		}

		protected void onPostExecute(Uri uri) {

			if (uri != null) {
				// Display in Browser.
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
			}
		}
	}
}
