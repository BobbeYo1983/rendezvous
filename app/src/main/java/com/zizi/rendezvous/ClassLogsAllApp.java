package com.zizi.rendezvous;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;


// класс для записи логов на сервак
public class ClassLogsAllApp {

    FirebaseDatabase firebaseDatabase; // БД Realtime Database
    DatabaseReference databaseReference; //ссылка на данные в базе
    Map<String, Object> msg; // сообщение

    public ClassLogsAllApp () {
        firebaseDatabase = FirebaseDatabase.getInstance(); // БД
        databaseReference = firebaseDatabase.getReference("logs");
        msg = new HashMap<>();

    }

    public void Log (String message){

        msg.clear();
        msg.put("timestamp", ServerValue.TIMESTAMP);
        msg.put("message", message);
        databaseReference.push().setValue(msg);

    }

}
