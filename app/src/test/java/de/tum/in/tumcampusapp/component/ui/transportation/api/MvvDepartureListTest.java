package de.tum.in.tumcampusapp.component.ui.transportation.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class MvvDepartureListTest {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DateTime.class, new MvvDateSerializer())
            .create();

    private static String loadJsonResource(String location) throws IOException {
        InputStream resourceStream = Thread.currentThread()
                                           .getContextClassLoader()
                                           .getResourceAsStream(location);
        return IOUtils.toString(resourceStream, StandardCharsets.UTF_8);
    }

    @Test
    public void testEmptyValue() throws IOException {
        String emptyResultExample = loadJsonResource("mvv/emptyResult.json");
        MvvDepartureList stationList = gson.fromJson(emptyResultExample, MvvDepartureList.class);
        List<MvvDeparture> departureList = stationList.getDepartureList();
        Assert.assertNull(departureList);
    }

    @Test
    public void testMultiValue1() throws IOException {
        String multiValueResultExample1 = loadJsonResource("mvv/multiValueResult1.json");
        MvvDepartureList stationList = gson.fromJson(multiValueResultExample1, MvvDepartureList.class);
        List<MvvDeparture> departureList = stationList.getDepartureList();
        Assert.assertEquals(40, departureList.size());
    }

    @Test
    public void testMultiValue2() throws IOException {
        String multiValueResultExample2 = loadJsonResource("mvv/multiValueResult2.json");
        MvvDepartureList stationList = gson.fromJson(multiValueResultExample2, MvvDepartureList.class);
        List<MvvDeparture> departureList = stationList.getDepartureList();
        Assert.assertEquals(40, departureList.size());
    }
}
