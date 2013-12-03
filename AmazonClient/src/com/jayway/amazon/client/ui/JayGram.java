package com.jayway.amazon.client.ui;

import android.app.ListActivity;
import android.os.Bundle;
import com.jayway.amazon.R;
import com.jayway.amazon.client.Content;

import java.util.ArrayList;
import java.util.List;

public class JayGram extends ListActivity {

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
}
