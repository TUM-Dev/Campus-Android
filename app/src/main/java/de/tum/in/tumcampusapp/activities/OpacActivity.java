package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;


import de.geeksfactory.opacclient.OpacApiFactory;
import de.geeksfactory.opacclient.apis.OpacApi;
import de.geeksfactory.opacclient.objects.DetailedItem;
import de.geeksfactory.opacclient.objects.Library;
import de.geeksfactory.opacclient.objects.SearchRequestResult;
import de.geeksfactory.opacclient.searchfields.SearchField;
import de.geeksfactory.opacclient.searchfields.SearchQuery;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OpacActivity extends BaseActivity {
   public static String LIBRARY_NAME = "Bremen";
   public static String LIBRARY_CONFIG = "{\"account_supported\":true,\"api\":\"sisis\",\"city\":\"Bremen\",\"country\":\"Deutschland\",\"data\":{\"baseurl\":\"https://opac.stadtbibliothek-bremen.de/webOPACClient\"},\"geo\":[53.07929619999999,8.8016937],\"information\":\"http://www.stadtbibliothek-bremen.de/bibliotheken.php\",\"replacedby\":\"de.opacapp.bremen\",\"state\":\"Bremen\",\"title\":\"Stadtbibliothek\"}";

   public OpacActivity() {
       super(R.layout.activity_kino);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       try {
           System.out.println("Hello OPAC!");

           // Create a library object
           Library library;
           library = Library.fromJSON(LIBRARY_NAME, new JSONObject(LIBRARY_CONFIG));

           // Instantiate the appropriate API class
           OpacApi api = OpacApiFactory.create(library, "HelloOpac/1.0.0");

           System.out.println("Obtaining search fields...");
           List<SearchField> searchFields = api.getSearchFields();
           System.out.println("Found a first search field: " + searchFields.get(0)
                                                                           .getDisplayName());

           List<SearchQuery> query = new ArrayList<SearchQuery>();
           query.add(new SearchQuery(searchFields.get(0), "Hello"));
           System.out.println("Searching for 'hello' in this field...");

           SearchRequestResult searchRequestResult = api.search(query);
           System.out.println("Found " + searchRequestResult.getTotal_result_count() + " matches.");
           System.out.println("First match: " + searchRequestResult.getResults()
                                                                   .get(0)
                                                                   .toString());

           System.out.println("Fetching details for the first result...");
           DetailedItem detailedItem = api.getResult(0);
           System.out.println("Got details: " + detailedItem.toString());
       } catch (Exception e) {

       }
   }

   @Override
   protected void onStart() {
       super.onStart();
   }

   @Override
   protected void onDestroy() {
       super.onDestroy();
   }
}
