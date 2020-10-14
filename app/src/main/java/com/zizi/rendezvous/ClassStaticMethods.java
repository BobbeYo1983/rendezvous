package com.zizi.rendezvous;

import androidx.annotation.NonNull;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ClassStaticMethods {



    private static BadgeDrawable badgeDrawable; // для изменения количества непрочитанных сообщений
    private static FirebaseDatabase firebaseDatabase; // = FirebaseDatabase.getInstance(); // БД
    private static DatabaseReference databaseReference;// = database.getReference("message"); //ссылка на данные
    private static int countUnreads;


    /**
     * Обновляет количество непрочитанных чатов. На вход подаем панельку, что нужно с ней делаем, возвращаем готовую панельку.
     */
    public static BottomNavigationView getCountUnreads (BottomNavigationView bottomNavigationView){

        firebaseDatabase = FirebaseDatabase.getInstance(); // инициализируем БД
        badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.chats); // создаем значек около вкладки Чаты на нижней панели, пока без номера
        badgeDrawable.setVisible(false); // сразу делаем невидимым навсякий
        countUnreads = 0; //количество непрочитанных чатов делаем ноль навсякий

        databaseReference = firebaseDatabase.getReference("chats/unreads/"); // ссылочка на количество непрочитанных сообщений текущего пользователя
        databaseReference.addValueEventListener(new ValueEventListener() { // добавляем слушателя при изменении значения
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                countUnreads = (int) snapshot.getChildrenCount(); // получаем количество непрочитанных чатов
                if (countUnreads > 0) { // если есть непрочитанные чаты
                    badgeDrawable.setVisible(true); // показываем значек
                    badgeDrawable.setNumber(countUnreads); // показываем количество непрочитанных чатов
                } else {
                    badgeDrawable.setVisible(false); // скрываем значек
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
