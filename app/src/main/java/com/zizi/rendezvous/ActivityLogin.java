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

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener {

    Button btn_signin; // кнопка для входа...
    Button btn_reg; // кнопка для регистрации
    String currentUserID; // ID текущего пользователя
    FirebaseFirestore fbStore; // база данных
    FirebaseAuth mAuth; // объект для работы с авторизацией в FireBase
    FirebaseUser currentUser; //текущий пользователь
    DocumentReference documentReference; // для работы с документами в базе, нужно знать структуру базы FirebaseFirestore
    SharedPreferences saveParams; // хранилище в энергонезависимой памяти любых параметров
    SharedPreferences.Editor editorSaveParams; // объект для редакции энергонезависимого хранилища
    String email; // почта пользователя
    String password; // пароль пользователя
    String email_storage; // для запоминания почты для автовхода
    String password_storage; // для запоминания пароля для автовхода

    // для Material Design Components (MDC)
    TextInputLayout til_email;
    TextInputEditText til_email_et;
    TextInputLayout til_password; //элемент целиком
    TextInputEditText til_password_et; // это внутри til_password работать с текстом
    ProgressBar progressBar; // крутилка для показа, когда выполняется длительная операция




    @Override
    protected void onCreate(Bundle savedInstanceState) { //когда создается активити
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //инициализация - НАЧАЛО
        mAuth = FirebaseAuth.getInstance(); // инициализация объект для работы с авторизацией в FireBase
        fbStore = FirebaseFirestore.getInstance(); // инициализация объект для работы с базой
        saveParams = getSharedPreferences("saveParams", MODE_PRIVATE); // инициализация объекта работы энергонезавичимой памятью, первый параметр имя файла, второй режим доступа, только для этого приложения
        email_storage = saveParams.getString("email_storage", ""); // читаем их энергонезависимой памяти
        password_storage = saveParams.getString("password_storage", ""); // читаем их энергонезависимой памяти
        // находим все вьюхи на активити
        btn_signin = (Button) findViewById(R.id.btn_signin);
        btn_reg = (Button) findViewById(R.id.btn_reg);
        til_email = (TextInputLayout) findViewById(R.id.til_email);
        til_email_et = (TextInputEditText) findViewById(R.id.til_email_et);
        til_password = (TextInputLayout) findViewById(R.id.til_password);
        til_password_et = (TextInputEditText) findViewById(R.id.til_password_et);
        progressBar = findViewById(R.id.progressBar);
        //добавляем на кнопки слушателя
        btn_signin.setOnClickListener(this);
        btn_reg.setOnClickListener(this);
        //слушатели полей ввода
        til_email_et.addTextChangedListener(new TextWatcher() { // при изменении текста
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                til_email.setError(null);
                til_password.setError(null);  // убираем сообщение об ошибке
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        til_password_et.addTextChangedListener(new TextWatcher() { // добавим слушателя, если редактируем пароль
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Toast.makeText(Login.this, "Текст изменен", Toast.LENGTH_LONG).show();
                til_email.setError(null);
                til_password.setError(null);  // убираем сообщение об ошибке
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //инициализация - КОНЕЦ

        VisibilityViews(false); // делаем вьюхи видимыми

    }

    // показывает или скрывает вьюхи
    public void VisibilityViews (boolean visibility) {
        if (visibility == true){
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

    @Override
    public void onStart() {
        super.onStart();

        if (!email_storage.equals("") && !password_storage.equals("") ) { // если поля из памяти не пустые и ранее запоминались, то автовход
            email = email_storage; // то присваиваем почту из памяти
            password = password_storage; // то присваиваем пароль из памяти
            Signin();
        } else { // нужно показать поля и кнопки для входа и регистрации

            VisibilityViews(true); // делаем вьюхи видимыми
        }

    }


    @Override
    public void onClick(View v) { // при нажатии на кнопки, метод сгенерировалмя при добавлении к объявлению класса implements View.OnClickListener

        email = til_email_et.getText().toString(); // при клике формируем почту
        password = til_password_et.getText().toString(); // при клике на любую кнопку формируем пароль

        if (v.getId() == R.id.btn_signin) { // если нажали на кнопку входа
            //Toast.makeText(MainActivity.this, "Нажата кнопка входа", Toast.LENGTH_LONG).show();
            //Signin(til_email_et.getText().toString(), til_password_et.getText().toString());
            Signin();
        }
        if (v.getId() == R.id.btn_reg) { // если нажали на кнопку регистрации
            //Toast.makeText(MainActivity.this, "Нажата кнопка регистрации", Toast.LENGTH_LONG).show();
            //Registration(til_email_et.getText().toString(), til_password_et.getText().toString());
            Registration();
        }

    }

    public void Signin (){ // вход в систему
        //email = "9@9.com"; password = "111111";// заглушка для отладки
        if (!email.equals("") ){ // если поле почты не пустое, то  переходим к проверке пароля пытаемся войти
            if (!password.equals("")) { // если пароль не пустой, то пытаемся войти
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { // пробуем войти по email и паролю
                    @Override // как попытка войти завершится
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        VisibilityViews(false); // скрывам вьюхи и крутим прогрессбар бублик

                        if (task.isSuccessful()) {// если задача входы выполнится успешно
                            //Toast.makeText(Login.this, "Авторизация успешна", Toast.LENGTH_LONG).show();
                            SaveProfileAndEnter();
                        } else { // если вход не успешен

                            VisibilityViews(true); //показываем вьюхи

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
                            //Toast.makeText(Login.this, "Авторизация не успешна", Toast.LENGTH_LONG).show();
                            //Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else { // если пароль пустой, то просим заполнить
                til_password.setError(getString(R.string.til_password));
            }
        } else { // если поле почты пустое, то просим заполнить
            //Toast.makeText(Login.this, "Введите Email и пароль", Toast.LENGTH_LONG).show();
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

                        VisibilityViews(false); // скрывам вьюхи и крутим прогрессбар бублик

                        if (task.isSuccessful()) {// если задача регистрации выполнена успешно, то юзер автоматои и авторизируется
                            SaveProfileAndEnter();
                        } else { // если регистрация не успешна

                            VisibilityViews(true); //показываем вьюхи

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
                            //Toast.makeText(MainActivity.this, "Регистрация не успешна", Toast.LENGTH_LONG).show();
                            //Toast.makeText(Login.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                // если пароль не соответствует политике
                //til_password.setError("Не менее 8-ми символов без пробелов, обязательны: буква, цифра, заглавная буква.");
                til_password.setError(getString(R.string.til_password));
            }
        } else { // если поля почты и пароля пустые, то просим заполнить
            //Toast.makeText(Login.this, "Введите Email и пароль", Toast.LENGTH_LONG).show();
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
                //Toast.makeText(Login.this, "Регистрация успешна", Toast.LENGTH_LONG).show();
                if (email_storage.equals("") && password_storage.equals("") ) { // если пользователь и пароль из памяти пустые и ранее не запоминались, то запомним для автовхода
                    editorSaveParams = saveParams.edit(); // запоминаем в энергонезависимою память для входа
                    editorSaveParams.putString("email_storage", email.toString());
                    editorSaveParams.putString("password_storage", password.toString());
                    editorSaveParams.apply();
                }
                //Intent intent = new Intent(ActivityLogin.this, ActivityListMeetingsTb.class);
                //intent.putExtra("1", "1");
                //startActivity(intent);
                startActivity(new Intent(ActivityLogin.this, ActivityMeetings.class));// переходим на след активити ко встречам
                finish(); // убиваем активити
            }
        });
    }
}

