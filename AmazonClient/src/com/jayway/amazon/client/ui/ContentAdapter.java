package com.jayway.amazon.client.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.jayway.amazon.R;
import com.jayway.amazon.client.content.Content;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jennynilsson
 * Date: 2013-12-03
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
public class ContentAdapter extends ArrayAdapter<Content> {

    /**  */
    public static class ContentHolder {
        public ImageView image;
        public TextView date;
        public TextView comment;
        public TextView user;
    }

    public ContentAdapter(Context context, List<Content> contents){
        super(context, R.layout.list_item, contents);

    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View view = v;

        ContentHolder temp = null;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.list_item, parent, false);
            temp =  new ContentHolder();

            temp.user = (TextView) view.findViewById(R.id.user);
            temp.date = (TextView) view.findViewById(R.id.date);
            temp.comment = (TextView) view.findViewById(R.id.comment);

            temp.image = (ImageView) view.findViewById(R.id.image);

            view.setTag(temp);
        } else {
            temp = (ContentHolder) view.getTag();
        }


        final ContentHolder contentHolder = temp;
        final Content info = getItem(position);
        final String urlTag = info != null ? info.url.toString() : "";

        if (!urlTag.equals(contentHolder.date.getTag())) {
            contentHolder.user.setText(info.name);
            contentHolder.date.setText(info.date);
            contentHolder.comment.setText(info.text);
            contentHolder.image.setImageDrawable(null);

            contentHolder.date.setTag(urlTag);

            new AsyncTask<Void, Void, Drawable>() {

                @Override
                protected Drawable doInBackground(Void... params) {

                    Drawable drawable = null;

                    InputStream is = null;

                    if (!TextUtils.isEmpty(urlTag)){
                        Log.d("JayGram", "URL: " + urlTag);
                        try{
                            is = (InputStream) new URL(urlTag).getContent();
                            drawable = Drawable.createFromStream(is, "src name");
                        } catch (Exception e) {
                            System.out.println("Exc="+e);
                        }
                        finally {
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    return drawable;
                }

                @Override
                protected void onPostExecute(Drawable picture) {
                    if (urlTag.equals(contentHolder.date.getTag())) {
                        contentHolder.image.setImageDrawable(picture);
                        contentHolder.date.invalidate();
                    }
                }
            }.execute();
        }
        return view;
    }
}