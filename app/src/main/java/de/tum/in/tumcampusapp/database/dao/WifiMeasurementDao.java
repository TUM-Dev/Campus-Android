package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import javax.annotation.Nullable;

import de.tum.in.tumcampusapp.models.tumcabe.WifiMeasurement;

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
