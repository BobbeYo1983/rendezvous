package com.zizi.rendezvous;

import android.app.Application;
import android.util.Log;

/**
 * Класс наследованный от базового класса приложения. Предназначен для работы с общими функциями для всех активити, фрагментов и сервисов.
 * Функции по логирования, авторизации, работе с БД, хранению переменных.
 */
public class ClassGlobalApp extends Application {

    //public String TagForLog; ////
    //public String tmp;

    /**
     * Добавляет запись в журнал (Logcat) выполнения программы, важность Verbose
     * @param _class - класс источник
     * @param method - метод класса
     * @param message - сообщение
     */
    public void Log (String _class, String method, String message) {

        if (BuildConfig.DEBUG) { // если режим отладки, то ведем ЛОГ
            //символы !@# достаточно уникальны для фильтровки и быстро набираются на клавиатуре
            Log.v("!@#", "[" + _class + "/" + method + "]: " +  message);
        }

    }

}
