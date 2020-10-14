package com.zizi.rendezvous;

import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.Exclude;
import com.google.firestore.v1.DocumentTransform;

import java.util.Map;

public class ModelMessage {

    public String userID;
    public String textMessage;
    public String dateTimeDevice;

    // с этим полем острожно, магия. Дело в том, что метка времени сервера Firebase имеет формат java.util.Map<String, String>
    // а вычитывается уже с типом данных Long, поэтому 2 геттера и один помечен директивой @Exclude, типа в БД такого поля нет и видимо автоматом геттер не вызывается
    // но я закоментировал этот геттор, так как не юзаю его
    // таким способом у нас в классе нет несоответствия типов ServerValue.TIMESTAMP и Long
    // так же нужно следить за регистром геттеров, сеттеров и полей, причем полей в классе этом и реально записанных в БД
    public Long timeStamp;

    public ModelMessage() {}

    public java.util.Map<String, String> getTimeStamp() {
        return ServerValue.TIMESTAMP;
    }

/*    @Exclude // эта директива помечает поле как исключенное из БД
    public Long getTimeStampLong() {
        return timeStamp;
    }*/

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }




}
