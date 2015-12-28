package de.tum.in.tumcampus.models;

/**
 * Created by enricogiga on 16/06/2015.
 */
public class MVVDeparture extends  MVVObject {

    private static final long serialVersionUID = 4605590640826856584L;
    private String line;
    private String direction;
    private Integer min;

    public static enum TransportationType {UBAHN, SBAHN, BUS_TRAM};

    public MVVDeparture(String line, String direction, Integer min) {
        this.isDeparture = true;
        this.line = line;
        this.direction = direction;
        this.min = min;
    }

    public TransportationType getTransportationType(){
       if (line == null)
           return null;

        if(line.indexOf("U") >= 0)
            return TransportationType.UBAHN;
        else if (line.indexOf("S") >= 0)
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
