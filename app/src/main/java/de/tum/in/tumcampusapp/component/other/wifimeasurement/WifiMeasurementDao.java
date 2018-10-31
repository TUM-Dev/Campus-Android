package de.tum.in.tumcampusapp.component.other.wifimeasurement;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import javax.annotation.Nullable;

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
