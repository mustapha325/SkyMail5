package com.example.skymail.Data;

import android.net.Uri;

public class UploadImages {
    private String userEmail;
    private String mImageUrl;
    private String userID;
    private String userFullname;



    public UploadImages() {
    }

    public UploadImages(String userID,String userFullname,String userEmail, String mImageUrl) {

        this.userEmail = userEmail;
        this.mImageUrl = mImageUrl;
        this.userID = userID;
        this.userFullname=userFullname;


    }

    public String getUserFullname() {
        return userFullname;
    }

    public void setUserFullname(String userFullname) {
        this.userFullname = userFullname;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

}


