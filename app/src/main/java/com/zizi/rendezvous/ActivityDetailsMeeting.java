package com.zizi.rendezvous;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class ActivityDetailsMeeting extends AppCompatActivity {

    Bundle arguments; // для получения параметров переданных от другой активити
    FirebaseFirestore fbSrore; // база данных
    FirebaseUser currentUser; //текущий пользователь
    FirebaseAuth mAuth; // для работы с FireBase
    TextView tv_name; // для отображения имени чела
    TextView tv_age; // для отображения возраста чела
    TextView tv_contact; // для отображения возраста чела
    TextView tv_comment; // для отображения комментария чела
    DocumentReference documentReference; //переменная для работы с документом/записями БД

    TextInputEditText til_name_et; // имя пользователя
    TextInputEditText til_age_et; // возраст пользователя
    TextInputEditText til_comment_et; // комментарий к встрече
    TextInputEditText til_contact_et; // контактные данные

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_meeting);

        //убираем статус бар, акшен бар делаем приложение на весь экран - НАЧАЛО
        //Window w = getWindow();
        //w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getSupportActionBar().hide(); // скрыть Экшнбар
        //убираем статус бар, акшен бар делаем приложение на весь экран - КОНЕЦ

        //инициализация - НАЧАЛО
        fbSrore = FirebaseFirestore.getInstance(); //инициализация БД
        mAuth = FirebaseAuth.getInstance(); // инициализация объекта для работы с авторизацией
        arguments = getIntent().getExtras(); // получаем параметры от другой активити
        //TextView meeting_id = (TextView) findViewById(R.id.tv_meeting_id);
        //meeting_id.setText(arguments.getString("meeting_id")); // пример использования аргументов
        // находим все вьюхи на активити
        //tv_name = (TextView) findViewById(R.id.tv_name);
        //tv_age = (TextView) findViewById(R.id.tv_age);
        //tv_contact = (TextView) findViewById(R.id.tv_contact);
        //tv_comment = (TextView) findViewById(R.id.tv_comment);

        til_name_et = (TextInputEditText) findViewById(R.id.til_name_et);
        til_age_et = (TextInputEditText) findViewById(R.id.til_age_et);
        til_comment_et = (TextInputEditText) findViewById(R.id.til_comment_et);
        til_contact_et = (TextInputEditText) findViewById(R.id.til_contact_et);

        //инициализация - КОНЕЦ


    }

    private void UpdateUI() { // обновляем интерфейс

        //arguments.getString("meeting_id");
        //userID = currentUser.getUid();
        documentReference = fbSrore.collection("meetings").document(arguments.getString("meeting_id")); // будем читать из коллекции meetings и получать документ c именем из meeting_id
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() { // непосредственно чтение
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                til_name_et.setText(documentSnapshot.getString("name"));
                til_age_et.setText(documentSnapshot.getString("age"));
                til_comment_et.setText(documentSnapshot.getString("comment"));
                til_contact_et.setText(documentSnapshot.getString("contact"));

            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) { // если пользователь пустой, не авторизирован
            Toast.makeText(ActivityDetailsMeeting.this, "Пользователь не авторизован", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ActivityDetailsMeeting.this, ActivityLogin.class)); // отправляем к началу на авторизацию
        } else {
            UpdateUI(); // если пользователь авторизирован, то начинаем работать
        }
    }

    @Override
    public void onBackPressed(){ // перегружаем системную кнопку назад
        //startActivity(new Intent(DetailsMeeting.this, DelListMeeting.class)); // отправляем к началу на авторизацию
        //finish(); // деструктор активити
    }


}