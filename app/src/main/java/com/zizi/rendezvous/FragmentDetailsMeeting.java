package com.zizi.rendezvous;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class FragmentDetailsMeeting extends Fragment {

    // ОБЪЯВЛЕНИЕ ///////////////////////////////////////////////////////////////////////////////
    private ClassGlobalApp classGlobalApp; // глобальный класс приложения
    private Map<String, Object> mapDocument; //Документ с информацией о встрече
    private FirebaseFirestore firebaseFirestore; // база данных
    private DocumentReference documentReference; // ссылка на документ

    //виджеты
    private MaterialToolbar materialToolbar; // верхняя панелька

    private TextInputLayout til_name;
    private TextInputEditText til_name_et; // имя пользователя
    private TextInputLayout til_age;
    private TextInputEditText til_age_et;
    private TextInputLayout til_phone;
    private TextInputEditText til_phone_et;
    private CheckBox cb_only_write; //галка можно ли звонить
    private TextInputLayout til_soc_net;
    private TextInputEditText til_soc_net_et;
    private TextInputLayout til_contact;
    private TextInputEditText til_contact_et;
    private TextInputLayout til_place;
    private TextInputEditText til_place_et;
    private TextInputLayout til_time;
    private TextInputEditText til_time_et;
    private TextInputLayout til_comment;
    private TextInputEditText til_comment_et;

    public FragmentDetailsMeeting() {
        // Required empty public constructor
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

        // ИНИЦИАЛИЗАЦИЯ //////////////////////////////////////////////////////////////////////////
        classGlobalApp = (ClassGlobalApp) getActivity().getApplicationContext();
        mapDocument = new HashMap<String, Object>();
        firebaseFirestore = FirebaseFirestore.getInstance(); //инициализация БД
        //==========================================================================================


        // находим все вьюхи на активити
        materialToolbar = getActivity().findViewById(R.id.materialToolbar);

        til_name = getActivity().findViewById(R.id.til_name);
        til_name_et = getActivity().findViewById(R.id.til_name_et);
        til_age = getActivity().findViewById(R.id.til_age);
        til_age_et = getActivity().findViewById(R.id.til_age_et);
        til_phone = getActivity().findViewById(R.id.til_phone);
        til_phone_et = getActivity().findViewById(R.id.til_phone_et);
        cb_only_write = getActivity().findViewById(R.id.cb_only_write);
        til_soc_net = getActivity().findViewById(R.id.til_soc_net);
        til_soc_net_et = getActivity().findViewById(R.id.til_soc_net_et);
        til_contact = getActivity().findViewById(R.id.til_contact);
        til_contact_et = getActivity().findViewById(R.id.til_contact_et);
        til_place = getActivity().findViewById(R.id.til_place);
        til_place_et = getActivity().findViewById(R.id.til_place_et);
        til_time = getActivity().findViewById(R.id.til_time);
        til_time_et = getActivity().findViewById(R.id.til_time_et);
        til_comment = getActivity().findViewById(R.id.til_comment);
        til_comment_et = getActivity().findViewById(R.id.til_comment_et);

        // topAppBar ////////////////////////////////////////////////////////////////////////////////
        materialToolbar.setTitle("Подробности"); // заголовок в панельке верхней
        materialToolbar.getMenu().findItem(R.id.request).setVisible(false); // скрываем пункт заявки на встречу
        materialToolbar.setNavigationIcon(R.drawable.ic_outline_arrow_back_24); // делаем кнопку навигации стрелкой в верхней панельке

        // событие при клике на кнопку навигации, на этом фрагменте она в виде стрелочки
        materialToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        //==========================================================================================



    }

    @Override //когда фрагмент становится видимым, но не интерактивным/ не нажимаемым
    public void onStart() {
        super.onStart();

        if (!classGlobalApp.IsAuthorized()) { // если пользователь не авторизован
            startActivity(new Intent(getActivity().getApplicationContext(), ActivityLogin.class)); // отправляем к началу на авторизацию
            getActivity().finish(); // убиваем активити
        }



        //Читаем документ со встречей партнера из БД //////////////////////////////////////////////////
        //classGlobalApp.Log(getClass().getSimpleName(), "onStart", "partnerUserID = ", false);
        documentReference = classGlobalApp.GenerateDocumentReference("meetings", classGlobalApp.GetBundle("partnerUserID")); // формируем путь к документу
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() { // вешаем слушателя на задачу чтения документа из БД
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) { // как задача чтения выполнилась
                if (task.isSuccessful()) { // если выполнилась успешно
                    DocumentSnapshot document = task.getResult(); // получаем документ
                    if (document.exists()) { // если документ такой есть, не null
                        ModelSingleMeeting requestMeetingPartner = document.toObject(ModelSingleMeeting.class); // получаем заявку текущего пользователя из БД
                        //mapDocument = document.getData(); // получаем данные из документа БД
                        //classGlobalApp.Log("FragmentDetailsMeeting", "onStart/onComplete", "Fields count in document is: " + Integer.toString(mapDocument.size()), false);
                        UpdateUI(requestMeetingPartner); // обновляем данные в полях
                    } else { // если документа не существует

                        classGlobalApp.Log("FragmentDetailsMeeting", "onStart/onComplete", "Запрошенного документа нет в БД", true);
                    }

                } else { // если ошибка чтения БД

                    classGlobalApp.Log ("FragmentDetailsMeeting", "onStart/onComplete", "Ошибка чтения БД: " + task.getException(), true);
                }
            }
        });
        //=============================================================================================
    }

    /**
     * Обновляет пользовательский интерфейс
     */
    void UpdateUI(ModelSingleMeeting requestMeetingPartner) {

        til_name_et.setText(requestMeetingPartner.getName());
        til_age_et.setText(requestMeetingPartner.getAge());
        til_phone_et.setText(requestMeetingPartner.getPhone());

        if (requestMeetingPartner.getOnlyWrite().equals("trueTrue")) {
            cb_only_write.setChecked(true);
        } else {
            cb_only_write.setChecked(false);
        }

        til_soc_net_et.setText(requestMeetingPartner.getSocNet());
        Linkify.addLinks(til_soc_net_et, Linkify.ALL); // для распознования ссылок
        til_soc_net_et.setLinkTextColor(Color.BLUE);

        til_contact_et.setText(requestMeetingPartner.getContact());
        til_place_et.setText(requestMeetingPartner.CreateStringFromArrayListPlaces());
        til_time_et.setText(requestMeetingPartner.getTime());
        til_comment_et.setText(requestMeetingPartner.getComment());

    }

}