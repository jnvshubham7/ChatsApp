package com.example.chatsapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.chatsapp.R;
import com.example.chatsapp.Activities.Main_Activity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingService";
    private static final String CHANNEL_ID = "default_channel_id";
    private static final String CHANNEL_NAME = "Default Channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: called");

        if (remoteMessage.getNotification() != null) {
            // If message contains a notification payload
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification payload: title=" + title + ", body=" + body);
            showNotification(title, body);
        } else if (!remoteMessage.getData().isEmpty()) {
            // If message contains a data payload
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");

            if (title == null) {
                title = "Default Title";
            }
            if (body == null) {
                body = remoteMessage.getData().get("message");
                if (body == null) {
                    body = "Default Body";
                }
            }

            Log.d(TAG, "Data payload: title=" + title + ", body=" + body);
            showNotification(title, body);
        } else {
            Log.d(TAG, "Message data payload is empty");
        }
    }

    private void showNotification(String title, String body) {
        Log.d(TAG, "showNotification: called with title: " + title + ", body: " + body);

        Intent intent = new Intent(this, Main_Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Log.d(TAG, "Notification manager is null");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_chats)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);
        Log.d(TAG, "Notification built");

        try {
            notificationManager.notify(0, notificationBuilder.build());
            Log.d(TAG, "Notification displayed");
        } catch (Exception e) {
            Log.e(TAG, "Error displaying notification", e);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New token: " + token);
        // Send the token to your server
    }
}
