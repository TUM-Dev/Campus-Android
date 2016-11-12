package de.tum.in.tumcampusapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.auxiliary.luis.Action;
import de.tum.in.tumcampusapp.auxiliary.luis.LuisResponseReader;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class AssistantService extends IntentService {

    private static final String EXTRA_QUERY = "de.tum.in.tumcampusapp.services.extra.QUERY";
    public static final String EXTRA_RESULT = "de.tum.in.tumcampusapp.services.extra.RESULT";

    private static final String ACTION_PROCESS_QUERY = "de.tum.in.tumcampusapp.services.action.PROCESS_QUERY";
    private static final String EXTRA_RESULT_RECEIVER = "de.tum.in.tumcampusapp.services.extra.RESULT_RECEIVER";
    private static final String ASSISTANT_SERVICE = "AssistantService";

    private final String SERVER_URL = "https://api.projectoxford.ai/luis/v2.0/apps/b23eef2a-f9c8-47a4-8eaf-e59ff5b8ea20?subscription-key=5d2b6cb3f9a6470f88e49a00aa0ef694";

    public AssistantService() {
        super(ASSISTANT_SERVICE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.log("AssistantService has started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.log("AssistantService has stopped");
    }

    /**
     * Starts this service to perform action Process Query
     * with the string we got from Speech Recognition.
     * If the service is already performing a task this action will be queued.
     * @see IntentService
     */
    public static void startActionProcessQuery(Context context, String query) {
        Intent intent = new Intent(context, AssistantService.class);
        intent.setAction(ACTION_PROCESS_QUERY);
        intent.putExtra(EXTRA_QUERY, query);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final ResultReceiver resultReceiver = intent.getParcelableExtra(EXTRA_RESULT_RECEIVER);

            if (ACTION_PROCESS_QUERY.equals(action)) {
                String answer = processQuery(intent.getStringExtra(EXTRA_QUERY));

                Intent i = new Intent(Const.ASSISTANT_BROADCAST_INTENT);
                i.putExtra(EXTRA_RESULT, answer);
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            }
        }
    }

    /**
     * Handles action query in the provided background thread.
     * TODO: Do this.
     */
    private String processQuery(String query) {
        // This is a test
        // 1. Make a request to Microsoft Azure (using NetUtils)
        Optional<JSONObject> result = null;
        try {
            result = NetUtils.downloadJson(getApplicationContext(), SERVER_URL.concat("&q=" + URLEncoder.encode(query, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (result != null && result.isPresent()) {
            JSONObject resultJSON = result.get();
            //return resultJSON.toString();
            LuisResponseReader luisResponseReader = new LuisResponseReader();
            List<Action> actions = luisResponseReader.readResponse(resultJSON);
            for(Action a: actions){
                //// TODO: Action handling
            }
        }
//            LuisResponseReader luisResponseReader = new LuisResponseReader();
//            Action a = luisResponseReader.readResponse(resultJSON);
//            switch (a) {
//                case MENSA_LOCATION:
//                    break;
//                case TRANSPORTATION_TIME:
//                    break;
//                default:
//                    break;
//            }
//        }
        // 4. Make calls to other services.
        // 5. Update the activity.
        String answer = "Hi, how can I help you?" + new String(Character.toChars(0x1F60A));
        return answer;
    }
}
