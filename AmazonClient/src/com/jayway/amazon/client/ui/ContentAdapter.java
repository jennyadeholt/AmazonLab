package com.jayway.amazon.client.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.jayway.amazon.R;
import com.jayway.amazon.client.content.Content;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;


public class ContentAdapter extends BaseAdapter {

    private List<Content> mContents;
    private Context mContext;

    /**  */
    private static class ContentHolder {
        public View progress;
        public ImageView image;
        public TextView date;
        public TextView comment;
        public TextView user;
    }

    public ContentAdapter(Context context, List<Content> contents){
        super();
        mContext = context;
        mContents = contents;
    }

    public void updateContent(List<Content> contents) {
        for (Content content : contents){
            if (!mContents.contains(content)){
                mContents.add(content);
            }
        }

        notifyDataSetChanged();

    }

    @Override
    public int getCount() {
        return mContents.size();
    }

    @Override
    public Content getItem(int i) {
        return mContents.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View view = v;

        ContentHolder temp = null;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.list_item, parent, false);
            temp =  new ContentHolder();

            temp.progress = view.findViewById(R.id.progress_bar);
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
            contentHolder.progress.setVisibility(View.VISIBLE);

            Picasso.with(mContext)
                    .load(urlTag)
                    .resize(150, 150)
                    .centerCrop()
                    .into(contentHolder.image, new Callback() {
                        @Override
                        public void onSuccess() {
                            contentHolder.progress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {

                        }
                    });

            contentHolder.date.setTag(urlTag);

        }
        return view;
    }
}