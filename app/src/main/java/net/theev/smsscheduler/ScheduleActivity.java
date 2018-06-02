package net.theev.smsscheduler;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleActivity extends AppCompatActivity {

    private String type = "";
    Spinner intervalSpinner;
    EditText numberEditText;
    EditText messageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        this.setFinishOnTouchOutside(false);
        setPopupStrings();
        setupSpinner();
        setupFields();
    }

    /**
     * Sets popup window title and confirm button strings
     */
    private void setPopupStrings() {
        type = getIntent().getStringExtra("type");
        Button confirmButton = (Button) findViewById(R.id.confirmButton);
        Resources res = getResources();
        switch (type) {
            case "add_new":  // if new number is being added
                setTitle(res.getString(R.string.add_new_number));
                confirmButton.setText(res.getString(R.string.add));
                break;
            case "edit":  // if existing number is being edited
                setTitle(res.getString(R.string.update_number));
                confirmButton.setText(res.getString(R.string.update));
                findViewById(R.id.removeButton).setVisibility(View.VISIBLE);
                break;
            default:  // if defaults are being edited
                setTitle(res.getString(R.string.defaults));
                confirmButton.setText(res.getString(R.string.save));
                break;
        }
    }

    /**
     * Sets up interval spinner
     */
    private void setupSpinner() {
        intervalSpinner = (Spinner) findViewById(R.id.intervalSpinner);
        String[] arraySpinner = new String[]{"Once a day", "Once a week", "Once a month"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arraySpinner);
        intervalSpinner.setAdapter(adapter);
    }

    /**
     * Cancels adding or updating number by finishing activity
     *
     * @param view button view
     */
    public void cancel(View view) {
        finish();
    }

    /**
     * Sets up all fields
     */
    private void setupFields() {
        numberEditText = (EditText) findViewById(R.id.numberEditText);
        messageEditText = (EditText) findViewById(R.id.messageEditText);

        switch (type) {
            case "edit": {
                Intent intent = getIntent();
                int interval = intent.getIntExtra("interval", 0);
                String number = intent.getStringExtra("number");
                String message = intent.getStringExtra("message");
                intervalSpinner.setSelection(interval);
                if (number != null)
                    numberEditText.setText(number);
                if (message != null)
                    messageEditText.setText(message);
                break;
            }
            case "defaults":
            case "add_new": {
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                int interval = preferences.getInt("default_interval", 0);
                String number = preferences.getString("default_number", "");
                String message = preferences.getString("default_message", "");
                intervalSpinner.setSelection(interval);
                numberEditText.setText(number);
                messageEditText.setText(message);
                break;
            }
        }
    }

    /**
     * Confirms adding or updating number
     *
     * @param view button view
     */
    public void confirm(View view) {
        String number = numberEditText.getText().toString();
        String message = messageEditText.getText().toString();
        if (!type.equals("defaults") && (number.isEmpty() || message.isEmpty())) {
            Toast.makeText(this, getResources().getString(R.string.please_fill_in_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        int interval = intervalSpinner.getSelectedItemPosition();
        DBHelper dbHelper = new DBHelper(this);

        switch (type) {
            case "add_new":  // if new number is being added
                Schedule schedule = new Schedule(-1, number, message, interval);
                dbHelper.addNewSchedule(schedule);
                break;
            case "edit":  // if existing number is being updated
                int id = getIntent().getIntExtra("id", -1);
                if (id > -1) {
                    Schedule updatedSchedule = new Schedule(id, number, message, interval);
                    dbHelper.updateSchedule(updatedSchedule);
                    setResult(RESULT_OK, new Intent());
                }
                finish();
                break;
            default:
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("default_interval", interval);
                editor.putString("default_number", number);
                editor.putString("default_message", message);
                editor.apply();
                finish();
                break;
        }

        setResult(RESULT_OK, new Intent());
        finish();
    }

    /**
     * Displays an alert and removes schedule
     * @param view button view
     */
    public void remove(View view) {
        Resources res = getResources();
        AlertDialog dialog = new AlertDialog.Builder(ScheduleActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(res.getString(R.string.warning))
                .setMessage(res.getString(R.string.remove_confirm_message))
                .setPositiveButton(res.getString(R.string.remove), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final int id = getIntent().getIntExtra("id", -1);
                        DBHelper dbHelper = new DBHelper(getApplicationContext());
                        dbHelper.deleteSchedule(id);
                        setResult(RESULT_OK, new Intent());
                        finish();
                    }
                })
                .setNegativeButton(res.getString(R.string.cancel), null)
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setCancelable(false)
                .show();
        TextView textView1 = (TextView) dialog.findViewById(android.R.id.button1);
        TextView textView2 = (TextView) dialog.findViewById(android.R.id.button2);
        if (textView1 != null) {
            textView1.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        if (textView2 != null) {
            textView2.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }
}
