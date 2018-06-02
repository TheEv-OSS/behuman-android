package net.theev.smsscheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static net.theev.smsscheduler.Config.ALARM_HOUR;
import static net.theev.smsscheduler.Config.ALARM_MINUTE;

/**
 * Common utilities
 */
class Utils {

    /**
     * Sets alarm that repeats every day
     *
     * @param context application context
     */
    static void setAlarm(Context context) {
        Calendar currentCalendar = new GregorianCalendar();
        currentCalendar.setTimeInMillis(System.currentTimeMillis());

        Calendar triggerCalendar = new GregorianCalendar();
        triggerCalendar.add(Calendar.DAY_OF_YEAR, currentCalendar.get(Calendar.DAY_OF_YEAR));
        triggerCalendar.set(Calendar.DATE, currentCalendar.get(Calendar.DATE));
        triggerCalendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH));
        triggerCalendar.set(Calendar.HOUR_OF_DAY, ALARM_HOUR);
        triggerCalendar.set(Calendar.MINUTE, ALARM_MINUTE);
        triggerCalendar.set(Calendar.SECOND, 0);
        triggerCalendar.set(Calendar.MILLISECOND, 0);

        long triggerAtMillis;
        if (triggerCalendar.getTime().after(currentCalendar.getTime()))
            triggerAtMillis = triggerCalendar.getTimeInMillis();
        else
            triggerAtMillis = triggerCalendar.getTimeInMillis() + 24 * 60 * 60 * 1000;

        Intent i = new Intent(context, SmsSendingService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, 24 * 60 * 60 * 1000, pendingIntent);
    }
}
