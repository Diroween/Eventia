package com.proyect;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UploadWorker extends Worker {
    public UploadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
            super(context, params);
    }

    @Override
    public  Result doWork() {

        Log.v("NOTIFICACION", "mensaje_hola_mundo");

        try{
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Result.success();
    }
}
