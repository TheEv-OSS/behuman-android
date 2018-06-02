package net.theev.smsscheduler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static net.theev.smsscheduler.Config.MONTHLY_SMS_LIMIT_DEFAULT;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        setupStatistics();
        setupLimit();
    }

    /**
     * Sets up statistics
     */
    private void setupStatistics() {
        DBHelper dbHelper = new DBHelper(this);
        TextView totalMessagesSentTextView = (TextView) findViewById(R.id.totalMessagesSentTextView);
        totalMessagesSentTextView.setText(getResources().getString(R.string.total_messages_sent, dbHelper.getTotalNumberOfSentMessages()));
        ArrayList<NumberSentStatistic> numberSentStatistics = dbHelper.getNumbersSentStatistics();
        ListView numbersListView = (ListView) findViewById(R.id.statisticsListView);
        ListAdapter listAdapter = new ListAdapter(this, R.layout.item_statistic, numberSentStatistics);
        numbersListView.setAdapter(listAdapter);
    }

    /**
     * Custom list element adapter
     */
    private class ListAdapter extends ArrayAdapter<NumberSentStatistic> {

        ListAdapter(Context context, int resource, List<NumberSentStatistic> items) {
            super(context, resource, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.item_statistic, null);
            }

            final NumberSentStatistic item = getItem(position);

            if (item != null) {
                TextView numberTextView = (TextView) v.findViewById(R.id.numberTextView);
                TextView sentTextView = (TextView) v.findViewById(R.id.sentTextView);

                if (numberTextView != null) {
                    numberTextView.setText(item.getNumber());
                }

                if (sentTextView != null) {
                    sentTextView.setText(getResources().getString(R.string.messages_sent, item.getSent()));
                }
            }

            return v;
        }
    }

    /**
     * Opens default values popup
     *
     * @param view button view
     */
    public void openDefaultsPopup(View view) {
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.putExtra("type", "defaults");
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sets up limit
     */
    private void setupLimit() {
        final EditText limitEditText = (EditText) findViewById(R.id.limitEditText);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int limit = preferences.getInt("monthly_limit", MONTHLY_SMS_LIMIT_DEFAULT);
        limitEditText.setText(String.valueOf(limit));
        limitEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                findViewById(R.id.saveLimitImageButton).setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        limitEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // hide keyboard
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    limitEditText.clearFocus(); // Clear focus here from editText
                }
                return false;
            }
        });
    }

    /**
     * Saves monthly limit of messages
     *
     * @param view button view
     */
    public void saveLimit(View view) {
        view.setVisibility(View.GONE);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        EditText limitEditText = (EditText) findViewById(R.id.limitEditText);
        int limit;
        if (limitEditText.getText().toString().isEmpty()) {
            limit = MONTHLY_SMS_LIMIT_DEFAULT;
            limitEditText.setText(String.valueOf(MONTHLY_SMS_LIMIT_DEFAULT));
        } else
            limit = Integer.parseInt(limitEditText.getText().toString());

        editor.putInt("monthly_limit", limit);
        editor.apply();

        // hide keyboard
        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

        // remove focus from editText
        limitEditText.clearFocus();
    }

    /**
     * Starts share intent where the user can choose where he wants to share the statistics
     * @param view button view
     */
    public void shareStatistics(View view) {
        Resources res = getResources();

        String appName = res.getString(R.string.app_name);
        DBHelper dbHelper = new DBHelper(this);
        int sentMessages = dbHelper.getTotalNumberOfSentMessages();
        String shareText = res.getString(R.string.share_text, sentMessages, appName);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, res.getString(R.string.share_title, appName)));
    }
}
