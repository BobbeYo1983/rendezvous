package com.zizi.rendezvous;

import android.content.Intent;
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


public class FragmentPlace extends Fragment {

    //ОБЪЯВЛЕНИЕ///////////////////////////////////////////////////////////////////////////////////
    FirebaseAuth mAuth; // для работы с FireBase
    FirebaseUser currentUser; //текущий пользователь
    MaterialToolbar topAppBar; // верхняя панелька

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

        //ИЩЕМ ВЬЮХИ
        topAppBar = getActivity().findViewById(R.id.topAppBar);

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





        topAppBar.setTitle("Место встречи"); // заголовок панельки
        topAppBar.setNavigationIcon(R.drawable.ic_outline_arrow_back_24); // делаем кнопку навигации стрелкой в верхней панельке

        // событие при клике на кнопку навигации, на этом фрагменте она в виде стрелочки
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

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

        //adapter.startListening(); // адаптер начинает слушать БД

    }
}