package com.zizi.rendezvous;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private Map<String, Object> mapDocument; //коллекция ключ-значение для вычитки документа из БД, будем возвращать ее

    // инициализация объекта работы энергонезавичимой памятью, первый параметр имя файла, второй режим доступа, только для этого приложения
    private SharedPreferences sharedPreferences; //для работы с памятью
    private SharedPreferences.Editor editorSharedPreferences; // объект для редакции энергонезависимого хранилища

    // Cloud Firestore
    private FirebaseFirestore firebaseFirestore; // база данных
    private DocumentReference documentReference; // ссылка на документ


    public ClassGlobalApp(){

        paramsToSave = new HashMap<>(); // коллекция ключ-значение
        mapDocument = new HashMap<String, Object>();
        firebaseFirestore = FirebaseFirestore.getInstance(); //инициализация БД

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
     * Читает документ из базы Firebase Firestore
     * @param nameCollection имя коллекции
     * @param nameDocument имя документа
     * @return коллекцию "ключ-значение"
     */
    public Map<String, Object> ReadDocument (final String nameCollection, final String nameDocument) {

        mapDocument.clear(); // очищаем коллекцию

        documentReference = firebaseFirestore.collection(nameCollection).document(nameDocument); // формируем путь к документу
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() { // вешаем слушателя на задачу чтения документа из БД
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) { // как задача чтения выполнилась
                if (task.isSuccessful()) { // если выполнилась успешно
                    DocumentSnapshot document = task.getResult(); // получаем документ
                    if (document.exists()) { // если документ такой есть, не null

                        mapDocument = document.getData(); // получаем данные из документа БД

                    } else { // если документа не существует

                        Log(this.getClass().getSimpleName(), "ReadDocument", "Запрошенного документа (" + nameCollection + "/" + nameDocument + ") нет в БД");
                    }

                } else { // если ошибка чтения БД

                    Log (this.getClass().getSimpleName(), "ReadDocument", "Ошибка чтения БД: " + task.getException());
                }
            }
        });

        return mapDocument;
    }



}
