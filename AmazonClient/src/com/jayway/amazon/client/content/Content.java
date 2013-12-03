package com.jayway.amazon.client.content;

import android.graphics.drawable.Drawable;

/**
 * Created with IntelliJ IDEA.
 * User: jennynilsson
 * Date: 2013-12-03
 * Time: 14:29
 * To change this template use File | Settings | File Templates.
 */
public class Content {

    public int url;
    public String name;
    public String date;
    public String text;


    public Content(int url, String name, String date, String text) {
        this.url = url;
        this.name = name;
        this.date = date;
        this.text = text;
    }

}
