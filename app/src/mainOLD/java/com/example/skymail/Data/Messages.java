package com.example.skymail.Data;


import java.io.Serializable;

public class Messages implements Serializable{
    private String userID;
    private String messagID;
    private String From;
    private String To;
    private String Subject;
    private String Object;
    private String MessageText;
    private String SenderProfilePicture;
    private String SenderFullName;
    private String Fileurl;
    private String date;
    private String ReplyMessageID;


    public Messages() {
    }

    public Messages(String userID, String messagID,
                    String from, String to, String subject,
                    String object, String messageText, String senderProfilePicture,
                    String SenderFullName , String fileurl,String date,String ReplyMessageId ) {


        this.userID = userID;
        this.messagID = messagID;
        this.SenderFullName = SenderFullName;
        From = from;
        To = to;
        Subject = subject;
        Object = object;
        MessageText = messageText;
        SenderProfilePicture = senderProfilePicture;
        Fileurl = fileurl;
        this.date = date;
        this.ReplyMessageID = ReplyMessageId;


    }

    public String getReplyMessageID() {
        return ReplyMessageID;
    }

    public void setReplyMessageID(String replyMessageID) {
        ReplyMessageID = replyMessageID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSenderFullName() {
        return SenderFullName;
    }

    public void setSenderFullName(String senderFullName) {
        SenderFullName = senderFullName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getMessagID() {
        return messagID;
    }

    public void setMessagID(String messagID) {
        this.messagID = messagID;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

    public String getTo() {
        return To;
    }

    public void setTo(String to) {
        To = to;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getObject() {
        return Object;
    }

    public void setObject(String object) {
        Object = object;
    }

    public String getMessageText() {
        return MessageText;
    }

    public void setMessageText(String messageText) {
        MessageText = messageText;
    }

    public String getSenderProfilePicture() {
        return SenderProfilePicture;
    }

    public void setSenderProfilePicture(String senderProfilePicture) {
        SenderProfilePicture = senderProfilePicture;
    }
    public String getFileurl() {
        return Fileurl;
    }

    public void setFileurl(String fileurl) {
        Fileurl = fileurl;
    }
}
