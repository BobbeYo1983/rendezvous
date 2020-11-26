package com.zizi.rendezvous;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.Context.MODE_PRIVATE;


public class FragmentPlace extends Fragment {

    //ОБЪЯВЛЕНИЕ///////////////////////////////////////////////////////////////////////////////////
    FirebaseAuth mAuth; // для работы с FireBase
    FirebaseUser currentUser; //текущий пользователь
    MaterialToolbar topAppBar; // верхняя панелька
    SharedPreferences saveParams; // хранилище в энергонезависимой памяти любых параметров
    SharedPreferences.Editor editorSaveParams; // объект для редакции энергонезависимого хранилища

    CheckBox cb_anyPlace; // чекбокс любое место
    CheckBox cb_street; // Прогуляться на улице
    CheckBox cb_picnic;
    CheckBox cb_car;
    CheckBox cb_sport;
    CheckBox cb_film;
    CheckBox cb_billiards;
    CheckBox cb_cafe;
    CheckBox cb_disco;
    CheckBox cb_bath;
    CheckBox cb_myHome;
    CheckBox cb_youHome;
    CheckBox cb_hotel;
    CheckBox cb_other;

    TextInputEditText til_other_et; // поле для ввода прочего места

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_place, container, false);
    }

    @Override //Вызывается, когда отработает метод активности onCreate(), а значит фрагмент может обратиться к компонентам активности
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //ИНИЦИАЛИЗАЦИЯ//////////////////////////////////////////////////////////////////////////////
        mAuth = FirebaseAuth.getInstance(); // инициализация объекта для работы с авторизацией
        saveParams = getActivity().getSharedPreferences("saveParams", MODE_PRIVATE); // инициализация объекта работы энергонезавичимой памятью, первый параметр имя файла, второй режим доступа, только для этого приложения

        //ИЩЕМ ВЬЮХИ
        topAppBar = getActivity().findViewById(R.id.materialToolbar);

        cb_anyPlace = getActivity().findViewById(R.id.cb_anyPlace);
        cb_street = getActivity().findViewById(R.id.cb_street);
        cb_picnic = getActivity().findViewById(R.id.cb_picnic);
        cb_car = getActivity().findViewById(R.id.cb_car);
        cb_sport = getActivity().findViewById(R.id.cb_sport);
        cb_film = getActivity().findViewById(R.id.cb_film);
        cb_billiards = getActivity().findViewById(R.id.cb_billiards);
        cb_cafe = getActivity().findViewById(R.id.cb_cafe);
        cb_disco = getActivity().findViewById(R.id.cb_disco);
        cb_bath = getActivity().findViewById(R.id.cb_bath);
        cb_myHome = getActivity().findViewById(R.id.cb_myHome);
        cb_youHome = getActivity().findViewById(R.id.cb_youHome);
        cb_hotel = getActivity().findViewById(R.id.cb_hotel);
        cb_other = getActivity().findViewById(R.id.cb_other);

        til_other_et = getActivity().findViewById(R.id.til_other_et);
        // ==========================================================================================



        // topAppBar /////////////////////////////////////////////////////////////////////////////////
        topAppBar.setTitle("Место встречи"); // заголовок панельки
        topAppBar.setNavigationIcon(R.drawable.ic_outline_arrow_back_24); // делаем кнопку навигации стрелкой в верхней панельке

        // событие при клике на кнопку навигации, на этом фрагменте она в виде стрелочки
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        //===========================================================================================

        //Слушатель изменения общей галки Любое место
        cb_anyPlace.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {

                    cb_street.setChecked(true); // ставим галки
                    cb_picnic.setChecked(true);
                    cb_car.setChecked(true);
                    cb_sport.setChecked(true);
                    cb_film.setChecked(true);
                    cb_billiards.setChecked(true);
                    cb_cafe.setChecked(true);
                    cb_disco.setChecked(true);
                    cb_bath.setChecked(true);
                    cb_myHome.setChecked(true);
                    cb_youHome.setChecked(true);
                    cb_hotel.setChecked(true);
                    cb_other.setChecked(true);

                    cb_street.setEnabled(false); // делаем не активные
                    cb_picnic.setEnabled(false);
                    cb_car.setEnabled(false);
                    cb_sport.setEnabled(false);
                    cb_film.setEnabled(false);
                    cb_billiards.setEnabled(false);
                    cb_cafe.setEnabled(false);
                    cb_disco.setEnabled(false);
                    cb_bath.setEnabled(false);
                    cb_myHome.setEnabled(false);
                    cb_youHome.setEnabled(false);
                    cb_hotel.setEnabled(false);
                    cb_other.setEnabled(false);


                } else {

                    cb_street.setChecked(false);
                    cb_picnic.setChecked(false);
                    cb_car.setChecked(false);
                    cb_sport.setChecked(false);
                    cb_film.setChecked(false);
                    cb_billiards.setChecked(false);
                    cb_cafe.setChecked(false);
                    cb_disco.setChecked(false);
                    cb_bath.setChecked(false);
                    cb_myHome.setChecked(false);
                    cb_youHome.setChecked(false);
                    cb_hotel.setChecked(false);
                    cb_other.setChecked(false);

                    cb_street.setEnabled(true);
                    cb_picnic.setEnabled(true);
                    cb_car.setEnabled(true);
                    cb_sport.setEnabled(true);
                    cb_film.setEnabled(true);
                    cb_billiards.setEnabled(true);
                    cb_cafe.setEnabled(true);
                    cb_disco.setEnabled(true);
                    cb_bath.setEnabled(true);
                    cb_myHome.setEnabled(true);
                    cb_youHome.setEnabled(true);
                    cb_hotel.setEnabled(true);
                    cb_other.setEnabled(true);
                }
            }
        });

        //Слушатель за галкой Прочее место, дает или не дает вводить текст в поле Прочее место
        cb_other.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    til_other_et.setEnabled(true);
                } else {
                    til_other_et.setText("");
                    til_other_et.setEnabled(false);
                }
            }
        });


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
    public void onPause() {
        super.onPause();

        // сохраняем выбранные места встреч
        editorSaveParams = saveParams.edit(); // запоминаем в энергонезависимою память для входа

        if (cb_anyPlace.isChecked())    { editorSaveParams.putString("placeAnyPlace",   cb_anyPlace.getText().toString()); } else {editorSaveParams.putString("placeAnyPlace", ""); }
        if (cb_street.isChecked())      { editorSaveParams.putString("placeStreet",     cb_street.getText().toString()); } else {editorSaveParams.putString("placeStreet", ""); }
        if (cb_picnic.isChecked())      { editorSaveParams.putString("placePicnic",     cb_picnic.getText().toString()); } else {editorSaveParams.putString("placePicnic", ""); }
        if (cb_car.isChecked())         { editorSaveParams.putString("placeCar",        cb_car.getText().toString()); } else {editorSaveParams.putString("placeCar", ""); }
        if (cb_sport.isChecked())       { editorSaveParams.putString("placeSport",      cb_sport.getText().toString()); } else {editorSaveParams.putString("placeSport", ""); }
        if (cb_film.isChecked())        { editorSaveParams.putString("placeFilm",       cb_film.getText().toString()); } else {editorSaveParams.putString("placeFilm", ""); }
        if (cb_billiards.isChecked())   { editorSaveParams.putString("placeBilliards",  cb_billiards.getText().toString()); } else {editorSaveParams.putString("placeBilliards", ""); }
        if (cb_cafe.isChecked())        { editorSaveParams.putString("placeCafe",       cb_cafe.getText().toString()); } else {editorSaveParams.putString("placeCafe", ""); }
        if (cb_disco.isChecked())       { editorSaveParams.putString("placeDisco",      cb_disco.getText().toString()); } else {editorSaveParams.putString("placeDisco", ""); }
        if (cb_bath.isChecked())        { editorSaveParams.putString("placeBath",       cb_bath.getText().toString()); } else {editorSaveParams.putString("placeBath", ""); }
        if (cb_myHome.isChecked())      { editorSaveParams.putString("placeMyHome",     cb_myHome.getText().toString()); } else {editorSaveParams.putString("placeMyHome", ""); }
        if (cb_youHome.isChecked())     { editorSaveParams.putString("placeYouHome",    cb_youHome.getText().toString()); } else {editorSaveParams.putString("placeYouHome", ""); }
        if (cb_hotel.isChecked())       { editorSaveParams.putString("placeHotel",      cb_hotel.getText().toString()); } else {editorSaveParams.putString("placeHotel", ""); }
        if (cb_other.isChecked())       { editorSaveParams.putString("placeOther",      cb_other.getText().toString()); } else {editorSaveParams.putString("placeOther", ""); }
        editorSaveParams.putString("placeOtherDescription", til_other_et.getText().toString());

        editorSaveParams.apply();
    }

    @Override
    public void onResume() {
        super.onResume();

        // восстанавливаем места встречь из памяти телефона
        if (saveParams.getString("placeAnyPlace",   cb_anyPlace.getText().toString())   .equals(cb_anyPlace.getText().toString()))  {cb_anyPlace.setChecked(true);} else {cb_anyPlace.setChecked(false);};
        if (saveParams.getString("placeStreet",     cb_street.getText().toString())     .equals(cb_street.getText().toString()))    {cb_street.setChecked(true);} else {cb_street.setChecked(false);};
        if (saveParams.getString("placePicnic",     cb_picnic.getText().toString())     .equals(cb_picnic.getText().toString()))    {cb_picnic.setChecked(true);} else {cb_picnic.setChecked(false);};
        if (saveParams.getString("placeCar",        cb_car.getText().toString())        .equals(cb_car.getText().toString()))       {cb_car.setChecked(true);} else {cb_car.setChecked(false);};
        if (saveParams.getString("placeSport",      cb_sport.getText().toString())      .equals(cb_sport.getText().toString()))     {cb_sport.setChecked(true);} else {cb_sport.setChecked(false);};
        if (saveParams.getString("placeFilm",       cb_film.getText().toString())       .equals(cb_film.getText().toString()))      {cb_film.setChecked(true);} else {cb_film.setChecked(false);};
        if (saveParams.getString("placeBilliards",  cb_billiards.getText().toString())  .equals(cb_billiards.getText().toString())) {cb_billiards.setChecked(true);} else {cb_billiards.setChecked(false);};
        if (saveParams.getString("placeCafe",       cb_cafe.getText().toString())       .equals(cb_cafe.getText().toString()))      {cb_cafe.setChecked(true);} else {cb_cafe.setChecked(false);};
        if (saveParams.getString("placeDisco",      cb_disco.getText().toString())      .equals(cb_disco.getText().toString()))     {cb_disco.setChecked(true);} else {cb_disco.setChecked(false);};
        if (saveParams.getString("placeBath",       cb_bath.getText().toString())       .equals(cb_bath.getText().toString()))      {cb_bath.setChecked(true);} else {cb_bath.setChecked(false);};
        if (saveParams.getString("placeMyHome",     cb_myHome.getText().toString())     .equals(cb_myHome.getText().toString()))    {cb_myHome.setChecked(true);} else {cb_myHome.setChecked(false);};
        if (saveParams.getString("placeYouHome",    cb_youHome.getText().toString())    .equals(cb_youHome.getText().toString()))   {cb_youHome.setChecked(true);} else {cb_youHome.setChecked(false);};
        if (saveParams.getString("placeHotel",      cb_hotel.getText().toString())      .equals(cb_hotel.getText().toString()))     {cb_hotel.setChecked(true);} else {cb_hotel.setChecked(false);};
        if (saveParams.getString("placeOther",      cb_other.getText().toString())      .equals(cb_other.getText().toString()))     {cb_other.setChecked(true);} else {cb_other.setChecked(false);};
        til_other_et.setText(saveParams.getString("placeOtherDescription", ""));



    }
}