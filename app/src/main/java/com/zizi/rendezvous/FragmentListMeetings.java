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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
    private DatabaseReference databaseReference;// ссылка на данные в БД
    private CollectionReference collectionReference; // для работы с коллекциями в БД, нужно знать структуру/информационную модель базы FirebaseFirestore
    private FirebaseDatabase firebaseDatabase; // БД RealTime DataBase
    private ArrayList<String> arrayListPlaces; // список с местами встреч партнера
    ////private ArrayList<?> arrayListPlaces; //сюда вычитывать массив с местами будем

    //вьюхи
    private BottomNavigationView bottomNavigationView; // нижняя панель с кнопками
    private MaterialToolbar materialToolbar; // верхняя панелька

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
        classGlobalApp.Log(getClass().getSimpleName(), "onActivityCreated", "Метод запущен", false);
        firebaseFirestore = FirebaseFirestore.getInstance(); //инициализация БД
        firebaseDatabase = FirebaseDatabase.getInstance(); // БД
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
        materialToolbar = getActivity().findViewById(R.id.materialToolbar);
        //==========================================================================================



        // bottomNavigationView ////////////////////////////////////////////////////////////////////
        bottomNavigationView.setSelectedItemId(R.id.meetings); // делаем нужный пункт нижней панели по умолчанию
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chats: // при нажатии на кнопочку Чаты в нижней панели
                        activityMeetings.ChangeFragment(fragmentListChats, false);
                        return true;
                }
                return false;
            }
        });

        // ЗНАЧЕК с количеством непрочитанных сообщений текущего пользователя
        databaseReference = classGlobalApp.GenerateDatabaseReference("chats/unreads/" + classGlobalApp.GetCurrentUserUid() + "/");
        databaseReference.addValueEventListener(new ValueEventListener() { // добавляем слушателя при изменении значения
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classGlobalApp.Log("FragmentListMeetings", "onActivityCreated/onDataChange", "Количество непрочитанных изменилось", false);
                countUnreads = (int) snapshot.getChildrenCount(); // получаем количество непрочитанных чатов
                if (countUnreads > 0) { // если есть непрочитанные чаты
                    badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.chats); // создаем значек около вкладки Чаты на нижней панели, пока без номера
                    badgeDrawable.setNumber(countUnreads); // показываем количество непрочитанных чатов
                } else {
                    bottomNavigationView.removeBadge(R.id.chats); // удаляем значек с панели
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // тут надо обработать ошибочку и залогировать
            }
        });
        //============================================================================================


        // materialToolbar ////////////////////////////////////////////////////////////////////////////////
        materialToolbar.setTitle("Встречи");
        materialToolbar.getMenu().findItem(R.id.request).setVisible(true); // показываем пункт заявки на встречу
        materialToolbar.setNavigationIcon(R.drawable.ic_outline_menu_24); // делаем кнопку навигации менюшкой

        // событие при клике на кнопку навигации на верхней панельке
        materialToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ничего не делаем
                //getActivity().onBackPressed();
            }
        });
        //==========================================================================================



        // rv_meeting ////////////////////////////////////////////////////////////////////////////////
        //подгружаем заявку из памяти телефона, чтобы делать выборку с актуальными данными
        classGlobalApp.LoadRequestMeetingFromMemory();

        // запрос к БД c фильтрами
        collectionReference = classGlobalApp.GenerateCollectionReference("meetings");
        query = collectionReference// коллекция meetings
                .whereEqualTo("gender", classGlobalApp.GetRequestMeeting().getGender_partner()) //совпадает пол в запросе и пол партнера
                .whereEqualTo("region", classGlobalApp.GetRequestMeeting().getRegion()) //совпадает регион в запросе и в заявке партнера
                .whereEqualTo("town", classGlobalApp.GetRequestMeeting().getTown()) //совпадает город в запросе и в заявке партнера
                ;

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

                DocumentSnapshot snapshot =  getSnapshots().getSnapshot(position); // документ из БД, один из списка

                int age = Integer.parseInt(model.getAge()); //получаем возраст
                int age_min = Integer.parseInt(classGlobalApp.GetRequestMeeting().getAge_min()); //минимальный возраст из заявки текущего пользователя
                int age_max = Integer.parseInt(classGlobalApp.GetRequestMeeting().getAge_max()); //максимальный возраст из заявки текущего пользователя

                //arrayListPlaces = (ArrayList<String>) snapshot.get("placeArray"); // получаем все места партнера
                ////arrayListPlaces = new ArrayList<>((Collection<?>)snapshot.get("placeArray")); // получаем все места партнера
                arrayListPlaces = model.getPlaceArray();

                //отфильтровываем встречи по фильтру текущего пользователя и свою заявку тоже скрываем
                if (snapshot.getId().equals(classGlobalApp.GetCurrentUserEmail()) || // если название документа в коллекции встреч такое же, как у текущего юзера, то скрываем эту встречу в списке
                        !(age >= age_min && age <= age_max) || //если возраст не попадает в диапазон запроса
                        !IsPlace(arrayListPlaces) || //если нет общих мест для встречи
                        !IsTime(model.getTime()) // если не совпадает время встречи
                ) {

                    RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams)holder.itemView.getLayoutParams(); // получаем параметры элемента
                    layoutParams.height = 0; // высота ячейки ноль, то есть скрываем ее
                    layoutParams.topMargin = 0; // отступ сверху
                    layoutParams.bottomMargin = 0; // отступ снизу

                } else { // связываем данные и представление

                        holder.tv_name.setText(model.getName()); // связываем поле из item_meeting.xml и поле из Java-класса ModelSingleMeeting
                        holder.tv_age.setText(model.getAge());
                        holder.tv_comment.setText(model.getComment());

                }

                usersInfoAll.add(model); // добавляем в список всех пользователей для передачи информации в другие фрагменты


            }


        };

        recyclerView.setHasFixedSize(true); // говорят для производительности RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext())); // ориентацию RecyclerView делаем вертикальной, еще бывает мозаикой помоему или горизонтальной
        recyclerView.setAdapter(adapter); // ну и связываем вьюху с адаптером железно и навсегда
        //==========================================================================================



    }



    /**
     * Проверяет есть ли совпадения в местах текущего пользователя с местами других пользователей, то есть есть ли общие места для встречи.
     * @param arrayListPlaces список мест для встречи одного из пользователей
     * @return есть или нет совпадения
     */
    public boolean IsPlace (ArrayList<String> arrayListPlaces) {

        for (String place : arrayListPlaces) {  // перебираем места партнера
            classGlobalApp.Log(getClass().getSimpleName(), "IsPlace", "place = " + place, false);
            for (String placeCurrentUser : classGlobalApp.GetRequestMeeting().getPlaceArray()) { //перебираем места текущего пользователя
                classGlobalApp.Log(getClass().getSimpleName(), "IsPlace", "placeCurrentUser = " + placeCurrentUser, false);
                if (!place.isEmpty() && place.equals(placeCurrentUser)) { return true; } // как находим любое совпадение и строка не пустая

            }
        }

        return false;

    }



    /**
     * Проверяет есть ли совпадения по времени встречи текущего пользователя и других пользователей
     * @param time выбранное значение времени партнера
     * @return есть или нет совпадения
     */
    public boolean IsTime (String time) {

        if (time.equals(Data.anyTime)){ // если у текущего пользователя выбрано любое время
            return true;
        } else if (time.equals(classGlobalApp.GetRequestMeeting().getTime())){ //если время не любое, и есть совпадение выбранного значения текущего пользователя с партнером
                return true;
        }

        return false;
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
            btn_details.setOnClickListener(new View.OnClickListener() { //если нажали на кнопку Инфо (Подробности)
                @Override
                public void onClick(View v) {

                    //готовим аргументы для передачи в другой фрагмент
                    classGlobalApp.ClearBundle();
                    classGlobalApp.AddBundle("partnerUserID", usersInfoAll.get(getAdapterPosition()).getUserID());

                    activityMeetings.ChangeFragment(fragmentDetailsMeeting, true); //переходим в подробности встречи

                }
            });
            //========================================================================================



            //Нажатие на кнопку НАПИСАТЬ //////////////////////////////////////////////////////////////
            btn_write.setOnClickListener(new View.OnClickListener() { //если нажали на кнопку написать
                @Override
                public void onClick(View v) {

                    //готовим аргументы для передачи
                    classGlobalApp.ClearBundle();
                    classGlobalApp.AddBundle("partnerUserID", usersInfoAll.get(getAdapterPosition()).getUserID());
                    classGlobalApp.AddBundle("partnerTokenDevice", usersInfoAll.get(getAdapterPosition()).getTokenDevice());
                    classGlobalApp.AddBundle("partnerName", usersInfoAll.get(getAdapterPosition()).getName());
                    classGlobalApp.AddBundle("partnerAge", usersInfoAll.get(getAdapterPosition()).getAge());

                    activityMeetings.ChangeFragment(fragmentChat, true); //переходим в личный чат
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