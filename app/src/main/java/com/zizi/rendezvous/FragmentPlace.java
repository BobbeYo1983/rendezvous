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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


public class FragmentPlace extends Fragment {

    private ClassGlobalApp classGlobalApp; // глобальный класс для всего приложения

    //вьюхи
    private MaterialToolbar materialToolbar; // верхняя панелька

    private CheckBox cb_anyPlace; // чекбокс любое место
    private CheckBox cb_street; // Прогуляться на улице
    private CheckBox cb_picnic;
    private CheckBox cb_car;
    private CheckBox cb_sport;
    private CheckBox cb_film;
    private CheckBox cb_billiards;
    private CheckBox cb_cafe;
    private CheckBox cb_disco;
    private CheckBox cb_bath;
    private CheckBox cb_myHome;
    private CheckBox cb_youHome;
    private CheckBox cb_hotel;
    private CheckBox cb_other;

    private TextInputLayout til_other;
    private TextInputEditText til_other_et; // поле для ввода прочего места

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_place, container, false);
    }

    @Override //Вызывается, когда отработает метод активности onCreate(), а значит фрагмент может обратиться к компонентам активности
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        classGlobalApp = (ClassGlobalApp) getActivity().getApplicationContext();



        //ИЩЕМ ВЬЮХИ////////////////////////////////////////////////////////////////////////////////
        materialToolbar = getActivity().findViewById(R.id.materialToolbar);

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

        til_other = getActivity().findViewById(R.id.til_other);
        til_other_et = getActivity().findViewById(R.id.til_other_et);
        // ==========================================================================================



        // materialToolbar /////////////////////////////////////////////////////////////////////////////////
        materialToolbar.setTitle("Место встречи"); // заголовок панельки
        materialToolbar.setNavigationIcon(R.drawable.ic_outline_arrow_back_24); // делаем кнопку навигации стрелкой в верхней панельке

        // событие при клике на кнопку навигации, на этом фрагменте она в виде стрелочки
        materialToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        //===========================================================================================



        //восстанавливаем галки из памяти////////////////////////////////////////////////////////////
        if (classGlobalApp.GetRequestMeeting().getPlaceAnyPlace().equals(Data.ANY_PLACE)) {// если галка любое место стоит и сохранена
            cb_anyPlace.setChecked(true); //ставим галку любое место, а другие пункты делаем не активными

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

        } else { // если не стоит галка, что любое место, то пробегаемся по другим и восстанавливаем из памяти каждую

            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(0).equals(""))   {cb_street.setChecked(false);}        else {cb_street.setChecked(true);};
            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(1).equals(""))   {cb_picnic.setChecked(false);}        else {cb_picnic.setChecked(true);};
            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(2).equals(""))   {cb_car.setChecked(false);}           else {cb_car.setChecked(true);};
            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(3).equals(""))   {cb_sport.setChecked(false);}         else {cb_sport.setChecked(true);};
            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(4).equals(""))   {cb_film.setChecked(false);}          else {cb_film.setChecked(true);};
            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(5).equals(""))   {cb_billiards.setChecked(false);}     else {cb_billiards.setChecked(true);};
            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(6).equals(""))   {cb_cafe.setChecked(false);}          else {cb_cafe.setChecked(true);};
            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(7).equals(""))   {cb_disco.setChecked(false);}         else {cb_disco.setChecked(true);};
            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(8).equals(""))   {cb_bath.setChecked(false);}          else {cb_bath.setChecked(true);};
            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(9).equals(""))   {cb_myHome.setChecked(false);}        else {cb_myHome.setChecked(true);};
            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(10).equals(""))  {cb_youHome.setChecked(false);}       else {cb_youHome.setChecked(true);};
            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(11).equals(""))  {cb_hotel.setChecked(false);}         else {cb_hotel.setChecked(true);};
            if (classGlobalApp.GetRequestMeeting().getPlaceArray().get(12).equals(""))  {cb_other.setChecked(false);}         else {cb_other.setChecked(true);};

        }
        //============================================================================================



        // cb_anyPlace //////////////////////////////////////////////////////////////////////////////
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
        //==========================================================================================



        //Слушатель за галкой Прочее место, дает или не дает вводить текст в поле Прочее место////////
        cb_other.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    til_other.setEnabled(true);
                    til_other_et.setEnabled(true);
                } else {
                    til_other_et.setText("");
                    til_other.setEnabled(false);
                    til_other_et.setEnabled(false);
                }
            }
        });
        //============================================================================================



        // til_other_et ////////////////////////////////////////////////////////////////////////////
        til_other_et.setText(classGlobalApp.GetRequestMeeting().getPlaceOtherDescription());
        if (cb_other.isChecked()){
            til_other.setEnabled(true);
            til_other_et.setEnabled(true);
        }else{
            til_other.setEnabled(false);
            til_other_et.setEnabled(false);
        }
        //==========================================================================================


    }

    @Override
    public void onStart() {
        super.onStart();

        if (!classGlobalApp.IsAuthorized()) { // если пользователь не авторизован
            startActivity(new Intent(getActivity().getApplicationContext(), ActivityLogin.class)); // отправляем к началу на авторизацию
            getActivity().finish(); // убиваем активити
        }

    }


    @Override
    public void onPause() {
        super.onPause();

        // сохраняем выбранные места встреч в RAM
        classGlobalApp.Log("FragmentPlace", "onPause", "Сохранение выбранных мест в RAM", false);

        if (cb_anyPlace.isChecked())        { classGlobalApp.GetRequestMeeting().setPlaceAnyPlace(Data.ANY_PLACE); } else { classGlobalApp.GetRequestMeeting().setPlaceAnyPlace(""); }

        ArrayList<String> arrayListPlaces = new ArrayList<String>();

        if (cb_street.isChecked())      { arrayListPlaces.add(cb_street.getText().toString()); }     else { arrayListPlaces.add(""); }
        if (cb_picnic.isChecked())      { arrayListPlaces.add(cb_picnic.getText().toString()); }     else { arrayListPlaces.add("");}
        if (cb_car.isChecked())         { arrayListPlaces.add(cb_car.getText().toString()); }        else { arrayListPlaces.add(""); }
        if (cb_sport.isChecked())       { arrayListPlaces.add(cb_sport.getText().toString()); }      else { arrayListPlaces.add(""); }
        if (cb_film.isChecked())        { arrayListPlaces.add(cb_film.getText().toString()); }       else { arrayListPlaces.add(""); }
        if (cb_billiards.isChecked())   { arrayListPlaces.add(cb_billiards.getText().toString()); }  else { arrayListPlaces.add(""); }
        if (cb_cafe.isChecked())        { arrayListPlaces.add(cb_cafe.getText().toString()); }       else { arrayListPlaces.add(""); }
        if (cb_disco.isChecked())       { arrayListPlaces.add(cb_disco.getText().toString()); }      else { arrayListPlaces.add(""); }
        if (cb_bath.isChecked())        { arrayListPlaces.add(cb_bath.getText().toString()); }       else { arrayListPlaces.add(""); }
        if (cb_myHome.isChecked())      { arrayListPlaces.add(cb_myHome.getText().toString()); }     else { arrayListPlaces.add("");}
        if (cb_youHome.isChecked())     { arrayListPlaces.add(cb_youHome.getText().toString()); }    else { arrayListPlaces.add(""); }
        if (cb_hotel.isChecked())       { arrayListPlaces.add(cb_hotel.getText().toString()); }      else { arrayListPlaces.add(""); }
        if (cb_other.isChecked())       { arrayListPlaces.add(cb_other.getText().toString()); }      else { arrayListPlaces.add(""); }

        classGlobalApp.GetRequestMeeting().setPlaceArray(arrayListPlaces);

        classGlobalApp.GetRequestMeeting().setPlaceOtherDescription(til_other_et.getText().toString());

        classGlobalApp.ClearBundle();
        classGlobalApp.AddBundle("navigationFromFragmentPlace", "trueTrue");

/*        if (cb_anyPlace.isChecked())        { classGlobalApp.PreparingToSave("placeAnyPlace",   Data.ANY_PLACE); } else {classGlobalApp.PreparingToSave("placeAnyPlace",   ""); }

        if (cb_street.isChecked())          { classGlobalApp.PreparingToSave("placeStreet",     cb_street.getText().toString()); } else {classGlobalApp.PreparingToSave("placeStreet",   ""); }
        if (cb_picnic.isChecked())          { classGlobalApp.PreparingToSave("placePicnic",     cb_picnic.getText().toString()); } else {classGlobalApp.PreparingToSave("placePicnic",   ""); }
        if (cb_car.isChecked())             { classGlobalApp.PreparingToSave("placeCar",        cb_car.getText().toString()); } else {classGlobalApp.PreparingToSave("placeCar",   ""); }
        if (cb_sport.isChecked())           { classGlobalApp.PreparingToSave("placeSport",      cb_sport.getText().toString()); } else {classGlobalApp.PreparingToSave("placeSport",   ""); }
        if (cb_film.isChecked())            { classGlobalApp.PreparingToSave("placeFilm",       cb_film.getText().toString()); } else {classGlobalApp.PreparingToSave("placeFilm",   ""); }
        if (cb_billiards.isChecked())       { classGlobalApp.PreparingToSave("placeBilliards",  cb_billiards.getText().toString()); } else {classGlobalApp.PreparingToSave("placeBilliards",   ""); }
        if (cb_cafe.isChecked())            { classGlobalApp.PreparingToSave("placeCafe",       cb_cafe.getText().toString()); } else {classGlobalApp.PreparingToSave("placeCafe",   ""); }
        if (cb_disco.isChecked())           { classGlobalApp.PreparingToSave("placeDisco",      cb_disco.getText().toString()); } else {classGlobalApp.PreparingToSave("placeDisco",   ""); }
        if (cb_bath.isChecked())            { classGlobalApp.PreparingToSave("placeBath",       cb_bath.getText().toString()); } else {classGlobalApp.PreparingToSave("placeBath",   ""); }
        if (cb_myHome.isChecked())          { classGlobalApp.PreparingToSave("placeMyHome",     cb_myHome.getText().toString()); } else {classGlobalApp.PreparingToSave("placeMyHome",   ""); }
        if (cb_youHome.isChecked())         { classGlobalApp.PreparingToSave("placeYouHome",    cb_youHome.getText().toString()); } else {classGlobalApp.PreparingToSave("placeYouHome",   ""); }
        if (cb_hotel.isChecked())           { classGlobalApp.PreparingToSave("placeHotel",      cb_hotel.getText().toString()); } else {classGlobalApp.PreparingToSave("placeHotel",   ""); }
        if (cb_other.isChecked())           { classGlobalApp.PreparingToSave("placeOther",      cb_other.getText().toString()); } else {classGlobalApp.PreparingToSave("placeOther",   ""); }

        classGlobalApp.PreparingToSave("placeOtherDescription", til_other_et.getText().toString());

        classGlobalApp.Log("FragmentPlace", "onPause", "Сохранение выбранных мест в память", false);
        classGlobalApp.SaveParams();*/

    }


}