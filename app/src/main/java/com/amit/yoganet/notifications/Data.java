package com.amit.yoganet.notifications;

public class Data {

    private String user, body, title, sent, notificationType, icon;

    public Data() {
    }

    public Data(String user, String body, String title, String sent, String notificationType, String icon) {
        this.user = user;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.notificationType = notificationType;
        this.icon = icon;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
