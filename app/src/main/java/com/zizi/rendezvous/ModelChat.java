package com.zizi.rendezvous;

public class ModelChat {

    private String userID;
    private String token;
    private String name;
    private String age;
    private String unReadMsg;
    //private String email;

    public String getUnReadMsg() {
        return unReadMsg;
    }

    public void setUnReadMsg(String unReadMsg) {
        this.unReadMsg = unReadMsg;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    //public String getEmail() {return email;}

    //public void setEmail(String email) { this.email = email; }


}
