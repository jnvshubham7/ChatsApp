package com.example.chatsapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.chatsapp.R;
import com.example.chatsapp.Activities.Main_Activity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle FCM messages here.
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        // Show custom notification UI
        showNotification(title, body);
    }

    private void showNotification(String title, String body) {
        // Create an intent to open MainActivity when notification is clicked
        Intent intent = new Intent(this, Main_Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Create custom notification layout
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "default_channel_id")
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        // Get the notification manager
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Show the notification
        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }
}
