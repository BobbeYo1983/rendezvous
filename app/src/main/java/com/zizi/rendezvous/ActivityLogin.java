package com.zizi.rendezvous;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ActivityLogin extends AppCompatActivity {

    private ClassGlobalApp classGlobalApp; // класс для сервисных функций приложения, описание внутри класса
    private FirebaseFirestore firebaseFirestore; // база данных
    private FirebaseAuth firebaseAuth; // объект для работы с авторизацией в Firebase
    private DocumentReference documentReference; // для работы с документами в базе, нужно знать структуру базы FirebaseFirestore
    private String email; // почта пользователя
    private String password; // пароль пользователя
    private FragmentManager manager; //менеджер фрагментов
    private ClassDialog classDialog; //класс для показа всплывающих окон


    //Вьюхи
    private ConstraintLayout mainLayout; // для показа снекбаров
    private TextInputLayout til_email; //поле для ввода
    private TextInputEditText til_email_et;
    private TextInputLayout til_password; //элемент целиком
    private TextInputEditText til_password_et; // это внутри til_password работать с текстом
    private Button btn_signin; // кнопка для входа...
    private Button btn_reg; // кнопка для регистрации
    private ProgressBar progressBar; // крутилка для показа, когда выполняется длительная операция



    @Override
    protected void onCreate(Bundle savedInstanceState) { //когда создается активити
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Инициализация ////////////////////////////////////////////////////////////////////////////
        classGlobalApp = (ClassGlobalApp) getApplicationContext();
        Log.d("!@#", "Release");
        classGlobalApp.Log("ActivityLogin", "onCreate", "Метод запущен.", false);
        firebaseAuth = FirebaseAuth.getInstance(); // инициализация объект для работы с авторизацией в FireBase
        firebaseFirestore = FirebaseFirestore.getInstance(); // инициализация объект для работы с базой
        manager = getSupportFragmentManager();
        classDialog = new ClassDialog(); // класс для показа всплывающих окон
        //==========================================================================================



        // Находим все вьюхи ///////////////////////////////////////////////////////////////////////
        mainLayout = findViewById(R.id.mainLayout);
        til_email = findViewById(R.id.til_email);
        til_email_et = findViewById(R.id.til_email_et);
        til_password = findViewById(R.id.til_password);
        til_password_et = findViewById(R.id.til_password_et);
        btn_signin = findViewById(R.id.btn_signin);
        btn_reg = findViewById(R.id.btn_reg);
        progressBar = findViewById(R.id.progressBar);
        //==========================================================================================



        //til_email_et /////////////////////////////////////////////////////////////////////////////
        til_email_et.addTextChangedListener(new TextWatcher() { // при изменении текста
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                til_email.setError(null);
                til_password.setError(null);  // убираем сообщение об ошибке
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        //==========================================================================================



        // til_password_et ///////////////////////////////////////////////////////////////////////////
        til_password_et.addTextChangedListener(new TextWatcher() { // добавим слушателя, если редактируем пароль
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                til_email.setError(null);
                til_password.setError(null);  // убираем сообщение об ошибке
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        //===========================================================================================



        // btn_signin ////////////////////////////////////////////////////////////////////////////////
        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = til_email_et.getText().toString(); // при клике формируем почту
                password = til_password_et.getText().toString(); // при клике на любую кнопку формируем пароль
                Signin();
            }
        });
        // ===========================================================================================




        // btn_reg /////////////////////////////////////////////////////////////////////////////////
        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = til_email_et.getText().toString(); // при клике формируем почту
                password = til_password_et.getText().toString(); // при клике на любую кнопку формируем пароль
                Registration();
            }
        });
        //===========================================================================================



        SetVisibilityViews(false); // делаем вьюхи невидимыми

    }



    @Override
    public void onStart() {
        super.onStart();

        // если раньше не входили в приложение, то есть логин и пароль не запоминались и пустые
        if (classGlobalApp.GetParam("email").equals("") && classGlobalApp.GetParam("password").equals("") ) {
            SetVisibilityViews(true); // делаем вьюхи видимыми и предлагаем заполнить
        } else { // если раньше заполнял пользователь логин и пароль, то автовход
            email = classGlobalApp.GetParam("email");
            password = classGlobalApp.GetParam("password");
            classGlobalApp.Log("ActivityLogin", "onStart", "Запуск автоматического входа в приложение.", false);
            Signin();
        }
    }


    /**
     * Показывает или скрывает вьюхи, когда автовход
     * @param isVisibility сделать видимым или не видимым
     */
    public void SetVisibilityViews (boolean isVisibility) {
        if (isVisibility == true){
            til_email.setVisibility(View.VISIBLE); // делаем вьюхи видимыми для регистрации
            til_password.setVisibility(View.VISIBLE);
            btn_signin.setVisibility(View.VISIBLE);
            btn_reg.setVisibility(View.VISIBLE);

            //подставляем почту и пароль в поля для входа, чтобы дальше пользователь пробовал войти без автовхода путем нажатия на кнопку входа
            til_email_et.setText(email);
            til_password_et.setText(password);

            progressBar.setVisibility(View.INVISIBLE);
        } else {
            til_email.setVisibility(View.INVISIBLE); // делаем вьюхи невидимыми
            til_password.setVisibility(View.INVISIBLE);
            btn_signin.setVisibility(View.INVISIBLE);
            btn_reg.setVisibility(View.INVISIBLE);

            progressBar.setVisibility(View.VISIBLE);
        }


    }


    public void Signin (){ // вход в систему
        classGlobalApp.Log("ActivityLogin", "onStart/Signin", "Метод запущен.", false);

        if (!email.equals("") ){ // если поле почты не пустое, то  переходим к проверке пароля пытаемся войти
            if (!password.equals("")) { // если пароль не пустой, то пытаемся войти
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { // пробуем войти по email и паролю
                    @Override // как попытка войти завершится
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        SetVisibilityViews(false); // скрывам вьюхи и крутим прогрессбар бублик

                        if (task.isSuccessful()) {// если задача входы выполнится успешно
                            SaveProfileAndEnter();
                        } else { // если вход не успешен

                            SetVisibilityViews(true); //показываем вьюхи

                            switch (task.getException().getMessage()) { // переводим ошибки
                                case "We have blocked all requests from this device due to unusual activity. Try again later. [ Too many unsuccessful login attempts. Please try again later. ]":

                                    //показываем всплывающее окно
                                    classDialog.setTitle("Ошибка входа");
                                    classDialog.setMessage("Много неуспешных попыток входа. Повторите вход позже.");
                                    classDialog.show(manager, "classDialog");
                                    //til_password.setError("Много неуспешных попыток входа. Повторите вход позже.");

                                    break;
                                case "The password is invalid or the user does not have a password.":
                                    til_password.setError("Неверный пароль.");
                                    break;
                                case "The email address is badly formatted.":
                                    til_email.setError("Неверный формат email.");
                                    break;
                                case "There is no user record corresponding to this identifier. The user may have been deleted.":
                                    til_email.setError("Нет пользователя с таким email. Возможно он был удален.");
                                    break;

                                case "An internal error has occurred. [ Unable to resolve host \"www.googleapis.com\":No address associated with hostname ]":
                                    //показываем всплывающее окно
                                    classDialog.setTitle("Ошибка входа");
                                    classDialog.setMessage("Нет подключения к интернет, проверьте, что интернет включен на вашем устройстве.");
                                    classDialog.show(manager, "classDialog");
                                    break;

                                case "A network error (such as timeout, interrupted connection or unreachable host) has occurred.":
                                    //показываем всплывающее окно
                                    classDialog.setTitle("Ошибка входа");
                                    classDialog.setMessage("Нет подключения к интернет, возможно интернет не доступен.");
                                    classDialog.show(manager, "classDialog");
                                    break;

                                default:
                                    //показываем пользователю
                                    classDialog.setTitle("Ошибка входа");
                                    classDialog.setMessage("Ошибка при входе пользователя: " + task.getException().getMessage());
                                    classDialog.show(manager, "classDialog");

                                    //добавляем в лог и в БД
                                    classGlobalApp.Log("ActivityLogin",
                                            "Signin/onComplete",
                                            "Ошибка при входе пользователя: " + task.getException().getMessage(),
                                            true
                                    );
                                    break;
                            }

                        }
                    }
                });
            } else { // если пароль пустой, то просим заполнить
                SetVisibilityViews(true);
                til_password.setError(getString(R.string.til_password));
            }
        } else { // если поле почты пустое, то просим заполнить
            SetVisibilityViews(true);
            til_email.setError("Введите email");
        }
    }
    public void Registration () { // регистрация
        classGlobalApp.Log("ActivityLogin", "onStart/Registration", "Метод запущен.", false);
        if (!email.equals("")){ // если поля почты и пароля не пустые, то пытаемся делать регистрацию
            if (password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$")) {//если пароль соответствует политике
                //^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\S+$).{8,}$
                //^                 # start-of-string
                //(?=.*[0-9])       # a digit must occur at least once
                //(?=.*[a-z])       # a lower case letter must occur at least once
                //(?=.*[A-Z])       # an upper case letter must occur at least once
                //(?=.*[@#$%^&+=])  # a special character must occur at least once, его исключил для упрощения
                //(?=\S+$)          # no whitespace allowed in the entire string
                //.{8,}             # anything, at least eight places though
                //$                 # end-of-string
                //(?=\S+$) обратите внимание в коде стоит двойной обратный слеш \\
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        SetVisibilityViews(false); // скрывам вьюхи и крутим прогрессбар бублик

                        if (task.isSuccessful()) {// если задача регистрации выполнена успешно, то юзер автоматои и авторизируется
                            SaveProfileAndEnter();
                        } else { // если регистрация не успешна

                            SetVisibilityViews(true); //показываем вьюхи

                            switch (task.getException().getMessage()) { // переводим ошибки
                                case "The email address is badly formatted.":
                                    til_email.setError("Неверный формат email.");
                                    break;

                                case "The email address is already in use by another account.":
                                    til_email.setError("email уже используется другим пользователем.");
                                    break;

                                default:

                                    //показываем пользователю
                                    classDialog.setTitle("Ошибка регистрации");
                                    classDialog.setMessage("Ошибка при регистрации пользователя: " + task.getException().getMessage());
                                    classDialog.show(manager, "classDialog");

                                    //добавляем в лог и в БД
                                    classGlobalApp.Log("ActivityLogin",
                                                        "Registration/onComplete",
                                                    "Ошибка при регистрации пользователя: "
                                                                + task.getException().getMessage(),
                                                                true
                                                    );

                                    break;
                            }

                        }
                    }
                });
            } else {
                // если пароль не соответствует политике
                til_password.setError(getString(R.string.til_password));
            }
        } else { // если поля почты и пароля пустые, то просим заполнить
            til_email.setError("Введите email.");
        }
    }

    /**
     * Сохраняет профайл пользователя в БД и входит в приложение
     */
    public void SaveProfileAndEnter (){
        classGlobalApp.Log("ActivityLogin", "SaveProfileAndEnter", "Метод запущен.", false);

        documentReference = firebaseFirestore.collection("users").document(classGlobalApp.GetCurrentUserEmail()); // подготавливаем коллекцию, внутри нее будут документы, внутри документов поля
        Map<String, Object> user = new HashMap<>(); // коллекция ключ-значение
        user.put("email", classGlobalApp.GetCurrentUserEmail());
        user.put("userID", classGlobalApp.GetCurrentUserUid());
        user.put("tokenDevice", classGlobalApp.GetTokenDevice()); //сохраняем токен приложения на сервер, чтобы токен всегда был свежий и по нему могли прислать push-уведомление
        //user.put("email1", classGlobalApp.GetCurrentUserEmail());
        //user.put("token", ServiceFirebaseCloudMessaging.GetToken(this)); //сохраняем токен приложения на сервер, чтобы токен всегда был свежий и по нему могли прислать push-уведомление

        //сохраняем профайл пользователя в БД
        documentReference.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) { //если задача сохранениеия выполнилась
                if (task.isSuccessful()) { //если сохранение успешно

                    // если раньше не входили в приложение, то есть логин и пароль не запоминались в память и пустые
                    //if (classGlobalApp.GetParam("email").equals("") && classGlobalApp.GetParam("password").equals("") ) {
                        classGlobalApp.PreparingToSave("email", email);
                        classGlobalApp.PreparingToSave("password", password);
                        classGlobalApp.SaveParams(); // сохраним на устройство для автовхода
                    //}



                    //переходим на другую активити, то есть фактически входим в приложение
                    startActivity(new Intent(ActivityLogin.this, ActivityMeetings.class));// переходим на след активити ко встречам
                    finish(); // убиваем активити

                } else { // если сохранение не успешно

                    classGlobalApp.Log("ActivityLogin", "SaveProfileAndEnter/onComplete", "Ошибка при сохранении профайла пользователя в БД: " + task.getException(), true);

                    //показываем всплывающее окно
                    classDialog.setTitle("Ошибка входа");
                    classDialog.setMessage("Ошибка при сохранении профайла пользователя в БД: " + task.getException());
                    classDialog.show(manager, "classDialog");

                    //делаем вьюхи видимыми
                    SetVisibilityViews(true);

                }
            }
        });


    }


}



