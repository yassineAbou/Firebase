package com.example.firebaseoc.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.firebaseoc.MainActivity;
import com.example.firebaseoc.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Yassine Abou on 5/24/2021.
 */
public class NotificationsService extends FirebaseMessagingService {

    private final int NOTIFICATION_ID = 007;
    private final String NOTIFICATION_TAG = "FIREBASEOC";


    @Override
    public void onMessageReceived(@NonNull  RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getNotification() != null) {

            String message = remoteMessage.getNotification().getBody();
            sendVisualNotification(message);

        }
    }

    private void sendVisualNotification(String messageBody) {

        //Create an Intent that will be shown when user will click on the Notification
        Intent intent = new Intent(this, MainActivity.class);
       PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
         PendingIntent.FLAG_ONE_SHOT);

        //Create a Style for the Notification
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.notification_title));
        inboxStyle.addLine(messageBody);

        //Create a Channel (Android 8)
        String channelId = getString(R.string.default_notification_channel_id);

        //Build a Notification object
        NotificationCompat.Builder builder = new
                NotificationCompat.Builder(this, channelId)
                   .setSmallIcon(R.mipmap.ic_launcher1)
                   .setContentTitle(getString(R.string.app_name))
                   .setContentText(getString(R.string.notification_title))
                   .setAutoCancel(true)
                   .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                   .setContentIntent(pendingIntent)
                   .setStyle(inboxStyle);

        //Add the Notification to the Notification Manager and show it.
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        //Support Version >= Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Message from Firebase";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        //Show notification
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, builder.build());

    }
}
