package com.amit.yoganet.models;

public class ModelUsers {

    //use same name as in firebase database
    String pseudonym, purpose, country, city, practic, type, diet, image, cover, uid, onlineStatus, notifications, email, description, profileLikes;
    boolean isBlocked = false;
    boolean isLiked = false;

    public ModelUsers() {

    }

    public ModelUsers(String pseudonym, String purpose, String country, String city, String practic, String type, String diet, String image, String cover, String uid, String onlineStatus, String notifications, String email, String description, String profileLikes, boolean isBlocked, boolean isLiked) {
        this.pseudonym = pseudonym;
        this.country = country;
        this.purpose = purpose;
        this.city = city;
        this.practic = practic;
        this.type = type;
        this.diet = diet;
        this.image = image;
        this.cover = cover;
        this.uid = uid;
        this.onlineStatus = onlineStatus;
        this.notifications = notifications;
        this.email = email;
        this.description = description;
        this.profileLikes = profileLikes;
        this.isBlocked = isBlocked;
        this.isLiked = isLiked;
    }

    public String getPseudonym() {
        return pseudonym;
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
    }


    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }


    public String getPractic() {
        return practic;
    }

    public void setPractic(String practic) {
        this.practic = practic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDiet() {
        return diet;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public String getNotifications() {
        return notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
