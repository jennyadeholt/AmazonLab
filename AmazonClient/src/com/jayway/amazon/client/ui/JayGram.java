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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jayway.amazon.R;
import com.jayway.amazon.client.util.Constants;
import com.jayway.amazon.client.content.Content;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JayGram extends ListActivity {

    private String mLatestUpload;

    private ContentAdapter mContentAdapter;

    private static final int PHOTO_SELECTED = 1;

    private AmazonS3Client s3Client = new AmazonS3Client(
            new BasicAWSCredentials(Constants.ACCESS_KEY_ID,
                    Constants.SECRET_KEY));

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_view);

        mContentAdapter = new ContentAdapter(getApplicationContext(), new ArrayList<Content>());
        setListAdapter(mContentAdapter);


        new GetBucketInfoTask().execute();

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
                    new GetBucketInfoTask().execute();
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

                ObjectListing objects = s3Client.listObjects(Constants.getPictureBucket());
                for (S3ObjectSummary summery :objects.getObjectSummaries() ){
                    Log.d("JayGram", summery.getStorageClass() + " " + summery.getOwner().getDisplayName());
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
                new GetBucketInfoTask().execute();
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

                Date expirationDate = new Date(
                        System.currentTimeMillis() + 3600000);


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

    private class GetBucketInfoTask extends AsyncTask<String, Void, List<Content>> {

        Uri uri = null;

        protected List<Content> doInBackground(String... strings) {
            List<Content> contents = new ArrayList<Content>();
            try {
                if (s3Client.doesBucketExist(Constants.getPictureBucket())){
                    ObjectListing objects = s3Client.listObjects(Constants.getPictureBucket());
                    for (S3ObjectSummary summary :objects.getObjectSummaries() ){
                        contents.add(new Content(
                                s3Client.generatePresignedUrl(
                                        Constants.getPictureBucket(), summary.getKey(),
                                        new Date(System.currentTimeMillis() + 3600000)),
                                summary.getOwner().getDisplayName(),
                                summary.getLastModified().toLocaleString(),
                                "" ));
                    }
                }
            } catch (Exception exception) {

            }
            return contents;
        }

        @Override
        protected void onPostExecute(List<Content> contents) {
            super.onPostExecute(contents);
            mContentAdapter.addAll(contents);
        }
    }
}
