package com.zizi.rendezvous;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс наследованный от базового класса приложения. Предназначен для работы с общими функциями и переменными
 * для всех активити, фрагментов и сервисов. Функции по логированию, авторизации, хранению переменных.
 */
public class ClassGlobalApp extends Application {

    private Map<String, String> paramsToSave; // коллекция ключ-значение
    private Map<String, String> paramsToBundle; // коллекция ключ-значение

    // инициализация объекта работы энергонезавичимой памятью, первый параметр имя файла, второй режим доступа, только для этого приложения
    private SharedPreferences sharedPreferences; //для работы с памятью
    private SharedPreferences.Editor editorSharedPreferences; // объект для редакции энергонезависимого хранилища

    private FirebaseAuth firebaseAuth;

    public ClassGlobalApp(){

        paramsToSave = new HashMap<>(); // коллекция ключ-значение
        paramsToBundle = new HashMap<>(); // коллекция ключ-значение

    }

    /**
     * В конструкторе класса, еще не создан объект контекста приложения, а чтобы создать объект SharedPreferences нужен контекст приложения,
     * поэтому в этом методе инициализируем этот объект.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("saveParams", MODE_PRIVATE);
        editorSharedPreferences = sharedPreferences.edit(); // подготавливаем редактор работы с памятью перед записью'
        firebaseAuth = FirebaseAuth.getInstance(); // инициализация объекта для работы с авторизацией

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

    /**
     * Сохраняет параметры в БД после предварительной подготовки методом PreparingToSave()
     */
    public void SaveParams () {

        //editorSharedPreferences = sharedPreferences.edit(); // подготавливаем редактор работы с памятью перед записью

        for(Map.Entry<String, String> item : paramsToSave.entrySet()){

            editorSharedPreferences.putString(item.getKey(), item.getValue());

        }

        editorSharedPreferences.apply(); // записываем в память
        paramsToSave.clear(); //чистим буфер для параметров перед следующей записью
    }

    /**
     * Читает параметр из памяти телефона
     * @param paramName имя параметра
     * @return возвращает значение параметра в виде String
     */
    public String GetParam (String paramName) {

        return sharedPreferences.getString(paramName, "");

    }

    /**
     * Добавляет параметры в буфер для передачи между различными активити и фрагментами
     * @param paramName имя параметра
     * @param paramValue значение параметра
     */
    public void AddBundle (String paramName, String paramValue) {

        paramsToBundle.put(paramName, paramValue);
    }

    /**
     * Возвращает значение параметра из буфера для передачи между различными активити и фрагментами
     * @param paramName имя параметра
     * @return значение параметра
     */
    public String GetBundle(String paramName){

        return paramsToBundle.get(paramName);
    }

    /**
     * Очищает буфер для передачи между различными активити и фрагментами
     */
    public void ClearBundle(){
        paramsToBundle.clear();
    }

    /**
     * Проверяет авторизован ли пользователь
     */
    public boolean IsAuthorized(){

        //firebaseAuth.signOut(); //для проверки работы

        if (firebaseAuth.getCurrentUser() == null) { // если пользователь пустой, не авторизирован
            return false;
        } else {
            return true;
        }


    }


}
