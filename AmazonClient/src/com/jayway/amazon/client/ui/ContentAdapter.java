package com.jayway.amazon.client.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.jayway.amazon.R;
import com.jayway.amazon.client.content.Content;

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


    private ContentHolder mContentHolder;

    public ContentAdapter(Context context, List<Content> contents){
        super(context, R.layout.list_item, contents);

    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View view = v;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.list_item, parent, false);
            mContentHolder =  new ContentHolder();

            mContentHolder.user = (TextView) view.findViewById(R.id.user);
            mContentHolder.date = (TextView) view.findViewById(R.id.date);
            mContentHolder.comment = (TextView) view.findViewById(R.id.comment);

            mContentHolder.image = (ImageView) view.findViewById(R.id.image);

            view.setTag(mContentHolder);
        } else {
            mContentHolder = (ContentHolder) view.getTag();
        }

        final Content info = getItem(position);
        if (info != null) {
            mContentHolder.user.setText(info.name);
            mContentHolder.date.setText(info.date);
            mContentHolder.comment.setText(info.text);
            mContentHolder.image.setImageResource(info.url);
        }
        return view;
    }
}