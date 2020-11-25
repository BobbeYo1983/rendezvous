package com.zizi.rendezvous;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.NotificationCompat;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class ServiceFirebaseCloudMessaging extends FirebaseMessagingService
{
    ClassGlobalApp classGlobalApp; // класс для работы с общими для всех компонентов функциями приложения

    ServiceFirebaseCloudMessaging(){
        classGlobalApp = (ClassGlobalApp) getApplicationContext();
    }

    @Override // при получении сообщения
    public void onMessageReceived(RemoteMessage remoteMessage) { // когда получили уведомление
        super.onMessageReceived(remoteMessage);

        Intent intent = new Intent(getApplicationContext(), ActivityMeetings.class);
        //intent.putExtra("fragmentName", "fragmentListChats");
        //intent.putExtra("1", "1");

        Bundle bundle = new Bundle();
        bundle.putString("fragmentName", "fragmentListChats");
        intent.putExtras(bundle);


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ActivityMeetings.class); // добавляем в стек с активити, родительскую активити. В манифесте у дочерних активити можно прописать типа android:parentActivityName=".MainActivity", тогда  откроется дочерне активити.

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent); // добавляем в стек интент

        // Make this unique ID to make sure there is not generated just a brand new intent with new extra values:
        //int requestID = (int) System.currentTimeMillis();
        //PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // звук уведомления по умолчанию

        //строим уведомление
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "RendChat")
        //NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_outline_message_24) // иконка в самом верху в панели уведомлений
                .setContentTitle(remoteMessage.getNotification().getTitle()) // Заголовок уведомления
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_outline_message_24)) // большая иконка в уведомлении
                .setContentText(remoteMessage.getNotification().getBody()) // текст уведомления
                .setAutoCancel(true) // когда кликаем на уведомление, оно пропадает
                //.setColor(0xffff7700) // добавит фоновый цвет иконке
                //.setVibrate(new long[]{100, 100, 100, 100}) // настройка вибрации
                //.setPriority(Notification.PRIORITY_MAX)
                //.setSound(defaultSoundUri) // настройка звука
                .setContentIntent(pendingIntent)
                ;


        //notificationBuilder.setContentIntent(pendingIntent);
        //notificationBuilder.setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) { // метод вызывается когда генерируется новый токен
        //Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);

        //сохраняем в память телефона
        //getSharedPreferences("saveParams", MODE_PRIVATE).edit().putString("token", token).apply();
        classGlobalApp.SetTokenDevice(token);

    }

/*    public static String GetToken(Context context) { // получить текущий токен, запрашиваем сразу после логина
        return context.getSharedPreferences("saveParams", MODE_PRIVATE).getString("token", "");
    }*/

}

