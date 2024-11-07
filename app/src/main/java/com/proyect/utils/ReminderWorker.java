package com.proyect.utils;

import android.content.Context;

import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {

    private final Context mContext;
    private final WorkerParameters mParams;

    public ReminderWorker(Context context, WorkerParameters params) {
        super(context, params);
        this.mContext = context;
        this.mParams = params;
    }

    @Override
    public ListenableWorker.Result doWork() {
        NotificationHelper notificationHelper = new NotificationHelper(mContext);

        String title = getInputData().getString("title");
        String message = getInputData().getString("message");

        notificationHelper.createNotification(title != null ? title : "",
                message != null ? message : "");

        return ListenableWorker.Result.success();
    }
}
