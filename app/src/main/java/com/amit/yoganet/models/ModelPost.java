package com.amit.yoganet.models;

public class ModelPost {
    //use same name as we given while uploading post
    String pId, pTitle, pDescr, pLikes, pDislikes, pComments, pImage, pTime, uid, uPseudonym, uPractic, uDp, pVideo, hotScore, pAudio, youtubeUrl;

    public ModelPost() {
    }

    public ModelPost(String pId, String pTitle, String pDescr, String pLikes, String pDislikes, String pComments, String pImage, String pTime, String uid, String uPseudonym, String uPractic, String uDp, String pVideo, String hotScore, String pAudio, String youtubeUrl) {
        this.pId = pId;
        this.pTitle = pTitle;
        this.pDescr = pDescr;
        this.pLikes = pLikes;
        this.pDislikes = pDislikes;
        this.pComments = pComments;
        this.pImage = pImage;
        this.pTime = pTime;
        this.uid = uid;
        this.uPseudonym = uPseudonym;
        this.uPractic = uPractic;
        this.uDp = uDp;
        this.pVideo = pVideo;
        this.hotScore = hotScore;
        this.pAudio = pAudio;
        this.youtubeUrl = youtubeUrl;
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

    public String getpDislikes() {
        return pDislikes;
    }

    public void setpDislikes(String pDislikes) {
        this.pDislikes = pDislikes;
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

    public String getpVideo() {
        return pVideo;
    }

    public void setpVideo(String pVideo) {
        this.pVideo = pVideo;
    }

    public String getHotScore() {
        return hotScore;
    }

    public void setHotScore(String hotScore) {
        this.hotScore = hotScore;
    }

    public String getpAudio() {
        return pAudio;
    }

    public void setpAudio(String pAudio) {
        this.pAudio = pAudio;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }
}
