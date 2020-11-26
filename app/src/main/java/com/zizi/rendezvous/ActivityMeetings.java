package com.zizi.rendezvous;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActivityMeetings extends AppCompatActivity {

    //ИНИЦИАЛИЗАЦИЯ
    private ClassGlobalApp classGlobalApp; //класс для работы с функциями общими для всех активити, фрагментов, сервисов
    private MaterialToolbar materialToolbar; // верхняя панелька
    private FragmentManager fragmentManager; // для управления показом компонентов
    private FragmentTransaction fragmentTransaction; // для выполнения операций над фрагментами
    private Fragment fragmentListMeetings; // фрагмент со списком заявок
    private Fragment fragmentRequestMeeting; // фрагмент с заявкой
    private Fragment currentFragment; // текущий фрагмент

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);

        //инициализация /////////////////////////////////////////////////////////////////////////////
        classGlobalApp = (ClassGlobalApp) getApplicationContext();
        fragmentManager = getSupportFragmentManager();
        fragmentListMeetings = new FragmentListMeetings();
        fragmentRequestMeeting = new FragmentRequestMeeting();
        //requestNotFilled = saveParams.getString("requestNotFilled", "true"); // смотрим, подавалась ли ранее заявка или нет, если true, то не подавалась

        //ищем нужные элементы
        materialToolbar = (MaterialToolbar) findViewById(R.id.materialToolbar); // верхняя панель с кнопками
        //============================================================================================



        // materialToolbar ///////////////////////////////////////////////////////////////////////////
        materialToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {// слушатель нажатия на кнопки верхней панели
                if(item.getItemId() == R.id.request) // если нажата кнопка показать заявку
                {
                    ChangeFragment(fragmentRequestMeeting, "fragmentRequestMeeting", true); // грузим фрагмент с заявкой на встречу
                }
                return false;
            }
        });
        //==========================================================================================



        //грузим нужный фрагмент///////////////////////////////////////////////////////////////////////
        if (classGlobalApp.GetParam("requestIsActive").equals("trueTrue")) {// если заявка активна и заполнялась
            ChangeFragment(fragmentListMeetings, "fragmentListMeetings", false); // показываем встречи
        } else {
            ChangeFragment(fragmentRequestMeeting, "fragmentRequestMeeting", false); // показываем заявку
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

}