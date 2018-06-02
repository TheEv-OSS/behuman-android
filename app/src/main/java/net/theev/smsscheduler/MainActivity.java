package net.theev.smsscheduler;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static net.theev.smsscheduler.Config.MONTHLY_SMS_LIMIT_DEFAULT;

public class MainActivity extends AppCompatActivity {

    private int requestCode = 1;
    private ListAdapter listAdapter;
    private ArrayList<Schedule> schedules;
    private static String PERMISSION_SEND_SMS = Manifest.permission.SEND_SMS;
    private static final int REQUEST_SEND_SMS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ifFirstTimeDisplayAlert();
        setupNumbersListView();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), PERMISSION_SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestSendSMSPermission();
        }
        Utils.setAlarm(this);
    }

    /**
     * Checks if app is up for the first time and if true, then displays a message
     */
    private void ifFirstTimeDisplayAlert() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean fistTime = preferences.getBoolean("first_time", true);

        if (fistTime) {
            Resources res = getResources();
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle(res.getString(R.string.warning))
                    .setMessage(res.getString(R.string.warning_message))
                    .setPositiveButton(res.getString(R.string.accept), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("first_time", false);
                            editor.apply();
                        }
                    })
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setCancelable(false)
                    .show();
            TextView textView = (TextView) dialog.findViewById(android.R.id.button1);
            if (textView != null) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Opens popup to add a new number
     *
     * @param view button view
     */
    public void addNewNumber(View view) {
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.putExtra("type", "add_new");
        startActivityForResult(intent, requestCode);
    }

    /**
     * Sets up numbers listView
     */
    private void setupNumbersListView() {
        DBHelper dbHelper = new DBHelper(this);
        schedules = dbHelper.getSchedules();

        ListView numbersListView = (ListView) findViewById(R.id.numbersListView);
        listAdapter = new ListAdapter(this, R.layout.item_schedule, schedules);
        numbersListView.setAdapter(listAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onActivityResult");
        if (this.requestCode == requestCode) {
            System.out.println("onActivityResult requestCode");
            if (resultCode == RESULT_OK) {
                System.out.println("onActivityResult RESULT_OK");
                DBHelper dbHelper = new DBHelper(this);
                schedules.clear();
                ArrayList<Schedule> updatedSchedules = dbHelper.getSchedules();
                schedules.addAll(updatedSchedules);
                listAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Custom list element adapter
     */
    private class ListAdapter extends ArrayAdapter<Schedule> {

        ListAdapter(Context context, int resource, List<Schedule> items) {
            super(context, resource, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.item_schedule, null);
            }

            final Schedule item = getItem(position);

            if (item != null) {
                TextView numberTextView = (TextView) v.findViewById(R.id.numberTextView);
                TextView messageTextView = (TextView) v.findViewById(R.id.messageTextView);

                if (numberTextView != null) {
                    numberTextView.setText(item.getNumber());
                }

                if (messageTextView != null) {
                    messageTextView.setText(item.getMessage());
                }

                ImageButton editImageButton = (ImageButton) v.findViewById(R.id.editImageButton);
                editImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ScheduleActivity.class);
                        intent.putExtra("type", "edit");
                        intent.putExtra("id", item.getId());
                        intent.putExtra("interval", item.getInterval());
                        intent.putExtra("number", item.getNumber());
                        intent.putExtra("message", item.getMessage());
                        startActivityForResult(intent, requestCode);
                    }
                });

                ImageButton sendImmediatelyImageButton = (ImageButton) v.findViewById(R.id.sendImmediatelyImageButton);
                sendImmediatelyImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        int limit = preferences.getInt("monthly_limit", MONTHLY_SMS_LIMIT_DEFAULT);
                        int monthlyCurrent = preferences.getInt("current_month_sent", 0);
                        if (monthlyCurrent >= limit)
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.monthly_limit_reached), Toast.LENGTH_SHORT).show();
                        else {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("current_month_sent", monthlyCurrent + 1);
                            editor.apply();
                            SendSMS(item.getNumber(), item.getMessage());
                        }
                    }
                });
            }

            return v;
        }
    }

    /**
     * Sends SMS message
     *
     * @param number  phone number
     * @param message message to be sent
     */
    private void SendSMS(String number, String message) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), PERMISSION_SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestSendSMSPermission();
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message, null, null);
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.incrementSentMessages(number);
        Toast.makeText(this, getResources().getString(R.string.sms_has_been_sent), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SEND_SMS: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getResources().getString(R.string.please_accept_permission), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Requests permission to send SMS
     */
    private void requestSendSMSPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_SEND_SMS)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{PERMISSION_SEND_SMS},
                    REQUEST_SEND_SMS);
        } else
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION_SEND_SMS}, REQUEST_SEND_SMS);
    }
}
