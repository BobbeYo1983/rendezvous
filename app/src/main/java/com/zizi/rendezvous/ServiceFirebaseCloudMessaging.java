package com.zizi.rendezvous;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
//import android.support.v4.app.NotificationCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class ServiceFirebaseCloudMessaging extends FirebaseMessagingService
{
    ClassGlobalApp classGlobalApp; // класс для работы с общими для всех компонентов функциями приложения

    @Override
    public void onCreate() {
        super.onCreate();

        classGlobalApp = (ClassGlobalApp) getApplicationContext();
        classGlobalApp.Log("ServiceFirebaseCloudMessaging", "onCreate", "Метод запущен", false);
    }

    @Override // вызывается только, когда приложение запущено, не в фоновом режиме
    public void onMessageReceived(RemoteMessage remoteMessage) { // когда получили уведомление не в фоновом режиме
        super.onMessageReceived(remoteMessage);

        String stringTmp = Data.FRAGMENT_CHAT + remoteMessage.getData().get("userID"); //формируем строку для сравнения
        // если в настоящее время открыт фрагмент (видимый виджет) с чатом пользователя, который прислал уведомление, то не формировать уведомление
        if (!classGlobalApp.GetVisibleWidget().equals(stringTmp)) {


            classGlobalApp.Log("ServiceFirebaseCloudMessaging", "onMessageReceived", "Метод запущен", false);

            //создаем намерение, что хотим перейти на другую активити
            Intent intent = new Intent(this, ActivityMeetings.class); // новое намерение для перехода на активити
            intent.putExtra("fragmentForLoad", Data.FRAGMENT_CHAT);
            intent.putExtra("partnerID", remoteMessage.getData().get("userID")); //передаем идентификатор пользователя, чтобы открыть нужный чат
            intent.putExtra("partnerTokenDevice", remoteMessage.getData().get("tokenDevice"));
            intent.putExtra("partnerName", remoteMessage.getData().get("name"));
            intent.putExtra("partnerAge", remoteMessage.getData().get("age"));

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK //очищаем стек с задачей
                    | Intent.FLAG_ACTIVITY_NEW_TASK   //хотим создать активити в основной очищенной задаче
            );

            //отложенное намерение позволяет работать с другими внешними приложениями и службами
            // requestCode - номер отложенного намерения
            // PendingIntent.FLAG_UPDATE_CURRENT - будет всегда земенять данные
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Контент уведомления ///////////////////////////////////////////////////////////////////////////
            //Uri uriDefaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // звук уведомления по умолчанию
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Data.channelID); //канал уведомлений ранее регистрировали в classGlobalApp.CreateNotificationChannel()
            builder.setSmallIcon(R.drawable.ic_outline_wc_24); // устанавливаем маленькую иконку
            builder.setColor(ContextCompat.getColor(this, R.color.colorPrimary)); // цвет иконки в уведомлении, но не в строке уведомлений
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT); // приоритет
            builder.setContentTitle(remoteMessage.getData().get("title")); // заголовок
            builder.setContentText(remoteMessage.getData().get("body")); //текст уведомления
            //builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI); //звук по умолчанию для уведомлений
            //builder.setVibrate(new long[] {1000, 1000, 1000, 1000, 1000}); // вибрация
            builder.setAutoCancel(true); // при нажатии удаляется
            builder.setContentIntent(pendingIntent); // связываем, что нужно сделать при нажатии

            //Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            //v.vibrate(500);
            //=============================================================================================

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            // notificationId is a unique int for each notification that you must define
            //Remember to save the notification ID that you pass to NotificationManagerCompat.notify() because you'll need it later if you want to update or remove the notification.
            notificationManager.notify(0, builder.build());
        }

    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) { // метод вызывается когда генерируется новый токен

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);

        //сохраняем в память телефона
        classGlobalApp.SetTokenDevice(token);

    }

}

