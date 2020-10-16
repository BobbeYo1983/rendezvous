package com.zizi.rendezvous;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class ActivityListMeetingsTb extends AppCompatActivity {

    FirebaseAuth mAuth; // для работы с авторизацией FireBase
    MaterialToolbar topAppBar; // верхняя панелька
    FragmentManager fragmentManager; // для управления показом компонентов
    FragmentTransaction fragmentTransaction; // для выполнения операций над фрагментами
    Fragment fragmentListMeetings; // фрагмент со списком заявок
    Fragment fragmentRequestMeeting; // фрагмент с заявкой
    Fragment currentFragment; // текущий фрагмент
    DatabaseReference databaseReference; //ссылка на данные
    FirebaseDatabase firebaseDatabase; // = FirebaseDatabase.getInstance(); // БД

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_meetings_tb);

        //инициализация - НАЧАЛО
        mAuth = FirebaseAuth.getInstance(); // инициализация объекта для работы с авторизацией
        fragmentListMeetings = new FragmentListMeetings();
        fragmentRequestMeeting = new FragmentRequestMeeting();
        fragmentManager = getSupportFragmentManager();
        firebaseDatabase = FirebaseDatabase.getInstance(); // БД

        //ищем нужные элементы
        topAppBar = (MaterialToolbar) findViewById(R.id.topAppBar); // верхняя панель с кнопками

        //добавляем слушателей
        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {// слушатель нажатия на кнопки верхней панели
                if(item.getItemId() == R.id.request) // если нажата кнопка показать заявку
                {
                    //if (fragmentManager.findFragmentByTag(FragmentListMeetings.TAG) == null) { // если фрагмент с заявкой не показан, то показываем иначе бездействуем, чтобы в стек навигации не добавлялся
                        ChangeFragment(fragmentRequestMeeting, "fragmentRequestMeeting", true); // грузим фрагмент с заявкой на встречу
                    //}

                }
                return false;
            }
        });
        //инициализация - КОНЕЦ


        // при загрузке активити показываем фрагмент со встречами

        //Bundle bundle = getIntent().getExtras();
        //if (bundle != null) {
            //String str = bundle.get("fragmentName").toString();
        //}

        //Intent intent = getIntent();

        //String str = getIntent().getStringExtra("fragmentName");
        //String str2 = getIntent().getStringExtra("1");
        //Bundle b = savedInstanceState;
        //Bundle bundle = getIntent().getExtras();
        //if (bundle != null) {
            //String str1 = bundle.getString("fragmentName");;
            //String str2 = "";
        //}
        //intent.getEx

        ChangeFragment(fragmentListMeetings, "fragmentListMeetings", false);



    }

/*    @Override //метод когда принимается новое намерение
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);


        //Bundle bundle = getIntent().getExtras();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            tmp_str = bundle.getString("fragmentName");

        }

        String str20 = "sdv";
        str20 = "dfvdv";


    }*/

    void ChangeFragment (Fragment FragmentNameNew, String Tag, boolean toStack){ // меняет отображение фрагмента

        currentFragment = getSupportFragmentManager().findFragmentByTag(Tag);

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


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) { // если пользователь пустой, не авторизирован
            startActivity(new Intent(ActivityListMeetingsTb.this, ActivityLogin.class)); // отправляем к началу на авторизацию
            finish(); // убиваем активити
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //adapter.stopListening(); // адаптер прекращает слушать БД
    }

/*    // скрываем нужный пункт меню
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.request).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }*/
}