package com.android.yoganetwork.models;

/*Model class for recyclerview of notifications*/
public class ModelNotification {
    String pId, timestamp, pUid, notification, sUid, sPseudonym, sEmail, sImage;

    //empty constructor is required for firebase


    public ModelNotification() {
    }

    public ModelNotification(String pId, String timestamp, String pUid, String notification, String sUid, String sPseudonym, String sEmail, String sImage) {
        this.pId = pId;
        this.timestamp = timestamp;
        this.pUid = pUid;
        this.notification = notification;
        this.sUid = sUid;
        this.sPseudonym = sPseudonym;
        this.sEmail = sEmail;
        this.sImage = sImage;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getpUid() {
        return pUid;
    }

    public void setpUid(String pUid) {
        this.pUid = pUid;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getsUid() {
        return sUid;
    }

    public void setsUid(String sUid) {
        this.sUid = sUid;
    }

    public String getsPseudonym() {
        return sPseudonym;
    }

    public void setsPseudonym(String sPseudonym) {
        this.sPseudonym = sPseudonym;
    }

    public String getsEmail() {
        return sEmail;
    }

    public void setsEmail(String sEmail) {
        this.sEmail = sEmail;
    }

    public String getsImage() {
        return sImage;
    }

    public void setsImage(String sImage) {
        this.sImage = sImage;
    }

}
