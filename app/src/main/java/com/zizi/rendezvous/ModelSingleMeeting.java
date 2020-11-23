package com.zizi.rendezvous;

public class ModelSingleMeeting {



    //обратить внимание на регистр, далее этот класс используется при читке и записи с БД,
    // везде регистр сделал маленькими, заработало, пока дальше не стал разбираться
    //!!!!!!!!!!!! Переменные с нижним подчеркиванием из БД почему то не выбираются
    private String userID;
    private String token;
    private String email;

    private String name;
    private String gender;
    private String age;
    private String phone;
    private String onlyWrite;
    private String socNet;
    private String contact;
    private String gender_partner;
    private String age_min;
    private String age_max;
    private String region;
    private String town;
    private String place;
    private String time;
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

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }

    public String getPhone() {
        return phone;
    }

    public String getOnlyWrite() {
        return onlyWrite;
    }

    public String getSocNet() {
        return socNet;
    }

    public String getContact() {
        return contact;
    }

    public String getGender_partner() {
        return gender_partner;
    }

    public String getAge_min() {
        return age_min;
    }

    public String getAge_max() {
        return age_max;
    }

    public String getRegion() {
        return region;
    }

    public String getTown() {
        return town;
    }

    public String getPlace() {
        return place;
    }

    public String getTime() {
        return time;
    }

    public String getComment() {
        return comment;
    }



    //Setters///////////////////////////////////////////////////////////////////////////////////////
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setOnlyWrite(String onlyWrite) {
        this.onlyWrite = onlyWrite;
    }

    public void setSocNet(String socNet) {
        this.socNet = socNet;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setGender_partner(String gender_partner) {
        this.gender_partner = gender_partner;
    }

    public void setAge_min(String age_min) {
        this.age_min = age_min;
    }

    public void setAge_max(String age_max) {
        this.age_max = age_max;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
