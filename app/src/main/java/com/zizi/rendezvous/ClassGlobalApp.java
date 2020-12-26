package com.zizi.rendezvous;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс наследованный от базового класса приложения. Предназначен для работы с общими функциями и переменными
 * для всех активити, фрагментов и сервисов. Функции по логированию, авторизации, хранению переменных.
 */
public class ClassGlobalApp extends Application {

    //private boolean notificationMessage;

    private Map<String, String> paramsToSave; // коллекция ключ-значение
    private Map<String, String> paramsToBundle; // коллекция ключ-значение

    // инициализация объекта работы энергонезавичимой памятью, первый параметр имя файла, второй режим доступа, только для этого приложения
    private SharedPreferences sharedPreferences; //для работы с памятью
    private SharedPreferences.Editor editorSharedPreferences; // объект для редакции энергонезависимого хранилища

    private FirebaseAuth firebaseAuth;

    private FirebaseDatabase firebaseDatabase; // БД Realtime Database
    private DatabaseReference databaseReference; //ссылка на данные в базе
    private Map<String, Object> msg; // сообщение в БД

    private FirebaseFirestore firebaseFirestore; // база данных
    private DocumentReference documentReference; // для работы с документами в базе, нужно знать структуру базы FirebaseFirestore
    private CollectionReference collectionReference; // для работы с коллекциями в БД, нужно знать структуру/информационную модель базы FirebaseFirestore

    private String tokenDevice; //идентификатор устройства, он меняется только в некоторых случаях, читать интернет



    /**
     * Конструктор, тут еще контекст приложения не создан, не вся инициализация может проходить, поэтому можно инициализировать позже в onCreate()
     */
    public ClassGlobalApp(){

        Log("ClassGlobalApp", "ClassGlobalApp", "Создан объект класса", false);

    }

    /**
     * В конструкторе класса, еще не создан объект контекста приложения, а чтобы создать объект SharedPreferences нужен контекст приложения,
     * поэтому в этом методе инициализируем этот объект.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        paramsToSave = new HashMap<>(); // коллекция ключ-значение
        paramsToBundle = new HashMap<>(); // коллекция ключ-значение
        msg = new HashMap<>();

        sharedPreferences = getSharedPreferences("saveParams", MODE_PRIVATE);
        editorSharedPreferences = sharedPreferences.edit(); // подготавливаем редактор работы с памятью перед записью'

        firebaseAuth = FirebaseAuth.getInstance(); // инициализация объекта для работы с авторизацией
        firebaseDatabase = FirebaseDatabase.getInstance(); // БД RealTime DataBase
        firebaseFirestore = FirebaseFirestore.getInstance(); // инициализация объект для работы с базой

        tokenDevice = GetParam("tokenDevice");
        //notificationMessage = false;

        //Прежде чем генерировать уведомления в приложении, нужно один раз хотя бы зарегистрировать канал уведомлений
        createNotificationChannel();

    }

    /**
     * Добавляет запись в журнал (Logcat) выполнения программы, важность Verbose
     * @param _class класс источник
     * @param method метод класса
     * @param message сообщение
     */
    public void Log (String _class, String method, String message, boolean inDB) {

        if (BuildConfig.DEBUG) { // если режим отладки, то ведем ЛОГ
            //символы !@# достаточно уникальны для фильтровки и быстро набираются на клавиатуре
            //Log.v("!@#", "[" + _class + "." + method + "]: " +  message);
            //Data.tagLog еще есть в классе NotificationMessage
            Log.v(Data.tagLog, "[" + _class + "." + method + "]: " +  message);

        }

        if (inDB) {
            msg.clear();
            msg.put("timestamp", ServerValue.TIMESTAMP);
            msg.put("class", _class);
            msg.put("method", method);
            msg.put("message", message);
            //databaseReference = firebaseDatabase.getReference("logs");
            //databaseReference.push().setValue(msg);
            GenerateDatabaseReference("logs").push().setValue(msg);
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
        //return "paramsToBundle.get(paramName)";
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

    /**
     * Получить Email текущего пользователя
     * @return Email текущего пользователя
     */
    public String GetCurrentUserEmail() {
        return firebaseAuth.getCurrentUser().getEmail();
    }

    /**
     * Получить индентификатор текущего пользователя
     * @return индентификатор текущего пользователя
     */
    public String GetCurrentUserUid() {
        return firebaseAuth.getCurrentUser().getUid();
    }

    /**
     * Возвращает идентификатор (токен) устройства
     * @return идентификатор (токен) устройства
     */
    public String GetTokenDevice() {

        if (tokenDevice.isEmpty()) { // если токен пустой
            tokenDevice = GetParam("tokenDevice"); // прочитаем его из памяти телефона
        }
        return tokenDevice;
    }

    /**
     * Изменяет токен устройства и сразу его сохраняет в память устройства,
     * так как он меняется редко (когда смотреть интернет) и при авторизации его, например, не запросить
     * @param tokenDevice новый идентификатор устройства
     */
    public void SetTokenDevice(String tokenDevice) {
        this.tokenDevice = tokenDevice;
        PreparingToSave("tokenDevice", tokenDevice); //готовим к сохранению
        SaveParams(); // сохраняем в девайс

/*        if (GetParam("requestIsActive").equals("trueTrue")) { //если заявка подана и активна, то нужно сверить токен с заявкой, протому что, могли зайти с другого девайса или переустановить приложение
            documentReference = GenerateDocumentReference("meetings", GetCurrentUserUid()); // документ со встречей текущего пользователя
            paramsToSave.clear();
            paramsToSave.put("tokenDevice", tokenDevice);
            //записываем токен в БД
            documentReference.set(paramsToSave).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //Toast.makeText(SelectLocationActivity.this, "Запись прошла успешно", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log(this.getClass().getSimpleName().toString(), "SetTokenDevice",
                                "tokenDevice изменился. Ошибка при записи tokenDevice в активную заявку на встречу. Заявка будет помечена, как неактивная. Нужно заполнить заявку по новой.", true);
                        PreparingToSave("requestIsActive", ""); //готовим к сохранению
                        SaveParams(); // сохраняем в девайс

                    }
                }
            });
        }*/


    }



    /**
     * Формирует ссылку на БД FireBase Realtime Database в зависимости от варианта сборки Debug или Release
     * @param path путь к данным в БД
     * @return сгенерированная ссылка
     */
    public DatabaseReference GenerateDatabaseReference (String path){

        if (BuildConfig.DEBUG) { // если режим отладки
            path = "debug/" + path;
        }

        return databaseReference = firebaseDatabase.getReference(path); //формируем ссылку
    }



    /**
     * Формирует ссылку на документ в БД FireBase Cloud Farestore в зависимости от варианта сборки Debug или Release
     * @param collection имя коллекции в БД
     * @param document имя документа в коллекции
     * @return сгенерированная ссылка
     */
    public DocumentReference GenerateDocumentReference (String collection, String document){

        if (BuildConfig.DEBUG) { // если режим отладки
            collection = "_debug_" + collection;
        }

        return documentReference = firebaseFirestore.collection(collection).document(document); //формируем ссылку

    }

    public CollectionReference GenerateCollectionReference (String collection) {

        if (BuildConfig.DEBUG) { // если режим отладки
            collection = "_debug_" + collection;
        }

        return collectionReference = firebaseFirestore.collection(collection); //формируем ссылку
    }

/*    public boolean IsNotificationMessage() {
        return notificationMessage;
    }

    public void SetNotificationMessage(boolean notificationMessage) {
        this.notificationMessage = notificationMessage;
    }*/

    /**
     * Регистрирует канал уведомлений в системе
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = Data.channelID;
            String descriptionСhannel = "Description channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(Data.channelID, name, importance); //создаем канал
            notificationChannel.setDescription(descriptionСhannel); //добавляем описание канала


            //атрибуты для звука уведомлений////////////////////////////////////////////////////////////////////
/*             Uri uriDefaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // звук уведомления по умолчанию
           AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            notificationChannel.setSound(uriDefaultSound,att); //применяем звук уведомления по умолчанию

            //вибрация
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{500, 500, 500});*/



            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            //notificationManager.deleteNotificationChannel("appChannel");
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}
