package com.zizi.rendezvous;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ActivityMeetings extends AppCompatActivity {

    //ИНИЦИАЛИЗАЦИЯ
    private ClassGlobalApp classGlobalApp; //класс для работы с функциями общими для всех активити, фрагментов, сервисов
    private MaterialToolbar materialToolbar; // верхняя панелька
    private FragmentManager fragmentManager; // для управления показом компонентов
    private FragmentTransaction fragmentTransaction; // для выполнения операций над фрагментами
    private Fragment fragmentListMeetings; // фрагмент со списком заявок
    private Fragment fragmentRequestMeeting; // фрагмент с заявкой
    private Fragment fragmentChat; // фрагмент с чатом
    private Fragment currentFragment; // текущий фрагмент
    private Fragment fragmentListChats; // текущий фрагмент
    private DocumentReference documentReference; // ссылка на документ
    private Map<String, Object> mapDocument; //Документ с информацией о встрече
    private ClassDialog classDialog; //класс для показа всплывающих окон

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);

        //инициализация /////////////////////////////////////////////////////////////////////////////
        classGlobalApp = (ClassGlobalApp) getApplicationContext();
        //classGlobalApp.Log("ActivityMeetings", "onCreate", "Method is run", false);
        fragmentManager = getSupportFragmentManager();
        fragmentListMeetings = new FragmentListMeetings();
        fragmentRequestMeeting = new FragmentRequestMeeting();
        fragmentChat = new FragmentChat();
        fragmentListChats = new FragmentListChats();
        mapDocument = new HashMap<String, Object>();
        classDialog = new ClassDialog(); // класс для показа всплывающих окон

        //ищем нужные элементы
        materialToolbar = (MaterialToolbar) findViewById(R.id.materialToolbar); // верхняя панель с кнопками
        //============================================================================================


        // materialToolbar ///////////////////////////////////////////////////////////////////////////
        materialToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {// слушатель нажатия на кнопки верхней панели
                if (item.getItemId() == R.id.request) // если нажата кнопка показать заявку
                {
                    ChangeFragment(fragmentRequestMeeting, "fragmentRequestMeeting", true); // грузим фрагмент с заявкой на встречу
                }
                return false;
            }
        });
        //==========================================================================================


        //грузим нужный фрагмент///////////////////////////////////////////////////////////////////////
        // если в приложении залогинились не в первый раз, то работаем по штатной схеме, иначе нужно проверить в БД активная ли заявка и если активна, то восстановить ее из БД на устройство
        if (classGlobalApp.GetParam("loginNotFirstTime").equals("trueTrue")) {
            classGlobalApp.Log("ActivityMeetings", "onCreate", "Ранее уже входили под настоящим логином", false);
            if (classGlobalApp.GetParam("requestIsActive").equals("trueTrue")) {// если заявка активна и заполнялась
                classGlobalApp.Log("ActivityMeetings", "onCreate", "Заявка активна", false);

                if (getIntent().getExtras() != null) { // если есть какие-то параметры, то примем их

                    Bundle bundle = getIntent().getExtras(); //получаем параметры переданные в активити
                    classGlobalApp.Log("ActivityMeetings", "onCreate", "Получен параметр: partnerID="  + bundle.getString("partnerID"),false);
                    classGlobalApp.Log("ActivityMeetings", "onCreate", "Получен параметр: partnerTokenDevice="  + bundle.getString("partnerTokenDevice"),false);
                    classGlobalApp.Log("ActivityMeetings", "onCreate", "Получен параметр: partnerName="  + bundle.getString("partnerName"),false);
                    classGlobalApp.Log("ActivityMeetings", "onCreate", "Получен параметр: partnerAge="  + bundle.getString("partnerAge"),false);

                    if (bundle.getString("fragmentForLoad").equals(Data.fragmentChat)) { // если нужно грузить фрагмент с чатом
                        classGlobalApp.Log("ActivityMeetings", "onCreate", "Параметр: fragmentForLoad="  + bundle.getString("fragmentForLoad") + ", нужно грузить фрагмент с чатом",false);

                        //извлекаем параметры и передаем их дальше фрагменту
                        classGlobalApp.ClearBundle();
                        classGlobalApp.AddBundle("partnerID", bundle.getString("partnerID"));
                        classGlobalApp.AddBundle("partnerTokenDevice", bundle.getString("partnerTokenDevice"));
                        classGlobalApp.AddBundle("partnerName", bundle.getString("partnerName"));
                        classGlobalApp.AddBundle("partnerAge", bundle.getString("partnerAge"));

                        ChangeFragment(fragmentChat, "fragmentChat", false); // переходим к чату


                    } else {

                        ChangeFragment(fragmentListMeetings, "fragmentListMeetings", false); // показываем встречи
                    }


                }else { // если нештатно не нужно грузить другой виджет, то грузим штатно список встреч

                    ChangeFragment(fragmentListMeetings, "fragmentListMeetings", false); // показываем встречи
                }

            } else { //заявка не активна
                classGlobalApp.Log("ActivityMeetings", "onCreate", "Заявка не активна, загружаем заполнение заявки", false);
                ChangeFragment(fragmentRequestMeeting, "fragmentRequestMeeting", false); // показываем заявку
            }
        } else { //если логинемся в первый раз, то нужно  залезть на сервак и оттуда получить заявку, если она активна
            classGlobalApp.Log("ActivityMeetings", "onCreate", "Ранее не входили под настоящим логином", false);
            //Читаем документ с заявкой на встречу текущего пользователя
            documentReference = classGlobalApp.GenerateDocumentReference("meetings", classGlobalApp.GetCurrentUserUid()); // формируем путь к документу
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() { // вешаем слушателя на задачу чтения документа из БД
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) { // как задача чтения выполнилась
                    if (task.isSuccessful()) { // если выполнилась успешно
                        classGlobalApp.Log("ActivityMeetings", "onCreate", "Прочитали документ с заявкой из БД", false);
                        DocumentSnapshot document = task.getResult(); // получаем документ
                        if (document.exists()) { // если документ такой есть, не null
                            classGlobalApp.Log("ActivityMeetings", "onCreate", "Документ с заявкой есть в БД", false);
                            mapDocument = document.getData(); // получаем данные из документа БД
                            classGlobalApp.Log("FragmentDetailsMeeting", "onStart/onComplete", "Fields count in document is: " + Integer.toString(mapDocument.size()), false);

                            if (mapDocument.size() > 0) { // если заявка на встречу есть в БД, то есть активна, то нужно восстановить ее
                                classGlobalApp.Log("ActivityMeetings", "onCreate", "Документ с заявкой содержить какие-то поля", false);
                                for (Map.Entry<String, Object> entry : mapDocument.entrySet()) { //пробегаемся по всему документу
                                    if (!entry.getKey().equals("deviceToken") || !entry.getKey().equals("userID")) { //если не токен, то готовим к сохранению
                                        //TODO: может переделать хранение мест без массива, обрабатывать неудобно, посмотреть в других местах возможно удобно ипользовать

                                        if (entry.getKey().equals("placeArray")) { // если попался массив со встречами
                                            ArrayList<?> arrayListPlaces = new ArrayList<>((Collection<?>)entry.getValue()); // получаем все места партнера

                                            classGlobalApp.PreparingToSave("placeStreet",     arrayListPlaces.get(0).toString());
                                            classGlobalApp.PreparingToSave("placePicnic",     arrayListPlaces.get(1).toString());
                                            classGlobalApp.PreparingToSave("placeCar",        arrayListPlaces.get(2).toString());
                                            classGlobalApp.PreparingToSave("placeSport",      arrayListPlaces.get(3).toString());
                                            classGlobalApp.PreparingToSave("placeFilm",       arrayListPlaces.get(4).toString());
                                            classGlobalApp.PreparingToSave("placeBilliards",  arrayListPlaces.get(5).toString());
                                            classGlobalApp.PreparingToSave("placeCafe",       arrayListPlaces.get(6).toString());
                                            classGlobalApp.PreparingToSave("placeDisco",      arrayListPlaces.get(7).toString());
                                            classGlobalApp.PreparingToSave("placeBath",       arrayListPlaces.get(8).toString());
                                            classGlobalApp.PreparingToSave("placeMyHome",     arrayListPlaces.get(9).toString());
                                            classGlobalApp.PreparingToSave("placeYouHome",    arrayListPlaces.get(10).toString());
                                            classGlobalApp.PreparingToSave("placeHotel",      arrayListPlaces.get(11).toString());
                                            classGlobalApp.PreparingToSave("placeOther",      arrayListPlaces.get(12).toString());

                                            //classGlobalApp.SaveParams();

                                        } else if (entry.getKey().equals("place")){ //если попался параметр place
                                            if (entry.getValue().equals(Data.anyPlace)) { // если выбрано любое место, то в память нужно сохранить такой папаметр
                                                classGlobalApp.PreparingToSave("placeAnyPlace", Data.anyPlace);
                                            }

                                        } else {

                                            classGlobalApp.PreparingToSave(entry.getKey(), entry.getValue().toString()); //готовим к сохранению в память телефона
                                        }
                                    }
                                }
                                classGlobalApp.SaveParams(); // сохраняем в память телефона все параметры заявки из БД

                                RefreshDeviceTokenInMeeting();


                            } else {//если заявки для текущего пользователя нет в БД, то есть не активна

                                //считаем заявку не активной
                                classGlobalApp.PreparingToSave("requestIsActive", ""); //готовим к сохранению
                                classGlobalApp.PreparingToSave("loginNotFirstTime", "trueTrue"); // отмечаем, что уже разок логинились
                                classGlobalApp.SaveParams(); // сохраняем в девайс

                                ChangeFragment(fragmentRequestMeeting, "fragmentRequestMeeting", false); // показываем заявку

                            }

                        } else { // если запрошенного документа не существует в БД

                            classGlobalApp.Log("FragmentDetailsMeeting", "onStart/onComplete", "Запрошенного документа нет в БД", true);

                            //считаем заявку не активной
                            classGlobalApp.PreparingToSave("requestIsActive", ""); //готовим к сохранению
                            classGlobalApp.PreparingToSave("loginNotFirstTime", "trueTrue"); // отмечаем, что уже разок логинились
                            classGlobalApp.SaveParams(); // сохраняем в девайс

                            ChangeFragment(fragmentRequestMeeting, "fragmentRequestMeeting", false); // показываем заявку
                        }

                    } else { // если ошибка чтения БД

                        classGlobalApp.Log("FragmentDetailsMeeting", "onStart/onComplete", "Ошибка чтения БД: " + task.getException(), true);

                        //показываем всплывающее окно
                        classDialog.setTitle("Ошибка чтения БД");
                        classDialog.setMessage("Ошибка при чтении заявки на встречу из БД, попробуйте войти позже. Подробности ошибки: " + task.getException());
                        classDialog.setPositiveButtonRedirect(Data.activityLogin);
                        classDialog.show(fragmentManager, "classDialog");

                    }
                }
            });

        }


        //===========================================================================================

    }



    @Override
    public void onStart() {
        super.onStart();

        if (!classGlobalApp.IsAuthorized()) { // если пользователь не авторизован
            startActivity(new Intent(getApplicationContext(), ActivityLogin.class)); // отправляем к началу на авторизацию
            finish(); // убиваем активити
        }


    }


    /**
     * Меняет фрагмент на экране (в активити)
     * @param FragmentNameNew новый фрагмент, который надо показать
     * @param Tag идентификатор фрагмента в менеджере фрагментов
     * @param toStack добавлять его в стек или нет, чтобы можно было переходить по кнопке назад
     */
    void ChangeFragment (Fragment FragmentNameNew, String Tag, boolean toStack){ // меняет отображение фрагмента

        currentFragment = getSupportFragmentManager().findFragmentByTag(Tag); //ищем фрагмент по тегу, тег мы ниже в функции добавляем при смене

        if ( !(currentFragment != null && currentFragment.isVisible()) ) { //если фрагмент этот уже открыт, то не выполнять операцию https://www.youtube.com/watch?v=2VLXdjpDD2g

            fragmentTransaction = fragmentManager.beginTransaction();           // начинаем транзакцию
            fragmentTransaction.replace(R.id.fragment_place, FragmentNameNew, Tag);  // обновляем фрагмент
            if (toStack) { // если нужно добавить для навигации в стек фрагментов
                fragmentTransaction.addToBackStack(null);                           // добавляем в конец стека фрагментов для навигации
            }
            fragmentTransaction.commit();                                       // применяем
            currentFragment = FragmentNameNew;                                  // запоминаем текущий фрагмент
        }

    }

    /**
     * Записывает новый tokenDevice в завку встречи в БД
     */
    private void RefreshDeviceTokenInMeeting() {

        classGlobalApp.Log("ActivityMeetings", "RefreshDeviceTokenInMeeting", "Обновляем tokenDevice в заявке в БД", false);

        documentReference = classGlobalApp.GenerateDocumentReference("meetings", classGlobalApp.GetCurrentUserUid()); // документ со встречей текущего пользователя
        documentReference.update("tokenDevice", classGlobalApp.GetTokenDevice()).addOnCompleteListener(new OnCompleteListener<Void>() { // записываем новый tokenDevice в БД в заявку встречи
            @Override
            public void onComplete(@NonNull Task<Void> task) { // если токен записан
                if(task.isSuccessful()){
                    //если восстановление заявки с сервера прошло успешно и токен записали на сервер успешно
                    classGlobalApp.PreparingToSave("loginNotFirstTime", "trueTrue"); // отмечаем, что уже разок логинились
                    classGlobalApp.PreparingToSave("requestIsActive", "trueTrue"); //отмечаем, что заявочка активна
                    classGlobalApp.SaveParams(); // сохраняем в девайс

                    ChangeFragment(fragmentListMeetings, "fragmentListMeetings", false); // показываем встречи
                }
                // если tokenDevice не записан в заявку встречи, то пользователь по старому токену не будет получать уведомления, то есть заявку можно считать неактивной
                //удалять ее из БД бессмысленно, что-то не так с БД, раз мы записать не смогли, думаю нужно направить пользователя к заполнению заявки, пусть по кнопке еще пытается подать заявку
                else{

                    classGlobalApp.Log("ActivityMeetings", "RefreshDeviceTokenInMeeting",
                            "Ошибка при записи tokenDevice в активную заявку на встречу. Пользователю не будут приходить уведомлени. Нужно заполнить заявку по новой.", true);

                    //считаем заявку не активной
                    classGlobalApp.PreparingToSave("requestIsActive", ""); //готовим к сохранению
                    classGlobalApp.PreparingToSave("loginNotFirstTime", "trueTrue"); // отмечаем, что уже разок логинились
                    classGlobalApp.SaveParams(); // сохраняем в девайс


                    ChangeFragment(fragmentRequestMeeting, "fragmentRequestMeeting", false); // показываем заявку



                }
            }
        });

    }

}