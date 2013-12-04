package com.jayway.amazon.client.content;

import android.util.Log;

import java.net.URL;

public class Content {

    private String eTag;
    public URL url;
    public String name;
    public String date;
    public String text;


    public Content(String eTag, URL url, String name, String date, String text) {
        this.eTag = eTag;
        this.url = url;
        this.name = name;
        this.date = date;
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        return eTag.equals(((Content) o).eTag);
    }
}
