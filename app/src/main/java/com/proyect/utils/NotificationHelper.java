package com.proyect.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;

import com.proyect.MainActivity;
import com.proyect.R;

import java.util.Random;

public class NotificationHelper {

    private final Context context;
    private final String CHANNEL_ID = "reminder_channel_id";

    public NotificationHelper(Context context) {
        this.context = context;
    }

    public void createNotification(String title, String message) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_eventia);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notes_fab)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(icon).bigLargeIcon((Bitmap) null))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        createNotificationChannel(notificationBuilder.build());
    }

    public void createNotificationChannel(Notification notification) {

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Reminder Channel Description");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        // Es necesario un ID para cada notificación y que éste no se repita para poder concatenarlas
        notificationManager.notify(new Random().nextInt(), notification);
    }
}
