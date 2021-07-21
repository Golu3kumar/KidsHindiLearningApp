package com.selfhelpindia.kidsquizapp.modelclass;

public class UserModelClass {

    private String name,email,pass,referCode,uId;
    private Long coins = 0L;
    private String profileUrl;
    private int referCounter = 0;

    public UserModelClass() {

    }

    public UserModelClass(String name, String email, String pass, String referCode,Long coins,String uId) {
        this.name = name;
        this.email = email;
        this.pass = pass;
        this.referCode = referCode;
        this.coins = coins;
        this.uId = uId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getReferCode() {
        return referCode;
    }

    public void setReferCode(String referCode) {
        this.referCode = referCode;
    }

    public Long getCoins() {
        return coins;
    }

    public void setCoins(Long coins) {
        this.coins = coins;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public int getReferCounter() {
        return referCounter;
    }

    public void setReferCounter(int referCounter) {
        this.referCounter = referCounter;
    }
}
