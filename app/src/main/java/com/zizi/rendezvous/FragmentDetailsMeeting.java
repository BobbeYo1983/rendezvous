package com.zizi.rendezvous;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class FragmentDetailsMeeting extends Fragment {

    // ОБЪЯВЛЕНИЕ ///////////////////////////////////////////////////////////////////////////////
    //private Bundle bundle; // для приема параметров в фрагмент
    private Map<String, Object> mapDocument; //Документ с информацией о встрече
    private ActivityMeetings activityMeetings; // активити для переключения фрагментов из фрагментов
    private FirebaseFirestore firebaseFirestore; // база данных
    private DocumentReference documentReference; // ссылка на документ

    //виджеты
    private TextInputLayout til_name;
    private TextInputEditText til_name_et; // имя пользователя

    public FragmentDetailsMeeting() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ИНИЦИАЛИЗАЦИЯ //////////////////////////////////////////////////////////////////////////
        mapDocument = new HashMap<String, Object>();
        activityMeetings = (ActivityMeetings)getActivity(); // получаем объект текущей активити
        firebaseFirestore = FirebaseFirestore.getInstance(); //инициализация БД
        //==========================================================================================

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details_meeting, container, false);
    }

    @Override //Вызывается, когда отработает метод активности onCreate(), а значит фрагмент может обратиться к компонентам активности
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // находим все вьюхи на активити
        til_name = getActivity().findViewById(R.id.til_name);
        til_name_et = getActivity().findViewById(R.id.til_name_et);

    }

    @Override //когда фрагмент становится видимым, но не интерактивным/ не нажимаемым
    public void onStart() {
        super.onStart();

        if (!activityMeetings.classGlobalApp.IsAuthorized()) { // если пользователь не авторизован
            startActivity(new Intent(getActivity().getApplicationContext(), ActivityLogin.class)); // отправляем к началу на авторизацию
            getActivity().finish(); // убиваем активити
        }


        //Читаем документ со встречей партнера из БД //////////////////////////////////////////////////
        documentReference = firebaseFirestore.collection("meetings").document(activityMeetings.classGlobalApp.GetBundle("partnerEmail")); // формируем путь к документу
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() { // вешаем слушателя на задачу чтения документа из БД
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) { // как задача чтения выполнилась
                activityMeetings.classGlobalApp.Log("FragmentDetailsMeeting", "onStart/onComplete", "Method is run");
                if (task.isSuccessful()) { // если выполнилась успешно
                    activityMeetings.classGlobalApp.Log("FragmentDetailsMeeting", "onStart/onComplete", "Task is Successful");
                    DocumentSnapshot document = task.getResult(); // получаем документ
                    if (document.exists()) { // если документ такой есть, не null

                        activityMeetings.classGlobalApp.Log("FragmentDetailsMeeting", "onStart/onComplete", "Document is exists");
                        mapDocument = document.getData(); // получаем данные из документа БД
                        activityMeetings.classGlobalApp.Log("FragmentDetailsMeeting", "onStart/onComplete", "Fields count in document is: " + Integer.toString(mapDocument.size()));

                        UpdateUI(); // обновляем данные в полях

                    } else { // если документа не существует

                        activityMeetings.classGlobalApp.Log("FragmentDetailsMeeting", "onStart/onComplete", "Запрошенного документа нет в БД");
                    }

                } else { // если ошибка чтения БД

                    activityMeetings.classGlobalApp.Log ("FragmentDetailsMeeting", "onStart/onComplete", "Ошибка чтения БД: " + task.getException());
                }
            }
        });
        //=============================================================================================

    }

    void UpdateUI() {

        til_name_et.setText(mapDocument.get("name").toString());
    }

}