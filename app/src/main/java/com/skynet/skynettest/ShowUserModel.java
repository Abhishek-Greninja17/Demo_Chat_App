package com.skynet.skynettest;

public class ShowUserModel {

    private String name;
    private String company;
    private String type;
    private String uid;
    private String email;
    private String image;

    public ShowUserModel(){}

    public ShowUserModel(String name, String type, String image){
        this.name = name;
        this.type = type;
        this.image = image;
    }

    public ShowUserModel(String name, String company, String type, String uid, String email) {
        this.name = name;
        this.company = company;
        this.type = type;
        this.uid = uid;
        this.email = email;
    }

    public ShowUserModel(String name, String company, String type, String uid, String email, String image) {
        this.name = name;
        this.company = company;
        this.type = type;
        this.uid = uid;
        this.email = email;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
