package com.zizi.rendezvous;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityLogin extends AppCompatActivity {

    private ClassGlobalApp classGlobalApp; // класс для сервисных функций приложения, описание внутри класса
    private FirebaseFirestore fbStore; // база данных
    private FirebaseAuth mAuth; // объект для работы с авторизацией в Firebase
    private FirebaseUser currentUser; //текущий пользователь
    private DocumentReference documentReference; // для работы с документами в базе, нужно знать структуру базы FirebaseFirestore
    private String email; // почта пользователя
    private String password; // пароль пользователя

    //Вьюхи
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
        mAuth = FirebaseAuth.getInstance(); // инициализация объект для работы с авторизацией в FireBase
        fbStore = FirebaseFirestore.getInstance(); // инициализация объект для работы с базой
        //==========================================================================================



        // Находим все вьюхи ///////////////////////////////////////////////////////////////////////
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

        if (!email.equals("") ){ // если поле почты не пустое, то  переходим к проверке пароля пытаемся войти
            if (!password.equals("")) { // если пароль не пустой, то пытаемся войти
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { // пробуем войти по email и паролю
                    @Override // как попытка войти завершится
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        SetVisibilityViews(false); // скрывам вьюхи и крутим прогрессбар бублик

                        if (task.isSuccessful()) {// если задача входы выполнится успешно
                            //Toast.makeText(Login.this, "Авторизация успешна", Toast.LENGTH_LONG).show();
                            SaveProfileAndEnter();
                        } else { // если вход не успешен

                            SetVisibilityViews(true); //показываем вьюхи

                            switch (task.getException().getMessage()) { // переводим ошибки
                                case "We have blocked all requests from this device due to unusual activity. Try again later. [ Too many unsuccessful login attempts. Please try again later. ]":
                                    til_password.setError("Много неуспешных попыток входа. Повторите вход позже.");
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
                                default:
                                    til_password.setError(task.getException().getMessage());
                                    task.getException().printStackTrace();
                                    break;
                            }

                        }
                    }
                });
            } else { // если пароль пустой, то просим заполнить
                til_password.setError(getString(R.string.til_password));
            }
        } else { // если поле почты пустое, то просим заполнить
            til_email.setError("Введите email");
        }
    }
    public void Registration () { // регистрация
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
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
                                    til_password.setError(task.getException().getMessage());
                                    task.getException().printStackTrace();
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

    public void SaveProfileAndEnter (){
        currentUser = mAuth.getCurrentUser(); //получаем текущего пользователя
        documentReference = fbStore.collection("users").document(currentUser.getEmail().toString()); // подготавливаем коллекцию, внутри нее будут документы, внутри документов поля
        Map<String, Object> user = new HashMap<>(); // коллекция ключ-значение
        user.put("email", currentUser.getEmail());
        user.put("userID", currentUser.getUid());
        user.put("token", ServiceFirebaseCloudMessaging.GetToken(this)); //сохраняем токен приложения на сервер, чтобы токен всегда был свежий и по нему могли прислать push-уведомление

        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() { //
            @Override
            public void onSuccess(Void aVoid) {// если профайл пользователя записался успешно

                // если раньше не входили в приложение, то есть логин и пароль не запоминались и пустые
                if (classGlobalApp.GetParam("email").equals("") && classGlobalApp.GetParam("password").equals("") ) {
                    classGlobalApp.PreparingToSave("email", email.toString());
                    classGlobalApp.PreparingToSave("password", password.toString());
                    classGlobalApp.SaveParams(); // сохраним для автовхода
                }

                startActivity(new Intent(ActivityLogin.this, ActivityMeetings.class));// переходим на след активити ко встречам
                finish(); // убиваем активити
            }
        });
    }
}

