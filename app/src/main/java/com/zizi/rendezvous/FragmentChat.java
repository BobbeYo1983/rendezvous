package com.zizi.rendezvous;


import android.content.Intent;

import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class FragmentChat extends Fragment {

    private ClassGlobalApp classGlobalApp; // глобальный класс для всего приложения
    private ArrayList<ModelMessage> arrayListAllMessages; // коллекция с сообщениями
    private Adapter adapter; // адаптер с данными для RecyclerView
    private FirebaseDatabase firebaseDatabase; // = FirebaseDatabase.getInstance(); // БД
    private DatabaseReference databaseReference;// = database.getReference("message"); //ссылка на данные
    private TextInputEditText til_message_et; // поле с техтом ввода сообщения
    private Date dateNow; // для работы с датой
    private RelativeLayout.LayoutParams layoutParams; // параметры для изменениЯ геометрии поля с сообщением в чате и изменения позиции
    private Display display; // для разрешения экрана
    private Point size; // для разрешения экрана
    private int width; // ширина экрана
    private int height; // высота экрана
    private ModelMessage modelMessage; // одно сообщение
    private float dp; //плотность экрана на 1 dp
    private LinearLayoutManager linearLayoutManager; // для вертикальной ориентации recyclerView
    private SimpleDateFormat formatForDateNow; // для формата вывода даты
    private ModelChat partnerInfo; // информация о партнере
    private ModelChat currentUserInfo; // информация о текущем пользователе
    private List<String> usersIDs; // для формирования канала чата
    private boolean fragmentIsVisible;
    private String pushKeyDeleteBefore;
    private Query query;
    private boolean firstVisibleMessage; // флаг для определния первого видимого сообщения

    //вьюхи
    private FloatingActionButton floatingActionButton; //кнопка отправить сообщение
    private RecyclerView recyclerView; // список с сообщениями
    private MaterialToolbar materialToolbar; // верхняя панелька
    private TextView tv_unread;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override //Вызывается, когда отработает метод активности onCreate(), а значит фрагмент может обратиться к компонентам активности
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //инициализация ////////////////////////////////////////////////////////////////////////////
        classGlobalApp = (ClassGlobalApp) getActivity().getApplicationContext();
        arrayListAllMessages = new ArrayList<>();
        adapter = new Adapter(arrayListAllMessages);
        modelMessage = new ModelMessage();
        size = new Point();
        formatForDateNow = new SimpleDateFormat("HH:mm:ss"); // для формата вывода даты
        partnerInfo = new ModelChat(); // информация об партнере по чату, класс логически не так называется, но чтобы не плодить, использую этот
        currentUserInfo = new ModelChat(); // информация о текущем пользователе, класс логически не так называется, но чтобы не плодить, использую этот
        usersIDs = new ArrayList<String>();
        dp = getActivity().getResources().getDisplayMetrics().density; // получаем плотность экрана на 1 dp
        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext()); // для вертикальной ориентации recyclerView
        firebaseDatabase = FirebaseDatabase.getInstance(); // БД
        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // узнаем разрешение экрана
        display = getActivity().getWindowManager().getDefaultDisplay();
        display.getSize(size);
        width = size.x;
        height = size.y;

        //ищем нужные элементы
        floatingActionButton = getActivity().findViewById(R.id.fab);
        recyclerView = getActivity().findViewById(R.id.recyclerView);
        til_message_et = getActivity().findViewById(R.id.til_message_et);
        materialToolbar = getActivity().findViewById(R.id.materialToolbar);
        tv_unread = getActivity().findViewById(R.id.tv_unread);
        //=============================================================================================



        // информация о партнере чата ///////////////////////////////////////////////////////////////
        partnerInfo.setUserID(classGlobalApp.GetBundle("partnerUserID"));
        partnerInfo.setTokenDevice(classGlobalApp.GetBundle("partnerTokenDevice"));
        partnerInfo.setName(classGlobalApp.GetBundle("partnerName"));
        partnerInfo.setAge(classGlobalApp.GetBundle("partnerAge"));
        partnerInfo.setUnReadMsg("0"); // делаем по умолчанию ноль непрочитанных сообщений
        //===========================================================================================



        // заготовим информацию о текущем пользователе при загрузке фрагмента////////////////////////
        currentUserInfo.setUserID(classGlobalApp.GetCurrentUserUid());
        currentUserInfo.setTokenDevice(classGlobalApp.GetTokenDevice());
        currentUserInfo.setName(classGlobalApp.GetRequestMeeting().getName()); // подгружаем из памяти девайса
        currentUserInfo.setAge(classGlobalApp.GetRequestMeeting().getAge()); // подгружаем из памяти девайса
        //==========================================================================================



        // materialToolbar //////////////////////////////////////////////////////////////////////////
        materialToolbar.setTitle(partnerInfo.getName() + ", " + partnerInfo.getAge()); // заголовок чата
        materialToolbar.getMenu().findItem(R.id.request).setVisible(false); // скрываем пункт заявки на встречу
        materialToolbar.setNavigationIcon(R.drawable.ic_outline_arrow_back_24); // делаем кнопку навигации менюшкой в верхней панельке


        // событие при клике на кнопку навигации, на этом фрагменте она в виде стрелочки
        materialToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        //ЗНАЧЕК в нашей нижней панели materialToolbar, слушаем, если нам прислали сообщение и мы находимся в чате с партнером (фрагмент работает),
        //то тут же делаем, что нами чат прочитан для показа правильного количества непрочитанных на значке в нижней панели
        //databaseReference = firebaseDatabase.getReference("chats/unreads/" + classGlobalApp.GetCurrentUserUid() + "/"); // путь к непрочитанным нашим чатам
        databaseReference = classGlobalApp.GenerateDatabaseReference("chats/unreads/" + classGlobalApp.GetCurrentUserUid() + "/"); // путь к непрочитанным нашим чатам
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classGlobalApp.Log("FragmentChat", "onActivityCreated/onDataChange", "Можно сделать, что чат прочитан", false);
                // если ветка в непрочитанных с ID партнера существует и фрагмет активен/открыт, то ее нужно удалить, тем самым сказать, что чат прочитан

                if (fragmentIsVisible){
                    databaseReference = classGlobalApp.GenerateDatabaseReference("chats/unreads/" + classGlobalApp.GetCurrentUserUid() + "/" + partnerInfo.getUserID());
                    databaseReference.removeValue();
                    classGlobalApp.Log("FragmentChat", "onActivityCreated/onDataChange", "Этот чат прочитан", false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                classGlobalApp.Log("FragmentChat",
                        "onActivityCreated/onCancelled",
                        "Ошибка слушателя количества непрочитанных сообщений: " + error.getMessage(),
                        false
                );

            }
        });
        //===========================================================================================



        // recyclerView ////////////////////////////////////////////////////////////////////////////
        recyclerView.setHasFixedSize(true); // для производительности recyclerView
        linearLayoutManager.setOrientation(linearLayoutManager.VERTICAL); //вертикальная ориентация
        recyclerView.setLayoutManager(linearLayoutManager); // применяем к recyclerView ориентацию
        recyclerView.setAdapter(adapter); // применяем адаптер
        UpdateMessages(); // событийный метод по обновлению данных из БД, если будут меняться
        //==========================================================================================



        //КОНВЕРТ/ИНДИКАТОР В СПИСКЕ ЧАТОВ//////////////////////////////////////////////////////////
        //слушаем, если нам прислали сообщение, то тут же делаем, что нами чат прочитан для правильного показа в списке наших чатов
        databaseReference = classGlobalApp.GenerateDatabaseReference("chats/lists/" + classGlobalApp.GetCurrentUserUid() + "/" + partnerInfo.getUserID() + "/");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // если непрочитанных сообщений больше нуля и фрагмент чата активен/открыт/показан, то сбросить опять в ноль
                if (snapshot.getValue(ModelChat.class) != null && //если вообще есть чаты
                    Integer.parseInt(snapshot.getValue(ModelChat.class).getUnReadMsg()) > 0 &&
                    classGlobalApp.GetVisibleWidget().equals(Data.FRAGMENT_CHAT)){ // сейчас показывается пользователю фрагмент с чатом
                    //fragmentIsVisible){
                        databaseReference = classGlobalApp.GenerateDatabaseReference("chats/lists/" + classGlobalApp.GetCurrentUserUid() + "/" + partnerInfo.getUserID() + "/unReadMsg");
                        databaseReference.setValue("0"); // делаем отметочку, что прочитали чат, чтобы убрать конвертик напротив чата в списке чатов.
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                classGlobalApp.Log("FragmentChat",
                        "onActivityCreated/onCancelled",
                        "Ошибка слушателя чата (есть ли непрочитанные сообщения): " + error.getMessage(),
                        false
                );

            }
        });
        //===========================================================================================



        //ИНДИКАТОР В ЧАТЕ tv_unread слушаем, прочитан чат партнером или нет, чтобы показать//////////////////
        tv_unread.setVisibility(View.INVISIBLE);
        databaseReference = classGlobalApp.GenerateDatabaseReference("chats/unreads/" + partnerInfo.getUserID());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tv_unread.setVisibility(View.VISIBLE);
                // если ветка в непрочитанных с ID текущего пользователя в непрочитанных у партнера существует и фрагмент активен, значит непрочитан
                if (snapshot.child(classGlobalApp.GetCurrentUserUid()).exists()) {
                    tv_unread.setText("Не прочитано...");
                }else{
                    tv_unread.setText("Прочитано...");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                classGlobalApp.Log("FragmentChat",
                        "onActivityCreated/onCancelled",
                        "Ошибка слушателя индикатора (Прочитано/Не прочитано) в чате: " + error.getMessage(),
                        false
                );

            }
        });
        //============================================================================================



        // ОТПРАВКА СООБЩЕНИЯ ////////////////////////////////////////////////////////////////////////
        floatingActionButton.setOnClickListener(new View.OnClickListener() { // при нажатии на кнопку отправить сообщение в чате
            @Override
            public void onClick(View v) {

                if (!til_message_et.getText().toString().equals("")) // если сообщение не пустое
                {

                    // отправляем сообщение /////////////////////////////////////////////////////////
                    databaseReference = classGlobalApp.GenerateDatabaseReference("chats/chanels/" + CreateChatChanel(classGlobalApp.GetCurrentUserUid(), partnerInfo.getUserID()) ); //ссылка на данные, формируем канал чата
                    //modelMessage.userID = classGlobalApp.GetCurrentUserUid(); // формируем ID пользователя
                    //modelMessage.textMessage = til_message_et.getText().toString().trim(); // текст сообщения без пробелов в начале и конце строки
                    //modelMessage.dateTimeDevice = formatForDateNow.format(new Date()); // формируем даты на девайсе, не на сервере
                    modelMessage.setUserID(classGlobalApp.GetCurrentUserUid()); // формируем ID пользователя
                    modelMessage.setTextMessage(til_message_et.getText().toString().trim()); // текст сообщения без пробелов в начале и конце строки
                    modelMessage.setDateTimeDevice(formatForDateNow.format(new Date())); // формируем даты на девайсе, не на сервере
                    //databaseReference.push().setValue(modelMessage); // записываем сообщение в базу на сервак
                    String pushKey = databaseReference.push().getKey(); //запоминаем сгенерированный ключ, чтобы его потом записать вместе с сообщение, чтобы потом делать выборку по нему
                    modelMessage.setPushKey(pushKey);
                    databaseReference.child(pushKey).setValue(modelMessage); // записываем сообщение в базу на сервак
                    //===============================================================================



                    //Если пишем партнеру в первый раз, то в нашем списке чатов создастся чат с партнером
                    databaseReference = classGlobalApp.GenerateDatabaseReference("chats/lists/" + classGlobalApp.GetCurrentUserUid() + "/" + partnerInfo.getUserID() + "/");
                    databaseReference.setValue(partnerInfo); // записываем модель данных в БД
                    //=================================================================================



                    //ЗНАЧЕК в нижней панели и ИНДИКАТОР В ЧАТЕ, надо партнеру подсветить, что у него есть непрочитанный чат
                    databaseReference = classGlobalApp.GenerateDatabaseReference("chats/unreads/" + partnerInfo.getUserID() + "/"); // путь к непрочитанным чатам партнера
                    databaseReference.child(classGlobalApp.GetCurrentUserUid()).setValue("thisChatUnread"); // записываем, что от меня у партнера есть непрочитанный чат
                    //==============================================================================



                    //КОНВЕРТ/ИНДИКАТОР В СПИСКЕ ЧАТОВ ссылка на данные, формируем информацию о чатах партнера
                    databaseReference = classGlobalApp.GenerateDatabaseReference("chats/lists/" + partnerInfo.getUserID() + "/" + classGlobalApp.GetCurrentUserUid()  + "/"); // путь к листу чатов партнера
                    currentUserInfo.setUnReadMsg("1"); // записываем отметку, что есть непрочитанные сообщения
                    databaseReference.setValue(currentUserInfo); // записываем модель данных в БД
                    //===================================================================================



                    til_message_et.setText(""); //очищаем поле с текстом


                    //Отправляем уведомление в асинхронной задаче по ключу устройства, в конструктор ссылку на глобальный класс, чтобы писать логи в БД
                    new ClassNotificationMessage(classGlobalApp).execute(partnerInfo.getTokenDevice());
                    classGlobalApp.Log("FragmentChat", "floatingActionButton.setOnClickListener", "partnerTokenDevice = " + partnerInfo.getTokenDevice(), false);


                }

            }
        });
        //===========================================================================================


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
    public void onPause() {
        super.onPause();

        fragmentIsVisible = false; // делаем статус фрагмента не видимым
        classGlobalApp.SetVisibleWidget(""); // делаем статус фрагмента не видимым

    }



    @Override
    public void onResume() {
        super.onResume();

        fragmentIsVisible = true; // делаем статус фрагмента видимым
        classGlobalApp.SetVisibleWidget(Data.FRAGMENT_CHAT + partnerInfo.getUserID()); // делаем статус фрагмента видимым и закладываем в строку с кем открыт чат
    }

    /**
     * Обновление сообщений. Метод вызывается при изменении данных в БД
     */
    private void UpdateMessages(){

        firstVisibleMessage = true; // флаг для определния первого видимого сообщения

        //ссылка на канал чата с партнером
        databaseReference = classGlobalApp.GenerateDatabaseReference("chats/chanels/" + CreateChatChanel(classGlobalApp.GetCurrentUserUid(), partnerInfo.getUserID())); //ссылка на данные
        query = databaseReference.orderByChild("pushKey").limitToLast(30); //читаем последние 30 сообщений, все остальные будут удалены
        query.addChildEventListener(new ChildEventListener() {
            @Override // при добавлении в БД сообщения
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                arrayListAllMessages.add(snapshot.getValue(ModelMessage.class)); // записываем сообщение в коллекцию со всеми сообщениями
                if (firstVisibleMessage) { //если пришло первое сообщение для отображения
                    //то оно добавлено в буфер всех сообщений, запоминаем у него идентификатор, до которого потом будем удалять сообщения
                    pushKeyDeleteBefore = arrayListAllMessages.get(0).getPushKey();

                    DeleteMessagesInDB(pushKeyDeleteBefore); // удаляем лишние сообщения из БД
                    firstVisibleMessage = false; // сообщаем, что уже почистили, больше не надо
                }
                adapter.notifyDataSetChanged(); // обновление адаптера
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount()); // пролистать чат в самый конец

            }

            @Override // при изменении сообщения
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //ModelMessage modelMessage = snapshot.getValue(ModelMessage.class);
                //int index = GetItemIndex(modelMessage);
                //arrayListAllMessages.set(index, modelMessage); // изменяем в recyclerView по индексу
                //adapter.notifyItemChanged(index); // обновляем адаптер
            }

            @Override // при удалении сообщения
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // вся фича это искать по индексу
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
     * Удаляет сообщения из БД из канала чата ДО указанного pushKey, перед удалением упорядочивает по pushKey
     * @param pushKey уникальный идентификатор сообщения, генерируется в зависимости от времени, сравнивая их, можно определять кто был ранее сгенерирован
     */
    private void DeleteMessagesInDB (final String pushKey) {
        databaseReference = classGlobalApp.GenerateDatabaseReference("chats/chanels/" + CreateChatChanel(classGlobalApp.GetCurrentUserUid(), partnerInfo.getUserID()));
        //запрос, упорядочиваем по полю и выбираем до указанного значения
        Query queryDeleteMessages = databaseReference.orderByChild("pushKey").endAt(pushKey);

        queryDeleteMessages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child: snapshot.getChildren()) { //перебираем все сообщения

                    if (!child.getKey().equals(pushKey)) { // сообщение с указанным идентификатором не чистим
                        child.getRef().removeValue(); // удаляем сообщение из БД
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * Класс адаптера для получения данных
     */
    class Adapter extends RecyclerView.Adapter<Adapter.MessageViewHolder> {

        private ArrayList<ModelMessage> arrayListMessages; // коллекция с сообщениями

        public Adapter(ArrayList<ModelMessage> arrayListMessages) {
            this.arrayListMessages = arrayListMessages;
        }

        @NonNull // надуваем из представления ячейку RecyclerView
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
        }

        @Override // тут подставляются значения в ячейки RecyclerView
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

            // для редактирования параметров вьюхи
            RelativeLayout.LayoutParams layoutParams_til_textMessage = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams layoutParams_tv_timeStamp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // меняем размер контейнера сообщения на 75% от ширины экрана
            layoutParams_til_textMessage.width = Math.round((width/4)*3);


            // изменить цвет заливки сообщения
            //пока не делаю, буду делать когда полностью осваивать темную и светлую тему

            // подставляем нужный текст полей
            ModelMessage modelMessage = arrayListMessages.get(position);
            holder.til_textMessage_et.setText(modelMessage.getTextMessage());
            //переводим метку времени сервера в нужный формат даты

            //holder.tv_timeStamp.setText(formatForDateNow.format(new Date(Long.parseLong(modelMessage.timeStamp.toString()))));
            holder.tv_timeStamp.setText(formatForDateNow.format(new Date(modelMessage.getTimeStampLong() ) ) );

            if (modelMessage.getUserID().equals(classGlobalApp.GetCurrentUserUid())) { // если сообщение в чате мое, то показывать его справа

                layoutParams_til_textMessage.addRule(RelativeLayout.ALIGN_PARENT_END); //дальше нужно изменить положение слева или справа от экрана
                layoutParams_til_textMessage.setMarginEnd((int) (10 * dp)); // переводим все в dp и делаем отступ справа

                layoutParams_tv_timeStamp.addRule(RelativeLayout.ALIGN_END, R.id.til_textMessage); //дальше нужно изменить положение справа от соседнего элемента
                //layoutParams_tv_dateTime.setMarginEnd((int) (10 * dp)); // мерку времени выравниваем справа с отступом
                layoutParams_tv_timeStamp.setMargins(0, (int) (5 * dp), (int) (10 * dp), 0); // мерку времени выравниваем справа с отступом

            } else { // если сообщение партнера

                //выровнять все как нужно
                layoutParams_til_textMessage.setMarginStart((int) (10 * dp)); // переводим все в dp и делаем отступ слева

                layoutParams_tv_timeStamp.addRule(RelativeLayout.ALIGN_START, R.id.til_textMessage); //дальше нужно изменить положение слева от соседнего элемента
                layoutParams_tv_timeStamp.setMargins((int) (10 * dp), (int) (5 * dp), 0, 0); // мерку времени выравниваем справа с отступом


            }

            // применяем новые параметры к вьюхе
            holder.til_textMessage.setLayoutParams(layoutParams_til_textMessage);
            holder.tv_timeStamp.setLayoutParams(layoutParams_tv_timeStamp);
        }

        @Override
        public int getItemCount() { // возвращает количество сообщений для построения RecyclerView
            return arrayListMessages.size();
        }

        /**
         * Класс одной ячейки RecyclerView
         */
        class MessageViewHolder extends RecyclerView.ViewHolder {

            //TextView tv_textMessage; // Текст сообщения
            TextInputLayout til_textMessage; // Контейнер текста сообщения
            TextInputEditText til_textMessage_et; // Текст сообщения
            TextView tv_timeStamp;    //дата сообщения

            public MessageViewHolder(View itemView) {
                super(itemView);

                til_textMessage = itemView.findViewById(R.id.til_textMessage);
                til_textMessage_et = itemView.findViewById(R.id.til_textMessage_et);
                tv_timeStamp = itemView.findViewById(R.id.tv_timeStamp);

            }
        }
    }



    /**
     * Формируем айдишник канала чата
     * @param userID1 - имя первого пользователя
     * @param userID2 - имя второго пользователя
     * @return - ID канала чата указанных пользователей
     */
    private String CreateChatChanel (String userID1, String userID2)    {

        usersIDs.clear(); // чистим список
        usersIDs.add(userID1);
        usersIDs.add(userID2);

        Collections.sort(usersIDs);

        return usersIDs.get(0) + usersIDs.get(1);

    }


    /**
     * Ищем позицию/индекс конкретного сообщения во всей коллекции сообщений
     * @param modelMessage - модель с конкретным сообщением
     * @return - возвращаем номер в коллекции сообщений
     */
    private int GetItemIndex (ModelMessage modelMessage){

        int index = -1;

        for (int i = 0; i < arrayListAllMessages.size(); i++) {
            if (arrayListAllMessages.get(i).getUserID() == modelMessage.getUserID()) { // пробегаемся по всей коллекции пользователей
                index = i;
                break;
            }
        }

        return index;

    }

}
