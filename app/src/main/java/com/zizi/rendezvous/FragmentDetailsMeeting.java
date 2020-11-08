package com.zizi.rendezvous;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;


public class FragmentDetailsMeeting extends Fragment {

    // ОБЪЯВЛЕНИЕ ///////////////////////////////////////////////////////////////////////////////
    private Bundle bundle; // для приема параметров в фрагмент
    private Map<String, Object> mapDocument; //Документ с информацией о встрече
    private ActivityMeetings activityMeetings; // активити для переключения фрагментов из фрагментов

    //виджеты
    private TextInputLayout til_name;
    private TextInputEditText til_name_et; // имя пользователя

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
        bundle = getArguments();
        mapDocument = new HashMap<String, Object>();
        activityMeetings = (ActivityMeetings)getActivity(); // получаем объект текущей активити

        // находим все вьюхи на активити
        til_name = getActivity().findViewById(R.id.til_name);
        til_name_et = getActivity().findViewById(R.id.til_name_et);
        //==========================================================================================


        if (bundle != null) { // извлекаем аргументы полученные из другого фрагмента

            //Читаем документ со встречей партнера из БД
            mapDocument = activityMeetings.classDataBase.ReadDocument("meetings", bundle.getString("partnerEmail", ""));
            //mapDocument = activityMeetings.classDataBase.ReadDocument("meetings", bundle.getString("partnerEmail", ""));

        }

        //til_name_et.setText(mapDocument.get("email"));


    }
}