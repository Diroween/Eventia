package com.proyect.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.proyect.MainActivity;
import com.proyect.R;

public class NotificationHelper {
    private final Context mContext;
    private String CHANNEL_ID = "reminder_channel_id";
    private int NOTIFICATION_ID = 1;

    public NotificationHelper(Context context) {
        this.mContext = context;
    }

    public void createNotification(String title, String message) {


        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.logo_eventia);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notes_fab)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(icon).bigLargeIcon((Bitmap) null))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        createNotificationChannel(notificationBuilder.build());

        int notificationId = NOTIFICATION_ID;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

    }

    public void createNotificationChannel(Notification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Reminder Channel Description");

            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(10, notification);
        }
    }
}
