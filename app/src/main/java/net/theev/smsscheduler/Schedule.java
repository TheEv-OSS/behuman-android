package net.theev.smsscheduler;

/**
 * Schedule object class
 */
class Schedule {

    private int id;
    private String number;
    private String message;
    private int interval;

    Schedule(int id, String number, String message, int interval) {
        this.id = id;
        this.number = number;
        this.message = message;
        this.interval = interval;
    }

    int getId() {
        return id;
    }

    String getNumber() {
        return number;
    }

    String getMessage() {
        return message;
    }

    int getInterval() {
        return interval;
    }
}
