package de.tum.in.tumcampusapp.component.ui.cafeteria.controller;

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        CafeteriaWithMenus cafeteria = getCafeteriaWithMenus();
        if (cafeteria == null || cafeteria.getMenus().isEmpty()) {
            return results;
        }

        CafeteriaMenuCard card = new CafeteriaMenuCard(mContext);
        card.setCafeteriaWithMenus(cafeteria);

        results.add(card.getIfShowOnStart());
        return results;
    }

    @Override
    public boolean hasNotificationsEnabled() {
        return Utils.getSettingBool(mContext, "card_cafeteria_phone", true);
    }

    @Nullable
    private CafeteriaWithMenus getCafeteriaWithMenus() {
        // Choose which mensa should be shown
        int cafeteriaId = new LocationManager(mContext).getCafeteria();
        if (cafeteriaId == -1) {
            return null;
        }
        return localRepository.getCafeteriaWithMenus(cafeteriaId);
    }

    /**
     * Returns a list of {@link CafeteriaMenu}s of the best-matching cafeteria. If there's no
     * best-matching cafeteria, it returns an empty list.
     */
    public List<CafeteriaMenu> getBestMatchCafeteriaMenus() {
        int cafeteriaId = getBestMatchMensaId();
        if (cafeteriaId == -1) {
            return Collections.emptyList();
        }

        return getCafeteriaMenusByCafeteriaId(cafeteriaId);
    }

    public int getBestMatchMensaId() {
        // Choose which mensa should be shown
        int cafeteriaId = new LocationManager(mContext).getCafeteria();
        if (cafeteriaId == -1) {
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