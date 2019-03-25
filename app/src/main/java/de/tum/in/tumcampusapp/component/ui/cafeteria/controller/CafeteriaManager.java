package de.tum.in.tumcampusapp.component.ui.cafeteria.controller;

import android.content.Context;
import android.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.notifications.ProvidesNotifications;
import de.tum.in.tumcampusapp.component.other.locations.LocationManager;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuCard;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Cafeteria Manager, handles database stuff, external imports
 */
public class CafeteriaManager implements ProvidesCard, ProvidesNotifications {

    private Context mContext;
    private final CafeteriaLocalRepository localRepository;

    public CafeteriaManager(Context context) {
        mContext = context;
        TcaDb db = TcaDb.getInstance(context);
        localRepository = new CafeteriaLocalRepository(db);
    }

    @NotNull
    @Override
    public List<Card> getCards(@NonNull CacheControl cacheControl) {
        List<Card> results = new ArrayList<>();

        // ids have to be added to a new set because the data would be changed otherwise
        Collection<String> cafeteriaIds = new HashSet<>(20);
        cafeteriaIds.addAll(PreferenceManager.getDefaultSharedPreferences(mContext)
                                             .getStringSet(Const.CAFETERIA_CARDS_SETTING, new HashSet<>(0)));

        // adding the location based id to the set now makes sure that the cafeteria is not shown twice
        if (cafeteriaIds.contains(Const.CAFETERIA_BY_LOCATION_SETTINGS_ID)){
            cafeteriaIds.remove(Const.CAFETERIA_BY_LOCATION_SETTINGS_ID);
            cafeteriaIds.add(Integer.toString(new LocationManager(mContext).getCafeteria()));
        }

        for (String id: cafeteriaIds) {
            int cafeteria = Integer.parseInt(id);
            if (cafeteria == Const.NO_CAFETERIA_FOUND){
                // no cafeteria based on the location could be found
                continue;
            }
            CafeteriaMenuCard card = new CafeteriaMenuCard(mContext);
            card.setCafeteriaWithMenus(localRepository.getCafeteriaWithMenus(cafeteria));
            results.add(card.getIfShowOnStart());
        }

        return results;
    }

    @Override
    public boolean hasNotificationsEnabled() {
        return Utils.getSettingBool(mContext, "card_cafeteria_phone", true);
    }

    /**
     * Returns a list of {@link CafeteriaMenu}s of the best-matching cafeteria. If there's no
     * best-matching cafeteria, it returns an empty list.
     */
    public List<CafeteriaMenu> getBestMatchCafeteriaMenus() {
        int cafeteriaId = getBestMatchMensaId();
        if (cafeteriaId == Const.NO_CAFETERIA_FOUND) {
            return Collections.emptyList();
        }

        return getCafeteriaMenusByCafeteriaId(cafeteriaId);
    }

    public int getBestMatchMensaId() {
        // Choose which mensa should be shown
        int cafeteriaId = new LocationManager(mContext).getCafeteria();
        if (cafeteriaId == Const.NO_CAFETERIA_FOUND) {
            Utils.log("could not get a Cafeteria from locationManager!");
        }
        return cafeteriaId;
    }

    private List<CafeteriaMenu> getCafeteriaMenusByCafeteriaId(int cafeteriaId) {
        CafeteriaWithMenus cafeteria = new CafeteriaWithMenus(cafeteriaId);

        List<DateTime> menuDates = localRepository.getAllMenuDates();
        cafeteria.setMenuDates(menuDates);

        DateTime nextMenuDate = cafeteria.getNextMenuDate();
        List<CafeteriaMenu> menus = localRepository.getCafeteriaMenus(cafeteriaId, nextMenuDate);
        cafeteria.setMenus(menus);

        return cafeteria.getMenus();
    }

}