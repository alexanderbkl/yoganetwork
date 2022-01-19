package com.android.yoganetwork.models;

public class ModelComment {
    String cId, comment, timestamp, uid, uEmail, uDp, uPseudonym, uPractic;

    public ModelComment() {

    }
    public ModelComment(String cId, String uPractic, String comment, String timestamp, String uid, String uEmail, String uDp, String uPseudonym) {
        this.cId = cId;
        this.comment = comment;
        this.timestamp = timestamp;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uPractic = uPractic;
        this.uDp = uDp;
        this.uPseudonym = uPseudonym;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuPseudonym() {
        return uPseudonym;
    }

    public void setuPseudonym(String uPseudonym) {
        this.uPseudonym = uPseudonym;
    }

    public String getuPractic() {
        return uPractic;
    }

    public void setuPractic(String uPractic) {
        this.uPractic = uPractic;
    }
}
