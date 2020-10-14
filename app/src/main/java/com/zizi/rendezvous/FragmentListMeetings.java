package com.zizi.rendezvous;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FragmentListMeetings extends Fragment {


    //Объявление - НАЧАЛО ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    FirebaseAuth mAuth; // для работы с FireBase
    FirebaseUser currentUser; //текущий пользователь
    FirebaseFirestore fB_fStore; // база данных
    RecyclerView rv_meeting; // список со встречами
    Query query; // запрос к БД
    FirestoreRecyclerOptions<ModelSingleMeeting> options; // штука для построения контента для списка встречь из БД
    FirestoreRecyclerAdapter adapter; //связывает вьюху и БД
    //ArrayList<String> usersIDs; // айдишники юзеров
    BottomNavigationView bottomNavigationView; // нижняя панель с кнопками
    ActivityListMeetingsTb listMeetingsTbActivity; // активити для переключения фрагментов из фрагментов
    //FragmentListMeetings fragmentListMeetings; //фрагмент со встречами
    FragmentListChats fragmentListChats; //фрагмент с чатами
    FragmentChat fragmentChat; // фрагмент с одним чатом
    Bundle bundleToChat; // параметры для передачи в фрагмент чата
    Map<String, String> userInfo; // коллекция ключ-значение для информации о пользователях
    //ArrayList<Map<String, String>> usersInfoAll; // информация по всем пользователям
    ArrayList<ModelSingleMeeting> usersInfoAll; // информация по всем пользователям
    MaterialToolbar topAppBar; // верхняя панелька
    BadgeDrawable badgeDrawable; // для изменения количества непрочитанных сообщений
    FirebaseDatabase firebaseDatabase; // = FirebaseDatabase.getInstance(); // БД
    DatabaseReference databaseReference;// = database.getReference("message"); //ссылка на данные
    int countUnreads;
    //Объявление - КОНЕЦ ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_meetings, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) { // если пользователь пустой, не авторизирован
            startActivity(new Intent(getActivity().getApplicationContext(), ActivityLogin.class)); // отправляем к началу на авторизацию
            getActivity().finish(); // убиваем активити
        }

        adapter.startListening(); // адаптер начинает слушать БД

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening(); // адаптер прекращает слушать БД
    }


    @Override //Вызывается, когда отработает метод активности onCreate(), а значит фрагмент может обратиться к компонентам активности
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //String str = intent.getStringExtra("fragmentName");
        //String str2 = getActivity().getIntent().getStringExtra("1");

        //инициализация - НАЧАЛО
        mAuth = FirebaseAuth.getInstance(); // инициализация объекта для работы с авторизацией
        fB_fStore = FirebaseFirestore.getInstance(); //инициализация БД
        //usersIDs = new ArrayList<>(); // айдишники юзеров, которые подали заявки на встречи
        bundleToChat = new Bundle(); // аргументы для передачи на другой фрагмент
        userInfo = new HashMap<>(); // коллекция ключ-значение для описания встречи
        usersInfoAll = new ArrayList<>(); // информация по всем пользователям
        firebaseDatabase = FirebaseDatabase.getInstance(); // БД
        fragmentListChats = new FragmentListChats(); //фрагмент с чатами
        fragmentChat = new FragmentChat(); // фрагмент с одним чатом
        currentUser = mAuth.getCurrentUser(); // получаем текущего пользователя
        countUnreads = 0; // количество непрочитанных переменных

        //ищем нужные элементы
        rv_meeting = getActivity().findViewById(R.id.rv_meeting); // список со встречами
        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        listMeetingsTbActivity = (ActivityListMeetingsTb)getActivity();
        topAppBar = getActivity().findViewById(R.id.topAppBar);

        //инициализация - КОНЕЦ

        bottomNavigationView.setSelectedItemId(R.id.meetings); // делаем нужный пункт нижней панели по умолчанию
        topAppBar.setTitle("Встречи");
        topAppBar.getMenu().findItem(R.id.request).setVisible(true); // показываем пункт заявки на встречу
        topAppBar.setNavigationIcon(R.drawable.ic_outline_menu_24); // делаем кнопку навигации стрелочкой назад в верхней панельке

        //добавляем слушателей - НАЧАЛО
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chats: // при нажатии на кнопочку Чаты в нижней панели
                        listMeetingsTbActivity.ChangeFragment(fragmentListChats, "fragmentListChats", false);
                        return true;
                }
                return false;
            }
        });

        // событие при клике на кнопку навигации на верхней панельке
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getActivity().onBackPressed();
            }
        });
        //добавляем слушателей - КОНЕЦ


        ArrayList<ModelSingleMeeting> a1 = new ArrayList<>();

        query = fB_fStore.collection("meetings"); // запрос к БД
        options = new FirestoreRecyclerOptions.Builder<ModelSingleMeeting>().setQuery(query, ModelSingleMeeting.class).build(); // строим наполнение для списка встреч
        adapter = new FirestoreRecyclerAdapter<ModelSingleMeeting, FragmentListMeetings.SingleMeetingViewHolder>(options) { //показываем адаптеру класс одной встречи, вид встречи и подсовываем выборку из БД
            @NonNull
            @Override
            public FragmentListMeetings.SingleMeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // метод когда создается одна ячейка списка, тут нужно знать как работает RecyclerView
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting, parent, false); // надуваем/создаем из item_meeting.xml в java-объект ячейку, она пустая пока без данных
                return new FragmentListMeetings.SingleMeetingViewHolder(view); //находим все элементики на форме и возвращаем адаптеру

            }

            @Override
            protected void onBindViewHolder(@NonNull FragmentListMeetings.SingleMeetingViewHolder holder, int position, @NonNull ModelSingleMeeting model) { // метод при обновлении данных

                DocumentSnapshot snapshot =  getSnapshots().getSnapshot(position); // документ из БД
                //String id = snapshot.getId(); // имя докумета, которое видится в FireBase console
                //String str = currentUser.getUid();
                //String str2 = model.getUserID();
                if (snapshot.getId().equals(currentUser.getEmail())) { // если название документа в коллекции встреч такое же, как у текущего юзера, то скрываем эту встречу в списке

                    RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams)holder.itemView.getLayoutParams(); // получаем параметры элемента
                    layoutParams.height = 0; // высота ячейки ноль, то есть скрываем ее
                    layoutParams.topMargin = 0; // отступ сверху
                    layoutParams.bottomMargin = 0; // отступ снизу
                    //param.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    //holder.itemView.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_name.setText(model.getName()); // связываем поле из item_meeting.xml и поле из Java-класса ModelSingleMeeting
                    holder.tv_age.setText(model.getAge());
                    holder.tv_comment.setText(model.getComment());
                    //String str = model.getUserId();
                    //usersIDs.add(model.getUserID()); // запоминаем добавленные айдишники юзеров, чтобы потом переходить по клику

                }

                usersInfoAll.add(model); // добавляем в список всех пользователей для передачи в информации в другие фрагменты


            }


        };

        rv_meeting.setHasFixedSize(true); // говорят для производительности RecyclerView
        rv_meeting.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext())); // ориентацию RecyclerView делаем вертикальной, еще бывает мозаикой помоему или горизонтальной
        rv_meeting.setAdapter(adapter); // ну и связываем вьюху с адаптером железно и навсегда

        ClassStaticMethods.getCountUnreads(bottomNavigationView); // подписываемся на обновление количества непрочитанных чатов на нижней панельке

    }



    class SingleMeetingViewHolder extends RecyclerView.ViewHolder { // класс одной ячейки

        // вьюхи которые в одной ячейке из item_meeting.xml
        TextView tv_name;
        TextView tv_age;
        TextView tv_comment;
        MaterialButton btn_details;
        MaterialButton btn_write;

        public SingleMeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            // находим и связываем все поля из из item_meeting.xml
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_age = itemView.findViewById(R.id.tv_age);
            tv_comment = itemView.findViewById(R.id.tv_comment);
            btn_details = itemView.findViewById(R.id.btn_details);
            btn_write = itemView.findViewById(R.id.btn_write);


            //устанавливаем слушателей
            btn_details.setOnClickListener(new View.OnClickListener() { //если нажали на кнопку подробности
                @Override
                public void onClick(View v) {
                    // перейти на фрагмент с подробностями
                    //listMeetingsTbActivity.ChangeFragment(fragmentListMeetings);// переходим к списку со встречами
                }
            });

            btn_write.setOnClickListener(new View.OnClickListener() { //если нажали на кнопку написать
                @Override
                public void onClick(View v) {

                    bundleToChat.putString("partnerID", usersInfoAll.get(getAdapterPosition()).getUserID()); // добавляем аргумент для передачи в другой фрагмент
                    bundleToChat.putString("partnerToken", usersInfoAll.get(getAdapterPosition()).getToken()); // добавляем аргумент для передачи в другой фрагмент
                    bundleToChat.putString("partnerName", usersInfoAll.get(getAdapterPosition()).getName()); // добавляем аргумент для передачи в другой фрагмент
                    bundleToChat.putString("partnerAge", usersInfoAll.get(getAdapterPosition()).getAge()); // добавляем аргумент для передачи в другой фрагмент

                    //bundleToChat.putString("currentUserID", currentUser.getUid()); // ID текущего пользователя
                    //bundleToChat.putString("currentUserToken", ServiceFirebaseCloudMessaging.GetToken(getActivity().getApplicationContext())); // Токен текущего пользователя

                    fragmentChat.setArguments(bundleToChat); // добавить все аргументы
                    listMeetingsTbActivity.ChangeFragment(fragmentChat, "fragmentChat", true); //переходим в личный чат
                }
            });



            itemView.setOnClickListener(new View.OnClickListener() { // делаем слушателя нажатия по элементу списка на всю карточку
                @Override
                public void onClick(View v) { // делаем слушателя нажатия по элементу списка
                    //Toast.makeText(ListMeeting.this, "Нажат элемент " + getAdapterPosition() + "/" + meetingsIDs.get(getAdapterPosition()), Toast.LENGTH_LONG).show();
                    //Intent intent = new Intent(ListMeeting.this, DetailsMeeting.class);
                    //intent.putExtra("meeting_id", meetingsIDs.get(getAdapterPosition())); // формируем данные для передачи другой активити
                    //startActivity(intent); // переходим к детализации встречи

                }
            });

        }
    }
}