package de.tum.in.tumcampusapp.component.ui.transportation.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import junit.framework.Assert;

import org.junit.Test;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.transportation.model.efa.StationResult;

public class MvvStationListTest {

    private static String emptyResultExample =
            "{ \"parameters\": [ { \"name\":\"serverID\", \"value\":\"WEB3_\" }, { \"name\":\"requestID\", \"value\":\"0\" }, { \"name\":\"sessionID\", \"value\":\"0\" }, { \"name\":\"calcTime\", \"value\":\"31.622\" } ], \"stopFinder\": { \"message\": [ { \"name\":\"code\", \"value\":\"-8020\" }, { \"name\":\"error\", \"value\":\"\" }, { \"name\":\"type\", \"value\":\"\" }, { \"name\":\"module\", \"value\":\"BROKER\" } ], \"input\": { \"input\":\"0000000000000000000000000000\" }, " +
                    "\"points\": null " +
                    "} }";

    private static String singletonResultExample =
            "{ \"parameters\": [ { \"name\":\"serverID\", \"value\":\"WEB3_\" }, { \"name\":\"requestID\", \"value\":\"0\" }, { \"name\":\"sessionID\", \"value\":\"0\" }, { \"name\":\"calcTime\", \"value\":\"57.516\" } ], \"stopFinder\": { \"message\": [ { \"name\":\"code\", \"value\":\"-8010\" }, { \"name\":\"error\", \"value\":\"\" }, { \"name\":\"type\", \"value\":\"\" }, { \"name\":\"module\", \"value\":\"BROKER\" } ], \"input\": { \"input\":\"Garching, Forschungszentrum\" }, " +
                    "\"points\": {" +
                    " \"point\": { \"usage\":\"sf\", \"type\":\"any\", \"name\":\"Garching (b München), Garching, Forschungszentrum\", \"stateless\":\"1000460\", \"anyType\":\"stop\", \"sort\":\"2\", \"quality\":\"814\", \"best\":\"1\", \"object\":\"Garching, Forschungszentrum\", \"mainLoc\":\"Garching (b München)\", \"modes\":\"2,6\", \"ref\": { \"id\":\"1000460\", \"gid\":\"de:09184:460\", \"omc\":\"9184119\", \"placeID\":\"1\", \"place\":\"Garching (b München)\", \"coords\":\"11671228.33979,48264859.55499\" }, \"infos\": null, \"hasStaticInfo\":\"1\" } }, \"itdOdvAssignedStops\": { \"stopID\":\"1000460\", \"name\":\"Garching, Forschungszentrum\", \"x\":\"11671228.33979\", \"y\":\"48264859.55499\", \"mapName\":\"WGS84\", \"value\":\"1000460:Garching, Forschungszentrum\", \"place\":\"Garching (b München)\", \"nameWithPlace\":\"Garching, Forschungszentrum\", \"distanceTime\":\"0\", \"isTransferStop\":\"0\", \"hasStaticInfo\":\"1\", \"vm\":\"100\", \"gid\":\"de:09184:460\" } " +
                    "} }";

    private static String multiValueResultExample = "{ \"parameters\": [ { \"name\":\"serverID\", \"value\":\"WEB3_\" }, { \"name\":\"requestID\", \"value\":\"0\" }, { \"name\":\"sessionID\", \"value\":\"0\" }, { \"name\":\"calcTime\", \"value\":\"34.117\" } ], \"stopFinder\": { \"message\": [ { \"name\":\"code\", \"value\":\"-8011\" }, { \"name\":\"error\", \"value\":\"\" }, { \"name\":\"type\", \"value\":\"\" }, { \"name\":\"module\", \"value\":\"BROKER\" } ], \"input\": { \"input\":\"garching\" }, \"points\": [ { \"usage\":\"sf\", \"type\":\"any\", \"name\":\"Garching (b München), Gutenbergstraße\", \"stateless\":\"1002075\", \"anyType\":\"stop\", \"sort\":\"2\", \"quality\":\"414\", \"best\":\"0\", \"object\":\"Gutenbergstraße\", \"mainLoc\":\"Garching (b München)\", \"modes\":\"6\", \"ref\": { \"id\":\"1002075\", \"gid\":\"de:09184:2075\", \"omc\":\"9184119\", \"placeID\":\"1\", \"place\":\"Garching (b München)\", \"coords\":\"11624682.96318,48247612.21449\" } }, { \"usage\":\"sf\", \"type\":\"any\", \"name\":\"Garching (b München), Auweg\", \"stateless\":\"1002062\", \"anyType\":\"stop\", \"sort\":\"2\", \"quality\":\"484\", \"best\":\"0\", \"object\":\"Auweg\", \"mainLoc\":\"Garching (b München)\", \"modes\":\"6\", \"ref\": { \"id\":\"1002062\", \"gid\":\"de:09184:2062\", \"omc\":\"9184119\", \"placeID\":\"1\", \"place\":\"Garching (b München)\", \"coords\":\"11648979.99422,48246672.59669\" } }, { \"usage\":\"sf\", \"type\":\"any\", \"name\":\"Garching (b München), Brunnenweg\", \"stateless\":\"1002025\", \"anyType\":\"stop\", \"sort\":\"2\", \"quality\":\"421\", \"best\":\"0\", \"object\":\"Brunnenweg\", \"mainLoc\":\"Garching (b München)\", \"modes\":\"6\", \"ref\": { \"id\":\"1002025\", \"gid\":\"de:09184:2025\", \"omc\":\"9184119\", \"placeID\":\"1\", \"place\":\"Garching (b München)\", \"coords\":\"11657006.39757,48250482.92242\" } }, { \"usage\":\"sf\", \"type\":\"any\", \"name\":\"Garching (b München), Mühlgasse\", \"stateless\":\"1002019\", \"anyType\":\"stop\", \"sort\":\"2\", \"quality\":\"432\", \"best\":\"0\", \"object\":\"Mühlgasse\", \"mainLoc\":\"Garching (b München)\", \"modes\":\"6\", \"ref\": { \"id\":\"1002019\", \"gid\":\"de:09184:2019\", \"omc\":\"9184119\", \"placeID\":\"1\", \"place\":\"Garching (b München)\", \"coords\":\"11655841.98939,48247412.76250\" } }, { \"usage\":\"sf\", \"type\":\"any\", \"name\":\"Garching (b München), Keltenweg\", \"stateless\":\"1002018\", \"anyType\":\"stop\", \"sort\":\"2\", \"quality\":\"432\", \"best\":\"0\", \"object\":\"Keltenweg\", \"mainLoc\":\"Garching (b München)\", \"modes\":\"6\", \"ref\": { \"id\":\"1002018\", \"gid\":\"de:09184:2018\", \"omc\":\"9184119\", \"placeID\":\"1\", \"place\":\"Garching (b München)\", \"coords\":\"11641128.70342,48250731.32148\" } }, { \"usage\":\"sf\", \"type\":\"any\", \"name\":\"Garching (b München), Garching, Forschungszentrum\", \"stateless\":\"1000460\", \"anyType\":\"stop\", \"sort\":\"2\", \"quality\":\"444\", \"best\":\"0\", \"object\":\"Garching, Forschungszentrum\", \"mainLoc\":\"Garching (b München)\", \"modes\":\"2,6\", \"ref\": { \"id\":\"1000460\", \"gid\":\"de:09184:460\", \"omc\":\"9184119\", \"placeID\":\"1\", \"place\":\"Garching (b München)\", \"coords\":\"11671228.33979,48264859.55499\" } }, { \"usage\":\"sf\", \"type\":\"any\", \"name\":\"Garching (b München), Gewerbegebiet\", \"stateless\":\"1002014\", \"anyType\":\"stop\", \"sort\":\"2\", \"quality\":\"414\", \"best\":\"0\", \"object\":\"Gewerbegebiet\", \"mainLoc\":\"Garching (b München)\", \"modes\":\"6\", \"ref\": { \"id\":\"1002014\", \"gid\":\"de:09184:2014\", \"omc\":\"9184119\", \"placeID\":\"1\", \"place\":\"Garching (b München)\", \"coords\":\"11622551.98320,48249925.50070\" } }, { \"usage\":\"sf\", \"type\":\"any\", \"name\":\"Garching (b München), Garching\", \"stateless\":\"1000490\", \"anyType\":\"stop\", \"sort\":\"2\", \"quality\":\"666\", \"best\":\"1\", \"object\":\"Garching\", \"mainLoc\":\"Garching (b München)\", \"modes\":\"2,6\", \"ref\": { \"id\":\"1000490\", \"gid\":\"de:09184:490\", \"omc\":\"9184119\", \"placeID\":\"1\", \"place\":\"Garching (b München)\", \"coords\":\"11652502.62541,48249417.21671\" } }, { \"usage\":\"sf\", \"type\":\"any\", \"name\":\"Garching (b München), Garching-Hochbrück\", \"stateless\":\"1000480\", \"anyType\":\"stop\", \"sort\":\"2\", \"quality\":\"510\", \"best\":\"0\", \"object\":\"Garching-Hochbrück\", \"mainLoc\":\"Garching (b München)\", \"modes\":\"2,6\", \"ref\": { \"id\":\"1000480\", \"gid\":\"de:09184:480\", \"omc\":\"9184119\", \"placeID\":\"1\", \"place\":\"Garching (b München)\", \"coords\":\"11630771.91332,48247218.20876\" } } ] } }";

    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(MvvStationList.class, new MvvStationListSerializer())
            .create();

    @Test
    public void testEmptyValue() {
        MvvStationList stationList = gson.fromJson(emptyResultExample, MvvStationList.class);
        List<StationResult> departureList = stationList.getStations();
        Assert.assertEquals(0, departureList.size());
    }

    @Test
    public void testSingletonValue() {
        MvvStationList stationList = gson.fromJson(singletonResultExample, MvvStationList.class);
        List<StationResult> departureList = stationList.getStations();
        Assert.assertEquals(1, departureList.size());
        StationResult result = departureList.get(0);
        Assert.assertEquals("Garching (b München), Garching, Forschungszentrum", result.getStation());
        Assert.assertEquals("1000460", result.getId());
    }

    @Test
    public void testMultiValue() {
        MvvStationList stationList = gson.fromJson(multiValueResultExample, MvvStationList.class);
        List<StationResult> departureList = stationList.getStations();
        Assert.assertEquals(9, departureList.size());
    }
}
