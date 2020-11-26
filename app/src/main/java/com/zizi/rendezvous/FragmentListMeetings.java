package com.zizi.rendezvous;

import android.content.Intent;
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
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FragmentListMeetings extends Fragment {


    //Объявление - НАЧАЛО ///////////////////////////////////////////////////////////////////////////
    private ClassGlobalApp classGlobalApp; //глобальный класс приложения, общий для всех компонентов
    private ActivityMeetings activityMeetings; // активити для переключения фрагментов из фрагментов
    private FirebaseFirestore firebaseFirestore; // база данных
    private RecyclerView recyclerView; // список со встречами
    private Query query; // запрос к БД
    private FirestoreRecyclerOptions<ModelSingleMeeting> options; // штука для построения контента для списка встречь из БД
    private FirestoreRecyclerAdapter adapter; //связывает вьюху и БД
    private FragmentListChats fragmentListChats; //фрагмент с чатами
    private FragmentChat fragmentChat; // фрагмент с одним чатом
    private Map<String, String> userInfo; // коллекция ключ-значение для информации о пользователях
    private ArrayList<ModelSingleMeeting> usersInfoAll; // информация по всем пользователям
    private BadgeDrawable badgeDrawable; // для изменения количества непрочитанных сообщений
    private int countUnreads;
    private FragmentDetailsMeeting fragmentDetailsMeeting; // фрагмент с подробностями встречи

    //вьюхи
    private BottomNavigationView bottomNavigationView; // нижняя панель с кнопками
    private MaterialToolbar topAppBar; // верхняя панелька

    public FragmentListMeetings() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_meetings, container, false);
    }



    @Override
    public void onStart() {
        super.onStart();

        if (!classGlobalApp.IsAuthorized()) { // если пользователь не авторизован
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


        //инициализация ////////////////////////////////////////////////////////////////////////////
        classGlobalApp = (ClassGlobalApp) getActivity().getApplicationContext();
        firebaseFirestore = FirebaseFirestore.getInstance(); //инициализация БД
        userInfo = new HashMap<>(); // коллекция ключ-значение для описания встречи
        usersInfoAll = new ArrayList<>(); // информация по всем пользователям
        fragmentListChats = new FragmentListChats(); //фрагмент с чатами
        fragmentChat = new FragmentChat(); // фрагмент с одним чатом
        fragmentDetailsMeeting = new FragmentDetailsMeeting();
        countUnreads = 0; // количество непрочитанных переменных


        //ищем нужные элементы
        recyclerView = getActivity().findViewById(R.id.rv_meeting); // список со встречами
        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        activityMeetings = (ActivityMeetings)getActivity();
        topAppBar = getActivity().findViewById(R.id.topAppBar);
        //инициализация - КОНЕЦ

        // bottomNavigationView ////////////////////////////////////////////////////////////////////
        bottomNavigationView.setSelectedItemId(R.id.meetings); // делаем нужный пункт нижней панели по умолчанию
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chats: // при нажатии на кнопочку Чаты в нижней панели
                        activityMeetings.ChangeFragment(fragmentListChats, "fragmentListChats", false);
                        return true;
                }
                return false;
            }
        });

        ClassStaticMethods.getCountUnreads(bottomNavigationView); // подписываемся на обновление количества непрочитанных чатов на нижней панельке
        //==========================================================================================



        // topAppBar ////////////////////////////////////////////////////////////////////////////////
        topAppBar.setTitle("Встречи");
        topAppBar.getMenu().findItem(R.id.request).setVisible(true); // показываем пункт заявки на встречу
        topAppBar.setNavigationIcon(R.drawable.ic_outline_menu_24); // делаем кнопку навигации стрелочкой назад в верхней панельке

        // событие при клике на кнопку навигации на верхней панельке
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getActivity().onBackPressed();
            }
        });
        //==========================================================================================



        // rv_meeting ////////////////////////////////////////////////////////////////////////////////
        query = firebaseFirestore.collection("meetings"); // запрос к БД
        options = new FirestoreRecyclerOptions.Builder<ModelSingleMeeting>().setQuery(query, ModelSingleMeeting.class).build(); // строим наполнение для списка встреч
        adapter = new FirestoreRecyclerAdapter<ModelSingleMeeting, FragmentListMeetings.SingleMeetingViewHolder>(options) { //показываем адаптеру класс одной встречи, вид встречи и подсовываем выборку из БД
            @NonNull
            @Override
            public FragmentListMeetings.SingleMeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // метод когда создается одна ячейка списка, тут нужно знать как работает RecyclerView
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting, parent, false); // надуваем/создаем из item_meeting.xml в java-объект ячейку, она пустая пока без данных
                return new FragmentListMeetings.SingleMeetingViewHolder(view); //находим все элементики на форме и возвращаем адаптеру

            }

            // метод при обновлении данных
            @Override
            protected void onBindViewHolder(@NonNull FragmentListMeetings.SingleMeetingViewHolder holder, int position, @NonNull ModelSingleMeeting model) {

                DocumentSnapshot snapshot =  getSnapshots().getSnapshot(position); // документ из БД
                //String id = snapshot.getId(); // имя докумета, которое видится в FireBase console
                //String str = currentUser.getUid();
                //String str2 = model.getUserID();
                if (snapshot.getId().equals(classGlobalApp.GetCurrentUserEmail())) { // если название документа в коллекции встреч такое же, как у текущего юзера, то скрываем эту встречу в списке

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

        recyclerView.setHasFixedSize(true); // говорят для производительности RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext())); // ориентацию RecyclerView делаем вертикальной, еще бывает мозаикой помоему или горизонтальной
        recyclerView.setAdapter(adapter); // ну и связываем вьюху с адаптером железно и навсегда
        //==========================================================================================

    }

    /**
     * Класс одной ячейки RecyclerView
     */
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


            //Нажати на кнопку ПОДРОБНОСТИ/////////////////////////////////////////////////////////////
            btn_details.setOnClickListener(new View.OnClickListener() { //если нажали на кнопку подробности
                @Override
                public void onClick(View v) {

                    //готовим аргументы для передачи в другой фрагмент
                    classGlobalApp.ClearBundle();
                    classGlobalApp.AddBundle("partnerEmail", usersInfoAll.get(getAdapterPosition()).getEmail());

                    activityMeetings.ChangeFragment(fragmentDetailsMeeting, "fragmentDetailsMeeting", true); //переходим в подробности встречи

                }
            });
            //========================================================================================



            //Нажатие на кнопку НАПИСАТЬ //////////////////////////////////////////////////////////////
            btn_write.setOnClickListener(new View.OnClickListener() { //если нажали на кнопку написать
                @Override
                public void onClick(View v) {

                    //готовим аргументы для передачи
                    classGlobalApp.ClearBundle();
                    classGlobalApp.AddBundle("partnerID", usersInfoAll.get(getAdapterPosition()).getUserID());
                    classGlobalApp.AddBundle("partnerToken", usersInfoAll.get(getAdapterPosition()).getToken());
                    classGlobalApp.AddBundle("partnerName", usersInfoAll.get(getAdapterPosition()).getName());
                    classGlobalApp.AddBundle("partnerAge", usersInfoAll.get(getAdapterPosition()).getAge());

                    activityMeetings.ChangeFragment(fragmentChat, "fragmentChat", true); //переходим в личный чат
                }
            });
            //=======================================================================================


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