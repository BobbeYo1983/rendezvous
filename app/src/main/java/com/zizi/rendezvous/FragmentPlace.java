package com.zizi.rendezvous;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class FragmentPlace extends Fragment {

    //ОБЪЯВЛЕНИЕ///////////////////////////////////////////////////////////////////////////////////
    FirebaseAuth mAuth; // для работы с FireBase
    FirebaseUser currentUser; //текущий пользователь
    MaterialToolbar topAppBar; // верхняя панелька

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

        topAppBar.setTitle("Место встречи"); // заголовок чата
        topAppBar.setNavigationIcon(R.drawable.ic_outline_arrow_back_24); // делаем кнопку навигации стрелкой в верхней панельке

        // событие при клике на кнопку навигации, на этом фрагменте она в виде стрелочки
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
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