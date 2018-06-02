package net.theev.smsscheduler;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static net.theev.smsscheduler.Config.ALARM_DAY_OF_WEEK;
import static net.theev.smsscheduler.Config.ALARM_DAY_OF_MONTH;
import static net.theev.smsscheduler.Config.MONTHLY_SMS_LIMIT_DEFAULT;

public class SmsSendingService extends Service {

    private static final String TAG = "SmsSendingService";

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");
        sendSmsMessages();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    /**
     * Sends SMS messages to necessary numbers
     */
    private void sendSmsMessages() {
        DBHelper dbHelper = new DBHelper(this);
        GregorianCalendar date = new GregorianCalendar();
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        if (date.get(Calendar.DAY_OF_MONTH) == ALARM_DAY_OF_MONTH) { // if first day of month, restart current month sent counter
            editor.putInt("current_month_sent", 0);
            editor.apply();
        }
        int limit = preferences.getInt("monthly_limit", MONTHLY_SMS_LIMIT_DEFAULT);
        int monthlyCurrent = preferences.getInt("current_month_sent", 0);

        if (monthlyCurrent >= limit)
            return;

        ArrayList<Schedule> schedules = dbHelper.getSchedules();
        for (Schedule schedule : schedules) {
            boolean shouldSend = false;
            switch (schedule.getInterval()) {
                case 0: // once a day
                    shouldSend = true;
                    break;
                case 1: // once a week
                    if (date.get(Calendar.DAY_OF_WEEK) == ALARM_DAY_OF_WEEK)
                        shouldSend = true;
                    break;
                case 2: // once a month
                    if (date.get(Calendar.DAY_OF_MONTH) == ALARM_DAY_OF_MONTH)
                        shouldSend = true;
                    break;
                default:
                    break;
            }
            if (shouldSend && (monthlyCurrent < limit)) {
                SendSMS(schedule.getNumber(), schedule.getMessage());
                editor.putInt("current_month_sent", monthlyCurrent++);
                editor.apply();
            }
        }
    }

    /**
     * Sends SMS message
     *
     * @param number  phone number
     * @param message message to be sent
     */
    private void SendSMS(String number, String message) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message, null, null);
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.incrementSentMessages(number);
    }
}
