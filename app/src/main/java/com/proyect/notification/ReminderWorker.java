package com.proyect.notification;

import android.content.Context;

import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {

    private final Context context;
    private final WorkerParameters params;

    public ReminderWorker(Context context, WorkerParameters params) {
        super(context, params);
        this.context = context;
        this.params = params;
    }

    @Override
    public ListenableWorker.Result doWork() {
        NotificationHelper notificationHelper = new NotificationHelper(context);

        String title = getInputData().getString("title");
        String message = getInputData().getString("message");

        notificationHelper.createNotification(title != null ? title : "",
                message != null ? message : "");

        return ListenableWorker.Result.success();
    }
}