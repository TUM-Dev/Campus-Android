package de.tum.in.tumcampusapp.managers;

import android.content.Context;

import org.json.JSONException;

import java.util.List;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.entities.Movie;
import de.tum.in.tumcampusapp.entities.TcaBoxes;
import io.objectbox.Box;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * TU Movie Manager, handles content
 */
public class KinoManager extends AbstractManager {

    private static final int TIME_TO_SYNC = CacheManager.VALIDITY_TWO_DAYS; // 1/2 hour
    private Box<Movie> kinoBox;

    /**
     * Constructor open/create database
     *
     * @param context Context
     */
    public KinoManager(Context context) {
        super(context);
        kinoBox = TcaBoxes.getBoxStore().boxFor(Movie.class);
    }

    /**
     * download kino from external interface (JSON)
     *
     * @param force True to force download over normal sync period, else false
     * @throws JSONException
     */
    public void downloadFromExternal(boolean force) throws JSONException {
        SyncManager sync = new SyncManager(mContext);
        if (!force && !sync.needSync(this, TIME_TO_SYNC)) {
            return;
        }

        // Download from the tumcabe api
        TUMCabeClient.getInstance(mContext).getMovies(new Callback<List<Movie>>() {
            @Override
            public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                if(response.isSuccessful()) {
                    kinoBox.put(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Movie>> call, Throwable t) {
                Utils.log(t, "Could not fetch movies");
            }
        });
    }

    public List<Movie> getAll() {
        return kinoBox.getAll();
    }


    public void cacheCovers() {
        List<Movie> allMovies = this.getAll();
        NetUtils net = new NetUtils(mContext);

        for (Movie e : allMovies) {
            if (e.getCover() != null && !e.getCover().equals("")) {
                net.downloadImage(e.getCover());
            }
        }
    }
}
