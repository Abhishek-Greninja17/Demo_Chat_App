package com.skynet.skynettest.superAdmin;

public class AdminModel {

    private String name;
    private String company;
    private String email;
    private String password;
    private String type;
    private String uid;

    public AdminModel(String name, String company, String email, String password, String type, String uid) {
        this.name = name;
        this.company = company;
        this.email = email;
        this.password = password;
        this.type = type;
        this.uid = uid;
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
}
