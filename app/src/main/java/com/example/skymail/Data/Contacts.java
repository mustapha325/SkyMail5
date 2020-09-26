package com.example.skymail.Data;

public class Contacts {
    private String contacterID;
    private String UserFullname;
    private String UserEmail;
    private String UserProfilePicURI;
    private String UserPhonenumber;
    String userID;

    public Contacts() {
    }

    public Contacts(String contacterID, String userFullname, String userEmail, String userProfilePicURI,String userID,String UserPhonenumber) {
        this.contacterID = contacterID;
        UserFullname = userFullname;
        UserEmail = userEmail;
        UserProfilePicURI = userProfilePicURI;
        this.UserPhonenumber = UserPhonenumber;
        this.userID = userID;
    }

    public String getUserPhonenumber() {
        return UserPhonenumber;
    }

    public void setUserPhonenumber(String userPhonenumber) {
        UserPhonenumber = userPhonenumber;
    }

    public String getContacterID() {
        return contacterID;
    }

    public void setContacterID(String contacterID) {
        this.contacterID = contacterID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserFullname() {
        return UserFullname;
    }

    public void setUserFullname(String userFullname) {
        UserFullname = userFullname;
    }

    public String getUserEmail() {
        return UserEmail;
    }

    public void setUserEmail(String userEmail) {
        UserEmail = userEmail;
    }

    public String getUserProfilePicURI() {
        return UserProfilePicURI;
    }

    public void setUserProfilePicURI(String userProfilePicURI) {
        UserProfilePicURI = userProfilePicURI;
    }
}
