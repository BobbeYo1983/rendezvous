package com.zizi.rendezvous;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class FragmentRequestMeeting extends Fragment implements View.OnClickListener {

    //Объявление - НАЧАЛО ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    FirebaseFirestore fbStore; // база данных
    FirebaseAuth mAuth; // для работы с FireBase
    FirebaseUser currentUser; //текущий пользователь
    DocumentReference documentReference; // для работы с документами в базе, нужно знать структуру базы FirebaseFirestore
    Map<String, Object> meeting; // коллекция ключ-значение для описания встречи
    SharedPreferences saveParams; // хранилище в энергонезависимой памяти любых параметров
    SharedPreferences.Editor editorSaveParams; // объект для редакции энергонезависимого хранилища
    ActivityListMeetingsTb listMeetingsTbActivity; // настоящая активити
    FragmentListMeetings fragmentListMeetings; //фрагмент со встречами
    FragmentPlace fragmentPlace; // фрагмент с выбором места
    ArrayAdapter<String> adapter_towns; //адаптер для списка городов
    ArrayAdapter<String> arrayAdapterMaxAge; // адаптер для формирование максимального возраста партнера
    String tmp_str; // временный буфер

    MaterialToolbar topAppBar; // верхняя панелька

    TextInputLayout til_name;
    TextInputEditText til_name_et; // имя пользователя
    TextInputLayout til_gender; // пол пользователя
    AutoCompleteTextView til_gender_act; // пол пользователя
    TextInputLayout til_age;
    AutoCompleteTextView til_age_act; // возраст пользователя
    TextInputLayout til_contact;
    TextInputEditText til_contact_et;
    AutoCompleteTextView til_gender_you_act; // пол партнера
    TextInputLayout til_age_min;
    AutoCompleteTextView til_age_min_act; // возраст партнера минимальный
    TextInputLayout til_age_max;
    AutoCompleteTextView til_age_max_act; // возраст партнера максимальный
    AutoCompleteTextView til_region_act; // регион
    AutoCompleteTextView til_town_act; // город
    TextInputLayout til_place; // место встречи
    TextInputEditText til_place_et; // место встречи
    TextInputLayout til_comment;
    TextInputEditText til_comment_et; // комментарий к встрече
    //Объявление - КОНЕЦ ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request_meeting, container, false);
    }

    @Override //Вызывается, когда отработает метод активности onCreate(), а значит фрагмент может обратиться к компонентам активности
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //инициализация - НАЧАЛО
        mAuth = FirebaseAuth.getInstance(); // инициализация объекта для работы с авторизацией
        fbStore = FirebaseFirestore.getInstance(); //инициализация БД
        meeting = new HashMap<>(); // коллекция ключ-значение для описания встречи
        listMeetingsTbActivity = (ActivityListMeetingsTb)getActivity();
        fragmentListMeetings = new FragmentListMeetings();
        fragmentPlace = new FragmentPlace();
        //arrayListAges = new ArrayList();



        // находим все вьюхи на активити
        til_name = getActivity().findViewById(R.id.til_name);
        til_name_et = getActivity().findViewById(R.id.til_name_et);
        til_gender = getActivity().findViewById(R.id.til_gender);
        til_gender_act = getActivity().findViewById(R.id.til_gender_act);
        til_age = getActivity().findViewById(R.id.til_age);
        til_age_act = getActivity().findViewById(R.id.til_age_act);
        til_contact = getActivity().findViewById(R.id.til_contact);
        til_contact_et = getActivity().findViewById(R.id.til_contact_et);
        til_gender_you_act = getActivity().findViewById(R.id.til_gender_you_act);
        til_age_min = getActivity().findViewById(R.id.til_age_min);
        til_age_min_act = getActivity().findViewById(R.id.til_age_min_act);
        til_age_max = getActivity().findViewById(R.id.til_age_max);
        til_age_max_act = getActivity().findViewById(R.id.til_age_max_act);
        til_region_act = getActivity().findViewById(R.id.til_region_act);
        til_town_act = getActivity().findViewById(R.id.til_town_act);
        til_place = getActivity().findViewById(R.id.til_place);
        til_place_et = getActivity().findViewById(R.id.til_place_et);
        til_comment = getActivity().findViewById(R.id.til_comment);
        til_comment_et = getActivity().findViewById(R.id.til_comment_et);
        topAppBar = getActivity().findViewById(R.id.topAppBar);

        topAppBar.setTitle("Заявка"); // заголовок в панельке верхней
        topAppBar.getMenu().findItem(R.id.request).setVisible(false); // скрываем пункт заявки на встречу

        //добавляем слушателей
        getActivity().findViewById(R.id.btn_apply_request).setOnClickListener((View.OnClickListener) this); // добавляем слушателя на кнопку

        // подгружаем сохраненные в памяти телефона данные
        saveParams = getActivity().getSharedPreferences("saveParams", MODE_PRIVATE); // инициализация объекта работы энергонезавичимой памятью, первый параметр имя файла, второй режим доступа, только для этого приложения
        til_name_et.setText(saveParams.getString("name", ""));
        til_gender_act.setText(saveParams.getString("gender", ""));
        til_age_act.setText(saveParams.getString("age", ""));
        til_contact_et.setText(saveParams.getString("contact", ""));
        til_gender_you_act.setText(saveParams.getString("gender_you", ""));
        til_age_min_act.setText(saveParams.getString("age_min", "18"));
        til_age_max_act.setText(saveParams.getString("age_max", "70"));
        til_region_act.setText(saveParams.getString("region", ""));
        til_town_act.setText(saveParams.getString("town", ""));
        //til_place_et.setText(saveParams.getString("place", ""));
        til_comment_et.setText(saveParams.getString("comment", ""));
        //инициализация - КОНЕЦ

        //наполняем низпадающий список выбора пола для выбора пола
        String[] gender = new String[] {"Мужской", "Женский"}; // Ниспадающий список выбора пола
        ArrayAdapter<String> adapter_gender = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, gender); // связываем с адаптером
        til_gender_act.setAdapter(adapter_gender);

        ArrayAdapter<String> arrayAdapterAge = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, CreateAges(18,70)); //  связываем адаптер с данными
        til_age_act.setAdapter(arrayAdapterAge);

        // til_contact ////////////////////////////////////////////////////////////////////////////
        if (til_contact_et.getText().toString().isEmpty()) {
            til_contact.setHelperText(getString(R.string.til_contact));
        } else {
            til_contact.setHelperText(" ");
        }
        til_contact_et.addTextChangedListener(new TextWatcher() { // при изменении текста в контактных данных
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (til_contact_et.getText().toString().isEmpty()) {
                    til_contact.setHelperText(getString(R.string.til_contact));
                } else {
                    til_contact.setHelperText(" ");
                }
            }
        });
        //////////////////////////////////////////////////////////////////////////////////////////////



        //til_gender_you_act//////////////////////////////////////////////////////////////////////////
        til_gender_you_act = getActivity().findViewById(R.id.til_gender_you_act); // низпадающий список выбора пола партнера
        til_gender_you_act.setAdapter(adapter_gender);
        //////////////////////////////////////////////////////////////////////////////////////////////



        //til_age_min_act,  til_age_max_act///////////////////////////////////////////////////////////////////////////
        ArrayAdapter<String> arrayAdapterMinAge = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, CreateAges(18,70)); //  связываем адаптер с данными
        til_age_min_act.setAdapter(arrayAdapterMinAge);

        if (til_age_min_act.getText().toString().equals("")) {//если поле с начальным возрастом пустое, то делваем весь диапазон возрастов в максимальном возразте
            arrayAdapterMaxAge = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, CreateAges(18,70)); //  связываем адаптер с данными
        } else { // если минимальный возраст выбран, то делаем диапазон макимальных возрастов от минимального
            arrayAdapterMaxAge = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, CreateAges(Integer.parseInt(til_age_min_act.getText().toString()),70)); //  связываем адаптер с данными
        }
        til_age_max_act.setAdapter(arrayAdapterMaxAge); // применяем данные

        til_age_min_act.setOnItemClickListener(new AdapterView.OnItemClickListener() { // как только выбрали минимальный возраст
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                if (!til_age_min_act.getText().toString().equals("")) { // если поле не пустое
                    //если выбранный минимальный возраст больше максимального, то в максимальный подставить минимальный
                    if (Integer.parseInt(til_age_min_act.getText().toString()) > Integer.parseInt(til_age_max_act.getText().toString())) {
                        til_age_max_act.setText(til_age_min_act.getText());
                    }
                    arrayAdapterMaxAge = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, CreateAges(Integer.parseInt(til_age_min_act.getText().toString()), 70));
                    til_age_max_act.setAdapter(arrayAdapterMaxAge); // применяем данные
//
            }
        });
        /////////////////////////////////////////////////////////////////////////////////////////////////////////



        // til_region_act //////////////////////////////////////////////////////////////////////////////////////
        ArrayAdapter<String> adapter_regions = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_region, Data.regionsTmp);
        til_region_act.setAdapter(adapter_regions);
        //////////////////////////////////////////////////////////////////////////////////////////////




        //til_town_act заполняем список с городами///////////////////////////////////////////////////
        til_region_act.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                switch(parent.getItemAtPosition(position).toString()) {
                    case "Республика Мордовия":
                        adapter_towns = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_town, Data.theRepublicOfMordovia);
                        break;
                    case "Нижегородская область":
                        adapter_towns = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_town, Data.nizhnyNovgorodRegion);
                        break;
                    default:
                        //оператор;
                        break;
                }
                til_town_act.setAdapter(adapter_towns);
                til_town_act.setText("");
            }
        });
        /////////////////////////////////////////////////////////////////////////////////////////////



        //til_place_et //////////////////////////////////////////////////////////////////////////////
        til_place_et.setText(""); //очищаем на всякий текст
        tmp_str = "";
        // если любое место, то так и пишем, если нет, то перечисляем все выбранные
        if(!saveParams.getString("placeAnyPlace", "").equals("")){
            tmp_str = "Любое место";
        } else { // не выбрано, что встреча в любом месте
            if (!saveParams.getString("placeStreet", "").equals("")){ //если выбрано это место, до добавляем его описание к общему списку
                tmp_str = tmp_str + saveParams.getString("placeStreet", "");
            }

            if (!saveParams.getString("placePicnic", "").equals("")){
                tmp_str = tmp_str + saveParams.getString("placePicnic", "");
            }
        }
        til_place_et.setText(tmp_str);


        til_place_et.setOnClickListener(new View.OnClickListener() { // при нажатии на поле
            @Override
            public void onClick(View v) {
                listMeetingsTbActivity.ChangeFragment(fragmentPlace, "fragmentPlace", true);
            }
        });
        //////////////////////////////////////////////////////////////////////////////////////////////



        // til_comment ///////////////////////////////////////////////////////////////////////////////
        if (til_comment_et.getText().toString().isEmpty()) {
            til_comment.setHelperText(getString(R.string.til_comment));
        } else {
            til_comment.setHelperText(" ");
        }
        til_comment_et.addTextChangedListener(new TextWatcher() { // при изменении текста в комментарии к встрече
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //til_contact.setHelperText("");

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (til_comment_et.getText().toString().isEmpty()) {
                    til_comment.setHelperText(getString(R.string.til_comment));
                } else {
                    til_comment.setHelperText(" ");
                }
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////


    }

    @Override
    public void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) { // если пользователь пустой, не авторизирован
            startActivity(new Intent(getActivity().getApplicationContext(), ActivityLogin.class)); // отправляем к началу на авторизацию
            getActivity().finish(); // убиваем активити
        }

    }

    @Override
    public void onClick(View v) { //чтобы метод заработал, в объявлении класса добавить implements View.OnClickListener
        if (v.getId() == R.id.btn_apply_request) { // если нажали на кнопку "Подать заявку", не забыть найти кнопку по id заранее и добавить на нее слушателя
            // Если поля все введены корректно
            if (!til_name_et.getText().toString().isEmpty() &
                !til_gender_act.getText().toString().isEmpty() &
                !til_age_act.getText().toString().isEmpty() &
                !til_contact_et.getText().toString().isEmpty() &
                !til_gender_you_act.getText().toString().isEmpty() &
                !til_age_min_act.getText().toString().isEmpty() &
                !til_age_max_act.getText().toString().isEmpty() &
                !til_region_act.getText().toString().isEmpty() &
                !til_town_act.getText().toString().isEmpty() &
                !til_comment_et.getText().toString().isEmpty()
                ) {

                    documentReference = fbStore.collection("meetings").document(currentUser.getEmail()); // подготавливаем коллекцию, внутри нее будут документы, внутри документов поля
                    meeting.clear();

                    meeting.put("name", til_name_et.getText().toString());
                    meeting.put("gender", til_gender_act.getEditableText().toString());
                    meeting.put("age", til_age_act.getText().toString());
                    meeting.put("contact", til_contact_et.getText().toString());
                    meeting.put("gender_you", til_gender_you_act.getEditableText().toString());
                    meeting.put("age_min", til_age_min_act.getText().toString());
                    meeting.put("age_max", til_age_max_act.getText().toString());
                    meeting.put("region", til_region_act.getEditableText().toString());
                    meeting.put("town", til_town_act.getEditableText().toString());
                    meeting.put("comment", til_comment_et.getText().toString());
                    meeting.put("userID", currentUser.getUid().toString());
                    meeting.put("token", ServiceFirebaseCloudMessaging.GetToken(getActivity().getApplicationContext()));
                    //meeting.put("id", currentUser.getEmail().toString());
                    

                    documentReference.set(meeting).addOnSuccessListener(new OnSuccessListener<Void>() { //
                        @Override
                        public void onSuccess(Void aVoid) {// если запись успешна
                            //Toast.makeText(Request.this, "Заявка добавлена", Toast.LENGTH_LONG).show();
                            //startActivity(new Intent(Request.this, ListMeeting.class)); // переходим в список встреч
                            //finish();

                            editorSaveParams = saveParams.edit(); // запоминаем в энергонезависимою память для входа
                            editorSaveParams.putString("name", til_name_et.getText().toString());
                            editorSaveParams.putString("gender", til_gender_act.getEditableText().toString());
                            editorSaveParams.putString("age", til_age_act.getText().toString());
                            editorSaveParams.putString("contact", til_contact_et.getText().toString());
                            editorSaveParams.putString("gender_you", til_gender_you_act.getEditableText().toString());
                            editorSaveParams.putString("age_min", til_age_min_act.getText().toString());
                            editorSaveParams.putString("age_max", til_age_max_act.getText().toString());
                            editorSaveParams.putString("region", til_region_act.getEditableText().toString());
                            editorSaveParams.putString("town", til_town_act.getEditableText().toString());
                            editorSaveParams.putString("comment", til_comment_et.getText().toString());
                            editorSaveParams.apply();

                            //Если лимит не исчерпан грузим фрагмент с заявками
                            //ActivityListMeetingsTb listMeetingsTbActivity = (ActivityListMeetingsTb)getActivity();
                            listMeetingsTbActivity.ChangeFragment(fragmentListMeetings, "fragmentListMeetings", false);
                            //getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_place, fragmentListMeetings, null).addToBackStack(null).commit();

                            //если лимит исчерпан, то переходим к оплате
                            //startActivity(new Intent(getActivity().getApplicationContext(), Activity_Yandex_Pay.class));
                        }
                    });


            } else {// если одно из полей не заполнено
                Toast.makeText(getActivity().getApplicationContext(), "Заполните все поля", Toast.LENGTH_LONG).show();
            }

        }
    }

    /**
     * Формирование списка возрастов
     * @param beginAge начальный возраст
     * @param endAge конечный возраст
     * @return список возрастов
     */
    private ArrayList CreateAges(int beginAge, int endAge)
    {
        ArrayList arrayListAges = new ArrayList();

        for (int i=beginAge; i <= endAge; i++ ) {
            arrayListAges.add(i);
        }

        return arrayListAges;
    }

}