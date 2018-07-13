package de.tum.in.tumcampusapp.component.ui.cafeteria.controller;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.other.locations.LocationManager;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuCard;
import de.tum.in.tumcampusapp.component.ui.cafeteria.details.CafeteriaViewModel;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository;
import de.tum.in.tumcampusapp.component.ui.overview.card.Card;
import de.tum.in.tumcampusapp.component.ui.overview.card.ProvidesCard;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Cafeteria Manager, handles database stuff, external imports
 */
public class CafeteriaManager implements ProvidesCard {

    private Context mContext;
    private final CafeteriaViewModel cafeteriaViewModel;
    private final CompositeDisposable compositeDisposable;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public CafeteriaManager(Context context) {
        mContext = context;
        TcaDb db = TcaDb.getInstance(context);
        compositeDisposable = new CompositeDisposable();
        CafeteriaLocalRepository localRepository = CafeteriaLocalRepository.INSTANCE;
        localRepository.setDb(db);
        CafeteriaRemoteRepository remoteRepository = CafeteriaRemoteRepository.INSTANCE;
        remoteRepository.setTumCabeClient(TUMCabeClient.getInstance(context));
        cafeteriaViewModel = new CafeteriaViewModel(localRepository, remoteRepository, compositeDisposable);
    }

    @NotNull
    @Override
    public List<Card> getCards() {
        List<Card> results = new ArrayList<>();

        // Choose which mensa should be shown
        int cafeteriaId = new LocationManager(mContext).getCafeteria();
        if (cafeteriaId == -1) {
            return results;
        }

        CafeteriaMenuCard card = new CafeteriaMenuCard(mContext);
        CafeteriaWithMenus cafeteria = cafeteriaViewModel.getCafeteriaWithMenus(cafeteriaId);
        card.setCafeteriaWithMenus(cafeteria);

        results.add(card.getIfShowOnStart());
        return results;
    }

    /**
     * returns the menus of the best matching cafeteria
     */
    public Flowable<Map<String, List<CafeteriaMenu>>> getBestMatchMensaInfo(Context context) {
        // Choose which mensa should be shown
        int cafeteriaId = new LocationManager(context).getCafeteria();
        if (cafeteriaId == -1) {
            Utils.log("could not get a Cafeteria form locationManager!");
            return Flowable.just(Collections.emptyMap());
        }

        return createCafeteriaObservableForNonUIThreads(cafeteriaId)
                .map(cafeteria -> {
                    String mensaKey = cafeteria.getName() + ' ' + cafeteria.getNextMenuDate().toString();
                    Map<String, List<CafeteriaMenu>> selectedMensaMenus = new HashMap<>(1);
                    selectedMensaMenus.put(mensaKey, cafeteria.getMenus());
                    return selectedMensaMenus;
                })
                .onErrorReturnItem(new HashMap<>());

    }

    public Flowable<String> getBestMatchMensaName(Context context) {
        // Choose which mensa should be shown
        int cafeteriaId = new LocationManager(context).getCafeteria();
        if (cafeteriaId == -1) {
            Utils.log("could not get a Cafeteria form locationManager!");
            return Flowable.just("");
        }
        return createCafeteriaObservable(cafeteriaId)
                .map(s -> s.getName() + ' ' + s.getNextMenuDate());

    }

    public int getBestMatchMensaId(Context context) {
        // Choose which mensa should be shown
        int cafeteriaId = new LocationManager(context).getCafeteria();
        if (cafeteriaId == -1) {
            Utils.log("could not get a Cafeteria form locationManager!");
        }
        return cafeteriaId;
    }

    private Flowable<CafeteriaWithMenus> createCafeteriaObservableForNonUIThreads(int cafeteriaId) {
        CafeteriaWithMenus cafeteria = new CafeteriaWithMenus(cafeteriaId);

        return CafeteriaLocalRepository.INSTANCE
                .getCafeteria(cafeteriaId)
                .doOnError(throwable -> Utils.log(throwable.getMessage()))
                .flatMap(cafeteria1 -> {
                    cafeteria.setName(cafeteria1.getName());
                    return CafeteriaLocalRepository.INSTANCE.getAllMenuDates();
                })
                .flatMap(menuDates -> {
                    cafeteria.setMenuDates(menuDates);
                    return  CafeteriaLocalRepository.INSTANCE.getCafeteriaMenus(
                            cafeteria.getId(), cafeteria.getNextMenuDate());
                })
                .map(menus -> {
                    cafeteria.setMenus(menus);
                    return cafeteria;
                });
    }

    private Flowable<CafeteriaWithMenus> createCafeteriaObservable(int cafeteriaId) {
        CafeteriaWithMenus cafeteria = new CafeteriaWithMenus(cafeteriaId);

        return cafeteriaViewModel
                .getCafeteriaNameFromId(cafeteriaId)
                .doOnError(throwable -> Utils.log(throwable.getMessage()))
                .flatMap(cafeteriaName -> {
                    cafeteria.setName(cafeteriaName);
                    return cafeteriaViewModel.getAllMenuDates();
                })
                .flatMap(menuDates -> {
                    cafeteria.setMenuDates(menuDates);
                    return cafeteriaViewModel.getCafeteriaMenus(
                            cafeteria.getId(), cafeteria.getNextMenuDate());
                })
                .map(menus -> {
                    cafeteria.setMenus(menus);
                    return cafeteria;
                });
    }

}