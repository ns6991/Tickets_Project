package com.example.tickets_project;

import android.net.Uri;

public class putPDF {
    public String name;
    public String url;
    public  String uri;

    public putPDF(String name, String url, String uri){
        this.name = name;
        this.url =url;
        this.uri = uri;
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
