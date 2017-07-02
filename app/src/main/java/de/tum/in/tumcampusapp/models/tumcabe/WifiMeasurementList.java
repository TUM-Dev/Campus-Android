package de.tum.in.tumcampusapp.models.tumcabe;

import java.util.ArrayList;

public class WifiMeasurementList {
    private ArrayList<WifiMeasurement> wifiMeasurements = new ArrayList<>();
    public void addMeasurement(WifiMeasurement wifiMeasurement){
        wifiMeasurements.add(wifiMeasurement);
    }

    public boolean isEmpty(){
        return wifiMeasurements.isEmpty();
    }
}
