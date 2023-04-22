package com.example.tickets_project;

public class Upload {
    private String mName;
    private String mImgUrl;

    public Upload(){}
    public Upload(String name, String ImgUrl){
        if(name.trim().equals("")){
            name = "No Name";
        }
        mName = name;
        mImgUrl = ImgUrl;
    }

    public String getmImgUrl() { return mImgUrl; }

    public String getmName() { return mName; }

    public void setmImgUrl(String mImgUrl) { this.mImgUrl = mImgUrl; }

    public void setmName(String mName) { this.mName = mName;}
}
