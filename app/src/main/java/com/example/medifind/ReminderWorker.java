package com.example.medifind;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String clinicName = getInputData().getString("clinicName");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "appointment_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Appointment Reminder")
                .setContentText("Your appointment at " + clinicName + " is coming up!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1001, builder.build());

        return Result.success();
    }
}
