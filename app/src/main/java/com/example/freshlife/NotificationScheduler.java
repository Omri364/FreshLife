package com.example.freshlife;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The `NotificationScheduler` class is responsible for scheduling notifications
 * for food items based on their expiration date. Notifications are triggered
 * using the `AlarmManager` and handled by the `NotificationReceiver` class.
 */
public class NotificationScheduler {

    /**
     * Schedules notifications for all provided food items based on user preferences.
     *
     * @param context   The context in which the notifications are scheduled.
     * @param foodItems The list of food items for which notifications should be scheduled.
     */
    public static void scheduleNotifications(Context context, List<FoodItem> foodItems) {
        // Get preferences for notification timing
        SharedPreferences prefs = context.getSharedPreferences("FreshLifePrefs", Context.MODE_PRIVATE);

        int daysBefore = prefs.getInt("notificationDays", 3); // Default to 3 days
        int hour = prefs.getInt("notificationHour", 19);       // Default to 9:00 AM
        int minute = prefs.getInt("notificationMinute", 22);

        // Loop through each food item to schedule notifications
        for (FoodItem item : foodItems) {
            long notificationTime = calculateNotificationTime(item.getExpirationDate(), daysBefore, hour, minute);

            // Ensure the notification time is in the future
            if (notificationTime > System.currentTimeMillis()) {
                scheduleNotification(context, item.getName(), notificationTime, item.getId().hashCode());
            }
        }
    }

    /**
     * Schedules a notification for a specific food item at the specified time.
     *
     * @param context        The context in which the notification is scheduled.
     * @param itemName       The name of the food item.
     * @param triggerTime    The time at which the notification should be triggered.
     * @param notificationId A unique ID for the notification.
     */
    public static void scheduleNotification(Context context, String itemName, long triggerTime, int notificationId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("itemName", itemName);
        intent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Log the scheduling details
        Log.d("NotificationScheduler", "Scheduling notification for " + itemName + " at " + triggerTime);

        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    /**
     * Calculates the time in milliseconds for triggering a notification.
     *
     * @param expirationDate The expiration date of the food item in "yyyy-MM-dd" format.
     * @param daysBefore     The number of days before expiration to notify.
     * @param hour           The hour at which the notification should be triggered.
     * @param minute         The minute at which the notification should be triggered.
     * @return The calculated time in milliseconds.
     */
    private static long calculateNotificationTime(String expirationDate, int daysBefore, int hour, int minute) {
        // Parse the expiration date (e.g., "2024-11-21") into milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            long expirationMillis = sdf.parse(expirationDate).getTime();

            // Subtract daysBefore in milliseconds
            long notificationMillis = expirationMillis - (daysBefore * 24L * 60L * 60L * 1000L);

            // Adjust the time to the selected hour and minute
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(notificationMillis);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

}

