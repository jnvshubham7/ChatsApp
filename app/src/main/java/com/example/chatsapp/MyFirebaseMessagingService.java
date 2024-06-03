package com.example.chatsapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.chatsapp.Activities.ChatActivity; // Update this import based on your package structure
import com.example.chatsapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.ExecutionException;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingService";
    private static final String CHANNEL_ID = "default_channel_id";
    private static final String CHANNEL_NAME = "Default Channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String senderUid = remoteMessage.getData().get("sender_uid");
        String senderName = remoteMessage.getData().get("sender_name");
        String senderImage = remoteMessage.getData().get("sender_image");
        String message = remoteMessage.getData().get("message");

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("uid", senderUid);
        intent.putExtra("name", senderName);
        intent.putExtra("image", senderImage);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),  // Using a unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(senderName)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());  // Using unique notification ID
        }
    }

    private void showNotificationWithImage(String title, String body, String imageUrl, String senderUid) {
        Log.d(TAG, "showNotificationWithImage: called with title: " + title + ", body: " + body + ", imageUrl: " + imageUrl);

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("uid", senderUid);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),  // Using a unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Log.d(TAG, "Notification manager is null");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        try {
            Bitmap imageBitmap = Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();

            NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                    .bigPicture(imageBitmap)
                    .bigLargeIcon((Bitmap) null);

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_splash_app_icon) // Update this with your icon
                            .setContentTitle(title)
                            .setContentText(body)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .setStyle(style);

            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());  // Using unique notification ID
            Log.d(TAG, "Notification with image displayed");
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error displaying notification with image", e);
            showNotification(title, body, senderUid); // fallback to text notification
        }
    }

    private void showNotification(String title, String body, String senderUid) {
        Log.d(TAG, "showNotification: called with title: " + title + ", body: " + body + ", senderUid: " + senderUid);

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("uid", senderUid);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),  // Using a unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

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
                        .setSmallIcon(R.drawable.ic_chats) // Update this with your icon
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        try {
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());  // Using unique notification ID
            Log.d(TAG, "Notification displayed");
        } catch (Exception e) {
            Log.e(TAG, "Error displaying notification", e);
        }
    }
}
