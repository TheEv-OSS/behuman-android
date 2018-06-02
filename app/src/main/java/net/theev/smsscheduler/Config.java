package net.theev.smsscheduler;

import java.util.Calendar;

/**
 * Application configuration
 */
class Config {
    static final int DATABASE_VERSION = 1;

    // time when daily alarm should be fired
    static final int ALARM_HOUR = 12;
    static final int ALARM_MINUTE = 0;

    // day of week when weekly alarm should be fired
    static final int ALARM_DAY_OF_WEEK = Calendar.MONDAY;

    // day of month when monthly alarm should be fired
    static final int ALARM_DAY_OF_MONTH = 1;

    static final int MONTHLY_SMS_LIMIT_DEFAULT = 30;
}
