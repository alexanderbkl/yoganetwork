package com.android.yoganetwork.models;

public class ModelPost {
    //use same name as we given while uploading post
    String pId, pTitle, pDescr, pLikes, pComments, pImage, pTime, uid, uPseudonym, uPractic, uDp;

    public ModelPost() {
    }

    public ModelPost(String pId, String pTitle, String pDescr, String pLikes, String pComments, String pImage, String pTime, String uid, String uPseudonym, String uPractic, String uDp) {
        this.pId = pId;
        this.pTitle = pTitle;
        this.pDescr = pDescr;
        this.pLikes = pLikes;
        this.pComments = pComments;
        this.pImage = pImage;
        this.pTime = pTime;
        this.uid = uid;
        this.uPseudonym = uPseudonym;
        this.uPractic = uPractic;
        this.uDp = uDp;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpTitle() {
        return pTitle;
    }

    public void setpTitle(String pTitle) {
        this.pTitle = pTitle;
    }

    public String getpDescr() {
        return pDescr;
    }

    public void setpDescr(String pDescr) {
        this.pDescr = pDescr;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }
}