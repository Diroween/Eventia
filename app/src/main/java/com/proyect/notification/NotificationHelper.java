package com.proyect.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.proyect.MainActivity;
import com.proyect.R;
import com.proyect.event.Event;
import com.proyect.event.EventViewerActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NotificationHelper {

    private static Context context;
    private final String CHANNEL_ID;

    public NotificationHelper(Context context) {
        this.context = context;
        this.CHANNEL_ID = this.context.getString(R.string.event_notificationchannel);
    }

    /**
     * Método para calcular los segundos que quedan desde la fecha actual hasta la fecha del evento.
     */
    public static long getSecondsUntilEvent(Event event) {

        // Dividimos el año, mes y día
        String[] fechas = event.getDate().split("-");

        // Dividimos horas y minutos
        String[] horas = event.getHour().split(":");

        LocalDate fechaEvento = LocalDate.of(Integer.parseInt(fechas[0]), Integer.parseInt(fechas[1]), Integer.parseInt(fechas[2]));
        LocalTime horaEvento = LocalTime.of(Integer.parseInt(horas[0]), Integer.parseInt(horas[1]));

        LocalDateTime fechaHoraActual = LocalDateTime.now();
        LocalDateTime fechaHoraEvento = LocalDateTime.of(fechaEvento, horaEvento);

        ZoneOffset offset = ZonedDateTime.now().getOffset();

        long delaySegundos = fechaHoraEvento.toEpochSecond(offset) - fechaHoraActual.toEpochSecond(offset);

        return delaySegundos;
    }

    /**
     * Método para lanzar un trabajo en segundo plano (en nuestro caso, una notificación).
     */
    public static void createWorkRequest(String workName, String titulo, String message, String eventId, long timeDelayInSeconds) {
        OneTimeWorkRequest myWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(timeDelayInSeconds, TimeUnit.SECONDS)
                .setInputData(new Data.Builder().putString("title", titulo).putString("message", message).putString("event_id", eventId).build())
                .build();

        //empleamos ExistingWorkPolicy.REPLACE para sobreescribir trabajos previos en caso de haberse editado la hora del evento
        WorkManager.getInstance(context).enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                myWorkRequest
        );

    }

    /**
     * Método para cancelar todos los trabajos con un nombre dado.
     */
    public static void cancelAllWorkRequests(String name) {
        WorkManager instance = WorkManager.getInstance(context);
        instance.cancelUniqueWork(name);
        instance.cancelUniqueWork(name + "_3600");
        instance.cancelUniqueWork(name + "_86400");
        instance.cancelUniqueWork(name + "_604800");
    }

    /**
     * Método para cancelar un solo trabajo.
     */
    public static void cancelWorkRequest(String name) {
        WorkManager instance = WorkManager.getInstance(context);
        instance.cancelUniqueWork(name);
    }

    /**
     * Método para programar una o varias notificaciones dependiendo de los segundos que queden hasta la fecha del evento.
     */
    public static void enqueueNotifications(Context context, Event event, long delay) {

        String mensaje = context.getResources().getString(R.string.event_notificationmessage, event.getPlace());

        if (delay > 0) {
            //notificamos al momento
            createWorkRequest(event.getId(),
                    context.getResources().getString(R.string.event_notificationtitle_now, event.getName(), event.getHour()),
                    mensaje,
                    event.getId(),
                    delay);

            if (delay > 3600) {
                //notificamos una hora antes
                createWorkRequest(event.getId() + "_3600",
                        context.getResources().getString(R.string.event_notificationtitle_1h, event.getName()),
                        mensaje,
                        event.getId(),
                        delay - 3600);

                if (delay > 86400) {
                    //notificamos un día antes
                    createWorkRequest(event.getId() + "_86400",
                            context.getResources().getString(R.string.event_notificationtitle_24h, event.getName()),
                            mensaje,
                            event.getId(),
                            delay - 86400);

                    if (delay > 604800) {
                        //notificamos una semana antes
                        createWorkRequest(event.getId() + "_604800",
                                context.getResources().getString(R.string.event_notificationtitle_7d, event.getName()),
                                mensaje,
                                event.getId(),
                                delay - 604800);
                    }
                }
            }
        }
    }

    public void createNotification(String title, String message, String eventId) {

        //Instanciamos un intent de MainActivity, lo establecemos como stack principal y lo agregamos
        //a la pila
        Intent intentMain = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intentMain);

        //Instanciamos y agregamos a la pila EventViewerActivity, pasando el id del evento para poder
        //cargarlo al pulsar en la notificación
        Intent intentView = new Intent(context, EventViewerActivity.class);
        intentView.putExtra("event_id", eventId);
        stackBuilder.addNextIntent(intentView);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        //Establecemos el icono de la notificación
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_eventia);

        //Configuramos la notificación
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notes_fab)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(icon).bigLargeIcon((Bitmap) null))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

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
