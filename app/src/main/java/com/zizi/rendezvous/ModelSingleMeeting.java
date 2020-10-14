package com.zizi.rendezvous;

public class ModelSingleMeeting {



    //обратить внимание на регистр, далее этот класс используется при читке и записи с БД,
    // везде регистр сделал маленькими, заработало, пока дальше не стал разбираться
    //!!!!!!!!!!!! Переменные с нижним подчеркиванием из БД почему то не выбираются
    private String userID;
    private String token;
    private String name;
    private String age;
    private String comment;



    public ModelSingleMeeting() {}

/*    private ModelSingleMeeting(String userID, String name, String age, String comment) {
        this.userID = userID;
        this.name = name;
        this.age = age;
        this.comment =comment;
    }*/

    public String getUserID() {
        return userID;
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getComment() {
        return comment;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(String age) {
        this.age = age;
    }



}
