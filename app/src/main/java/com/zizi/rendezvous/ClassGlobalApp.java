package com.zizi.rendezvous;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс наследованный от базового класса приложения. Предназначен для работы с общими функциями для всех активити, фрагментов и сервисов.
 * Функции по логирования, авторизации, работе с БД, хранению переменных.
 */
public class ClassGlobalApp extends Application {

    private Map<String, String> paramsToSave; // коллекция ключ-значение

    // инициализация объекта работы энергонезавичимой памятью, первый параметр имя файла, второй режим доступа, только для этого приложения
    private SharedPreferences sharedPreferences; //для работы с памятью
    private SharedPreferences.Editor editorSharedPreferences; // объект для редакции энергонезависимого хранилища

    public ClassGlobalApp(){

        paramsToSave = new HashMap<>(); // коллекция ключ-значение

    }

    /**
     * В конструкторе класса, еще не создан объект контекста приложения, а чтобы создать объект SharedPreferences нужен контекст приложения,
     * поэтому в этом метоже инициализируем этот объект.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("saveParams", MODE_PRIVATE);
        editorSharedPreferences = sharedPreferences.edit(); // подготавливаем редактор работы с памятью перед записью
    }

    /**
     * Добавляет запись в журнал (Logcat) выполнения программы, важность Verbose
     * @param _class класс источник
     * @param method метод класса
     * @param message сообщение
     */
    public void Log (String _class, String method, String message) {

        if (BuildConfig.DEBUG) { // если режим отладки, то ведем ЛОГ
            //символы !@# достаточно уникальны для фильтровки и быстро набираются на клавиатуре
            Log.v("!@#", "[" + _class + "/" + method + "]: " +  message);
        }

    }

    /**
     * Подготавливаем параметры для сохранения, каждый выхов добавляет запись в Map<String, String>.
     * Чтобы завершить созранение в память, нужно вызвать метод SaveParams ().
     * @param paramName имя параметра
     * @param paramValue значение параметра
     */
    public void PreparingToSave (String paramName, String paramValue) {

        paramsToSave.put(paramName, paramValue);

    }


    public void SaveParams () {

        //editorSharedPreferences = sharedPreferences.edit(); // подготавливаем редактор работы с памятью перед записью

        for(Map.Entry<String, String> item : paramsToSave.entrySet()){

            editorSharedPreferences.putString(item.getKey(), item.getValue());

        }

        editorSharedPreferences.apply(); // записываем в память
        paramsToSave.clear(); //чистим буфер для параметров перед следующей записью
    }

    public String GetParam (String paramName) {

        return sharedPreferences.getString(paramName, "");

    }



}
