package com.zizi.rendezvous;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


/**
 * Класс для работы с базами данных
 */
public class ClassDataBase {

    // Cloud Firestore
    private FirebaseFirestore firebaseFirestore; // база данных
    private DocumentReference documentReference; // ссылка на документ
    private Map<String, Object> mapDocument; //коллекция ключ-значение, будем возвращать ее

    //Realtime Database
    private FirebaseDatabase firebaseDatabase; // БД Realtime Database
    private DatabaseReference databaseReference; //ссылка на данные в базе
    private Map<String, Object> msg; // объект для сообщения

    private FirebaseAuth firebaseAuth; // для работы с FireBase
    private FirebaseUser currentUser; //текущий пользователь

    private int countUnreads; // количество непрочитанных сообщений
    private BadgeDrawable badgeDrawable; // для изменения количества непрочитанных сообщений

    String tmp;


    /**
     * Конструктор с инициализацией
     */
    ClassDataBase() {
        firebaseFirestore = FirebaseFirestore.getInstance(); //инициализация БД
        mapDocument = new HashMap<String, Object>();
        firebaseDatabase = FirebaseDatabase.getInstance(); // БД
        firebaseAuth = FirebaseAuth.getInstance(); // инициализация объекта для работы с авторизацией
        currentUser = firebaseAuth.getCurrentUser(); // получаем инфу о текущем пользователе
        msg = new HashMap<>();

    }


    /**
     * Читает документ из базы Firebase Firestore
     * @param nameCollection имя коллекции
     * @param nameDocument имя документа
     * @return коллекцию "ключ-значение"
     */
    //public Map<String, Object> ReadDocument (final String nameCollection, final String nameDocument) {
    public String ReadDocument (final String nameCollection, final String nameDocument) {

        //mapDocument.clear(); // очищаем коллекцию

        documentReference = firebaseFirestore.collection(nameCollection).document(nameDocument); // формируем путь к документу
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() { // вешаем слушателя на задачу чтения документа из БД
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) { // как задача чтения выполнилась
                if (task.isSuccessful()) { // если выполнилась успешно
                    DocumentSnapshot document = task.getResult(); // получаем документ
                    if (document.exists()) { // если документ такой есть, не null

                        //mapDocument.clear(); // очищаем коллекцию
                        mapDocument = new HashMap<String, Object>();
                        mapDocument = document.getData(); // получаем данные из документа БД

                        // если нужно получить поле document.getString("names"));
                        tmp = "Hello!";
                        tmp = document.getString("name");

                    } else { // если документа не существует

                        Log("DataBase", "ReadDocument", "Запрошенного документа (" + nameCollection + "/" + nameDocument + ") нет в БД");
                    }

                } else { // если ошибка чтения БД

                    Log ("DataBase", "ReadDocument", "Ошибка чтения БД: " + task.getException());
                }
            }
        });

        //return mapDocument;
        return "cdvdvcdcvdcvdcv ";


    }

    public void Log (String _class, String _method, String _message){

        databaseReference = firebaseDatabase.getReference("logs");

        msg.clear();
        msg.put("timestamp", ServerValue.TIMESTAMP);
        msg.put("class" , _class);
        msg.put("method" , _method);
        msg.put("message", _message);
        databaseReference.push().setValue(msg);

    }

    /**
     * Обновляет количество непрочитанных чатов. На вход подаем панельку, что нужно с ней делаем, возвращаем готовую панельку.
     * @param bottomNavigationView bottomNavigationView
     * @return переделанная со значком bottomNavigationView
     */
    public BottomNavigationView getCountUnreads (final BottomNavigationView bottomNavigationView){

        databaseReference = firebaseDatabase.getReference("chats/unreads/" + currentUser.getUid() + "/"); // ссылочка на количество непрочитанных сообщений текущего пользователя
        databaseReference.addValueEventListener(new ValueEventListener() { // добавляем слушателя при изменении значения
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                countUnreads = (int) snapshot.getChildrenCount(); // получаем количество непрочитанных чатов
                if (countUnreads > 0) { // если есть непрочитанные чаты
                    //badgeDrawable.setVisible(true); // показываем значек
                    badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.chats); // создаем значек около вкладки Чаты на нижней панели, пока без номера
                    badgeDrawable.setNumber(countUnreads); // показываем количество непрочитанных чатов
                } else {
                    bottomNavigationView.removeBadge(R.id.chats); // удаляем значек с панели
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // тут надо обработать ошибочку и залогировать
            }
        });

        return bottomNavigationView;
    }

}

