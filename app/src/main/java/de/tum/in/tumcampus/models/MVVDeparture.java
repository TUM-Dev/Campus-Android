package de.tum.in.tumcampus.models;

/**
 * The MVV departures
 */
public class MVVDeparture extends  MVVObject {
    private String line;
    private String direction;
    private Integer min;

    public enum TransportationType {UBAHN, SBAHN, BUS_TRAM}

    public MVVDeparture(String line, String direction, Integer min) {
        this.isDeparture = true;
        this.line = line;
        this.direction = direction;
        this.min = min;
    }

    public TransportationType getTransportationType(){
       if (line == null)
           return null;

        if(line.contains("U"))
            return TransportationType.UBAHN;
        else if (line.contains("S"))
            return TransportationType.SBAHN;
        else
            return TransportationType.BUS_TRAM;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

}
