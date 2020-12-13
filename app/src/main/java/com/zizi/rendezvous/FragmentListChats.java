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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentListChats extends Fragment {

    private ClassGlobalApp classGlobalApp; //гобальный класс по работе с приложением
    private ActivityMeetings activityMeetings; // активити для переключения фрагментов из фрагментов
    private ArrayList<ModelChat> arrayListAllItems; // Имя для универсальности и использования на других экранах, коллекция со всеми ячейками recyclerView
    private Adapter adapter; // адаптер с данными для RecyclerView
    private FirebaseDatabase firebaseDatabase; // = FirebaseDatabase.getInstance(); // БД
    private DatabaseReference databaseReference;// = database.getReference("message"); //ссылка на данные
    private ModelChat modelChat; // модель сущности одного чата
    private LinearLayoutManager linearLayoutManager; // для вертикальной ориентации recyclerView
    private FragmentListMeetings fragmentListMeetings; // фрагмент со встречами
    private FragmentChat fragmentChat; // фрагмент с одним чатом
    private int countUnreads; // количество непрочитанных чатов текущего пользователя
    private BadgeDrawable badgeDrawable; // значек для изменения количества непрочитанных сообщений

    //вьюхи
    private RecyclerView recyclerView; // список с сообщениями
    private MaterialToolbar materialToolbar; // верхняя панелька
    private BottomNavigationView bottomNavigationView; // нижняя панелька


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_chats, container, false);
    }



    @Override //Вызывается, когда отработает метод активности onCreate(), а значит фрагмент может обратиться к компонентам активности
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //инициализация ////////////////////////////////////////////////////////////////////////////
        classGlobalApp = (ClassGlobalApp) getActivity().getApplicationContext();
        arrayListAllItems = new ArrayList<>();
        adapter = new Adapter(arrayListAllItems);
        modelChat = new ModelChat();
        fragmentListMeetings = new FragmentListMeetings();
        fragmentChat = new FragmentChat(); // фрагмент с одним чатом
        activityMeetings = (ActivityMeetings)getActivity();
        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext()); // для вертикальной ориентации recyclerView
        firebaseDatabase = FirebaseDatabase.getInstance(); // БД
        countUnreads = 0;
        //===========================================================================================



        //Ищем вьюхи /////////////////////////////////////////////////////////////////////////////////
        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        recyclerView = getActivity().findViewById(R.id.recycler_view);
        materialToolbar = getActivity().findViewById(R.id.materialToolbar);
        //===========================================================================================



        //recyclerView ///////////////////////////////////////////////////////////////////////////////
        recyclerView.setHasFixedSize(true); // для производительности recyclerView
        linearLayoutManager.setOrientation(linearLayoutManager.VERTICAL); //вертикальная ориентация
        recyclerView.setLayoutManager(linearLayoutManager); // применяем к recyclerView ориентацию
        recyclerView.setAdapter(adapter); // применяем адаптер
        UpdateChats(); // событийный метод по обновлению данных из БД, если будут меняться
        //==========================================================================================



        //materialToolbar /////////////////////////////////////////////////////////////////////////////
        materialToolbar.setTitle("Чаты"); // заголовок в панельке верхней
        materialToolbar.getMenu().findItem(R.id.request).setVisible(false); // скрываем пункт заявки на встречу
        materialToolbar.setNavigationIcon(R.drawable.ic_outline_menu_24); // делаем кнопку навигации менюшкой в верхней панельке

        // событие при клике на кнопку навигации на верхней панельке
        materialToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getActivity().onBackPressed();
            }
        });
        //==========================================================================================



        // bottomNavigationView ////////////////////////////////////////////////////////////////////
        bottomNavigationView.setSelectedItemId(R.id.chats); // делаем нужный пункт нижней панели по умолчанию

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.meetings: // при нажатии на кнопочку Встречи в нижней панели
                        activityMeetings.ChangeFragment(fragmentListMeetings, "fragmentListMeetings", false);
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
                classGlobalApp.Log("FragmentListChats", "onActivityCreated/onDataChange", "Количество непрочитанных изменилось", false);
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


    }



    @Override
    public void onStart() {
        super.onStart();

        if (!classGlobalApp.IsAuthorized()) { // если пользователь не авторизован
            startActivity(new Intent(getActivity().getApplicationContext(), ActivityLogin.class)); // отправляем к началу на авторизацию
            getActivity().finish(); // убиваем активити
        }

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();

    }


    /**
     * Метод вызывается при изменении данных в БД в списке чатов пользователя
     */
    private void UpdateChats(){
        databaseReference = classGlobalApp.GenerateDatabaseReference("chats/lists/" + classGlobalApp.GetCurrentUserUid() + "/"); //ссылка на данные

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override // при добавлении в БД чата
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                classGlobalApp.Log("FragmentListChats", "UpdateChats/onChildAdded", "В список добавились чаты", false);
                arrayListAllItems.add(snapshot.getValue(ModelChat.class)); // записываем инфу о чате в коллекцию со всеми сообщениями
                adapter.notifyDataSetChanged(); // обновление адаптера
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount()); // пролистать чат в самый конец

            }

            @Override // при изменении сообщения
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ModelChat modelChat = snapshot.getValue(ModelChat.class); // получаем экземпляр элемента списка чатов
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
     * @param modelChat модель чата из списка чатов
     * @return индекс чата в списке чатов arrayListAllItems
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

    /**
     * Класс адаптера для получения данных
     */
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


        /**
         * Подставляются/связываются значения в ячейки RecyclerView
         * @param holder Экземпляр класса одной ячейки ViewHolder
         * @param position позиция в RecyclerView
         */
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

        /**
         * Возвращает количество элементов для построения RecyclerView
         * @return количество элементов
         */
        @Override
        public int getItemCount() {
            return arrayListItems.size();
        }


        /**
         * Класс одной ячейки RecyclerView
         */
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

                        // добавляем аргументы для передачи в другой фрагмент
                        classGlobalApp.ClearBundle();
                        classGlobalApp.AddBundle("partnerID", arrayListItems.get(getAdapterPosition()).getUserID());
                        classGlobalApp.AddBundle("partnerToken", arrayListItems.get(getAdapterPosition()).getToken());
                        classGlobalApp.AddBundle("partnerName", arrayListItems.get(getAdapterPosition()).getName());
                        classGlobalApp.AddBundle("partnerAge", arrayListItems.get(getAdapterPosition()).getAge());

                        activityMeetings.ChangeFragment(fragmentChat, "fragmentChat", true); //переходим в личный чат

                    }
                });

            }
        }
    }
}