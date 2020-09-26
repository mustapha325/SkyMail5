package com.example.skymail.Data;

public class Users {
    private String userID;
    private String fullname;
    private String email;
    private String password;
    private String birthdate;
    private String gender;
    private String phonenumber;
    private String inscriptionDate;



    public Users(){
    }

    public Users(String userID, String fullname, String email, String password, String birthdate, String gender, String phonenumber,String inscriptionDate) {
        this.userID = userID;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.birthdate = birthdate;
        this.gender = gender;
        this.phonenumber = phonenumber;
        this.inscriptionDate = inscriptionDate;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getInscriptionDate() {
        return inscriptionDate;
    }

    public void setInscriptionDate(String inscriptionDate) { this.inscriptionDate = inscriptionDate; }
}
