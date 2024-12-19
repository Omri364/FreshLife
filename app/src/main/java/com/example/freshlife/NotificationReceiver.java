package com.example.freshlife;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.Manifest;
import android.util.Log;

/**
 * The `NotificationReceiver` class handles the display of notifications for expiring food items.
 * It extends `BroadcastReceiver` and is triggered when the scheduled alarm for a notification fires.
 */
public class NotificationReceiver extends BroadcastReceiver {
    /**
     * Called when the `BroadcastReceiver` receives a broadcast.
     * This method is responsible for creating and displaying a notification for an expiring item.
     *
     * @param context The context in which the receiver is running.
     * @param intent  The intent containing additional data for the notification (e.g., item name).
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String itemName = intent.getStringExtra("itemName");
        Log.d("NotificationReceiver", "Received notification for " + itemName);

        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "FreshLifeChannel")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("FreshLife Reminder")
                .setContentText(itemName + " is about to expire!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(itemName.hashCode(), builder.build());
            Log.d("NotificationReceiver", "Notification displayed for " + itemName);
        } else {
            Log.d("NotificationReceiver", "POST_NOTIFICATIONS permission not granted.");
        }
    }

    /**
     * Creates a notification channel for devices running Android O or higher.
     *
     * @param context The context in which the channel is created.
     */
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "FreshLife Channel";
            String description = "Notifications for expiring items";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("FreshLifeChannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}


