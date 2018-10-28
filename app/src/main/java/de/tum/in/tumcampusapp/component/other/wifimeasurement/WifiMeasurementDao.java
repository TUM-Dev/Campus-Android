package de.tum.in.tumcampusapp.component.other.wifimeasurement;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.support.annotation.Nullable;

import java.util.List;

import de.tum.in.tumcampusapp.component.other.wifimeasurement.model.WifiMeasurement;

@Dao
public interface WifiMeasurementDao {
    @Nullable
    @Query("SELECT * FROM wifi_measurement")
    List<WifiMeasurement> getAll();

    @Query("DELETE FROM wifi_measurement")
    void cleanup();

    @Insert
    void insert(WifiMeasurement wifiMeasurement);
}
