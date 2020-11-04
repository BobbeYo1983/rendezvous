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
import android.widget.Button;
import android.widget.CheckBox;
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

public class FragmentRequestMeeting extends Fragment {

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
    ArrayAdapter<String> arrayAdapterMaxAge; // адаптер для формирование максимального возраста партнера
    String tmpStr; // временный буфер
    String requestNotFilled; // заявка не заполнялась

    //виджеты
    MaterialToolbar topAppBar; // верхняя панелька
    TextInputLayout til_name;
    TextInputEditText til_name_et; // имя пользователя
    TextInputLayout til_gender; // пол пользователя
    AutoCompleteTextView til_gender_act; // пол пользователя
    TextInputLayout til_age;
    AutoCompleteTextView til_age_act; // возраст пользователя
    TextInputEditText til_phone_et;     // контактный номер
    CheckBox cb_only_write; //галка можно ли звонить
    TextInputEditText til_soc_net_et; // страничка в соц сети
    TextInputLayout til_contact;
    TextInputEditText til_contact_et;
    TextInputLayout til_gender_partner;
    AutoCompleteTextView til_gender_partner_act; // пол партнера
    TextInputLayout til_age_min;
    AutoCompleteTextView til_age_min_act; // возраст партнера минимальный
    TextInputLayout til_age_max;
    AutoCompleteTextView til_age_max_act; // возраст партнера максимальный
    TextInputLayout til_region;
    AutoCompleteTextView til_region_act; // регион
    TextInputLayout til_town; // город
    AutoCompleteTextView til_town_act; // город
    TextInputLayout til_place; // место встречи
    TextInputEditText til_place_et; // место встречи
    TextInputLayout til_time;
    AutoCompleteTextView til_time_act; // время
    TextInputLayout til_comment;
    TextInputEditText til_comment_et; // комментарий к встрече
    Button btn_apply_request; // кнопка подачи заявки
    //Объявление - КОНЕЦ =============================================================================


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request_meeting, container, false);
    }

    @Override //Вызывается, когда отработает метод активности onCreate(), а значит фрагмент может обратиться к компонентам активности
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //инициализация - НАЧАЛО////////////////////////////////////////////////////////////////////
        mAuth = FirebaseAuth.getInstance(); // инициализация объекта для работы с авторизацией
        fbStore = FirebaseFirestore.getInstance(); //инициализация БД
        meeting = new HashMap<>(); // коллекция ключ-значение для описания встречи
        listMeetingsTbActivity = (ActivityListMeetingsTb)getActivity();
        fragmentListMeetings = new FragmentListMeetings();
        fragmentPlace = new FragmentPlace();
        saveParams = getActivity().getSharedPreferences("saveParams", MODE_PRIVATE); // инициализация объекта работы энергонезавичимой памятью, первый параметр имя файла, второй режим доступа, только для этого приложения


        // находим все вьюхи на активити
        til_name = getActivity().findViewById(R.id.til_name);
        til_name_et = getActivity().findViewById(R.id.til_name_et);
        til_gender = getActivity().findViewById(R.id.til_gender);
        til_gender_act = getActivity().findViewById(R.id.til_gender_act);
        til_age = getActivity().findViewById(R.id.til_age);
        til_age_act = getActivity().findViewById(R.id.til_age_act);
        til_phone_et = getActivity().findViewById(R.id.til_phone_et);
        cb_only_write = getActivity().findViewById(R.id.cb_only_write);
        til_soc_net_et = getActivity().findViewById(R.id.til_soc_net_et);
        til_contact = getActivity().findViewById(R.id.til_contact);
        til_contact_et = getActivity().findViewById(R.id.til_contact_et);
        til_gender_partner = getActivity().findViewById(R.id.til_gender_partner);
        til_gender_partner_act = getActivity().findViewById(R.id.til_gender_partner_act); // низпадающий список выбора пола партнера
        til_age_min = getActivity().findViewById(R.id.til_age_min);
        til_age_min_act = getActivity().findViewById(R.id.til_age_min_act);
        til_age_max = getActivity().findViewById(R.id.til_age_max);
        til_age_max_act = getActivity().findViewById(R.id.til_age_max_act);
        til_region = getActivity().findViewById(R.id.til_region);
        til_region_act = getActivity().findViewById(R.id.til_region_act);
        til_town = getActivity().findViewById(R.id.til_town);
        til_town_act = getActivity().findViewById(R.id.til_town_act);
        til_place = getActivity().findViewById(R.id.til_place);
        til_place_et = getActivity().findViewById(R.id.til_place_et);
        til_time = getActivity().findViewById(R.id.til_time);
        til_time_act = getActivity().findViewById(R.id.til_time_act);
        til_comment = getActivity().findViewById(R.id.til_comment);
        til_comment_et = getActivity().findViewById(R.id.til_comment_et);
        topAppBar = getActivity().findViewById(R.id.topAppBar);
        btn_apply_request = getActivity().findViewById(R.id.btn_apply_request);
        //инициализация - КОНЕЦ ========================================================================



        // topAppBar ////////////////////////////////////////////////////////////////////////////////
        topAppBar.setTitle("Заявка"); // заголовок в панельке верхней
        topAppBar.getMenu().findItem(R.id.request).setVisible(false); // скрываем пункт заявки на встречу

        requestNotFilled = saveParams.getString("requestNotFilled", "true"); // смотрим, подавалась ли ранее заявка или нет, если true, то не подавалась

        if(requestNotFilled.equals("true")) {// если заявка не заполнялась/не сохранялась
            topAppBar.setNavigationIcon(R.drawable.ic_outline_menu_24); // делаем кнопку навигации менюшкой в верхней панельке
            topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //getActivity().onBackPressed();
                }
            });
        } else {
            topAppBar.setNavigationIcon(R.drawable.ic_outline_arrow_back_24); // делаем кнопку навигации стрелкой в верхней панельке
            // событие при клике на кнопку навигации, на этом фрагменте она в виде стрелочки
            topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }
        //==========================================================================================



        // til_name_et //////////////////////////////////////////////////////////////////////////////
        til_name_et.setText(saveParams.getString("name", "")); // восстанавливаем текст из памяти
        // слушатель изменения текста
        til_name_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (!til_name_et.getText().toString().isEmpty()){
                    til_name.setErrorEnabled(false); // убираем отображение ошибки
                }
            }
        });
        //==========================================================================================



        // til_gender_act /////////////////////////////////////////////////////////////////////////
        til_gender_act.setThreshold(100); // чтобы при установлении текста отображался весь список, иначе будет предлагать только найденные строки по введенному тексту
        til_gender_act.setText(saveParams.getString("gender", ""));
        //наполняем низпадающий список выбора пола для выбора пола
        String[] gender = new String[] {"Мужской", "Женский"}; // Ниспадающий список выбора пола
        ArrayAdapter<String> adapter_gender = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, gender); // связываем с адаптером
        til_gender_act.setAdapter(adapter_gender);

        til_gender_act.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                til_gender.setErrorEnabled(false); // убираем описание ошибки
            }
        });

        //==========================================================================================



        // til_age_act /////////////////////////////////////////////////////////////////////////////
        til_age_act.setThreshold(100);
        til_age_act.setText(saveParams.getString("age", "")); // восстанавливаем выбранное значение из памяти
        // набиваем список для выбора
        ArrayAdapter<String> arrayAdapterAge = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, CreateAges(18,70)); //  связываем адаптер с данными
        til_age_act.setAdapter(arrayAdapterAge); // связываем представление с адаптером

        til_age_act.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                til_age.setErrorEnabled(false); // убираем описание ошибки
            }
        });
        // =========================================================================================



        //til_phone_et ////////////////////////////////////////////////////////////////////////////
        til_phone_et.setText(saveParams.getString("phone", "")); // восстанавливаем выбранное значение из памяти

        //слушатель введенного текста, нужен для показать или спрятать подсказку
        til_phone_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (til_phone_et.getText().toString().isEmpty()){ // если телефон не указан, то галку "не звонить" делаем неактивной
                    cb_only_write.setEnabled(false); //то галку "не звонить" делаем неактивной
                    cb_only_write.setChecked(false); // то галку убираем
                } else {
                    cb_only_write.setEnabled(true);
                }

            }
        });
        //================================================================================================



        // cb_only_write ////////////////////////////////////////////////////////////////////////////////
        //восстанавливаем из памяти
        if (saveParams.getString("onlyWrite", "false").equals("false")){ // если галка не сохранена
            cb_only_write.setChecked(false);// то не ставим галку
        } else {
            cb_only_write.setChecked(true);
        }
        // ==============================================================================================



        // til_soc_net_et ////////////////////////////////////////////////////////////////////////////////
        til_soc_net_et.setText(saveParams.getString("socNet", "")); // восстанавливаем выбранное значение из памяти);
        // =============================================================================================


        // til_contact ////////////////////////////////////////////////////////////////////////////
        til_contact_et.setText(saveParams.getString("contact", "")); // восстанавливаем выбранное значение из памяти

        //слушатель введенного текста, нужен для показать или спрятать подсказку
        if (til_contact_et.getText().toString().isEmpty()) {
            til_contact.setHelperText(getString(R.string.til_contact));
        } else {
            til_contact.setHelperText(" ");
        }
        til_contact_et.addTextChangedListener(new TextWatcher() { // при изменении текста в контактных данных
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (til_contact_et.getText().toString().isEmpty()) {
                    til_contact.setHelperText(getString(R.string.til_contact));
                } else {
                    til_contact.setHelperText(" ");
                }
            }
        });
        //=============================================================================================



        //til_gender_partner_act//////////////////////////////////////////////////////////////////////////
        til_gender_partner_act.setThreshold(100);
        til_gender_partner_act.setText(saveParams.getString("gender_partner", "")); // восстанавливаем выбранное значение из памяти

        til_gender_partner_act.setAdapter(adapter_gender); //список для выбора

        //слушатель при выборе любого элемента
        til_gender_partner_act.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                til_gender_partner.setErrorEnabled(false);
            }
        });
        //=============================================================================================



        //til_age_min_act,  til_age_max_act///////////////////////////////////////////////////////////////////////////
        til_age_min_act.setThreshold(100);
        til_age_max_act.setThreshold(100);
        til_age_min_act.setText(saveParams.getString("age_min", "18")); // восстанавливаем выбранное значение из памяти
        til_age_max_act.setText(saveParams.getString("age_max", "70"));

        //  связываем адаптер с данными
        ArrayAdapter<String> arrayAdapterMinAge = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, CreateAges(18,70));
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
                //если выбранный минимальный возраст больше максимального, то в максимальный подставить минимальный
                if (Integer.parseInt(til_age_min_act.getText().toString()) > Integer.parseInt(til_age_max_act.getText().toString())) {
                    til_age_max_act.setText(til_age_min_act.getText());
                }
                arrayAdapterMaxAge = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, CreateAges(Integer.parseInt(til_age_min_act.getText().toString()), 70));
                til_age_max_act.setAdapter(arrayAdapterMaxAge); // применяем данные

            }
        });
        //============================================================================================================



        // til_region_act //////////////////////////////////////////////////////////////////////////////////////
        til_region_act.setThreshold(100);
        til_region_act.setText(saveParams.getString("region", ""));  // восстанавливаем выбранное значение из памяти

        //формируем список для выбора
        ArrayAdapter<String> adapter_regions = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, Data.regionsTmp);
        til_region_act.setAdapter(adapter_regions);

        //слушатель - если меняется выбор региона
        til_region_act.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                // в зависимости от выбранного региона формируем список городов
                til_town_act.setAdapter(CreateAdapterTowns(parent.getItemAtPosition(position).toString()));
                til_town.setEnabled(true);
                til_town_act.setEnabled(true);
                til_town_act.setText("");

                til_region.setErrorEnabled(false); //сбрасываем ошибку
            }
        });
        //=====================================================================================================



        //til_town_act заполняем список с городами///////////////////////////////////////////////////
        til_town_act.setThreshold(100);
        //if (saveParams.getString("town", "").equals("")) {//если в памяти поле с городом пустое, то
        //String str = til_region_act.getText().toString();
        if (til_region_act.getText().toString().equals("")) { // если поле с регионом пустое, то блокируем выбор города
            til_town.setEnabled(false); // то делаем не активным
            til_town_act.setEnabled(false); // то делаем не активным
        } else {
            til_town.setEnabled(true);
            til_town_act.setEnabled(true); // то делаем активным
            til_town_act.setText(saveParams.getString("town", "")); // подгружаем имя города из памяти
            til_town_act.setAdapter(CreateAdapterTowns(saveParams.getString("region", "")));//тут нужно дернуть лушатель, чтобы подгрузил города
        }

        til_town_act.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                til_town.setErrorEnabled(false); // сбрасываем описание ошибки
            }
        });
        //===========================================================================================



        //til_place_et //////////////////////////////////////////////////////////////////////////////
        //Обновление значения поля в onResume()
        // Слушатель при нажатии на поле
        til_place_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                til_place.setErrorEnabled(false);
                SaveParams(); // сохраняем значения полей в память
                listMeetingsTbActivity.ChangeFragment(fragmentPlace, "fragmentPlace", true);
            }
        });


        //===========================================================================================



        // til_time_act /////////////////////////////////////////////////////////////////////////////
        til_time_act.setThreshold(100);
        til_time_act.setText(saveParams.getString("time", "")); // восстанавливаем выбранное значение из памяти

        //формируем список для сохранения времени
        ArrayAdapter<String> adapter_time = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, Data.times);
        til_time_act.setAdapter(adapter_time);

        til_time_act.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                til_time.setErrorEnabled(false);
            }
        });
        //==========================================================================================



        // til_comment ///////////////////////////////////////////////////////////////////////////////
        til_comment_et.setText(saveParams.getString("comment", "")); // восстанавливаем выбранное значение из памяти

        // показывать/не показывать подсказку
        if (til_comment_et.getText().toString().isEmpty()) {
            til_comment.setHelperText(getString(R.string.til_comment));
        } else {
            til_comment.setHelperText(" ");
        }

        //слушатель - показывать/не показывать подсказку
        til_comment_et.addTextChangedListener(new TextWatcher() { // при изменении текста в комментарии к встрече
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {             }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {            }
            @Override
            public void afterTextChanged(Editable s) {
                if (til_comment_et.getText().toString().isEmpty()) {
                    til_comment.setHelperText(getString(R.string.til_comment));
                } else {
                    til_comment.setHelperText(" ");
                }
            }
        });
        //===============================================================================================



        // btn_apply_request /////////////////////////////////////////////////////////////////////////
        //слушатель нажатия на кнопку подачи заявки
        btn_apply_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Если поля все введены корректно
                if (!til_name_et.getText().toString().isEmpty() &           // если имя не пустое
                    !til_gender_act.getText().toString().isEmpty() &        // если пол выбран
                    !til_age_act.getText().toString().isEmpty() &           //если возраст выбран
                    !til_gender_partner_act.getText().toString().isEmpty() &//если пол партнера выбран
                    !til_age_min_act.getText().toString().isEmpty() &       //если возраст минимальный партнера выбран
                    !til_age_max_act.getText().toString().isEmpty() &       //если возраст максимальный партнера выбран
                    !til_region_act.getText().toString().isEmpty() &        //если регион выбран
                    !til_town_act.getText().toString().isEmpty() &          //если город выбран
                    !til_place_et.getText().toString().isEmpty() &          //если место выбрано
                    !til_time_act.getText().toString().isEmpty()            //если время выбрано

                ) {

                    // подготавливаем коллекцию, внутри нее будут документы, внутри документов поля для подачи заявки
                    documentReference = fbStore.collection("meetings").document(currentUser.getEmail());
                    meeting.clear();

                    //добавляем параметры для подачи заявки
                    meeting.put("name", til_name_et.getText().toString().trim());
                    meeting.put("gender", til_gender_act.getEditableText().toString().trim());
                    meeting.put("age", til_age_act.getText().toString().trim());
                    meeting.put("phone", til_phone_et.getText().toString().trim());
                    meeting.put("onlyWrite", cb_only_write.isChecked());
                    meeting.put("socNet", til_soc_net_et.getText().toString().trim());
                    meeting.put("contact", til_contact_et.getText().toString().trim());
                    meeting.put("gender_partner", til_gender_partner_act.getEditableText().toString().trim());
                    meeting.put("age_min", til_age_min_act.getText().toString().trim());
                    meeting.put("age_max", til_age_max_act.getText().toString().trim());
                    meeting.put("region", til_region_act.getEditableText().toString().trim());
                    meeting.put("town", til_town_act.getEditableText().toString().trim());
                    meeting.put("place", til_place_et.getEditableText().toString().trim());
                    meeting.put("time", til_time_act.getEditableText().toString().trim());
                    meeting.put("comment", til_comment_et.getText().toString().trim());

                    //добавляем прочие служебные параметры для подачи заявки
                    meeting.put("userID", currentUser.getUid().toString());
                    meeting.put("token", ServiceFirebaseCloudMessaging.GetToken(getActivity().getApplicationContext()));

                    // если запись в БД успешна
                    documentReference.set(meeting).addOnSuccessListener(new OnSuccessListener<Void>() { //
                        @Override
                        public void onSuccess(Void aVoid) {

                            SaveParams(); // запоминаем поля

                            editorSaveParams = saveParams.edit(); // запоминаем в энергонезависимою память
                            editorSaveParams.putString("requestNotFilled", "false"); //делаем отметочку, что заявка хоть раз заполнялась
                            editorSaveParams.apply();

                            //Если лимит не исчерпан грузим фрагмент с заявками
                            //ActivityListMeetingsTb listMeetingsTbActivity = (ActivityListMeetingsTb)getActivity();
                            listMeetingsTbActivity.ChangeFragment(fragmentListMeetings, "fragmentListMeetings", false);
                            //getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_place, fragmentListMeetings, null).addToBackStack(null).commit();

                            //если лимит исчерпан, то переходим к оплате
                            //startActivity(new Intent(getActivity().getApplicationContext(), Activity_Yandex_Pay.class));
                        }
                    });


                } else {// если одно из обязательных полей не заполнено в заявке

                    if (til_name_et.getText().toString().isEmpty()) {          // если имя не пустое
                        til_name.setError("Введите Ваше имя");
                    }

                    if (til_gender_act.getText().toString().isEmpty()) {       // если пол не выбран
                        til_gender.setError("Выберите Ваш пол");
                    }

                    if (til_age_act.getText().toString().isEmpty()) {       //если возраст не выбран
                        til_age.setError("Выберите Ваш возраст");
                    }

                    if (til_gender_partner_act.getText().toString().isEmpty()) {       //если пол партнера не выбран
                        til_gender_partner.setError("Выберите пол");
                    }

                    if (til_age_min_act.getText().toString().isEmpty()) {       //если возраст минимальный партнера не выбран
                        til_age_min.setError("Выберите минимальный возраст");
                    }

                    if (til_age_max_act.getText().toString().isEmpty()) {       //если возраст максимальный партнера не выбран
                        til_age_max.setError("Выберите максимальный возраст");
                    }

                    if (til_region_act.getText().toString().isEmpty()) {       //если регион не выбран
                        til_region.setError("Выберите регион");
                    }

                    if (til_town_act.getText().toString().isEmpty()) {       //если город не выбран
                        til_town.setError("Выберите город");
                    }

                    if (til_place_et.getText().toString().isEmpty()) {       //если место не выбрано
                        til_place.setError("Выберите место встречи");
                    }

                    if (til_time_act.getText().toString().isEmpty()) {      //если время не выбрано
                        til_time.setError("Выберите время встречи");
                    }

                    Toast.makeText(getActivity().getApplicationContext(), "Заполните обязательные поля выделенные красным цветом", Toast.LENGTH_LONG).show();
                }


            }
        });
        //==================================================================================================
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
    public void onResume() {
        super.onResume();

        til_name_et.setText(saveParams.getString("name", "")); // восстанавливаем текст из памяти
        til_gender_act.setText(saveParams.getString("gender", ""));
        til_age_act.setText(saveParams.getString("age", "")); // восстанавливаем выбранное значение из памяти
        til_phone_et.setText(saveParams.getString("phone", "")); // восстанавливаем выбранное значение из памяти

        // cb_only_write ////////////////////////////////////////////////////////////////////////////////
        //восстанавливаем из памяти
        if (saveParams.getString("onlyWrite", "false").equals("false")){ // если галка не сохранена
            cb_only_write.setChecked(false);// то не ставим галку
        } else {
            cb_only_write.setChecked(true);
        }
        // ==============================================================================================

        til_soc_net_et.setText(saveParams.getString("socNet", "")); // восстанавливаем выбранное значение из памяти);
        til_contact_et.setText(saveParams.getString("contact", "")); // восстанавливаем выбранное значение из памяти
        til_gender_partner_act.setText(saveParams.getString("gender_partner", "")); // восстанавливаем выбранное значение из памяти
        til_age_min_act.setText(saveParams.getString("age_min", "18")); // восстанавливаем выбранное значение из памяти
        til_age_max_act.setText(saveParams.getString("age_max", "70"));
        til_region_act.setText(saveParams.getString("region", ""));  // восстанавливаем выбранное значение из памяти
        til_town_act.setText(saveParams.getString("town", "")); // подгружаем имя города из памяти



        //til_place_et /////////////////////////////////////////////////////////////////////////////////////
        til_place_et.setText(""); //очищаем на всякий текст
        tmpStr = "";
        // если любое место, то так и пишем, если нет, то перечисляем все выбранные
        if(!saveParams.getString("placeAnyPlace", "").equals("")){
            tmpStr = "Любое место";
        } else { // не выбрано, что встреча в любом месте

            tmpStr = "Выбранные места:";

            if (!saveParams.getString("placeStreet", "").equals("")){ //если выбрано это место, то добавляем его описание к общему списку
                tmpStr += "\n- " + saveParams.getString("placeStreet", "");
            }

            if (!saveParams.getString("placePicnic", "").equals("")){
                tmpStr += "\n- " + saveParams.getString("placePicnic", "");
            }

            if (!saveParams.getString("placeCar", "").equals("")){
                tmpStr += "\n- " + saveParams.getString("placeCar", "");
            }

            if (!saveParams.getString("placeSport", "").equals("")){
                tmpStr += "\n- " + saveParams.getString("placeSport", "");
            }

            if (!saveParams.getString("placeFilm", "").equals("")){
                tmpStr += "\n- " + saveParams.getString("placeFilm", "");
            }

            if (!saveParams.getString("placeBilliards", "").equals("")){
                tmpStr += "\n- " + saveParams.getString("placeBilliards", "");
            }

            if (!saveParams.getString("placeCafe", "").equals("")){
                tmpStr += "\n- " + saveParams.getString("placeCafe", "");
            }

            if (!saveParams.getString("placeDisco", "").equals("")){
                tmpStr += "\n- " + saveParams.getString("placeDisco", "");
            }

            if (!saveParams.getString("placeBath", "").equals("")){
                tmpStr += "\n- " + saveParams.getString("placeBath", "");
            }

            if (!saveParams.getString("placeMyHome", "").equals("")){
                tmpStr += "\n- " + saveParams.getString("placeMyHome", "");
            }

            if (!saveParams.getString("placeYouHome", "").equals("")){
                tmpStr += "\n- " + saveParams.getString("placeYouHome", "");
            }

            if (!saveParams.getString("placeHotel", "").equals("")){
                tmpStr += "\n- " + saveParams.getString("placeHotel", "");
            }

            if (!saveParams.getString("placeOther", "").equals("")){
                tmpStr += "\n- " + saveParams.getString("placeOther", "") + ": " + saveParams.getString("placeOtherDescription", "");
            }

            if (tmpStr.equals("Выбранные места:")){ // если не одна галка/место не выбрана
                tmpStr = "";
            }


        }
        til_place_et.setText(tmpStr);
        //==================================================================================================

        til_time_act.setText(saveParams.getString("time", "")); // восстанавливаем выбранное значение из памяти
        til_comment_et.setText(saveParams.getString("comment", "")); // восстанавливаем выбранное значение из памяти1233333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333331

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

    /**
     * Формирует список городов и возвращает в виде адаптера ArrayAdapter<String>
     * @param region регион
     * @return заполненный адаптер типа ArrayAdapter<String>
     */
    private ArrayAdapter<String> CreateAdapterTowns(String region){

        ArrayAdapter<String> adapter_towns;

        switch(region) {
            case "Республика Мордовия":
                adapter_towns = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, Data.theRepublicOfMordovia);
                break;
            case "Нижегородская область":
                adapter_towns = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list, Data.nizhnyNovgorodRegion);
                break;
            default:
                adapter_towns = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_drop_down_list); // наверное пустой будет, не проверял
                break;
        }
        return adapter_towns;
    }

    /**
     * Сохраняет значения введенных полей в энергонезависимую память
     */
    private void SaveParams () {
        editorSaveParams = saveParams.edit(); // запоминаем в энергонезависимою память

        editorSaveParams.putString("name", til_name_et.getText().toString());
        editorSaveParams.putString("gender", til_gender_act.getEditableText().toString());
        editorSaveParams.putString("age", til_age_act.getText().toString());
        editorSaveParams.putString("phone", til_phone_et.getText().toString().trim());
        editorSaveParams.putString("onlyWrite", String.valueOf(cb_only_write.isChecked()));
        editorSaveParams.putString("socNet", til_soc_net_et.getText().toString().trim());
        editorSaveParams.putString("contact", til_contact_et.getText().toString().trim());
        editorSaveParams.putString("gender_partner", til_gender_partner_act.getEditableText().toString());
        editorSaveParams.putString("age_min", til_age_min_act.getText().toString());
        editorSaveParams.putString("age_max", til_age_max_act.getText().toString());
        editorSaveParams.putString("region", til_region_act.getEditableText().toString());
        editorSaveParams.putString("town", til_town_act.getEditableText().toString());
        editorSaveParams.putString("place", til_place_et.getEditableText().toString());
        editorSaveParams.putString("time", til_time_act.getEditableText().toString());
        editorSaveParams.putString("comment", til_comment_et.getText().toString().trim());

        editorSaveParams.apply();
    }

}