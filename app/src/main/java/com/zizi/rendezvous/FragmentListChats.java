package com.zizi.rendezvous;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.yandex.metrica.impl.s.a.v;

public class FragmentListChats extends Fragment {

    //Объявление - НАЧАЛО ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    FirebaseAuth mAuth; // для работы с FireBase
    FirebaseUser currentUser; //текущий пользователь
    RecyclerView recyclerView; // список с сообщениями
    ArrayList<ModelChat> arrayListAllItems; // Имя для универсальности и использования на других экранах, коллекция со всеми ячейками recyclerView
    Adapter adapter; // адаптер с данными для RecyclerView
    FirebaseDatabase firebaseDatabase; // = FirebaseDatabase.getInstance(); // БД
    DatabaseReference databaseReference;// = database.getReference("message"); //ссылка на данные
    Bundle bundle; // для приема параметров в фрагмент
    ModelChat modelChat; // модель сущности одного чата
    LinearLayoutManager linearLayoutManager; // для вертикальной ориентации recyclerView
    BottomNavigationView bottomNavigationView; // нижняя панелька
    ActivityListMeetingsTb listMeetingsTbActivity; // активити для переключения фрагментов из фрагментов
    FragmentListMeetings fragmentListMeetings; // фрагмент со встречами
    Bundle bundleToChat; // параметры для передачи в фрагмент чата
    FragmentChat fragmentChat; // фрагмент с одним чатом
    MaterialToolbar topAppBar; // верхняя панелька
    //ArrayList<ModelSingleMeeting> infoAllItems; // информация по всем пользователям
    //Объявление - КОНЕЦ ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_chats, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) { // если пользователь пустой, не авторизирован
            startActivity(new Intent(getActivity().getApplicationContext(), ActivityLogin.class)); // отправляем к началу на авторизацию
            getActivity().finish(); // убиваем активити
        }

        //adapter.startListening(); // адаптер начинает слушать БД

    }

    @Override
    public void onStop() {
        super.onStop();
        //adapter.stopListening(); // адаптер прекращает слушать БД
    }


    @Override //Вызывается, когда отработает метод активности onCreate(), а значит фрагмент может обратиться к компонентам активности
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //инициализация - НАЧАЛО
        arrayListAllItems = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance(); // инициализация объекта для работы с авторизацией
        currentUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance(); // БД
        bundle = this.getArguments();
        modelChat = new ModelChat();
        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext()); // для вертикальной ориентации recyclerView
        adapter = new Adapter(arrayListAllItems);
        fragmentListMeetings = new FragmentListMeetings();
        bundleToChat = new Bundle(); // аргументы для передачи на другой фрагмент
        fragmentChat = new FragmentChat(); // фрагмент с одним чатом
        //infoAllItems = new ArrayList<>(); // информация по всем пользователям
        //инициализация - КОНЕЦ

        //Ищем нужные вьюхи - НАЧАЛО
        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        recyclerView = getActivity().findViewById(R.id.recycler_view);
        listMeetingsTbActivity = (ActivityListMeetingsTb)getActivity();
        topAppBar = getActivity().findViewById(R.id.topAppBar);
        //Ищем нужные вьюхи - КОНЕЦ

        bottomNavigationView.setSelectedItemId(R.id.chats); // делаем нужный пункт нижней панели по умолчанию


        topAppBar.setTitle("Чаты"); // заголовок в панельке верхней
        topAppBar.getMenu().findItem(R.id.request).setVisible(true); // показываем пункт заявки на встречу
        topAppBar.setNavigationIcon(R.drawable.ic_outline_menu_24); // делаем кнопку навигации менюшкой в верхней панельке

        //добавляем слушателей - НАЧАЛО
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.meetings: // при нажатии на кнопочку Встречи в нижней панели
                        listMeetingsTbActivity.ChangeFragment(fragmentListMeetings, "fragmentListMeetings", false);
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

        recyclerView.setHasFixedSize(true); // для производительности recyclerView
        linearLayoutManager.setOrientation(linearLayoutManager.VERTICAL); //вертикальная ориентация
        recyclerView.setLayoutManager(linearLayoutManager); // применяем к recyclerView ориентацию
        recyclerView.setAdapter(adapter); // применяем адаптер

        UpdateChats(); // событийный метод по обновлению данных из БД, если будут меняться

        ClassStaticMethods.getCountUnreads(bottomNavigationView); // подписываемся на обновление количества непрочитанных чатов на нижней панельке

    }

    /**
     * Метод вызывается при изменении данных в БД в списке чатов пользователя
     */
    private void UpdateChats(){
        databaseReference = firebaseDatabase.getReference("chats/lists/" + currentUser.getUid() + "/"); //ссылка на данные

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override // при добавлении в БД чата
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                arrayListAllItems.add(snapshot.getValue(ModelChat.class)); // записываем инфу о чате в коллекцию со всеми сообщениями
                adapter.notifyDataSetChanged(); // обновление адаптера
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount()); // пролистать чат в самый конец

            }

            @Override // при изменении сообщения
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ModelChat modelChat = snapshot.getValue(ModelChat.class);
                int index = GetItemIndex(modelChat); // ищем в коллекции чатов пользователя, тот чат в котором изменилась информация
                arrayListAllItems.set(index, modelChat); // изменяем в recyclerView по индексу
                adapter.notifyItemChanged(index); // обновляем адаптер
            }

            @Override // при удалении сообщения
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // вся фича это искать по индексу, делать по аналогии, как м в изменении
                //ModelMessage modelMessage = snapshot.getValue(ModelMessage.class);
                //int index = GetItemIndex(modelMessage);
                //arrayListAllMessages.remove(index);
                //adapter.notifyItemRemoved(index); // удаляем по индексу

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Возвращает индекс чата в котором изменились данные
     */
    private int GetItemIndex (ModelChat modelChat){

        int index = -1;

        for (int i = 0; i < arrayListAllItems.size(); i++) { // пробегаемся по всей коллекции чатов и сверяем ID пользователей

            if (arrayListAllItems.get(i).getUserID().equals(modelChat.getUserID())) { // если найден соответствующий чат
                index = i; // запоминаем индекс
                break;
            }
        }

        return index;

    }

    // класс адаптера для получения данных
    class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private ArrayList<ModelChat> arrayListItems; // коллекция со всеми ячейками RecyclerView

        public Adapter(ArrayList<ModelChat> arrayListItems) {
            this.arrayListItems = arrayListItems;
        }

        @NonNull // надуваем из представления ячейку RecyclerView
        @Override
        public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Adapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false));
        }



        @Override // тут подставляются значения в ячейки RecyclerView
        public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {

            holder.tv_name.setText(arrayListItems.get(position).getName()); // имя пользователя, с кем открыт чат
            holder.tv_age.setText(arrayListItems.get(position).getAge()); // возраст

            // если чат непрочитан, то подсветить это
            if (Integer.parseInt(arrayListItems.get(position).getUnReadMsg()) < 1) { // если нет непрочитанных сообщений
                holder.iv_unReadMsg.setVisibility(View.INVISIBLE); // то скрываем идикатор непрочитанных сообщений
            }

            //infoAllItems.add()

        }

        @Override
        public int getItemCount() { // возвращает количество сообщений для построения RecyclerView
            return arrayListItems.size();
        }

        // класс одной ячейки RecyclerView
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_name; // имя партнера
            TextView tv_age; // возраст партнера
            ImageView iv_unReadMsg; // непрочитанные сообщения
            MaterialButton btn_write; // кнопка написать



            public ViewHolder(View itemView) {
                super(itemView);

                tv_name = itemView.findViewById(R.id.tv_name);
                tv_age = itemView.findViewById(R.id.tv_age);
                iv_unReadMsg = itemView.findViewById(R.id.iv_unReadMsg);
                btn_write = itemView.findViewById(R.id.btn_write);

                btn_write.setOnClickListener(new View.OnClickListener() { //если нажали на кнопку написать
                    @Override
                    public void onClick(View v) {


                    bundleToChat.putString("partnerID", arrayListItems.get(getAdapterPosition()).getUserID()); // добавляем аргумент для передачи в другой фрагмент
                    bundleToChat.putString("partnerToken", arrayListItems.get(getAdapterPosition()).getToken()); // добавляем аргумент для передачи в другой фрагмент
                    bundleToChat.putString("partnerName", arrayListItems.get(getAdapterPosition()).getName()); // добавляем аргумент для передачи в другой фрагмент
                    bundleToChat.putString("partnerAge", arrayListItems.get(getAdapterPosition()).getAge()); // добавляем аргумент для передачи в другой фрагмент

                    fragmentChat.setArguments(bundleToChat); // добавить все аргументы
                    listMeetingsTbActivity.ChangeFragment(fragmentChat, "fragmentChat", true); //переходим в личный чат

                    }
                });

            }
        }
    }
}