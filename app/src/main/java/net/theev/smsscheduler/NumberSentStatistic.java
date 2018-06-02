package net.theev.smsscheduler;

/**
 * Phone numbers and number of sent messages object class
 */
class NumberSentStatistic {

    private String number;
    private int sent;

    NumberSentStatistic(String number, int sent) {
        this.number = number;
        this.sent = sent;
    }

    String getNumber() {
        return number;
    }

    int getSent() {
        return sent;
    }
}
