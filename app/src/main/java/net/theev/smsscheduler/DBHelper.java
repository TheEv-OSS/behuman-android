package net.theev.smsscheduler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import static net.theev.smsscheduler.Config.DATABASE_VERSION;

class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sms_scheduler.sql";
    private static final String TABLE_SCHEDULE = "schedule";
    private static final String TABLE_STATISTICS = "statistics";

    /**
     * Database handler constructor
     *
     * @param context context
     */
    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SCHEDULE + " (_id INTEGER, number VARCHAR, message VARCHAR, interval INTEGER, " +
                "PRIMARY KEY (_id));");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_STATISTICS + " (_id INTEGER, number VARCHAR, sent INTEGER, " +
                "PRIMARY KEY (_id));");
    }

    /**
     * Updates database
     *
     * @param database   database
     * @param oldVersion old version code
     * @param newVersion new version code
     */
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // do something in the future
        }
    }

    /**
     * Adds a new schedule to database
     *
     * @param schedule new schedule
     */
    void addNewSchedule(Schedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("number", schedule.getNumber());
        values.put("message", schedule.getMessage());
        values.put("interval", schedule.getInterval());
        db.insertWithOnConflict(TABLE_SCHEDULE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    /**
     * Updates an existing schedule in database
     *
     * @param schedule schedule to be updated
     */
    void updateSchedule(Schedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("number", schedule.getNumber());
        values.put("message", schedule.getMessage());
        values.put("interval", schedule.getInterval());
        db.update(TABLE_SCHEDULE, values, "_id = " + schedule.getId(), null);
        db.close();
    }

    /**
     * Deletes schedule from database
     *
     * @param id schedule ID
     */
    void deleteSchedule(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SCHEDULE, " _id = " + id, null);
        db.close();
    }

    /**
     * Gets schedules from database
     *
     * @return schedules list
     */
    ArrayList<Schedule> getSchedules() {
        ArrayList<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_SCHEDULE, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            int id = res.getInt(res.getColumnIndex("_id"));
            String number = res.getString(res.getColumnIndex("number"));
            String message = res.getString(res.getColumnIndex("message"));
            int interval = res.getInt(res.getColumnIndex("interval"));

            Schedule schedule = new Schedule(id, number, message, interval);
            schedules.add(schedule);
            res.moveToNext();
        }

        res.close();
        db.close();
        return schedules;
    }

    /**
     * Increments number of sent messages to particular phone number
     *
     * @param number phone number
     */
    void incrementSentMessages(String number) {
        SQLiteDatabase db = this.getWritableDatabase();

        int id = -1;
        int sent = 1;
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_STATISTICS + " WHERE number = '" + number + "'", null);
        if (res.moveToFirst()) {
            id = res.getInt(res.getColumnIndex("_id"));
            sent = res.getInt(res.getColumnIndex("sent"));
        }
        res.close();

        if (id == -1) { // if first SMS was sent to this number
            ContentValues values = new ContentValues();
            values.put("sent", 1);
            values.put("number", number);
            db.insertWithOnConflict(TABLE_STATISTICS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        } else {
            ContentValues values = new ContentValues();
            values.put("sent", sent + 1);
            db.update(TABLE_STATISTICS, values, "_id = " + id, null);
        }
        db.close();
    }

    /**
     * Gets number of sent messages to particular phone number
     *
     * @return phone numbers and number of sent messages objects array
     */
    ArrayList<NumberSentStatistic> getNumbersSentStatistics() {
        ArrayList<NumberSentStatistic> numberSentStatistics = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_STATISTICS, null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String number = res.getString(res.getColumnIndex("number"));
            int sent = res.getInt(res.getColumnIndex("sent"));
            NumberSentStatistic numberSentStatistic = new NumberSentStatistic(number, sent);
            numberSentStatistics.add(numberSentStatistic);
            res.moveToNext();
        }
        res.close();
        db.close();
        return numberSentStatistics;
    }

    /**
     * Gets number of total sent messages
     *
     * @return number of total sent messages
     */
    int getTotalNumberOfSentMessages() {
        int totalSent = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT sum(sent) FROM " + TABLE_STATISTICS, null);
        if (res.moveToFirst()) {
            totalSent = res.getInt(res.getColumnIndex("sum(sent)"));
        }
        res.close();
        db.close();
        return totalSent;
    }
}
