package com.jayway.amazon.client.ui;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.jayway.amazon.R;
import com.jayway.amazon.client.util.Constants;
import com.jayway.amazon.client.content.Content;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JayGram extends ListActivity {

    private ProgressDialog dialog;
    private String mLatestUpload;

    private static final int PHOTO_SELECTED = 1;

    private AmazonS3Client s3Client = new AmazonS3Client(
            new BasicAWSCredentials(Constants.ACCESS_KEY_ID,
                    Constants.SECRET_KEY));

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_view);

        List<Content> contentList = new ArrayList<Content>();
        contentList.add(new Content(
                R.drawable.img_20121020_101012,
                "Jenny Nilsson",
                "2012/10/20 10:10",
                "På väg till kallbad" ));

        contentList.add(new Content(
                R.drawable.img_20121013_120559,
                "Jenny Nilsson",
                "2012/10/13 12:05",
                "Höst höst" ));

        contentList.add(new Content(
                R.drawable.img_20120708_095557,
                "Jenny Nilsson",
                "2012/07/08 09:55",
                "Mums" ));

        contentList.add(new Content(
                R.drawable.img_20120704_170415,
                "Jenny Nilsson",
                "2012/07/04 17:04",
                "Jordgubbsplantan blommar!" ));


        ContentAdapter contentAdapter = new ContentAdapter(getApplicationContext(), contentList);
        setListAdapter(contentAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.upload:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PHOTO_SELECTED);
                return true;
            case R.id.showInBrowser:
                if (!TextUtils.isEmpty(mLatestUpload)){
                    new ShowInBrowserTask().execute(mLatestUpload);
                } else {
                    Toast.makeText(this, "No uploads has been made", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private class ShowPhotoTask extends AsyncTask<Uri, Void, Boolean> {

        ProgressDialog dialog;

        String result = "Success";

        protected void onPreExecute() {
            dialog = new ProgressDialog(JayGram.this);
            dialog.setMessage(JayGram.this.getString(R.string.uploading));
            dialog.setCancelable(false);
            dialog.show();
        }

        protected Boolean doInBackground(Uri... uris) {
            boolean result = true;

            if (uris == null || uris.length != 1) {
                return false;
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
                if (!s3Client.doesBucketExist(Constants.getPictureBucket())){
                    s3Client.createBucket(Constants.getPictureBucket());
                }

                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1 );

                // Content type is determined by file extension.
                PutObjectRequest por = new PutObjectRequest(
                        Constants.getPictureBucket(), fileName,
                        new java.io.File(filePath));
                s3Client.putObject(por);
                mLatestUpload = fileName;
            } catch (Exception exception) {
                result = false;
            }


            return result;
        }

        protected void onPostExecute(Boolean result) {
            if (dialog != null && dialog.isShowing()){
                dialog.dismiss();

            }
            if (result) {
                Toast.makeText(JayGram.this, "Upload was successful", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(JayGram.this, "Upload failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ShowInBrowserTask extends AsyncTask<String, Void, Uri> {

        Uri uri = null;

        protected Uri doInBackground(String... strings) {

            try {
                String fileName = strings[0];
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
                        Constants.getPictureBucket(), fileName,
                        expirationDate);

                uri = Uri.parse(url.toURI().toString());

            } catch (Exception exception) {
                // TODO
            }

            return uri;
        }


        protected void onPostExecute(Uri uri) {

            if (uri != null) {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        }
    }

}
