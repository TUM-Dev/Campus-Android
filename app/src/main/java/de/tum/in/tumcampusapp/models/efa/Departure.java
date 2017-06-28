package de.tum.in.tumcampusapp.models.efa;

import java.util.concurrent.TimeUnit;

public class Departure {
    final public String servingLine;
    final public String direction;
    final public String symbol;
    final public int countDown;
    final public long departureTime;

    public Departure(String servingLine, String direction, String symbol, int countDown, long departureTime) {
        this.servingLine = servingLine;
        this.direction = direction;
        this.symbol = symbol;
        this.countDown = countDown;
        this.departureTime = departureTime;
    }

    /**
     * Calculates the countDown with the real departure time and the current time
     *
     * @return The calculated countDown in minutes
     */
    public long getCalculatedCountDown() {
        return TimeUnit.MINUTES.convert(departureTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
}