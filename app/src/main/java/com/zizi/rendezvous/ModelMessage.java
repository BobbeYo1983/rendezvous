package com.zizi.rendezvous;

import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.Exclude;

import java.util.Map;

public class ModelMessage {

    private String userID;
    private String textMessage;
    private String dateTimeDevice;
    private String pushKey;

    // с этим полем острожно, магия. Дело в том, что метка времени сервера Firebase имеет формат java.util.Map<String, String>
    // а вычитывается уже с типом данных Long, поэтому 2 геттера и один помечен директивой @Exclude, типа в БД такого поля нет и видимо автоматом геттер не вызывается
    // но я закоментировал этот геттор, так как не юзаю его
    // таким способом у нас в классе нет несоответствия типов ServerValue.TIMESTAMP и Long
    // так же нужно следить за регистром геттеров, сеттеров и полей, причем полей в классе этом и реально записанных в БД
    private Long timeStamp;

    public ModelMessage() {}

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public String getDateTimeDevice() {
        return dateTimeDevice;
    }

    public void setDateTimeDevice(String dateTimeDevice) {
        this.dateTimeDevice = dateTimeDevice;
    }

    public String getPushKey() {
        return pushKey;
    }

    public void setPushKey(String pushKey) {
        this.pushKey = pushKey;
    }

    //формируем дату и время, но это не на клиенте, то есть
    public Map<String, String> getTimeStamp() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude // эта директива помечает поле как исключенное из БД, то есть при обмене данных с БД, когда класс является моделью, farebase этот геттер не вызывает
    public Long getTimeStampLong() {
        return timeStamp;
    }

/*    @Exclude // эта директива помечает поле как исключенное из БД, то есть при обмене данных с БД, когда класс является моделью, farebase этот геттер не вызывает
    public String getTimeStampString() {
        return timeStamp.toString();
    }*/

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }




}
