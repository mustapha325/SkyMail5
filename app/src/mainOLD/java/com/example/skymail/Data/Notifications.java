package com.example.skymail.Data;

public class Notifications {
        private String MessageText;
        private String Messageid;
        private String Subject;
        private int notificationID;
        private String Email;
        private String Emailto;
        private String Fullname;
        private String FileUrl;
        private String ProfilePic;



        public Notifications(){
        }

        public Notifications(String MessageText, String Messageid,
                             String Subject,String Email,String Fullname,String Emailto,
                             String ProfilePic,String FileUrl,int notificationID) {
            this.MessageText = MessageText;
            this.Messageid = Messageid;
            this.Subject = Subject;
            this.notificationID = notificationID;
            this.Email = Email;
            this.Emailto = Emailto;
            this.Fullname = Fullname;
            this.FileUrl=FileUrl;
            this.ProfilePic=ProfilePic;
        }

        public int getNotificationID() {
            return notificationID;
        }

        public void setNotificationID(int NotificationID) { this.notificationID = NotificationID; }

        public String getFullname() {
        return Fullname;
    }

        public void setFullname(String fullname) { this.Fullname = fullname; }

        public String getFileUrl() {
        return FileUrl;
    }

        public void setFileUrl(String FileUrl) { this.FileUrl = FileUrl; }

        public String getProfilePic() {
        return ProfilePic;
    }

        public void setProfilePic(String ProfilePic) { this.ProfilePic = ProfilePic; }

        public String getEmail() { return Email; }

        public String getEmailto() {
        return Emailto;
    }

        public void setEmailto(String emailto) { this.Emailto = emailto; }

        public void setEmail(String email) { this.Email = email; }

        public String getSubject() {
        return Subject;
    }

        public void setSubject(String subject) { this.Subject = subject; }

        public String getMessageText() {
        return MessageText;
    }

        public void setMessageText(String Messagetext) { this.MessageText = Messagetext; }

        public String getMessageID() {
        return Messageid;
    }

        public void setMessageID(String MessageID) { this.Messageid = MessageID; }
}
